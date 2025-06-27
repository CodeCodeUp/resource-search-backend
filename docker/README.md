# 资源搜索后端 Docker 部署

## 快速开始

### 1. 环境准备
确保已安装：
- Docker (>= 20.10)
- Maven (>= 3.6)

### 2. 一键部署
```bash
cd docker
chmod +x *.sh
./quick-deploy-docker.sh
```

### 3. 验证部署
```bash
./deploy-docker.sh status
./deploy-docker.sh health
```

## 文件说明

- `Dockerfile` - Docker镜像构建文件
- `deploy-docker.sh` - 完整部署管理脚本
- `quick-deploy-docker.sh` - 快速部署脚本
- `monitor.sh` - 服务监控脚本
- `.env.example` - 环境配置示例

## 常用命令

### 部署管理
```bash
./deploy-docker.sh deploy    # 完整部署
./deploy-docker.sh start     # 启动服务
./deploy-docker.sh stop      # 停止服务
./deploy-docker.sh restart   # 重启服务
./deploy-docker.sh status    # 查看状态
```

### 日志查看
```bash
./deploy-docker.sh logs      # 实时日志
./deploy-docker.sh app-logs  # 应用日志
```

### 运维操作
```bash
./deploy-docker.sh health    # 健康检查
./deploy-docker.sh backup    # 备份数据
./deploy-docker.sh cleanup   # 清理资源
./deploy-docker.sh enter     # 进入容器
```

### 监控服务
```bash
./monitor.sh monitor  # 持续监控
./monitor.sh check    # 一次性检查
```

## 配置说明

### 环境配置
1. 复制配置文件：`cp .env.example .env`
2. 修改配置参数（数据库、夸克网盘等）

### 端口配置
默认端口：9090
修改：编辑 `deploy-docker.sh` 中的 PORT 变量

### JVM配置
默认：`-Xms512m -Xmx1024m -XX:+UseG1GC`
修改：编辑 `deploy-docker.sh` 中的 JAVA_OPTS 环境变量

## 目录结构
```
docker/
├── Dockerfile                # Docker镜像定义
├── deploy-docker.sh          # 主部署脚本
├── quick-deploy-docker.sh    # 快速部署脚本
├── monitor.sh               # 监控脚本
├── .env.example            # 环境配置示例
├── logs/                   # 日志目录
├── config/                 # 配置目录
└── backup/                 # 备份目录
```

## 故障排查

### 容器启动失败
```bash
docker logs resource-search-backend
./deploy.sh status
```

### 端口冲突
```bash
netstat -tuln | grep 9090
# 修改 docker-compose.yml 中的端口映射
```

### 内存不足
```bash
# 调整 deploy-docker.sh 中的 JAVA_OPTS
```

### 数据库连接失败
```bash
# 检查网络连通性和配置
docker exec -it resource-search-backend ping db_host
```

## 访问地址

- 应用地址：http://localhost:9090/api
- 健康检查：http://localhost:9090/api/health

## 注意事项

1. 首次部署前请配置好数据库连接信息
2. 生产环境建议修改默认端口和JVM参数
3. 定期备份重要数据
4. 监控服务运行状态
