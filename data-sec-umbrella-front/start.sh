#!/bin/bash

# 数据安全保护伞前端启动脚本
# 功能：一键启动前端开发服务器，自动处理依赖安装和启动过程

echo "======================================"
echo "数据安全保护伞前端启动脚本"
echo "======================================"

# 检查是否在正确的目录
if [ ! -f "package.json" ]; then
    echo "错误：当前目录不是前端项目根目录，请在 data-sec-umbrella-front 目录下运行此脚本"
    exit 1
fi

# 检查 Node.js 是否安装
if ! command -v node &> /dev/null; then
    echo "错误：未检测到 Node.js，请先安装 Node.js 16 或更高版本"
    exit 1
fi

# 检查 npm 是否安装
if ! command -v npm &> /dev/null; then
    echo "错误：未检测到 npm，请先安装 npm"
    exit 1
fi

echo "Node.js 版本: $(node -v)"
echo "npm 版本: $(npm -v)"
echo ""

# 检查 node_modules 是否存在，如果不存在则安装依赖
if [ ! -d "node_modules" ]; then
    echo "正在安装项目依赖..."
    npm install
    if [ $? -ne 0 ]; then
        echo "错误：依赖安装失败"
        exit 1
    fi
    echo "依赖安装成功"
else
    echo "检测到 node_modules 已存在，跳过依赖安装"
fi

echo ""
echo "======================================"
echo "正在启动前端开发服务器..."
echo "访问地址：http://localhost:3000"
echo "按 Ctrl+C 停止服务器"
echo "======================================"
echo ""

# 启动开发服务器
npm start