# 对称加密到非对称加密迁移文档

## 概述
已将数据源密码的加密方式从简单的XOR对称加密升级为RSA非对称加密，提高安全性。

## 主要更改

### 1. 后端更改

#### DataSourceController.java
- 新增 `getPublicKey()` API，提供RSA公钥给前端
- 移除了之前的 `CryptoUtil.decryptPassword()` 调用
- 密码在前端加密后直接保存到数据库
- 添加了RSA公钥常量（示例密钥）

#### DataSourceServiceImpl.java
- 将解密逻辑从 `CryptoUtil` 改为 `RSACryptoUtil`
- 使用RSA私钥解密密码
- 添加了RSA私钥常量（示例密钥，必须与公钥配对）
- 解密操作仅在需要使用密码时（如测试连接）进行

#### 工具类
- `RSACryptoUtil.java`: 已存在的RSA加密工具类
- `RSAKeyPairGenerator.java`: 新增密钥对生成工具

### 2. 前端更改

#### 依赖安装
```bash
npm install jsencrypt --save
```

#### api.ts 更改
- 引入 `JSEncrypt` 库进行RSA加密
- 新增 `getRSAPublicKey()` 函数，从后端获取公钥
- 修改 `encryptPassword()` 函数，使用RSA公钥加密
- 所有涉及密码的操作（创建、更新、测试连接）都使用RSA加密

#### 类型定义
- 创建 `src/types/jsencrypt.d.ts` 类型定义文件

## 安全优势

### 之前（对称加密）
- 使用简单的XOR算法
- 密钥硬编码在前端和后端
- 如果前端被攻击，密钥可能泄露

### 现在（非对称加密）
- 使用RSA-2048位密钥
- 公钥公开给前端，私钥只保存在后端
- 即使前端被攻击，也无法解密已加密的密码
- 符合安全最佳实践

## 使用说明

### 生成新的密钥对（生产环境）

#### 方法1：使用OpenSSL
```bash
# 生成私钥
openssl genrsa -out private_key.pem 2048

# 导出公钥
openssl rsa -in private_key.pem -pubout -out public_key.pem

# 转换为PKCS8格式（Java需要）
openssl pkcs8 -topk8 -inform PEM -outform DER -in private_key.pem -out private_key.der -nocrypt

# Base64编码
base64 -w 0 public_key.pem > public_key.txt
base64 -w 0 private_key.der > private_key.txt
```

#### 方法2：运行Java工具
```bash
cd data-sec-umbrella-server
mvn exec:java -Dexec.mainClass="com.arelore.data.sec.umbrella.server.util.RSAKeyPairGenerator"
```

### 配置密钥

1. 将公钥配置到 `DataSourceController.java` 的 `PUBLIC_KEY` 常量
2. 将私钥配置到 `DataSourceServiceImpl.java` 的 `PRIVATE_KEY` 常量
3. 确保公钥和私钥是配对的

### 最佳实践

1. **密钥管理**
   - 不要将私钥硬编码在代码中
   - 使用环境变量或配置文件
   - 考虑使用密钥管理系统（AWS KMS、Azure Key Vault等）
   - 定期轮换密钥

2. **密钥存储**
   - 前端：只存储公钥
   - 后端：私钥保存在安全的位置
   - 数据库：密码以加密形式存储

3. **安全性**
   - 使用HTTPS传输加密数据
   - 实施密钥访问控制
   - 记录密钥使用日志
   - 定期审计密钥使用情况

## 测试

### 测试步骤
1. 启动后端服务
2. 启动前端服务
3. 打开浏览器开发者工具，检查网络请求
4. 创建数据源，密码应该是加密后的字符串
5. 测试连接，应该能够正常解密密码

### 验证加密
前端发送的密码格式示例：
```
MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQC8v7w5hL9N2mY7XbR7qK3vP8rT5yL0nK1Q9z2O8xG7rJ6wN4mS3yL9qM8nT5xL0yR3zK8mP4vR9zN2yL5qM8nT5xL0yR3zK8mP3vR9zN1yL5qM8nT4xL0yR3zK8mP2vR9zN0yL5qM8nT3xL0yR3zK8mP1vR9zMyL5qM8nT1xL0yR3zK8mP0vR9zMzL5qM8nT0xL0yR3zK8mO9vR9zMzL5qM8nTxL0yR3zK8mO8vR9zMzL5qM8nTwL0yR3zK8mO7vR9zMzL5qM8nTvL0yR3zK8mO5vR9zMzL5qM8nTuL0yR3zK8mO4vR9zMzL5qM8nTtL0yR3zK8mO3vR9zMzL5qM8nTsL0yR3zK8mO2vR9zMzL5qM8nTrL0yR3zK8mO1vR9zMzL5qM8nTqL0yR3zK8mO0vR9zMzL5qM8nTpL0yR3zK8mOzvR9zMzL5qM8nToL0yR3zK8mOyvR9zMzL5qM8nTnL0yR3zK8mOxvR9zMzL5qM8nTmL0yR3zK8mOwvR9zMzL5qM8nTlL0yR3zK8mOvvR9zMzL5qM8nTkL0yR3zK8mOuvR9zMzL5qM8nTjL0yR3zK8mOtQIDAQAB
```

## 注意事项

1. **当前使用的是示例密钥**
   - 不要在生产环境使用示例密钥
   - 必须生成新的密钥对

2. **密钥长度**
   - 当前使用2048位
   - 考虑升级到4096位以获得更高安全性

3. **性能考虑**
   - RSA加密比对称加密慢
   - 考虑使用混合加密方案（RSA加密对称密钥）

4. **错误处理**
   - 添加了适当的异常处理
   - 加密/解密失败会抛出清晰的错误信息

## 后续改进建议

1. **密钥管理**
   - 集成AWS KMS或Azure Key Vault
   - 实施密钥轮换机制

2. **加密方案**
   - 考虑使用混合加密（RSA + AES）
   - 实现更复杂的加密协议

3. **安全审计**
   - 添加加密操作日志
   - 实施密钥使用监控

4. **合规性**
   - 确保符合相关安全标准（如PCI-DSS、GDPR等）
   - 定期进行安全审计
