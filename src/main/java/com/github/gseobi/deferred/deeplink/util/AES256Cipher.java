package com.github.gseobi.deferred.deeplink.util;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

public class AES256Cipher {

    private AES256Cipher() {}

    public static String encode(String plainText, String key32) throws Exception {
        byte[] keyBytes = key32.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length != 32) throw new IllegalArgumentException("AES-256 key must be 32 bytes.");

        byte[] iv = new byte[16];
        new SecureRandom().nextBytes(iv);

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE,
                new SecretKeySpec(keyBytes, "AES"),
                new IvParameterSpec(iv));

        byte[] enc = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(iv) + ":" + Base64.getEncoder().encodeToString(enc);
    }

    public static String decode(String cipherText, String key32) throws Exception {
        byte[] keyBytes = key32.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length != 32) throw new IllegalArgumentException("AES-256 key must be 32 bytes.");

        String[] parts = cipherText.split(":");
        if (parts.length != 2) throw new IllegalArgumentException("Invalid cipher format.");

        byte[] iv = Base64.getDecoder().decode(parts[0]);
        byte[] enc = Base64.getDecoder().decode(parts[1]);

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE,
                new SecretKeySpec(keyBytes, "AES"),
                new IvParameterSpec(iv));

        return new String(cipher.doFinal(enc), StandardCharsets.UTF_8);
    }
}
