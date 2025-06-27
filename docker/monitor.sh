#!/bin/bash

# 服务监控脚本
# 监控资源搜索后端服务状态

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

CONTAINER_NAME="resource-search-backend"
PORT="9090"
DOCKER_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
LOG_FILE="$DOCKER_DIR/monitor.log"

# 记录日志
log_message() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] $1" | tee -a "$LOG_FILE"
}

# 检查容器状态
check_container() {
    if docker ps | grep $CONTAINER_NAME > /dev/null 2>&1; then
        return 0
    else
        return 1
    fi
}

# 检查应用健康状态
check_health() {
    if command -v curl &> /dev/null; then
        if curl -s -f "http://localhost:$PORT/api/health" > /dev/null 2>&1; then
            return 0
        else
            return 1
        fi
    else
        # 如果没有curl，只检查端口
        if netstat -tuln 2>/dev/null | grep ":$PORT " > /dev/null 2>&1; then
            return 0
        else
            return 1
        fi
    fi
}

# 获取容器资源使用情况
get_container_stats() {
    docker stats $CONTAINER_NAME --no-stream --format "table {{.CPUPerc}}\t{{.MemUsage}}\t{{.MemPerc}}\t{{.NetIO}}\t{{.BlockIO}}" 2>/dev/null
}

# 重启服务
restart_service() {
    log_message "尝试重启服务..."

    if docker ps | grep $CONTAINER_NAME > /dev/null 2>&1; then
        docker restart $CONTAINER_NAME
        sleep 10

        if check_container && check_health; then
            log_message "服务重启成功"
            return 0
        else
            log_message "服务重启失败"
            return 1
        fi
    else
        log_message "容器未运行，无法重启"
        return 1
    fi
}

# 发送告警（可扩展）
send_alert() {
    local message=$1
    log_message "告警: $message"
    
    # 这里可以添加邮件、短信、钉钉等告警方式
    # 示例：发送到钉钉
    # curl -X POST "your_webhook_url" -H 'Content-Type: application/json' -d "{\"text\":{\"content\":\"$message\"}}"
}

# 主监控函数
monitor() {
    echo -e "${BLUE}开始监控服务状态...${NC}"
    echo -e "${YELLOW}按 Ctrl+C 停止监控${NC}"
    echo
    
    while true; do
        local status_ok=true
        local status_message=""
        
        # 检查容器状态
        if ! check_container; then
            status_ok=false
            status_message="容器未运行"
        fi
        
        # 检查应用健康状态
        if $status_ok && ! check_health; then
            status_ok=false
            status_message="应用健康检查失败"
        fi
        
        # 显示状态
        local timestamp=$(date '+%Y-%m-%d %H:%M:%S')
        if $status_ok; then
            echo -e "${GREEN}[$timestamp] 服务正常${NC}"
            
            # 显示资源使用情况
            local stats=$(get_container_stats)
            if [ -n "$stats" ]; then
                echo "  资源使用: $stats"
            fi
        else
            echo -e "${RED}[$timestamp] 服务异常: $status_message${NC}"
            send_alert "服务异常: $status_message"
            
            # 尝试自动重启
            if restart_service; then
                echo -e "${GREEN}[$timestamp] 自动重启成功${NC}"
            else
                echo -e "${RED}[$timestamp] 自动重启失败，需要人工干预${NC}"
                send_alert "自动重启失败，需要人工干预"
            fi
        fi
        
        sleep 30
    done
}

# 一次性检查
check_once() {
    echo -e "${BLUE}执行一次性健康检查...${NC}"
    
    if check_container; then
        echo -e "${GREEN}✅ 容器运行正常${NC}"
        
        if check_health; then
            echo -e "${GREEN}✅ 应用健康检查通过${NC}"
            
            # 显示详细信息
            echo
            echo -e "${BLUE}容器信息:${NC}"
            docker ps | grep $CONTAINER_NAME
            
            echo
            echo -e "${BLUE}资源使用:${NC}"
            get_container_stats
            
            echo
            echo -e "${BLUE}最近日志:${NC}"
            docker logs --tail 10 $CONTAINER_NAME
            
        else
            echo -e "${RED}❌ 应用健康检查失败${NC}"
            exit 1
        fi
    else
        echo -e "${RED}❌ 容器未运行${NC}"
        exit 1
    fi
}

# 显示帮助
show_help() {
    echo "服务监控脚本"
    echo
    echo "用法:"
    echo "  $0 [命令]"
    echo
    echo "命令:"
    echo "  monitor  - 持续监控服务状态"
    echo "  check    - 执行一次性健康检查"
    echo "  help     - 显示帮助信息"
    echo
    echo "示例:"
    echo "  $0 monitor  # 开始持续监控"
    echo "  $0 check    # 检查当前状态"
}

# 主函数
case "${1:-monitor}" in
    monitor)
        monitor
        ;;
    check)
        check_once
        ;;
    help|--help|-h)
        show_help
        ;;
    *)
        echo -e "${RED}未知命令: $1${NC}"
        show_help
        exit 1
        ;;
esac
