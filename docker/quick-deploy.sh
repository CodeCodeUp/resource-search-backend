#!/bin/bash

# 一键部署脚本

echo "========================================="
echo "    资源搜索后端 - 一键部署"
echo "========================================="

# 检查Docker
if ! command -v docker &> /dev/null; then
    echo "❌ Docker 未安装"
    exit 1
fi

cd "$(dirname "$0")"

# 构建应用
if [ ! -f "docker/resource-search-backend-1.0-SNAPSHOT.jar" ]; then
    echo "🔨 构建应用..."
    cd ..
    mvn clean package -DskipTests
    cd docker
fi

# 创建日志目录
mkdir -p logs

# 停止旧容器
echo "🛑 停止旧服务..."
docker stop resource-search-backend 2>/dev/null || true
docker rm resource-search-backend 2>/dev/null || true

# 构建镜像
echo "🏗️ 构建镜像..."
docker build -t resource-search-backend:latest .

# 启动容器
echo "🚀 启动服务..."
docker run -d \
    --name resource-search-backend \
    --restart unless-stopped \
    -p 9090:9090 \
    -v "$(pwd)/logs:/app/logs" \
    resource-search-backend:latest

# 等待启动
echo "⏳ 等待服务启动..."
sleep 10

# 检查状态
if docker ps | grep resource-search-backend > /dev/null; then
    echo "✅ 部署成功!"
    echo "🌐 访问地址: http://localhost:9090"
    echo ""
    echo "常用命令:"
    echo "  查看日志: ./deploy.sh logs"
    echo "  查看状态: ./deploy.sh status"
    echo "  重启服务: ./deploy.sh restart"
    echo "  停止服务: ./deploy.sh stop"
else
    echo "❌ 部署失败"
    echo "查看错误: docker logs resource-search-backend"
    exit 1
fi
