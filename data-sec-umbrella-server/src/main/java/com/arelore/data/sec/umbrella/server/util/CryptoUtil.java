package com.arelore.data.sec.umbrella.server.util;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * 加密解密工具类
 */
public class CryptoUtil {

    private static final String SECRET_KEY = "data-sec-umbrella-key";

    /**
     * 解密密码
     * @param encryptedPassword 加密后的密码
     * @return 解密后的密码
     */
    public static String decryptPassword(String encryptedPassword) {
        // 使用与前端对应的解密算法
        byte[] encryptedBytes = Base64.getDecoder().decode(encryptedPassword);
        String encrypted = new String(encryptedBytes, StandardCharsets.UTF_8);
        StringBuilder decrypted = new StringBuilder();
        for (int i = 0; i < encrypted.length(); i++) {
            decrypted.append((char) (encrypted.charAt(i) ^ SECRET_KEY.charAt(i % SECRET_KEY.length())));
        }
        return decrypted.toString();
    }
}