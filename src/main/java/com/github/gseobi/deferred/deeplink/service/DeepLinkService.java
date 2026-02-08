package com.github.gseobi.deferred.deeplink.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.gseobi.deferred.deeplink.domain.entity.AccessLog;
import com.github.gseobi.deferred.deeplink.domain.entity.AppConfig;
import com.github.gseobi.deferred.deeplink.domain.entity.ClickReferrer;
import com.github.gseobi.deferred.deeplink.domain.enums.ClientOs;
import com.github.gseobi.deferred.deeplink.domain.enums.StoreType;
import com.github.gseobi.deferred.deeplink.repository.AccessLogRepository;
import com.github.gseobi.deferred.deeplink.repository.ClickReferrerRepository;
import com.github.gseobi.deferred.deeplink.repository.query.AccessLogQueryRepository;
import com.github.gseobi.deferred.deeplink.repository.query.AppConfigQueryRepository;
import com.github.gseobi.deferred.deeplink.repository.query.ClickReferrerQueryRepository;
import com.github.gseobi.deferred.deeplink.util.AES256Cipher;
import com.github.gseobi.deferred.deeplink.util.ClientUtils;
import com.github.gseobi.deferred.deeplink.util.JsonUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DeepLinkService {

    private final ClickReferrerRepository clickReferrerRepository;
    private final AccessLogRepository accessLogRepository;

    private final AppConfigQueryRepository appConfigQueryRepository;
    private final ClickReferrerQueryRepository clickReferrerQueryRepository;
    private final AccessLogQueryRepository accessLogQueryRepository;

    @Value("${deeplink.crypto.key}")
    private String encKey;

    /** 1) /install/{app_type} 진입: crypt 생성 + 클릭 저장 */
    public String createCryptAndSaveClick(HttpServletRequest req, String appType, String campaignId,
                                          String utmSource, String utmMedium,
                                          String utmCampaign, String utmContent, String utmTerm, String utmLang) {

        ClientOs os = ClientUtils.detectOs(req);
        String clickId = clickReferrerRepository.nextClickId(); // ✅ DB Function
        String clientIp = ClientUtils.getClientIp(req);
        String userAgent = req.getHeader("User-Agent");

        Map<String, Object> payload = new HashMap<>();
        payload.put("provider", appType);          // ✅ 유의사항(4)
        payload.put("campaign_id", campaignId);
        payload.put("os", os.name());
        payload.put("click_id", clickId);

        payload.put("utm_source", utmSource);
        payload.put("utm_medium", utmMedium);
        payload.put("utm_campaign", utmCampaign);
        payload.put("utm_content", utmContent);
        payload.put("utm_term", utmTerm);
        payload.put("utm_lang", utmLang);

        String payloadJson = JsonUtils.toJson(payload);

        try {
            String crypt = AES256Cipher.encode(payloadJson, encKey);

            ClickReferrer entity = ClickReferrer.builder()
                    .clickId(clickId)
                    .provider(appType)
                    .campaignId(campaignId)
                    .os(os.name())
                    .clientIp(clientIp)
                    .userAgent(userAgent)
                    .crypt(crypt)
                    .payloadJson(payloadJson)
                    .createdAt(OffsetDateTime.now())
                    .build();

            clickReferrerRepository.save(entity);
            return crypt;
        } catch (Exception e) {
            return "";
        }
    }

    /** 2) /install/landing: crypt 복호화 → config 조회 → access_log 생성 → JSP 모델 반환 */
    public Map<String, Object> buildLandingModel(String crypt) {
        Map<String, Object> model = new HashMap<>();
        model.put("crypt", crypt);

        String payloadJson;
        try {
            payloadJson = AES256Cipher.decode(crypt, encKey);
        } catch (Exception e) {
            model.put("invalid", true);
            return model;
        }

        JsonNode node = JsonUtils.readTree(payloadJson);

        String provider = node.path("provider").asText("");
        String os = node.path("os").asText("OTHER");
        String campaignId = node.path("campaign_id").asText("UNKNOWN");
        String clickId = node.path("click_id").asText("");

        if (provider.isBlank() || clickId.isBlank()) {
            model.put("invalid", true);
            return model;
        }

        // click 존재 확인
        clickReferrerQueryRepository.findByCrypt(crypt).orElse(null);

        AppConfig cfg = appConfigQueryRepository.findByProvider(provider).orElse(null);
        if (cfg == null) {
            model.put("invalid", true);
            return model;
        }

        AccessLog log = AccessLog.builder()
                .clickId(clickId)
                .crypt(crypt)
                .verifiedYn(false)
                .landingAt(OffsetDateTime.now())
                .build();

        log = accessLogRepository.save(log);

        boolean isAndroid = "ANDROID".equalsIgnoreCase(os);

        model.put("provider", provider);
        model.put("os", isAndroid ? "ANDROID" : "IOS");
        model.put("campaign_id", campaignId);
        model.put("access_seq", String.valueOf(log.getAccessSeq()));

        model.put("appName", cfg.getAppName());
        model.put("appIconUrl", cfg.getAppIconUrl());

        if (isAndroid) {
            model.put("storeType", StoreType.PLAY_STORE.name());
            model.put("appScheme", cfg.getAndroidScheme());
            model.put("storeUrl", cfg.getPlayStoreUrl());
        } else {
            model.put("storeType", StoreType.APP_STORE.name());
            model.put("appScheme", cfg.getIosScheme());
            model.put("storeUrl", cfg.getAppStoreUrl());
        }

        return model;
    }

    /** 3) /install/check: 최초 실행 검증(crypt + access_seq + ) */
    public Map<String, Object> verifyFirstOpen(String crypt, Long accessSeq, String userPin, String platform) {
        Map<String, Object> res = new HashMap<>();

        AccessLog log = accessLogQueryRepository.findByAccessSeqAndCrypt(accessSeq, crypt).orElse(null);
        if (log == null) {
            res.put("result_cd", "4041");
            res.put("result_msg", "NOT_MATCHED");
            return res;
        }

        log.verify(userPin, platform);
        accessLogRepository.save(log);

        res.put("result_cd", "0000");
        res.put("result_msg", "VERIFIED");
        res.put("access_seq", accessSeq);
        return res;
    }
}
