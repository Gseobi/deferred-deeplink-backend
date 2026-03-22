package com.github.gseobi.deferred.deeplink.controller;

import com.github.gseobi.deferred.deeplink.service.DeepLinkService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class DeepLinkControllerTest {

    private MockMvc mockMvc;
    private DeepLinkService deepLinkService;

    @BeforeEach
    void setUp() {
        deepLinkService = Mockito.mock(DeepLinkService.class);
        DeepLinkController controller = new DeepLinkController(deepLinkService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @DisplayName("데스크톱 User-Agent로 install 진입 시 invalidRequest 페이지를 반환한다")
    @Test
    void entryReturnsInvalidRequestForDesktop() throws Exception {
        mockMvc.perform(get("/install/appA")
                        .param("campaign_id", "CMP001")
                        .param("utm_source", "google")
                        .param("utm_medium", "cpc")
                        .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)"))
                .andExpect(status().isOk())
                .andExpect(view().name("deeplink/invalidRequest"));
    }

    @DisplayName("모바일 User-Agent로 install 진입 시 landing으로 redirect 한다")
    @Test
    void entryRedirectsForMobile() throws Exception {
        given(deepLinkService.createCryptAndSaveClick(
                any(), eq("appA"), eq("CMP001"),
                eq("google"), eq("cpc"),
                isNull(), isNull(), isNull(), isNull()
        )).willReturn("encrypted-crypt");

        mockMvc.perform(get("/install/appA")
                        .param("campaign_id", "CMP001")
                        .param("utm_source", "google")
                        .param("utm_medium", "cpc")
                        .header("User-Agent", "Mozilla/5.0 (Linux; Android 14; Pixel 8)"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/install/landing?crypt=encrypted-crypt"));
    }

    @DisplayName("서비스가 빈 crypt를 반환하면 invalidRequest 페이지를 반환한다")
    @Test
    void entryReturnsInvalidWhenCryptIsBlank() throws Exception {
        given(deepLinkService.createCryptAndSaveClick(
                any(), anyString(), anyString(),
                anyString(), anyString(),
                any(), any(), any(), any()
        )).willReturn("");

        mockMvc.perform(get("/install/appA")
                        .param("campaign_id", "CMP001")
                        .param("utm_source", "google")
                        .param("utm_medium", "cpc")
                        .header("User-Agent", "Mozilla/5.0 (Linux; Android 14; Pixel 8)"))
                .andExpect(status().isOk())
                .andExpect(view().name("deeplink/invalidRequest"));
    }

    @DisplayName("landing에서 invalid=true 이면 invalidRequest 페이지를 반환한다")
    @Test
    void landingInvalid() throws Exception {
        Map<String, Object> model = new HashMap<>();
        model.put("invalid", true);

        given(deepLinkService.buildLandingModel("bad-crypt")).willReturn(model);

        mockMvc.perform(get("/install/landing")
                        .param("crypt", "bad-crypt"))
                .andExpect(status().isOk())
                .andExpect(view().name("deeplink/invalidRequest"));
    }

    @DisplayName("landing에서 정상 model이면 landingPage를 반환한다")
    @Test
    void landingSuccess() throws Exception {
        Map<String, Object> model = new HashMap<>();
        model.put("crypt", "ok-crypt");
        model.put("provider", "appA");
        model.put("os", "ANDROID");
        model.put("campaign_id", "CMP001");
        model.put("access_seq", "1");
        model.put("appName", "Test App");
        model.put("appIconUrl", "https://cdn.test/app.png");
        model.put("storeType", "PLAY_STORE");
        model.put("appScheme", "testapp://open");
        model.put("storeUrl", "https://play.google.com/store/apps/details?id=test");

        given(deepLinkService.buildLandingModel("ok-crypt")).willReturn(model);

        mockMvc.perform(get("/install/landing")
                        .param("crypt", "ok-crypt"))
                .andExpect(status().isOk())
                .andExpect(view().name("deeplink/landingPage"))
                .andExpect(model().attribute("appName", "Test App"))
                .andExpect(model().attribute("storeUrl", "https://play.google.com/store/apps/details?id=test"))
                .andExpect(model().attribute("access_seq", "1"));
    }

    @DisplayName("check 요청은 JSON 응답을 반환한다")
    @Test
    void checkReturnsJson() throws Exception {
        Map<String, Object> result = new HashMap<>();
        result.put("result_cd", "0000");
        result.put("result_msg", "VERIFIED");
        result.put("access_seq", 1L);

        given(deepLinkService.verifyFirstOpen("ok-crypt", 1L, "PIN001", "android"))
                .willReturn(result);

        mockMvc.perform(get("/install/check")
                        .param("crypt", "ok-crypt")
                        .param("access_seq", "1")
                        .param("user_pin", "PIN001")
                        .param("platform", "android"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result_cd").value("0000"))
                .andExpect(jsonPath("$.result_msg").value("VERIFIED"))
                .andExpect(jsonPath("$.access_seq").value(1));
    }
}