package com.arelore.data.sec.umbrella.server.constant;

/**
 * RSA密钥常量类
 *
 * 注意：这些密钥仅用于开发测试，请勿在生产环境使用！
 * 在生产环境中，应该使用环境变量或配置文件来管理密钥
 */
public class RSAKeyConstants {

    /**
     * RSA公钥（真实密钥，从generate-rsa-keys.sh生成）
     *
     * 请使用 temp-keys/RSA_KEYS.md 中的方法生成新的密钥对
     * 该公钥提供给前端用于密码加密
     */
    public static final String PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA" +
            "u5HPzTZn65ivQJCx7g57pIpmo/qjj+qEUVS5hiGsDaImIpYxPz63sdQ21xJSODhJ" +
            "VCTu2xEcWyD5v1aJQwCibK8a0B0yHzVYuYeRQ3vGgSgsdAhLvQawhmOC6jOeAM" +
            "M4rhXglv3WnD9OCAINcAql10XK46ue0gPBHjnn7MJUe81k8IpwcYgY7XnzWpScm" +
            "M6H0MDTyn/4w6nLciw6LTCmLMBRUTZliQkrAVkG2I8k/5uVw7YSt0ASyIUN0GC" +
            "pBFC0OHIgIWQhJ54AExiKW2Lcg5g3qJA7mYPMmERh/2mSSi7aA4ODO8r8mV7l" +
            "dRVFt5TZq61DalK6IirTwyNCR2ExMwIDAQAB";

    /**
     * RSA私钥（PKCS#8格式，与公钥配对）
     *
     * 请使用 temp-keys/generate-rsa-keys.sh 生成新的密钥对
     * 该私钥用于解密前端加密的密码，必须保密
     *
     * 注意：实际项目中应该从配置文件或密钥管理系统获取，不要硬编码在代码中
     */
    public static final String PRIVATE_KEY = "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQC7kc/NNmfrmK9A" +
            "kLHuDnukimaj+qOP6oRRVLmGIawNoiYiljE/Prex1DbXElI4OElUJO7bERxbIPm/Vo" +
            "lDAKJsrxrQHTIfNVi5h5FDe8aBKCx0CEu9BrCGY4LqM54AwziuFeCW/dacP04IAg1" +
            "wCqXXRcrjq57SA8EeOefswlR7zWTwinBxiBjtefNalJyYzofQwNPKf/jDqctyLDotM" +
            "KYswFFRNmWJCSsBWQbYjyT/m5XDthK3QBLIhQ3QYKkEULQ4ciAhZCEnngATGIpbYty" +
            "DmDeokDuZg8yYRGH/aZJKLtoDg4M7yvyZXuV1FUW3lNmrrUNqUroiKtPDI0JHYTEzA" +
            "gMBAAECggEBAI/tIvbkeHlff5qbbOyuUFHBoWDSQg98dGu0B2fVDqCne3uuo7tT9M" +
            "CPhkUh2dUp25qlfGK8jnWeqAUZ8dln8zpSYtCulnc8CAjU7bJKl9cjbHjcpME3EN0O" +
            "HrZ3RRZwtl1ejCdQQA1Exit+57DBet/kvfpVBllAaD85T61ssEcN0ZYY2bHnQnHunk" +
            "0ncD9Oib3i3jid4ltgb+ZrJJiFmK0ZAzL2Na1gOrtp5r1kN/1JUSlnNxGDrIKCoJcJ" +
            "j3c9Jl4c/tEe75LIQ14ARTwT7fW5F5BuLNxJspt5IPUWEYLB27RWyjLP2TTZvk4UEG" +
            "StoNR6r47pd0S7OGM1W20/cgECgYEA9bwn38uzbPZ3WPw2g5Eu8ggAl91t2VQVC4jF" +
            "OZuNZLDq6z5SaBtfKL7wJLna+zZXj5gNRlMBwIeJMxS/Cs1bfc0nNOha+NeXvIqkR6" +
            "PRDtM5sbbq69KAeBcVjeAbEVFnhm7DEGHbT4SI1grTSfRdJGaKrju95XMso7ACND9t" +
            "JmMCgYEAw2elWxRvporWiXLIUqmioliHZrXyyjDuXp/gojrsERaU412dU/4L3xhGqb" +
            "gFGRTDlYKbMZctTuIEOmNzKG7E18we3puFcqsgyRSczoFbq0R+DvorOwkeIg9q9R50" +
            "1WQx9BbQ6L5F8+txslu/WBVUg34savEKQF7qAK2nIT1+GvECgYEA0XgfYVTp0NNTrw" +
            "XnjkVfCC+sGchGEEDtY5Sl0lJ3t8JbrkXExzu15SzF1fib4xJSl3PctPqi/rRKTsG7" +
            "jAC7ocQUTLRYf9B/Ztej6/GHjqSdhJDRNk6M9lFvLS5AxIVowx59puRTpzfp0NMPgv" +
            "YLw7CpPJZubD2+apXpexIp/9MCgYB2+L4GzFSGU/X3TbmGCt6IddpKwPEIBe68Eh+U" +
            "5scgSF1yaWcG4qCbvo4Xe4EvGiNS/mg7M05UvuvQGQjeT0LuGMuawZnojSSzCSfH25" +
            "7SkismN07m7NA58KUwhNChtBVuBg5ABslUj79z1sSTfrksLkAhBI03UBKtQw2FAYms" +
            "IQKBgGsQX+YSFhU2YJGXCjLUtMR/H5haW7UTpeRRVPPLzd0Zhsl86dZowkY4jMuZC+" +
            "dDFB+mFHm7MyxkLz+X8HYURckjN5s4Ayxwd7yFRCdfR+a26Xsaq0KcOp4oFSVRjf1/" +
            "2sI9No2ZYGjBl82nDUY90kwmJp/GELLdyV5aIuCyNpla";

    /**
     * 私有构造函数，防止实例化
     */
    private RSAKeyConstants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
