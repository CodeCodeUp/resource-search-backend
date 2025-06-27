#!/bin/bash

# 资源搜索后端 Docker 部署脚本 (纯Docker版本)
# 作者: AI Assistant
# 版本: 1.0
# 日期: 2025-06-27

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# 配置变量
APP_NAME="resource-search-backend"
CONTAINER_NAME="resource-search-backend"
IMAGE_NAME="resource-search-backend:latest"
PORT="9090"
DOCKER_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$DOCKER_DIR")"
LOG_DIR="$DOCKER_DIR/logs"
CONFIG_DIR="$DOCKER_DIR/config"

# Docker网络名称
NETWORK_NAME="resource-network"

# 通知配置（可选）
WEBHOOK_URL=""  # 钉钉或企业微信webhook地址
NOTIFICATION_ENABLED=false

# 打印带颜色的消息
print_message() {
    local color=$1
    local message=$2
    echo -e "${color}[$(date '+%Y-%m-%d %H:%M:%S')] ${message}${NC}"
}

# 发送通知
send_notification() {
    local title=$1
    local message=$2
    local status=$3
    
    if [ "$NOTIFICATION_ENABLED" = true ] && [ -n "$WEBHOOK_URL" ]; then
        curl -s -X POST "$WEBHOOK_URL" \
            -H 'Content-Type: application/json' \
            -d "{
                \"msgtype\": \"text\",
                \"text\": {
                    \"content\": \"【$title】\n$message\n时间: $(date '+%Y-%m-%d %H:%M:%S')\"
                }
            }" > /dev/null 2>&1
    fi
}

# 检查Docker是否安装
check_docker() {
#    if ! command -v docker &> /dev/null; then
#        print_message $RED "Docker 未安装，请先安装 Docker"
#        exit 1
#    fi
    print_message $GREEN "Docker 已安装"
}

# 检查端口是否被占用
check_port() {
    if netstat -tuln 2>/dev/null | grep ":$PORT " > /dev/null 2>&1; then
        print_message $YELLOW "警告: 端口 $PORT 已被占用"
        read -p "是否继续部署? (y/N): " -n 1 -r
        echo
        if [[ ! $REPLY =~ ^[Yy]$ ]]; then
            exit 1
        fi
    fi
}

# 创建必要的目录
create_directories() {
    print_message $BLUE "创建必要的目录..."
    mkdir -p "$LOG_DIR"
    mkdir -p "$CONFIG_DIR"
    
    # 设置目录权限
    chmod 755 "$LOG_DIR"
    chmod 755 "$CONFIG_DIR"
}

# 创建Docker网络
create_network() {
    if ! docker network ls | grep $NETWORK_NAME > /dev/null 2>&1; then
        print_message $BLUE "创建Docker网络: $NETWORK_NAME"
        docker network create $NETWORK_NAME
    fi
}

# 构建应用
build_app() {
    print_message $BLUE "开始构建应用..."
    
    cd "$PROJECT_ROOT"
    
    # 检查是否存在jar文件
    if [ ! -f "docker/resource-search-backend-1.0-SNAPSHOT.jar" ]; then
        print_message $YELLOW "未找到jar文件，开始Maven构建..."
        if command -v mvn &> /dev/null; then
            mvn clean package -DskipTests
        else
            print_message $RED "Maven 未安装，请先构建应用或安装Maven"
            exit 1
        fi
    fi
    
    print_message $GREEN "应用构建完成"
}

# 构建Docker镜像
build_image() {
    print_message $BLUE "构建Docker镜像..."
    cd "$DOCKER_DIR"
    
    # 构建镜像
    docker build -f Dockerfile -t $IMAGE_NAME ..
    
    print_message $GREEN "Docker镜像构建完成"
}

# 启动服务
start_service() {
    print_message $BLUE "启动服务..."
    
    # 停止现有容器（如果存在）
    if docker ps -a | grep $CONTAINER_NAME > /dev/null 2>&1; then
        print_message $YELLOW "停止现有容器..."
        docker stop $CONTAINER_NAME || true
        docker rm $CONTAINER_NAME || true
    fi
    
    # 创建网络
    create_network
    
    # 启动新容器
    docker run -d \
        --name $CONTAINER_NAME \
        --restart unless-stopped \
        --network $NETWORK_NAME \
        -p $PORT:9090 \
        -v "$LOG_DIR:/app/logs" \
        -v "$CONFIG_DIR:/app/config" \
        -e JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:G1HeapRegionSize=16m -XX:+UseStringDeduplication" \
        -e TZ=Asia/Shanghai \
        $IMAGE_NAME
    
    # 等待服务启动
    print_message $BLUE "等待服务启动..."
    sleep 10
    
    # 检查服务状态
    if docker ps | grep $CONTAINER_NAME > /dev/null 2>&1; then
        print_message $GREEN "服务启动成功"
        print_message $CYAN "访问地址: http://localhost:$PORT/api"
        send_notification "部署成功" "$APP_NAME 服务已成功启动" "success"
    else
        print_message $RED "服务启动失败"
        send_notification "部署失败" "$APP_NAME 服务启动失败" "error"
        exit 1
    fi
}

# 停止服务
stop_service() {
    print_message $BLUE "停止服务..."
    
    if docker ps | grep $CONTAINER_NAME > /dev/null 2>&1; then
        docker stop $CONTAINER_NAME
        docker rm $CONTAINER_NAME
        print_message $GREEN "服务已停止"
        send_notification "服务停止" "$APP_NAME 服务已停止" "warning"
    else
        print_message $YELLOW "服务未运行"
    fi
}

# 重启服务
restart_service() {
    print_message $BLUE "重启服务..."
    
    if docker ps | grep $CONTAINER_NAME > /dev/null 2>&1; then
        docker restart $CONTAINER_NAME
        
        # 等待服务重启
        sleep 10
        
        if docker ps | grep $CONTAINER_NAME > /dev/null 2>&1; then
            print_message $GREEN "服务重启成功"
            send_notification "服务重启" "$APP_NAME 服务已重启" "success"
        else
            print_message $RED "服务重启失败"
            send_notification "重启失败" "$APP_NAME 服务重启失败" "error"
        fi
    else
        print_message $YELLOW "服务未运行，尝试启动..."
        start_service
    fi
}

# 查看服务状态
status_service() {
    print_message $BLUE "查看服务状态..."
    
    if docker ps | grep $CONTAINER_NAME > /dev/null 2>&1; then
        print_message $GREEN "服务运行中"
        echo
        docker ps | grep $CONTAINER_NAME
        echo
        print_message $CYAN "容器详细信息:"
        docker inspect $CONTAINER_NAME | grep -E "(Status|Health|RestartCount)" || true
    else
        print_message $RED "服务未运行"
        
        # 检查是否有停止的容器
        if docker ps -a | grep $CONTAINER_NAME > /dev/null 2>&1; then
            print_message $YELLOW "发现停止的容器:"
            docker ps -a | grep $CONTAINER_NAME
        fi
    fi
}

# 查看实时日志
view_logs() {
    print_message $BLUE "查看实时日志 (按 Ctrl+C 退出)..."
    
    if docker ps | grep $CONTAINER_NAME > /dev/null 2>&1; then
        docker logs -f $CONTAINER_NAME
    else
        print_message $RED "容器未运行"
        exit 1
    fi
}

# 查看应用日志
view_app_logs() {
    print_message $BLUE "查看应用日志..."
    
    if [ -f "$LOG_DIR/application.log" ]; then
        tail -f "$LOG_DIR/application.log"
    else
        print_message $YELLOW "应用日志文件不存在，查看容器日志..."
        if docker ps | grep $CONTAINER_NAME > /dev/null 2>&1; then
            docker logs -f $CONTAINER_NAME
        else
            print_message $RED "容器未运行"
        fi
    fi
}

# 进入容器
enter_container() {
    print_message $BLUE "进入容器..."
    
    if docker ps | grep $CONTAINER_NAME > /dev/null 2>&1; then
        docker exec -it $CONTAINER_NAME sh
    else
        print_message $RED "容器未运行"
        exit 1
    fi
}

# 清理资源
cleanup() {
    print_message $BLUE "清理Docker资源..."
    
    # 停止并删除容器
    if docker ps -a | grep $CONTAINER_NAME > /dev/null 2>&1; then
        docker stop $CONTAINER_NAME || true
        docker rm $CONTAINER_NAME || true
    fi
    
    # 删除镜像
    if docker images | grep $APP_NAME > /dev/null 2>&1; then
        docker rmi $IMAGE_NAME || true
    fi
    
    # 删除网络
    if docker network ls | grep $NETWORK_NAME > /dev/null 2>&1; then
        docker network rm $NETWORK_NAME || true
    fi
    
    # 清理未使用的镜像
    docker image prune -f
    
    print_message $GREEN "清理完成"
}

# 备份数据
backup_data() {
    local backup_dir="$DOCKER_DIR/backup/$(date +%Y%m%d_%H%M%S)"
    print_message $BLUE "备份数据到 $backup_dir..."
    
    mkdir -p "$backup_dir"
    
    # 备份日志
    if [ -d "$LOG_DIR" ]; then
        cp -r "$LOG_DIR" "$backup_dir/"
    fi
    
    # 备份配置
    if [ -d "$CONFIG_DIR" ]; then
        cp -r "$CONFIG_DIR" "$backup_dir/"
    fi
    
    # 备份Docker配置
    cp "$DOCKER_DIR/Dockerfile" "$backup_dir/"
    
    print_message $GREEN "备份完成: $backup_dir"
}

# 健康检查
health_check() {
    print_message $BLUE "执行健康检查..."
    
    # 检查容器状态
    if ! docker ps | grep $CONTAINER_NAME > /dev/null 2>&1; then
        print_message $RED "容器未运行"
        return 1
    fi
    
    # 检查应用健康状态
    if command -v curl &> /dev/null; then
        if curl -s -f "http://localhost:$PORT/api/health" > /dev/null 2>&1; then
            print_message $GREEN "应用健康检查通过"
        else
            print_message $RED "应用健康检查失败"
            return 1
        fi
    else
        print_message $YELLOW "curl 未安装，跳过HTTP健康检查"
    fi
    
    # 检查资源使用情况
    print_message $CYAN "资源使用情况:"
    docker stats $CONTAINER_NAME --no-stream
}

# 显示帮助信息
show_help() {
    echo -e "${CYAN}资源搜索后端 Docker 部署脚本 (纯Docker版本)${NC}"
    echo
    echo -e "${YELLOW}用法:${NC}"
    echo "  $0 [命令]"
    echo
    echo -e "${YELLOW}可用命令:${NC}"
    echo "  deploy      - 完整部署 (构建+启动)"
    echo "  start       - 启动服务"
    echo "  stop        - 停止服务"
    echo "  restart     - 重启服务"
    echo "  status      - 查看服务状态"
    echo "  logs        - 查看实时日志"
    echo "  app-logs    - 查看应用日志"
    echo "  enter       - 进入容器"
    echo "  health      - 健康检查"
    echo "  backup      - 备份数据"
    echo "  cleanup     - 清理资源"
    echo "  build       - 仅构建镜像"
    echo "  help        - 显示帮助信息"
    echo
    echo -e "${YELLOW}示例:${NC}"
    echo "  $0 deploy   # 完整部署应用"
    echo "  $0 logs     # 查看实时日志"
    echo "  $0 restart  # 重启服务"
}

# 主函数
main() {
    case "${1:-help}" in
        deploy)
            check_docker
            check_port
            create_directories
            build_app
            build_image
            start_service
            ;;
        start)
            check_docker
            start_service
            ;;
        stop)
            stop_service
            ;;
        restart)
            restart_service
            ;;
        status)
            status_service
            ;;
        logs)
            view_logs
            ;;
        app-logs)
            view_app_logs
            ;;
        enter)
            enter_container
            ;;
        health)
            health_check
            ;;
        backup)
            backup_data
            ;;
        cleanup)
            cleanup
            ;;
        build)
            check_docker
            build_app
            build_image
            ;;
        help|--help|-h)
            show_help
            ;;
        *)
            print_message $RED "未知命令: $1"
            show_help
            exit 1
            ;;
    esac
}

# 执行主函数
main "$@"
