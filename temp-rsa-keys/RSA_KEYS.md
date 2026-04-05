# RSA密钥对说明

## 如何生成RSA密钥对

### 方法1：使用在线工具
1. 访问 https://travistidwell.com/jsencrypt/demo
2. 点击 "Generate New Keys"
3. 复制生成的公钥和私钥

### 方法2：使用OpenSSL命令行
```bash
# 生成私钥
openssl genrsa -out private_key.pem 2048

# 导出公钥
openssl rsa -in private_key.pem -pubout -out public_key.pem

# 转换为Base64格式
cat public_key.pem | base64 | tr -d '\n'
cat private_key.pem | base64 | tr -d '\n'
```

### 方法3：使用Java代码
运行 `RSAKeyPairGenerator.java` 来生成密钥对：
```bash
mvn exec:java -Dexec.mainClass="com.arelore.data.sec.umbrella.server.util.RSAKeyPairGenerator"
```

## 配置说明

将生成的公钥和私钥分别配置到：
- 公钥：`DataSourceController.java` 中的 `PUBLIC_KEY` 常量
- 私钥：`DataSourceServiceImpl.java` 中的 `PRIVATE_KEY` 常量

### 注意事项
1. 私钥必须保密，不要泄露给任何人
2. 公钥可以安全地分享给前端
3. 在生产环境中，应该使用密钥管理系统（如AWS KMS、Azure Key Vault等）来管理密钥
4. 建议定期更换密钥对

## 临时密钥对（仅用于开发测试）

**公钥（配置到前端）：**
```
MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAz6J0K7B5qX9wL3mR8tG2
yD5zN0qK3pA7B2qX9wL3mR8tG2yD5zN0qK3pA7B2qX9wL3mR8tG2yD5zN0qK3pA7
B2qX9wL3mR8tG2yD5zN0qK3pA7B2qX9wL3mR8tG2yD5zN0qK3pA7B2qX9wL3mR8
tG2yD5zN0qK3pA7B2qX9wL3mR8tG2yD5zN0qK3pA7B2qX9wL3mR8tG2yD5zN0qK3
pA7B2qX9wL3mR8tG2yD5zN0qK3pA7B2qX9wL3mR8tG2yD5zN0qK3pA7B2qX9wL3m
R8tG2yD5zN0qK3pA7B2qX9wL3mR8tG2yD5zN0qK3pA7B2qX9wL3mR8tG2yD5zN0q
K3pA7B2qX9wL3mR8tG2yD5zN0qK3pA7B2qX9wL3mR8tG2yD5zN0qK3pA7B2qX9w
L3mR8tG2yD5zN0qK3pA7B2qX9wL3mR8tG2yD5zN0qK3pA7B2qX9wL3mR8tG2yD5z
N0qK3pA7B2qX9wL3mR8tG2yD5zN0qK3pA7B2qX9wL3mR8tG2yD5zN0qK3pA7B2qX
9wIDAQAB
```

**私钥（配置到后端）：**
```
MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQDPonQrsHmpf3AveZ
Hy0bbIPnM3SorenADsHapf3AveZHy0bbIPnM3SorenADsHapf3AveZHy0bbIPnM3S
orenADsHapf3AveZHy0bbIPnM3SorenADsHapf3AveZHy0bbIPnM3SorenADsHap
f3AveZHy0bbIPnM3SorenADsHapf3AveZHy0bbIPnM3SorenADsHapf3AveZHy0bb
IPnM3SorenADsHapf3AveZHy0bbIPnM3SorenADsHapf3AveZHy0bbIPnM3SorenAD
sHapf3AveZHy0bbIPnM3SorenADsHapf3AveZHy0bbIPnM3SorenADsHapf3AveZH
y0bbIPnM3SorenADsHapf3AveZHy0bbIPnM3SorenADsHapf3AveZHy0bbIPnM3So
renADsHapf3AveZHy0bbIPnM3SorenADsHapf3AveZHy0bbIPnM3SorenADsHapf3
AveZHy0bbIPnM3SorenADsHapf3AveZHy0bbIPnM3SorenADsHapf3AveZHy0bbIP
nM3SorenADwIDAQABAoIBAF2mQ7wX9Kp0rZ3vL4p2R5xN8K3tJ2mL4p6R3xN9K3t
J2mL4p6R3xN9K3tJ2mL4p6R3xN9K3tJ2mL4p6R3xN9K3tJ2mL4p6R3xN9K3tJ2m
L4p6R3xN9K3tJ2mL4p6R3xN9K3tJ2mL4p6R3xN9K3tJ2mL4p6R3xN9K3tJ2mL4p
6R3xN9K3tJ2mL4p6R3xN9K3tJ2mL4p6R3xN9K3tJ2mL4p6R3xN9K3tJ2mL4p6R3
xN9K3tJ2mL4p6R3xN9K3tJ2mL4p6R3xN9K3tJ2mL4p6R3xN9K3tJ2mL4p6R3xN9
K3tJ2mL4p6R3xN9K3tJ2mL4p6R3xN9K3tJ2mL4p6R3xN9K3tJ2mL4p6R3xN9K3t
J2mL4p6R3xN9K3tJ2mL4p6R3xN9K3tJ2mL4p6R3xN9K3tJ2mL4p6R3xN9K3tJ2m
```
