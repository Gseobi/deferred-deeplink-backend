package com.github.gseobi.deferred.deeplink.util;

import com.github.gseobi.deferred.deeplink.domain.enums.ClientOs;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

class ClientUtilsTest {

    @DisplayName("Android User-Agent는 ANDROID로 판별한다")
    @Test
    void detectAndroid() {
        HttpServletRequest req = Mockito.mock(HttpServletRequest.class);
        given(req.getHeader("User-Agent"))
                .willReturn("Mozilla/5.0 (Linux; Android 14; Pixel 8)");

        assertThat(ClientUtils.detectOs(req)).isEqualTo(ClientOs.ANDROID);
    }

    @DisplayName("iPhone User-Agent는 IOS로 판별한다")
    @Test
    void detectIos() {
        HttpServletRequest req = Mockito.mock(HttpServletRequest.class);
        given(req.getHeader("User-Agent"))
                .willReturn("Mozilla/5.0 (iPhone; CPU iPhone OS 17_0 like Mac OS X)");

        assertThat(ClientUtils.detectOs(req)).isEqualTo(ClientOs.IOS);
    }

    @DisplayName("Windows User-Agent는 WINDOWS로 판별한다")
    @Test
    void detectWindows() {
        HttpServletRequest req = Mockito.mock(HttpServletRequest.class);
        given(req.getHeader("User-Agent"))
                .willReturn("Mozilla/5.0 (Windows NT 10.0; Win64; x64)");

        assertThat(ClientUtils.detectOs(req)).isEqualTo(ClientOs.WINDOWS);
    }

    @DisplayName("Mac User-Agent는 MAC으로 판별한다")
    @Test
    void detectMac() {
        HttpServletRequest req = Mockito.mock(HttpServletRequest.class);
        given(req.getHeader("User-Agent"))
                .willReturn("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7)");

        assertThat(ClientUtils.detectOs(req)).isEqualTo(ClientOs.MAC);
    }

    @DisplayName("알 수 없는 User-Agent는 OTHER로 판별한다")
    @Test
    void detectOther() {
        HttpServletRequest req = Mockito.mock(HttpServletRequest.class);
        given(req.getHeader("User-Agent"))
                .willReturn("Unknown-Agent");

        assertThat(ClientUtils.detectOs(req)).isEqualTo(ClientOs.OTHER);
    }

    @DisplayName("User-Agent가 null이면 OTHER로 판별한다")
    @Test
    void detectOtherWhenNull() {
        HttpServletRequest req = Mockito.mock(HttpServletRequest.class);
        given(req.getHeader("User-Agent")).willReturn(null);

        assertThat(ClientUtils.detectOs(req)).isEqualTo(ClientOs.OTHER);
    }

    @DisplayName("X-Forwarded-For가 있으면 첫 번째 IP를 사용한다")
    @Test
    void getClientIpFromXForwardedFor() {
        HttpServletRequest req = Mockito.mock(HttpServletRequest.class);
        given(req.getHeader("X-Forwarded-For"))
                .willReturn("1.2.3.4, 5.6.7.8");
        given(req.getRemoteAddr()).willReturn("127.0.0.1");

        assertThat(ClientUtils.getClientIp(req)).isEqualTo("1.2.3.4");
    }

    @DisplayName("X-Forwarded-For가 없으면 remoteAddr를 사용한다")
    @Test
    void getClientIpFromRemoteAddr() {
        HttpServletRequest req = Mockito.mock(HttpServletRequest.class);
        given(req.getHeader("X-Forwarded-For")).willReturn(null);
        given(req.getRemoteAddr()).willReturn("127.0.0.1");

        assertThat(ClientUtils.getClientIp(req)).isEqualTo("127.0.0.1");
    }
}