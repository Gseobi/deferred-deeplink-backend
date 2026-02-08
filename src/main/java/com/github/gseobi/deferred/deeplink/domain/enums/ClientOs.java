package com.github.gseobi.deferred.deeplink.domain.enums;

public enum ClientOs {
    ANDROID, IOS, WINDOWS, MAC, OTHER;

    public boolean isDesktopInvalid() {
        return this == WINDOWS || this == MAC;
    }
}
