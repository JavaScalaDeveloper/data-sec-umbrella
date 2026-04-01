#!/bin/bash

# 数据安全保护伞前端项目启动脚本

# 项目根目录
PROJECT_DIR=$(cd "$(dirname "$0")" && pwd)

# 环境配置
ENV_CONFIGS=(
    "dev:3000"
    "test:3001"
    "pre:3002"
    "prd:3003"
)

# 日志文件
LOG_FILE="$PROJECT_DIR/logs/front.log"

# 获取环境对应的端口
get_port() {
    local env=$1
    for config in "${ENV_CONFIGS[@]}"; do
        if [[ "$config" == "$env:"* ]]; then
            echo "${config#*:}"
            return
        fi
    done
    echo "3000"
}

# 检查进程是否运行
check_process() {
    local port=$1
    lsof -i :$port > /dev/null 2>&1
    return $?
}

# 获取进程ID
get_pid() {
    local port=$1
    lsof -i :$port | grep LISTEN | awk '{print $2}'
}

# 启动项目
start() {
    local env=$1
    local port=$(get_port $env)
    
    echo "正在启动前端项目 [$env]，端口：$port..."
    
    # 创建日志目录
    mkdir -p "$(dirname "$LOG_FILE")"
    
    # 检查进程是否已运行
    if check_process $port; then
        echo "前端项目 [$env] 已在端口 $port 运行，进程ID：$(get_pid $port)"
        return 1
    fi
    
    # 启动项目
    cd $PROJECT_DIR
    npm run dev -- --port $port > $LOG_FILE 2>&1 &
    
    # 等待进程启动
    sleep 3
    
    # 检查启动是否成功
    if check_process $port; then
        echo "前端项目 [$env] 启动成功，端口：$port，进程ID：$(get_pid $port)"
        echo "访问地址：http://localhost:$port"
        return 0
    else
        echo "前端项目 [$env] 启动失败，请查看日志：$LOG_FILE"
        return 1
    fi
}

# 停止项目
stop() {
    local env=$1
    local port=$(get_port $env)
    
    echo "正在停止前端项目 [$env]，端口：$port..."
    
    # 检查进程是否运行
    if ! check_process $port; then
        echo "前端项目 [$env] 未运行"
        return 1
    fi
    
    # 停止进程
    kill $(get_pid $port)
    
    # 等待进程停止
    sleep 2
    
    # 检查是否停止成功
    if ! check_process $port; then
        echo "前端项目 [$env] 停止成功"
        return 0
    else
        echo "前端项目 [$env] 停止失败，尝试强制停止..."
        kill -9 $(get_pid $port)
        
        sleep 1
        
        if ! check_process $port; then
            echo "前端项目 [$env] 强制停止成功"
            return 0
        else
            echo "前端项目 [$env] 停止失败，请手动处理"
            return 1
        fi
    fi
}

# 重启项目
restart() {
    local env=$1
    
    echo "正在重启前端项目 [$env]..."
    
    # 先停止项目
    stop $env
    
    # 再启动项目
    start $env
    
    return $?
}

# 显示帮助信息
show_help() {
    echo "数据安全保护伞前端项目管理脚本"
    echo ""
    echo "用法："
    echo "  $0 [命令] [环境]"
    echo ""
    echo "命令："
    echo "  start    - 启动项目"
    echo "  stop     - 停止项目"
    echo "  restart  - 重启项目"
    echo "  status   - 查看项目状态"
    echo "  help     - 显示帮助信息"
    echo ""
    echo "环境："
    echo "  dev      - 开发环境（端口：3000）"
    echo "  test     - 测试环境（端口：3001）"
    echo "  pre      - 预生产环境（端口：3002）"
    echo "  prd      - 生产环境（端口：3003）"
    echo ""
    echo "示例："
    echo "  $0 start dev      - 启动开发环境"
    echo "  $0 stop test      - 停止测试环境"
    echo "  $0 restart prd    - 重启生产环境"
    echo "  $0 status dev     - 查看开发环境状态"
}

# 查看项目状态
status() {
    local env=$1
    local port=$(get_port $env)
    
    echo "前端项目 [$env] 状态："
    
    # 检查进程是否运行
    if check_process $port; then
        echo "  运行中，端口：$port，进程ID：$(get_pid $port)"
        echo "  访问地址：http://localhost:$port"
        return 0
    else
        echo "  未运行"
        return 1
    fi
}

# 主函数
main() {
    # 检查参数
    if [ $# -lt 1 ]; then
        show_help
        exit 1
    fi
    
    local command=$1
    local env=$2
    
    # 如果没有指定环境，默认使用dev
    if [ -z "$env" ]; then
        env="dev"
    fi
    
    # 执行命令
    case $command in
        start)
            start $env
            ;;
        stop)
            stop $env
            ;;
        restart)
            restart $env
            ;;
        status)
            status $env
            ;;
        help)
            show_help
            ;;
        *)
            echo "未知命令：$command"
            show_help
            exit 1
            ;;
    esac
}

# 执行主函数
main "$@"