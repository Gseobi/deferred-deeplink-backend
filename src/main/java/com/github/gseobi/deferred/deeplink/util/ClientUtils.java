package com.github.gseobi.deferred.deeplink.util;

import com.github.gseobi.deferred.deeplink.domain.enums.ClientOs;

import jakarta.servlet.http.HttpServletRequest;

public class ClientUtils {

    private ClientUtils() {}

    public static String getClientIp(HttpServletRequest req) {
        String xff = req.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        return req.getRemoteAddr();
    }

    public static ClientOs detectOs(HttpServletRequest req) {
        String ua = req.getHeader("User-Agent");
        if (ua == null) return ClientOs.OTHER;
        String u = ua.toLowerCase();

        if (u.contains("android")) return ClientOs.ANDROID;
        if (u.contains("iphone") || u.contains("ipad") || u.contains("ipod")) return ClientOs.IOS;
        if (u.contains("windows")) return ClientOs.WINDOWS;
        if (u.contains("macintosh") || u.contains("mac os x")) return ClientOs.MAC;

        return ClientOs.OTHER;
    }
}