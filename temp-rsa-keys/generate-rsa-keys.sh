#!/bin/bash

# 生成RSA密钥对
echo "生成RSA密钥对..."

# 生成私钥
openssl genrsa -out private_key.pem 2048

# 生成公钥
openssl rsa -in private_key.pem -pubout -out public_key.pem

# 转换为PKCS8格式
openssl pkcs8 -topk8 -inform PEM -outform DER -in private_key.pem -out private_key.der -nocrypt

# Base64编码
echo "公钥（PUBLIC_KEY）:"
echo -n "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA" > public_key.txt
cat public_key.pem | tail -n +2 | head -n -1 >> public_key.txt
echo "PUBLIC_KEY_BASE64_HERE" >> public_key.txt

echo "私钥（PRIVATE_KEY）:"
echo -n "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQC" > private_key.txt
cat private_key.der | base64 >> private_key.txt

echo "完成！"
echo "公钥保存在: public_key.txt"
echo "私钥保存在: private_key.txt"
