#!/bin/bash

# 简单监控脚本

CONTAINER_NAME="resource-search-backend"

# 检查容器状态
check_container() {
    docker ps | grep $CONTAINER_NAME > /dev/null
}

# 检查应用健康
check_health() {
    curl -s http://localhost:9090/api/health > /dev/null 2>&1
}

# 重启服务
restart_service() {
    echo "[$(date)] 重启服务..."
    docker restart $CONTAINER_NAME
    sleep 10
}

# 监控循环
monitor() {
    echo "开始监控服务..."
    echo "按 Ctrl+C 停止监控"
    
    while true; do
        if check_container; then
            if check_health; then
                echo "[$(date)] ✅ 服务正常"
            else
                echo "[$(date)] ⚠️ 健康检查失败，尝试重启..."
                restart_service
            fi
        else
            echo "[$(date)] ❌ 容器未运行"
        fi
        
        sleep 30
    done
}

# 一次性检查
check() {
    echo "执行健康检查..."
    
    if check_container; then
        echo "✅ 容器运行正常"
        
        if check_health; then
            echo "✅ 应用健康检查通过"
            echo ""
            echo "容器信息:"
            docker ps | grep $CONTAINER_NAME
            echo ""
            echo "资源使用:"
            docker stats $CONTAINER_NAME --no-stream
        else
            echo "❌ 应用健康检查失败"
            exit 1
        fi
    else
        echo "❌ 容器未运行"
        exit 1
    fi
}

case "${1:-monitor}" in
    monitor) monitor ;;
    check)   check ;;
    *)       echo "用法: $0 [monitor|check]"; exit 1 ;;
esac
