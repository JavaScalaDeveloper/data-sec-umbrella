package com.arelore.data.sec.umbrella.server.util;

import javax.crypto.Cipher;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * RSA非对称加密工具类
 */
public class RsaUtil {

    // 密钥长度
    private static final int KEY_SIZE = 2048;
    // 算法名称
    private static final String ALGORITHM = "RSA";

    // 公钥（实际项目中应该从配置文件或环境变量中获取）

}