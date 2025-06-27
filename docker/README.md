# Docker 部署

## 快速开始

```bash
cd docker
chmod +x *.sh
./quick-deploy.sh
```

## 常用命令

```bash
# 完整部署
./deploy.sh deploy

# 服务管理
./deploy.sh start      # 启动
./deploy.sh stop       # 停止
./deploy.sh restart    # 重启
./deploy.sh status     # 状态

# 日志和调试
./deploy.sh logs       # 查看日志
./deploy.sh enter      # 进入容器

# 监控
./monitor.sh monitor   # 持续监控
./monitor.sh check     # 检查状态

# 清理
./deploy.sh cleanup    # 清理资源
```

## 文件说明

- `Dockerfile` - Docker镜像
- `deploy.sh` - 部署脚本
- `quick-deploy.sh` - 一键部署
- `monitor.sh` - 监控脚本
- `logs/` - 日志目录

## 访问地址

- 应用: http://localhost:9090
- 健康检查: http://localhost:9090/api/health

## 故障排查

```bash
# 查看容器状态
docker ps -a

# 查看日志
docker logs resource-search-backend

# 重新部署
./deploy.sh cleanup
./deploy.sh deploy
```
