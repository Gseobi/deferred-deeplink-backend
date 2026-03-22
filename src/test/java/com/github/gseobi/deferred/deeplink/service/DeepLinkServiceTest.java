package com.github.gseobi.deferred.deeplink.service;

import com.github.gseobi.deferred.deeplink.domain.entity.AccessLog;
import com.github.gseobi.deferred.deeplink.domain.entity.AppConfig;
import com.github.gseobi.deferred.deeplink.domain.entity.ClickReferrer;
import com.github.gseobi.deferred.deeplink.repository.AccessLogRepository;
import com.github.gseobi.deferred.deeplink.repository.ClickReferrerRepository;
import com.github.gseobi.deferred.deeplink.repository.query.AccessLogQueryRepository;
import com.github.gseobi.deferred.deeplink.repository.query.AppConfigQueryRepository;
import com.github.gseobi.deferred.deeplink.repository.query.ClickReferrerQueryRepository;
import com.github.gseobi.deferred.deeplink.util.AES256Cipher;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class DeepLinkServiceTest {

    @Mock
    private ClickReferrerRepository clickReferrerRepository;

    @Mock
    private AccessLogRepository accessLogRepository;

    @Mock
    private AppConfigQueryRepository appConfigQueryRepository;

    @Mock
    private ClickReferrerQueryRepository clickReferrerQueryRepository;

    @Mock
    private AccessLogQueryRepository accessLogQueryRepository;

    @InjectMocks
    private DeepLinkService deepLinkService;

    private static final String VALID_KEY = "12345678901234567890123456789012";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(deepLinkService, "encKey", VALID_KEY);
    }

    @DisplayName("정상 요청이면 crypt를 생성하고 click referrer를 저장한다")
    @Test
    void createCryptAndSaveClickSuccess() {
        HttpServletRequest req = Mockito.mock(HttpServletRequest.class);

        given(req.getHeader("User-Agent"))
                .willReturn("Mozilla/5.0 (Linux; Android 14; Pixel 8)");
        given(req.getHeader("X-Forwarded-For"))
                .willReturn("1.2.3.4");
        given(clickReferrerRepository.nextClickId()).willReturn("CLK0001");

        String crypt = deepLinkService.createCryptAndSaveClick(
                req,
                "appA",
                "CMP001",
                "google",
                "cpc",
                "spring-sale",
                "banner",
                "deep-link",
                "ko"
        );

        assertThat(crypt).isNotBlank();
        then(clickReferrerRepository).should().save(any(ClickReferrer.class));
    }

    @DisplayName("landing 모델 생성 시 crypt 복호화와 config 조회가 성공하면 정상 model을 반환한다")
    @Test
    void buildLandingModelSuccess() throws Exception {
        String payloadJson = """
                {
                  "provider": "appA",
                  "campaign_id": "CMP001",
                  "os": "ANDROID",
                  "click_id": "CLK0001"
                }
                """;

        String crypt = AES256Cipher.encode(payloadJson, VALID_KEY);

        ClickReferrer clickReferrer = ClickReferrer.builder()
                .clickId("CLK0001")
                .provider("appA")
                .campaignId("CMP001")
                .os("ANDROID")
                .clientIp("1.2.3.4")
                .userAgent("Mozilla/5.0")
                .crypt(crypt)
                .payloadJson(payloadJson)
                .createdAt(OffsetDateTime.now())
                .build();

        AppConfig appConfig = AppConfig.builder()
                .provider("appA")
                .appName("Test App")
                .appIconUrl("https://cdn.test/app.png")
                .androidScheme("testapp://open")
                .iosScheme("testapp-ios://open")
                .playStoreUrl("https://play.google.com/store/apps/details?id=test")
                .appStoreUrl("https://apps.apple.com/app/test")
                .build();

        given(clickReferrerQueryRepository.findByCrypt(crypt))
                .willReturn(Optional.of(clickReferrer));
        given(appConfigQueryRepository.findByProvider("appA"))
                .willReturn(Optional.of(appConfig));
        given(accessLogRepository.save(any(AccessLog.class)))
                .willAnswer(invocation -> {
                    AccessLog arg = invocation.getArgument(0);
                    return AccessLog.builder()
                            .accessSeq(1L)
                            .clickId(arg.getClickId())
                            .crypt(arg.getCrypt())
                            .platform(arg.getPlatform())
                            .userPin(arg.getUserPin())
                            .verifiedYn(arg.isVerifiedYn())
                            .landingAt(arg.getLandingAt())
                            .firstOpenAt(arg.getFirstOpenAt())
                            .build();
                });

        Map<String, Object> result = deepLinkService.buildLandingModel(crypt);

        assertThat(result.get("invalid")).isNull();
        assertThat(result.get("provider")).isEqualTo("appA");
        assertThat(result.get("os")).isEqualTo("ANDROID");
        assertThat(result.get("campaign_id")).isEqualTo("CMP001");
        assertThat(result.get("access_seq")).isEqualTo("1");
        assertThat(result.get("appName")).isEqualTo("Test App");
        assertThat(result.get("storeType")).isEqualTo("PLAY_STORE");
        assertThat(result.get("appScheme")).isEqualTo("testapp://open");
        assertThat(result.get("storeUrl")).isEqualTo("https://play.google.com/store/apps/details?id=test");
    }

    @DisplayName("crypt 복호화 실패 시 invalid=true 를 반환한다")
    @Test
    void buildLandingModelInvalidWhenDecodeFails() {
        Map<String, Object> result = deepLinkService.buildLandingModel("bad-crypt");

        assertThat(result.get("invalid")).isEqualTo(true);
        assertThat(result.get("crypt")).isEqualTo("bad-crypt");
    }

    @DisplayName("provider 또는 click_id 가 비어 있으면 invalid=true 를 반환한다")
    @Test
    void buildLandingModelInvalidWhenPayloadMissingFields() throws Exception {
        String payloadJson = """
                {
                  "provider": "",
                  "click_id": ""
                }
                """;

        String crypt = AES256Cipher.encode(payloadJson, VALID_KEY);

        Map<String, Object> result = deepLinkService.buildLandingModel(crypt);

        assertThat(result.get("invalid")).isEqualTo(true);
    }

    @DisplayName("app config 가 없으면 invalid=true 를 반환한다")
    @Test
    void buildLandingModelInvalidWhenAppConfigMissing() throws Exception {
        String payloadJson = """
                {
                  "provider": "appA",
                  "campaign_id": "CMP001",
                  "os": "ANDROID",
                  "click_id": "CLK0001"
                }
                """;

        String crypt = AES256Cipher.encode(payloadJson, VALID_KEY);

        ClickReferrer clickReferrer = ClickReferrer.builder()
                .clickId("CLK0001")
                .provider("appA")
                .campaignId("CMP001")
                .os("ANDROID")
                .clientIp("1.2.3.4")
                .userAgent("Mozilla/5.0")
                .crypt(crypt)
                .payloadJson(payloadJson)
                .createdAt(OffsetDateTime.now())
                .build();

        given(clickReferrerQueryRepository.findByCrypt(crypt))
                .willReturn(Optional.of(clickReferrer));
        given(appConfigQueryRepository.findByProvider("appA"))
                .willReturn(Optional.empty());

        Map<String, Object> result = deepLinkService.buildLandingModel(crypt);

        assertThat(result.get("invalid")).isEqualTo(true);
    }

    @DisplayName("정상 check 요청이면 VERIFIED 응답과 함께 access log를 저장한다")
    @Test
    void verifyFirstOpenSuccess() {
        AccessLog log = AccessLog.builder()
                .accessSeq(1L)
                .clickId("CLK0001")
                .crypt("encrypted-crypt")
                .verifiedYn(false)
                .landingAt(OffsetDateTime.now())
                .build();

        given(accessLogQueryRepository.findByAccessSeqAndCrypt(1L, "encrypted-crypt"))
                .willReturn(Optional.of(log));

        Map<String, Object> result =
                deepLinkService.verifyFirstOpen("encrypted-crypt", 1L, "PIN001", "android");

        assertThat(result.get("result_cd")).isEqualTo("0000");
        assertThat(result.get("result_msg")).isEqualTo("VERIFIED");
        assertThat(result.get("access_seq")).isEqualTo(1L);
        assertThat(log.isVerifiedYn()).isTrue();
        assertThat(log.getUserPin()).isEqualTo("PIN001");
        assertThat(log.getPlatform()).isEqualTo("android");

        then(accessLogRepository).should().save(log);
    }

    @DisplayName("access log를 찾지 못하면 NOT_MATCHED 응답을 반환한다")
    @Test
    void verifyFirstOpenFailWhenLogMissing() {
        given(accessLogQueryRepository.findByAccessSeqAndCrypt(1L, "encrypted-crypt"))
                .willReturn(Optional.empty());

        Map<String, Object> result =
                deepLinkService.verifyFirstOpen("encrypted-crypt", 1L, "PIN001", "android");

        assertThat(result.get("result_cd")).isEqualTo("4041");
        assertThat(result.get("result_msg")).isEqualTo("NOT_MATCHED");

        then(accessLogRepository).should(never()).save(any(AccessLog.class));
    }
}