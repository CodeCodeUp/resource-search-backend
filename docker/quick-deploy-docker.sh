#!/bin/bash

# 快速部署脚本 (纯Docker版本)
# 一键部署资源搜索后端服务

set -e

# 颜色定义
GREEN='\033[0;32m'
BLUE='\033[0;34m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m'

# 获取脚本所在目录
DOCKER_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$DOCKER_DIR")"

# 配置变量
APP_NAME="resource-search-backend"
CONTAINER_NAME="resource-search-backend"
IMAGE_NAME="resource-search-backend:latest"
PORT="9090"
NETWORK_NAME="resource-network"

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}    资源搜索后端 - 快速部署脚本${NC}"
echo -e "${BLUE}       (纯Docker版本)${NC}"
echo -e "${BLUE}========================================${NC}"

# 检查Docker
if ! command -v docker &> /dev/null; then
    echo -e "${RED}错误: Docker 未安装${NC}"
    exit 1
fi

# 切换到docker目录
cd "$DOCKER_DIR"

# 创建目录
echo -e "${BLUE}创建必要目录...${NC}"
mkdir -p logs config

# 构建应用（如果需要）
if [ ! -f "$PROJECT_ROOT/target/resource-search-backend-1.0-SNAPSHOT.jar" ]; then
    echo -e "${BLUE}构建应用...${NC}"
    cd "$PROJECT_ROOT"
    if command -v mvn &> /dev/null; then
        mvn clean package -DskipTests
    else
        echo -e "${RED}错误: Maven 未安装，请先构建应用${NC}"
        exit 1
    fi
    cd "$DOCKER_DIR"
fi

# 停止现有服务
echo -e "${BLUE}停止现有服务...${NC}"
if docker ps | grep $CONTAINER_NAME > /dev/null 2>&1; then
    docker stop $CONTAINER_NAME
fi
if docker ps -a | grep $CONTAINER_NAME > /dev/null 2>&1; then
    docker rm $CONTAINER_NAME
fi

# 创建网络（如果不存在）
if ! docker network ls | grep $NETWORK_NAME > /dev/null 2>&1; then
    echo -e "${BLUE}创建Docker网络...${NC}"
    docker network create $NETWORK_NAME
fi

# 构建镜像
echo -e "${BLUE}构建Docker镜像...${NC}"
docker build -f Dockerfile -t $IMAGE_NAME ..

# 启动容器
echo -e "${BLUE}启动服务...${NC}"
docker run -d \
    --name $CONTAINER_NAME \
    --restart unless-stopped \
    --network $NETWORK_NAME \
    -p $PORT:9090 \
    -v "$PWD/logs:/app/logs" \
    -v "$PWD/config:/app/config" \
    -e JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:G1HeapRegionSize=16m -XX:+UseStringDeduplication" \
    -e TZ=Asia/Shanghai \
    $IMAGE_NAME

# 等待启动
echo -e "${BLUE}等待服务启动...${NC}"
sleep 15

# 检查状态
if docker ps | grep $CONTAINER_NAME > /dev/null; then
    echo -e "${GREEN}✅ 部署成功!${NC}"
    echo -e "${GREEN}访问地址: http://localhost:$PORT/api${NC}"
    echo
    echo -e "${BLUE}常用命令:${NC}"
    echo -e "  查看日志: ${YELLOW}./deploy-docker.sh logs${NC}"
    echo -e "  查看状态: ${YELLOW}./deploy-docker.sh status${NC}"
    echo -e "  健康检查: ${YELLOW}./deploy-docker.sh health${NC}"
    echo -e "  重启服务: ${YELLOW}./deploy-docker.sh restart${NC}"
    echo -e "  停止服务: ${YELLOW}./deploy-docker.sh stop${NC}"
    echo -e "  进入容器: ${YELLOW}./deploy-docker.sh enter${NC}"
    echo -e "  查看帮助: ${YELLOW}./deploy-docker.sh help${NC}"
else
    echo -e "${RED}❌ 部署失败${NC}"
    echo -e "${BLUE}查看错误日志: ${YELLOW}docker logs $CONTAINER_NAME${NC}"
    exit 1
fi
