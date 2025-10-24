# GameVault Cloud - Quick Start Guide

快速部署指南 - 30分钟内完成部署！

## 快速概览

```
┌─────────────────────────────────────────────────────┐
│                  GitHub Actions                      │
│  (自动构建、测试、打包、部署)                          │
└─────────────────┬───────────────────────────────────┘
                  │
                  ▼
┌─────────────────────────────────────────────────────┐
│              Docker Hub Registry                     │
│   (存储所有微服务的Docker镜像)                        │
└─────────────────┬───────────────────────────────────┘
                  │
                  ▼
┌─────────────────────────────────────────────────────┐
│         AWS EC2 + K3s Kubernetes                    │
│  ┌──────────┬──────────┬──────────┬──────────┐    │
│  │ Gateway  │   Auth   │ Shopping │  Forum   │    │
│  ├──────────┼──────────┼──────────┼──────────┤    │
│  │Developer │  Social  │PostgreSQL│  Redis   │    │
│  └──────────┴──────────┴──────────┴──────────┘    │
└─────────────────────────────────────────────────────┘
```

## 前置条件检查清单

- [ ] AWS账号
- [ ] GitHub账号
- [ ] Docker Hub账号
- [ ] SSH密钥对
- [ ] 本地安装了 git 和 ssh

## 第一步：创建EC2实例 (5分钟)

### 1.1 登录AWS控制台

访问 https://console.aws.amazon.com/ec2/

### 1.2 启动实例

**基本配置:**
- AMI: Ubuntu Server 22.04 LTS
- 实例类型: **t3.xlarge** (4核16GB)
- 存储: **50GB gp3**

**网络配置:**
创建或选择安全组，允许以下端口:
```
SSH         22      0.0.0.0/0
HTTP        80      0.0.0.0/0
HTTPS       443     0.0.0.0/0
Custom TCP  6443    0.0.0.0/0  (Kubernetes API)
```

**密钥对:**
- 创建新密钥对或使用现有密钥
- 下载 .pem 文件并保存

### 1.3 连接到EC2

```bash
# 修改密钥权限
chmod 400 your-key.pem

# 连接到EC2
ssh -i your-key.pem ubuntu@<EC2_PUBLIC_IP>
```

## 第二步：安装K3s (5分钟)

```bash
# 克隆仓库
git clone https://github.com/HZHENJ/gamevault-cloud.git
cd gamevault-cloud

# 安装K3s
sudo ./scripts/install-k3s.sh

# 验证安装
kubectl get nodes
```

预期输出:
```
NAME       STATUS   ROLES                  AGE   VERSION
ip-xxx     Ready    control-plane,master   1m    v1.28.x+k3s1
```

## 第三步：配置GitHub Secrets (5分钟)

### 3.1 获取Docker Hub Token

1. 访问 https://hub.docker.com/settings/security
2. 点击 "New Access Token"
3. 名称: `GitHub Actions`
4. 权限: Read, Write, Delete
5. 复制生成的token

### 3.2 获取Kubeconfig

```bash
# 在EC2上执行
#sudo cat /etc/rancher/k3s/k3s.yaml | sed "s/127.0.0.1/<YOUR_EC2_PUBLIC_IP>/g" | base64 -w 0
sudo cat /etc/rancher/k3s/k3s.yaml | sed "s/127.0.0.1/52.77.169.8/g" | base64 -w 0
```

复制输出的base64字符串

### 3.3 添加GitHub Secrets

访问: `https://github.com/<your-username>/gamevault-cloud/settings/secrets/actions`

添加以下secrets:

| 名称 | 值 |
|------|-----|
| `DOCKERHUB_USERNAME` | 你的Docker Hub用户名 |
| `DOCKERHUB_TOKEN` | 步骤3.1的token |
| `KUBE_CONFIG_PRODUCTION` | 步骤3.2的base64字符串 |
| `PRODUCTION_SERVER_IP` | EC2公网IP |

## 第四步：触发部署 (10分钟)

### 4.1 Fork仓库 (如果还没有)

点击右上角 "Fork" 按钮

### 4.2 更新配置并推送

```bash
# 在本地克隆你fork的仓库
git clone https://github.com/<your-username>/gamevault-cloud.git
cd gamevault-cloud

# 创建新分支
git checkout -b setup/k8s-deployment

# 提交并推送
git add .
git commit -m "feat: setup k8s deployment"
git push origin setup/k8s-deployment
```

### 4.3 创建Pull Request

1. 访问你的GitHub仓库
2. 点击 "Pull requests" → "New pull request"
3. Base: `main` ← Compare: `setup/k8s-deployment`
4. 点击 "Create pull request"
5. 点击 "Merge pull request"

### 4.4 监控部署

访问: `https://github.com/<your-username>/gamevault-cloud/actions`

你会看到两个工作流:
- ✅ **CI - Build and Test**: 构建和测试
- ✅ **CD - Build and Deploy**: 构建镜像并部署

等待两个工作流都显示绿色✅

## 第五步：验证部署 (5分钟)

### 5.1 检查Pod状态

```bash
# 在EC2上执行
kubectl get pods -n gamevault
```

所有Pod应该显示 `Running`:
```
NAME                                   READY   STATUS    RESTARTS   AGE
gamevault-gateway-xxx-xxx              1/1     Running   0          2m
gamevault-auth-xxx-xxx                 1/1     Running   0          2m
gamevault-shopping-xxx-xxx             1/1     Running   0          2m
gamevault-forum-xxx-xxx                1/1     Running   0          2m
gamevault-developer-xxx-xxx            1/1     Running   0          2m
gamevault-social-xxx-xxx               1/1     Running   0          2m
postgres-xxx-xxx                       1/1     Running   0          3m
redis-xxx-xxx                          1/1     Running   0          3m
nacos-xxx-xxx                          1/1     Running   0          3m
minio-xxx-xxx                          1/1     Running   0          3m
```

### 5.2 获取访问地址

```bash
kubectl get svc gamevault-gateway -n gamevault
```

### 5.3 测试API

```bash
# 健康检查
curl http://<EC2_PUBLIC_IP>/actuator/health

# 预期输出
{"status":"UP"}
```

### 5.4 访问服务

在浏览器中访问:
- **API Gateway**: `http://<EC2_PUBLIC_IP>`
- **Nacos控制台**: `http://<EC2_PUBLIC_IP>:8848/nacos` (用户名/密码: nacos/nacos)

## 常见问题

### Q1: Pod一直处于Pending状态

**原因**: 资源不足

**解决**:
```bash
# 查看节点资源
kubectl describe nodes

# 减少副本数
kubectl scale deployment gamevault-gateway -n gamevault --replicas=1
```

### Q2: ImagePullBackOff错误

**原因**: 无法拉取Docker镜像

**解决**:
```bash
# 检查镜像名称是否正确
kubectl describe pod <pod-name> -n gamevault

# 验证Docker Hub上的镜像
# 访问 https://hub.docker.com/r/<your-username>/gamevault-gateway
```

### Q3: CrashLoopBackOff错误

**原因**: 应用启动失败

**解决**:
```bash
# 查看日志
kubectl logs <pod-name> -n gamevault

# 常见原因:
# 1. 数据库连接失败 - 检查PostgreSQL是否运行
# 2. Nacos连接失败 - 检查Nacos是否运行
# 3. 配置错误 - 检查ConfigMap和Secret
```

### Q4: 无法通过LoadBalancer IP访问

**原因**: K3s默认不包含LoadBalancer实现

**解决**:
```bash
# 方案1: 使用NodePort (推荐)
kubectl patch svc gamevault-gateway -n gamevault -p '{"spec":{"type":"NodePort"}}'
kubectl get svc gamevault-gateway -n gamevault
# 访问 http://<EC2_IP>:<NodePort>

# 方案2: 使用端口转发
kubectl port-forward -n gamevault svc/gamevault-gateway 8080:80
# 访问 http://localhost:8080
```

## 下一步

### 🔒 生产环境配置

1. **更新密码**
```bash
kubectl create secret generic postgres-secret \
  --from-literal=POSTGRES_PASSWORD='YOUR_SECURE_PASSWORD' \
  -n gamevault --dry-run=client -o yaml | kubectl apply -f -
```

2. **配置域名和HTTPS**
```bash
# 安装cert-manager
kubectl apply -f https://github.com/cert-manager/cert-manager/releases/download/v1.13.0/cert-manager.yaml

# 配置Ingress
# 参见 k8s/ingress/ 目录
```

3. **设置监控**
```bash
# 安装Prometheus和Grafana
helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
helm install prometheus prometheus-community/kube-prometheus-stack -n monitoring --create-namespace
```

### 📊 监控和日志

```bash
# 查看实时日志
kubectl logs -f deployment/gamevault-gateway -n gamevault

# 查看所有Pod状态
watch kubectl get pods -n gamevault

# 查看资源使用
kubectl top pods -n gamevault
kubectl top nodes
```

### 🚀 扩展和优化

```bash
# 水平扩展
kubectl scale deployment gamevault-gateway -n gamevault --replicas=3

# 配置自动扩缩容
kubectl autoscale deployment gamevault-gateway -n gamevault --min=2 --max=10 --cpu-percent=80
```

## 支持

遇到问题？
- 📖 查看完整文档: [DEPLOYMENT.md](DEPLOYMENT.md)
- 🐛 提交Issue: https://github.com/HZHENJ/gamevault-cloud/issues
- 💬 讨论区: https://github.com/HZHENJ/gamevault-cloud/discussions

## 成本估算

### 单EC2实例
- **按需实例**: ~$150/月
- **Spot实例**: ~$50/月 (节省67%)
- **Reserved (1年)**: ~$90/月 (节省40%)

### 成本优化技巧
1. 使用Spot实例
2. 非工作时间停止实例
3. 使用EBS快照替代持续存储
4. 配置自动关机脚本

---

**祝贺！** 🎉 你已经成功部署了GameVault Cloud微服务架构！
