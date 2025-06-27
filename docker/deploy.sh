#!/bin/bash

# 简单的Docker部署脚本

APP_NAME="resource-search-backend"
IMAGE_NAME="resource-search-backend:latest"
CONTAINER_NAME="resource-search-backend"
PORT="9090"

# 颜色
RED='\033[0;31m'
GREEN='\033[0;32m'
BLUE='\033[0;34m'
NC='\033[0m'

print_msg() {
    echo -e "${BLUE}[$(date '+%H:%M:%S')] $1${NC}"
}

print_success() {
    echo -e "${GREEN}[$(date '+%H:%M:%S')] $1${NC}"
}

print_error() {
    echo -e "${RED}[$(date '+%H:%M:%S')] $1${NC}"
}

# 构建镜像
build() {
    print_msg "构建Docker镜像..."
    cd "$(dirname "$0")"
    docker build -t $IMAGE_NAME .
    print_success "镜像构建完成"
}

# 启动容器
start() {
    print_msg "启动服务..."
    
    # 停止旧容器
    docker stop $CONTAINER_NAME 2>/dev/null || true
    docker rm $CONTAINER_NAME 2>/dev/null || true
    
    # 启动新容器
    docker run -d \
        --name $CONTAINER_NAME \
        --restart unless-stopped \
        -p $PORT:9090 \
        -v "$(pwd)/logs:/app/logs" \
        $IMAGE_NAME
    
    sleep 5
    
    if docker ps | grep $CONTAINER_NAME > /dev/null; then
        print_success "服务启动成功"
        print_success "访问地址: http://localhost:$PORT"
    else
        print_error "服务启动失败"
        exit 1
    fi
}

# 停止容器
stop() {
    print_msg "停止服务..."
    docker stop $CONTAINER_NAME
    docker rm $CONTAINER_NAME
    print_success "服务已停止"
}

# 重启容器
restart() {
    print_msg "重启服务..."
    docker restart $CONTAINER_NAME
    print_success "服务已重启"
}

# 查看状态
status() {
    print_msg "查看服务状态..."
    if docker ps | grep $CONTAINER_NAME > /dev/null; then
        print_success "服务运行中"
        docker ps | grep $CONTAINER_NAME
    else
        print_error "服务未运行"
    fi
}

# 查看日志
logs() {
    print_msg "查看实时日志..."
    docker logs -f $CONTAINER_NAME
}

# 进入容器
enter() {
    print_msg "进入容器..."
    docker exec -it $CONTAINER_NAME sh
}

# 清理
cleanup() {
    print_msg "清理资源..."
    docker stop $CONTAINER_NAME 2>/dev/null || true
    docker rm $CONTAINER_NAME 2>/dev/null || true
    docker rmi $IMAGE_NAME 2>/dev/null || true
    print_success "清理完成"
}

# 完整部署
deploy() {
    print_msg "开始完整部署..."
    
    # 检查jar文件
    if [ ! -f "resource-search-backend-1.0-SNAPSHOT.jar" ]; then
        print_msg "构建应用..."
        cd ..
        mvn clean package -DskipTests
        cd docker
    fi
    
    mkdir -p logs
    build
    start
    print_success "部署完成!"
}

# 帮助信息
help() {
    echo "用法: $0 [命令]"
    echo ""
    echo "命令:"
    echo "  deploy   - 完整部署"
    echo "  build    - 构建镜像"
    echo "  start    - 启动服务"
    echo "  stop     - 停止服务"
    echo "  restart  - 重启服务"
    echo "  status   - 查看状态"
    echo "  logs     - 查看日志"
    echo "  enter    - 进入容器"
    echo "  cleanup  - 清理资源"
    echo "  help     - 显示帮助"
}

# 主函数
case "${1:-help}" in
    deploy)   deploy ;;
    build)    build ;;
    start)    start ;;
    stop)     stop ;;
    restart)  restart ;;
    status)   status ;;
    logs)     logs ;;
    enter)    enter ;;
    cleanup)  cleanup ;;
    help)     help ;;
    *)        echo "未知命令: $1"; help; exit 1 ;;
esac
