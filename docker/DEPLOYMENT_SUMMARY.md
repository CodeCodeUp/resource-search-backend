# Docker 部署文件总结 (纯Docker版本)

## 已创建的文件

### 核心部署文件
- ✅ `Dockerfile` - Docker镜像构建文件
- ✅ `deploy-docker.sh` - 完整部署管理脚本（主脚本）
- ✅ `quick-deploy-docker.sh` - 快速一键部署脚本
- ✅ `monitor.sh` - 服务监控脚本

### 配置文件
- ✅ `.env.example` - 环境配置示例文件
- ✅ `README.md` - 部署使用说明文档
- ✅ `setup.bat` - Windows环境初始化脚本

## 功能特性

### 🚀 一键部署
```bash
cd docker
./quick-deploy-docker.sh
```

### 📊 完整管理
```bash
./deploy-docker.sh deploy    # 完整部署
./deploy-docker.sh start     # 启动服务
./deploy-docker.sh stop      # 停止服务
./deploy-docker.sh restart   # 重启服务
./deploy-docker.sh status    # 查看状态
./deploy-docker.sh logs      # 查看日志
./deploy-docker.sh health    # 健康检查
./deploy-docker.sh backup    # 备份数据
./deploy-docker.sh cleanup   # 清理资源
```

### 🔍 实时监控
```bash
./monitor.sh monitor  # 持续监控
./monitor.sh check    # 一次性检查
```

## 部署流程

### 1. 环境准备
- Docker & Docker Compose
- Maven（用于构建应用）

### 2. 快速部署
```bash
cd docker
chmod +x *.sh                    # Linux/Mac
./quick-deploy-docker.sh         # 一键部署
```

### 3. 验证部署
- 访问：http://localhost:9090/api
- 健康检查：http://localhost:9090/api/health

## 技术特性

### 🐳 Docker特性
- 基于OpenJDK 8 Alpine镜像
- 非root用户运行（安全）
- 自动健康检查
- 日志轮转配置
- 资源限制配置

### 🔧 运维特性
- 自动重启策略
- 实时日志查看
- 资源使用监控
- 自动备份功能
- 故障自动恢复

### 📱 通知特性
- 支持钉钉/企业微信通知
- 部署状态通知
- 故障告警通知
- 可扩展通知方式

## 目录结构

```
docker/
├── Dockerfile              # Docker镜像定义
├── docker-compose.yml      # 容器编排配置
├── deploy.sh              # 主部署脚本 ⭐
├── quick-deploy.sh        # 快速部署脚本 ⭐
├── monitor.sh             # 监控脚本 ⭐
├── .env.example          # 环境配置示例
├── README.md             # 使用说明
├── setup.bat             # Windows初始化
├── logs/                 # 日志目录（自动创建）
├── config/               # 配置目录（自动创建）
└── backup/               # 备份目录（自动创建）
```

## 使用建议

### 🎯 生产环境
1. 修改默认端口和JVM参数
2. 配置外部数据库连接
3. 启用通知功能
4. 定期备份数据
5. 监控资源使用情况

### 🔒 安全建议
1. 使用非默认端口
2. 配置防火墙规则
3. 启用HTTPS（通过反向代理）
4. 定期更新镜像
5. 监控异常访问

### 📈 性能优化
1. 根据负载调整JVM参数
2. 配置适当的资源限制
3. 使用SSD存储
4. 监控内存和CPU使用
5. 定期清理日志文件

## 常见问题

### Q: 如何修改端口？
A: 编辑 `docker-compose.yml` 中的 ports 配置

### Q: 如何调整内存？
A: 修改 `docker-compose.yml` 中的 JAVA_OPTS

### Q: 如何查看详细日志？
A: 使用 `./deploy.sh logs` 或 `./deploy.sh app-logs`

### Q: 如何备份数据？
A: 使用 `./deploy.sh backup`

### Q: 如何重置环境？
A: 使用 `./deploy.sh cleanup` 然后重新部署

## 联系支持

如遇到问题：
1. 查看 `README.md` 详细说明
2. 检查日志文件
3. 使用健康检查功能
4. 参考故障排查指南

---

**🎉 部署文件创建完成！现在可以开始使用Docker部署您的应用了。**
