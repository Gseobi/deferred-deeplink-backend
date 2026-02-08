package com.github.gseobi.deferred.deeplink.controller;

import com.github.gseobi.deferred.deeplink.domain.enums.ClientOs;
import com.github.gseobi.deferred.deeplink.service.DeepLinkService;
import com.github.gseobi.deferred.deeplink.util.ClientUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Controller
@RequestMapping("/install")
@RequiredArgsConstructor
public class DeepLinkController {

    private final DeepLinkService deepLinkService;

    @GetMapping("/{app_type}")
    public String entry(HttpServletRequest req,
                        @PathVariable("app_type") String appType,
                        @RequestParam("campaign_id") String campaignId,
                        @RequestParam("utm_source") String utmSource,
                        @RequestParam("utm_medium") String utmMedium,
                        @Nullable @RequestParam("utm_campaign") String utmCampaign,
                        @Nullable @RequestParam("utm_content") String utmContent,
                        @Nullable @RequestParam("utm_term") String utmTerm,
                        @Nullable @RequestParam("utm_lang") String utmLang) {

        ClientOs os = ClientUtils.detectOs(req);

        if (os.isDesktopInvalid()) {
            return "deeplink/invalidRequest";
        }

        String crypt = this.deepLinkService.createCryptAndSaveClick(
                req, appType, campaignId,
                utmSource, utmMedium, utmCampaign, utmContent, utmTerm, utmLang
        );

        if (crypt == null || crypt.isBlank()) {
            return "deeplink/invalidRequest";
        }

        return "redirect:/install/landing?crypt=" + URLEncoder.encode(crypt, StandardCharsets.UTF_8);
    }

    @GetMapping("/landing")
    public ModelAndView landing(@RequestParam("crypt") String crypt) {
        ModelAndView mav = new ModelAndView();

        Map<String, Object> model = deepLinkService.buildLandingModel(crypt);
        if (Boolean.TRUE.equals(model.get("invalid"))) {
            mav.setViewName("deeplink/invalidRequest");
            return mav;
        }

        mav.addAllObjects(model);
        mav.setViewName("deeplink/landingPage");
        return mav;
    }

    @GetMapping("/check")
    @ResponseBody
    public Map<String, Object> check(@RequestParam("crypt") String crypt,
                                     @RequestParam("access_seq") Long accessSeq,
                                     @RequestParam("user_pin") String userPin,
                                     @RequestParam("platform") String platform) {
        return this.deepLinkService.verifyFirstOpen(crypt, accessSeq, userPin, platform);
    }
}
