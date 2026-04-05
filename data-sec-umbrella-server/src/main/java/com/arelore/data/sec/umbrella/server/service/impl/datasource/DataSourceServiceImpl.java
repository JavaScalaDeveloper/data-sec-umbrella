package com.arelore.data.sec.umbrella.server.service.impl.datasource;

import com.arelore.data.sec.umbrella.server.entity.DataSource;
import com.arelore.data.sec.umbrella.server.mapper.DataSourceMapper;
import com.arelore.data.sec.umbrella.server.service.DataSourceService;
import com.arelore.data.sec.umbrella.server.strategy.DatabaseConnectionStrategy;
import com.arelore.data.sec.umbrella.server.strategy.DatabaseConnectionStrategyFactory;
import com.arelore.data.sec.umbrella.server.util.RSACryptoUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * 数据源Service实现类
 */
@Service
public class DataSourceServiceImpl extends ServiceImpl<DataSourceMapper, DataSource> implements DataSourceService {

    // RSA私钥（PKCS#8格式，与公钥配对）
    // 该密钥使用 generate-rsa-keys.sh 生成
    // 注意：实际项目中应该从配置文件或密钥管理系统获取，不要硬编码在代码中
    private static final String PRIVATE_KEY = "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQC7kc/NNmfrmK9A" +
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

    @Override
    public ConnectionTestResult testConnection(DataSource dataSource) {
        try {
            // 使用RSA私钥解密密码
            String decryptedPassword = decryptPassword(dataSource.getPassword());
            dataSource.setPassword(decryptedPassword);

            // 根据数据源类型获取对应的连接策略
            DatabaseConnectionStrategy strategy = DatabaseConnectionStrategyFactory.getStrategy(dataSource.getDataSourceType());

            if (strategy == null) {
                // 不支持的数据库类型
                return new ConnectionTestResult(false, "不支持的数据库类型: " + dataSource.getDataSourceType());
            }

            // 使用策略测试连接，策略会抛出详细的异常
            strategy.testConnection(dataSource);
            return new ConnectionTestResult(true);
        } catch (Exception e) {
            // 捕获异常并返回错误信息
            String errorMessage = e.getMessage();
            return new ConnectionTestResult(false, errorMessage);
        }
    }

    /**
     * 使用RSA私钥解密密码
     */
    private String decryptPassword(String encryptedPassword) {
        try {
            return RSACryptoUtil.decrypt(encryptedPassword, PRIVATE_KEY);
        } catch (Exception e) {
            throw new RuntimeException("密码解密失败: " + e.getMessage(), e);
        }
    }
}