package com.arelore.data.sec.umbrella.server.core;

import java.security.*;
import java.util.Base64;

/**
 * 生成RSA密钥对的简单工具
 */
public class GenerateKeys {
    public static void main(String[] args) throws NoSuchAlgorithmException {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair keyPair = keyGen.generateKeyPair();

        String publicKey = Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());
        String privateKey = Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded());

        System.out.println("===== 公钥 (PUBLIC KEY) =====");
        System.out.println(publicKey);
        System.out.println();
        System.out.println("===== 私钥 (PRIVATE KEY) =====");
        System.out.println(privateKey);
    }
}
