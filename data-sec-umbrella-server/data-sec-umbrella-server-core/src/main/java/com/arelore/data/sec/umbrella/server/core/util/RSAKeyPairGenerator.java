package com.arelore.data.sec.umbrella.server.core.util;

import java.io.FileWriter;
import java.io.IOException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.KeyPairGenerator;

/**
 * RSA密钥对生成工具
 */
public class RSAKeyPairGenerator {

    public static void main(String[] args) {
        try {
            System.out.println("开始生成RSA密钥对...");

            // 生成密钥对
            KeyPair keyPair = generateKeyPair();

            // 获取公钥和私钥字符串
            String publicKey = RSACryptoUtil.getPublicKeyString(keyPair);
            String privateKey = RSACryptoUtil.getPrivateKeyString(keyPair);

            // 保存到文件
            saveToFile("public-key.txt", publicKey);
            saveToFile("private-key.txt", privateKey);

            System.out.println("RSA密钥对生成完成！");
            System.out.println("公钥已保存到: public-key.txt");
            System.out.println("私钥已保存到: private-key.txt");

            // 打印公钥（用于前端）
            System.out.println("\n公钥（用于前端配置）:");
            System.out.println(publicKey);

            System.out.println("\n私钥（用于后端配置）:");
            System.out.println(privateKey);

        } catch (NoSuchAlgorithmException | IOException e) {
            System.err.println("生成密钥对失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 生成RSA密钥对
     */
    public static KeyPair generateKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        return keyPairGenerator.generateKeyPair();
    }

    private static void saveToFile(String filename, String content) throws IOException {
        try (FileWriter writer = new FileWriter(filename)) {
            writer.write(content);
        }
    }
}
