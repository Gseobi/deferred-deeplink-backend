package com.github.gseobi.deferred.deeplink.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AES256CipherTest {

    private static final String VALID_KEY = "12345678901234567890123456789012";

    @DisplayName("암호화 후 복호화하면 원문이 유지된다")
    @Test
    void encodeDecodeRoundTrip() throws Exception {
        String plain = "{\"click_id\":\"CLK0001\",\"provider\":\"appA\"}";

        String encoded = AES256Cipher.encode(plain, VALID_KEY);
        String decoded = AES256Cipher.decode(encoded, VALID_KEY);

        assertThat(encoded).isNotBlank();
        assertThat(encoded).isNotEqualTo(plain);
        assertThat(decoded).isEqualTo(plain);
    }

    @DisplayName("32바이트가 아닌 키를 사용하면 예외가 발생한다")
    @Test
    void invalidKeyLength() {
        assertThatThrownBy(() -> AES256Cipher.encode("plain", "short-key"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("잘못된 cipher format이면 복호화 시 예외가 발생한다")
    @Test
    void invalidCipherFormat() {
        assertThatThrownBy(() -> AES256Cipher.decode("invalid-format", VALID_KEY))
                .isInstanceOf(IllegalArgumentException.class);
    }
}