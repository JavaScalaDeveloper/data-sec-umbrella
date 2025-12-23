#!/bin/bash

# 数据安全伞后端启动脚本

echo "正在启动数据安全伞后端项目..."

# 检查Java是否安装
if ! command -v java &> /dev/null; then
    echo "错误: 未找到Java，请先安装Java 8或更高版本"
    exit 1
fi

# 检查Maven是否安装
if ! command -v mvn &> /dev/null; then
    echo "错误: 未找到Maven，请先安装Maven"
    exit 1
fi

# 检查pom.xml是否存在
if [ ! -f "pom.xml" ]; then
    echo "错误: 未找到pom.xml文件，请确保在正确的项目目录中"
    exit 1
fi

# 检查端口8081是否被占用
if lsof -Pi :8080 -sTCP:LISTEN -t >/dev/null ; then
    echo "警告: 端口8080已被占用，正在尝试终止占用进程..."
    lsof -ti:8080 | xargs kill -9
    sleep 2
fi

# 编译项目
echo "正在编译项目..."
mvn clean compile -q
if [ $? -ne 0 ]; then
    echo "错误: 项目编译失败"
    exit 1
fi

# 启动Spring Boot应用
echo "正在启动后端服务..."
echo "后端服务将在 http://localhost:8080 上运行"
echo "API文档地址: http://localhost:8080/api"
echo "按 Ctrl+C 停止服务器"
mvn spring-boot:run