# GameVault Cloud - Kubernetes + CI/CD 完整方案

## 📋 项目概览

本方案为 GameVault Cloud 微服务架构提供完整的 Kubernetes 部署和 CI/CD 流程。

### 架构组件

**微服务 (6个):**
- gamevault-gateway (API网关)
- gamevault-auth (认证服务)
- gamevault-shopping (购物服务)
- gamevault-forum (论坛服务)
- gamevault-developer (开发者服务)
- gamevault-social (社交服务)

**基础设施 (4个):**
- PostgreSQL 16 (数据库)
- Redis (缓存)
- Nacos (服务发现)
- MinIO (对象存储)

## 📁 项目结构

```
gamevault-cloud/
├── .github/workflows/          # GitHub Actions工作流
│   ├── ci.yml                 # 持续集成
│   └── cd.yml                 # 持续部署
├── k8s/                       # Kubernetes配置
│   ├── namespace.yaml         # 命名空间
│   ├── infrastructure/        # 基础设施
│   │   ├── postgres.yaml
│   │   ├── redis.yaml
│   │   ├── nacos.yaml
│   │   └── minio.yaml
│   └── services/             # 微服务
│       ├── gateway.yaml
│       ├── auth.yaml
│       ├── shopping.yaml
│       ├── forum.yaml
│       ├── developer.yaml
│       └── social.yaml
├── scripts/                   # 部署脚本
│   ├── install-k3s.sh        # K3s安装
│   ├── deploy-local.sh       # 本地部署
│   ├── build-images.sh       # 构建镜像
│   └── push-images.sh        # 推送镜像
├── gamevault-*/              # 各微服务目录
│   └── Dockerfile            # 服务专属Dockerfile
├── DEPLOYMENT.md             # 完整部署文档
├── QUICKSTART.md             # 快速开始指南
└── docker-compose.yml        # 本地开发环境
```

## 🚀 部署方案

### 方案A: 单EC2实例（推荐用于开发/测试）

**配置:**
- 实例类型: t3.xlarge (4核, 16GB)
- 存储: 50GB SSD
- 成本: ~$150/月 (或 ~$50/月 使用Spot实例)

**适用场景:**
- 开发和测试环境
- 概念验证 (PoC)
- 学习和实验

### 方案B: 多节点K8s集群（推荐用于生产）

**配置:**
- Master: 1x t3.medium (2核, 4GB)
- Worker: 2x t3.large (2核, 8GB)
- 成本: ~$180/月

**适用场景:**
- 生产环境
- 需要高可用
- 需要横向扩展

## 🔄 CI/CD 流程

### 持续集成 (CI)

**触发条件:**
- Push到 `main`, `master`, `dev/master` 分支
- Pull Request到这些分支

**流程:**
1. ✅ 代码检出
2. ✅ 设置JDK 17
3. ✅ Maven缓存
4. ✅ 依赖下载
5. ✅ 编译构建
6. ✅ 单元测试
7. ✅ 代码覆盖率报告
8. ✅ 打包JAR
9. ✅ 上传构建产物

**文件:** `.github/workflows/ci.yml`

### 持续部署 (CD)

**触发条件:**
- Push到 `main`, `master` 分支 → 部署到生产环境
- Push到 `dev/master` 分支 → 部署到Staging环境
- 创建版本标签 (v*) → 创建GitHub Release

**流程:**
1. 🏗️ **构建镜像阶段**
   - 并行构建6个微服务的Docker镜像
   - 推送到Docker Hub
   - Docker Scout安全扫描
   - 生成SBOM和Provenance

2. 🚀 **部署阶段**
   - 配置kubectl连接到K8s集群
   - 部署基础设施组件
   - 等待基础设施就绪
   - 部署微服务
   - 运行健康检查
   - 生成部署报告

3. 📦 **发布阶段** (仅标签触发)
   - 生成变更日志
   - 创建GitHub Release
   - 附加Docker镜像拉取命令

**文件:** `.github/workflows/cd.yml`

## 📦 Docker镜像

### 镜像特点

- ✅ **多阶段构建**: 减小镜像体积
- ✅ **非root用户**: 增强安全性
- ✅ **健康检查**: 支持Kubernetes探针
- ✅ **JVM优化**: 容器内存自适应
- ✅ **Alpine基础**: 最小化镜像大小

### 镜像列表

```bash
docker.io/<username>/gamevault-gateway:latest
docker.io/<username>/gamevault-auth:latest
docker.io/<username>/gamevault-shopping:latest
docker.io/<username>/gamevault-forum:latest
docker.io/<username>/gamevault-developer:latest
docker.io/<username>/gamevault-social:latest
```

## ☸️ Kubernetes配置

### 资源配置

**微服务资源限制:**
```yaml
resources:
  requests:
    memory: "512Mi"
    cpu: "250m"
  limits:
    memory: "1Gi"
    cpu: "500m"
```

**自动扩缩容 (HPA):**
- 最小副本: 2
- 最大副本: 5
- CPU目标: 70%
- 内存目标: 80%

### 高可用特性

- ✅ **多副本**: 每个服务至少2个副本
- ✅ **健康检查**: Liveness和Readiness探针
- ✅ **滚动更新**: 零停机部署
- ✅ **自动恢复**: Pod失败自动重启
- ✅ **负载均衡**: Service自动分发流量

## 🔐 安全配置

### GitHub Secrets

| Secret名称 | 说明 | 获取方式 |
|-----------|------|---------|
| `DOCKERHUB_USERNAME` | Docker Hub用户名 | 你的Docker Hub账号 |
| `DOCKERHUB_TOKEN` | Docker Hub访问令牌 | Docker Hub设置 → Security |
| `KUBE_CONFIG_PRODUCTION` | K8s配置文件(base64) | `cat ~/.kube/config \| base64` |
| `PRODUCTION_SERVER_IP` | 生产服务器IP | EC2公网IP |

### Kubernetes Secrets

生产环境需要更新以下Secret:
- `postgres-secret`: PostgreSQL密码
- `minio-secret`: MinIO访问密钥

```bash
kubectl create secret generic postgres-secret \
  --from-literal=POSTGRES_PASSWORD='your-secure-password' \
  -n gamevault
```

## 📊 监控和日志

### 查看日志

```bash
# 单个服务
kubectl logs -f deployment/gamevault-gateway -n gamevault

# 所有服务
kubectl logs -f -l app=gamevault -n gamevault --all-containers

# 最近100行
kubectl logs --tail=100 deployment/gamevault-auth -n gamevault
```

### 监控资源

```bash
# Pod资源使用
kubectl top pods -n gamevault

# 节点资源使用
kubectl top nodes

# 自动扩缩容状态
kubectl get hpa -n gamevault
```

## 🛠️ 常用命令

### 部署相关

```bash
# 完整部署
./scripts/deploy-local.sh

# 仅部署基础设施
kubectl apply -f k8s/infrastructure/

# 仅部署服务
kubectl apply -f k8s/services/

# 重启服务
kubectl rollout restart deployment gamevault-gateway -n gamevault
```

### 故障排查

```bash
# 查看Pod状态
kubectl get pods -n gamevault

# 查看Pod详情
kubectl describe pod <pod-name> -n gamevault

# 查看事件
kubectl get events -n gamevault --sort-by='.lastTimestamp'

# 进入Pod调试
kubectl exec -it <pod-name> -n gamevault -- sh
```

### 扩缩容

```bash
# 手动扩展
kubectl scale deployment gamevault-gateway -n gamevault --replicas=3

# 自动扩缩容
kubectl autoscale deployment gamevault-gateway -n gamevault \
  --min=2 --max=10 --cpu-percent=80
```

## 📖 文档导航

- **[快速开始](QUICKSTART.md)** - 30分钟快速部署指南
- **[完整部署文档](DEPLOYMENT.md)** - 详细的部署和运维指南
- **[Docker Compose](docker-compose.yml)** - 本地开发环境

## 🎯 快速开始

### 1. 准备环境

```bash
# 启动EC2实例 (Ubuntu 22.04, t3.xlarge)
# 配置安全组: 22, 80, 443, 6443

# 连接到EC2
ssh -i your-key.pem ubuntu@<EC2_IP>
```

### 2. 安装K3s

```bash
git clone https://github.com/HZHENJ/gamevault-cloud.git
cd gamevault-cloud
sudo ./scripts/install-k3s.sh
```

### 3. 配置GitHub Secrets

在GitHub仓库设置中添加:
- DOCKERHUB_USERNAME
- DOCKERHUB_TOKEN
- KUBE_CONFIG_PRODUCTION
- PRODUCTION_SERVER_IP

### 4. 触发部署

```bash
git add .
git commit -m "feat: setup k8s deployment"
git push origin main
```

### 5. 验证部署

```bash
kubectl get pods -n gamevault
kubectl get svc -n gamevault
curl http://<EC2_IP>/actuator/health
```

## 🔧 故障排查

### 常见问题

**Q: Pod一直Pending?**
```bash
kubectl describe pod <pod-name> -n gamevault
# 检查资源是否充足
kubectl top nodes
```

**Q: ImagePullBackOff?**
```bash
# 检查镜像名称
kubectl describe pod <pod-name> -n gamevault
# 验证Docker Hub凭据
kubectl get secret -n gamevault
```

**Q: CrashLoopBackOff?**
```bash
# 查看日志
kubectl logs <pod-name> -n gamevault --previous
# 检查依赖服务状态
kubectl get pods -n gamevault
```

## 💰 成本估算

### AWS EC2成本 (us-east-1)

| 配置 | 按需 | Spot | Reserved (1年) |
|-----|------|------|---------------|
| t3.xlarge (单实例) | $150/月 | $50/月 | $90/月 |
| 3节点集群 | $180/月 | $60/月 | $110/月 |

### 成本优化建议

1. **使用Spot实例**: 节省高达70%
2. **Reserved实例**: 节省40%
3. **非工作时间关机**: 节省50%
4. **按需调整规格**: 根据负载调整

## 📈 性能优化

### 应用层优化

- ✅ 启用连接池
- ✅ 配置Redis缓存
- ✅ 优化数据库查询
- ✅ 启用GZIP压缩

### 基础设施优化

- ✅ 使用SSD存储
- ✅ 配置HPA自动扩缩容
- ✅ 启用资源限制
- ✅ 配置健康检查

## 🤝 贡献

欢迎提交Issue和Pull Request!

## 📄 许可证

[Your License]

---

**部署愉快！** 🚀

如有问题，请查看 [QUICKSTART.md](QUICKSTART.md) 或 [DEPLOYMENT.md](DEPLOYMENT.md)
