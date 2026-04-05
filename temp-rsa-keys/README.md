# 临时 RSA 密钥文件

## 说明

这个文件夹包含了临时生成的 RSA 密钥对和相关文档。

## 文件列表

- `private_key.pem` - RSA 私钥（PEM 格式）
- `private_key.der` - RSA 私钥（DER 格式，Java 使用）
- `private_key.txt` - 私钥的 Base64 编码
- `private_key_base64.txt` - 私钥的 Base64 编码（备用）
- `public_key.pem` - RSA 公钥（PEM 格式）
- `public_key.txt` - 公钥的 Base64 编码
- `generate-rsa-keys.sh` - 生成新密钥对的脚本
- `RSA_MIGRATION.md` - RSA 加密迁移文档

## 当前状态

⚠️ **这些密钥仅用于开发测试，请勿在生产环境使用！**

## 生产环境配置

在生产环境中，请：

1. **生成新的密钥对**：
   ```bash
   cd temp-keys
   bash generate-rsa-keys.sh
   ```

2. **配置密钥**：
   - 将公钥配置到 `DataSourceController.java` 的 `PUBLIC_KEY` 常量
   - 将私钥配置到 `DataSourceServiceImpl.java` 的 `PRIVATE_KEY` 常量

3. **密钥管理最佳实践**：
   - 不要将私钥硬编码在代码中
   - 使用环境变量或配置文件
   - 考虑使用密钥管理系统（AWS KMS、Azure Key Vault 等）
   - 定期轮换密钥

## 安全注意事项

- 🔒 私钥必须保密，不要泄露给任何人
- 🌍 公钥可以安全地分享给前端
- 🔄 建议定期更换密钥对
- 📝 请勿将这些文件提交到版本控制系统（已在 .gitignore 中配置）

## 清理说明

这些文件已经从项目根目录移动到此文件夹，以保持项目结构整洁。
