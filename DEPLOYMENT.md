# GameVault Cloud - Kubernetes Deployment Guide

This guide explains how to deploy GameVault Cloud microservices to Kubernetes on AWS EC2.

## Architecture Overview

### Microservices
- **Gateway Service** (8080): API Gateway and routing
- **Auth Service** (8081): Authentication and authorization
- **Shopping Service** (8082): Game store and purchases
- **Forum Service** (8083): Community forums
- **Developer Service** (8084): Developer portal
- **Social Service** (8089): Social features and chat

### Infrastructure
- **PostgreSQL**: Primary database
- **Redis**: Caching and sessions
- **Nacos**: Service discovery and configuration
- **MinIO**: Object storage for media files

## Deployment Options

### Option 1: Single EC2 Instance (Recommended for Development/Testing)

**EC2 Specifications:**
- Instance Type: `t3.xlarge` (4 vCPU, 16GB RAM)
- Storage: 50GB+ SSD
- OS: Ubuntu 22.04 LTS
- Cost: ~$150/month

**Pros:**
- Lower cost
- Simple management
- Good for development/testing

**Cons:**
- No high availability
- Single point of failure
- Limited scalability

### Option 2: Multi-Node Cluster (Recommended for Production)

**EC2 Specifications:**
- **Master Node**: t3.medium (2 vCPU, 4GB RAM) x1
- **Worker Nodes**: t3.large (2 vCPU, 8GB RAM) x2
- Storage: 50GB+ SSD each
- OS: Ubuntu 22.04 LTS
- Total Cost: ~$180/month

**Pros:**
- High availability
- Horizontal scalability
- Production-ready
- Load balancing

**Cons:**
- Higher cost
- More complex setup

## Prerequisites

1. **AWS EC2 Instance(s)**
   - Ubuntu 22.04 LTS
   - Security group allowing:
     - SSH (22)
     - HTTP (80)
     - HTTPS (443)
     - K8s API (6443)
     - NodePort range (30000-32767)

2. **Docker Hub Account**
   - Create account at https://hub.docker.com
   - Note your username for later

3. **GitHub Repository**
   - Fork or clone this repository
   - Configure GitHub Secrets (see below)

## Step 1: EC2 Setup

### 1.1 Launch EC2 Instance

```bash
# Connect to EC2
ssh -i your-key.pem ubuntu@<EC2_PUBLIC_IP>

# Update system
sudo apt-get update && sudo apt-get upgrade -y
```

### 1.2 Install K3s

```bash
# Clone repository
git clone https://github.com/HZHENJ/gamevault-cloud.git
cd gamevault-cloud

# Install K3s
sudo ./scripts/install-k3s.sh
```

### 1.3 Get Kubeconfig for GitHub Actions

```bash
# Get base64 encoded kubeconfig
sudo cat /etc/rancher/k3s/k3s.yaml | base64 -w 0

# Copy this output - you'll need it for GitHub Secrets
```

**Important:** Before using this kubeconfig remotely:
1. Copy the kubeconfig file
2. Replace `https://127.0.0.1:6443` with `https://<YOUR_EC2_PUBLIC_IP>:6443`
3. Base64 encode it again

## Step 2: GitHub Configuration

### 2.1 Configure GitHub Secrets

Go to your GitHub repository → Settings → Secrets and variables → Actions

Add the following secrets:

| Secret Name | Value | Description |
|-------------|-------|-------------|
| `DOCKERHUB_USERNAME` | your-username | Docker Hub username |
| `DOCKERHUB_TOKEN` | your-token | Docker Hub access token |
| `KUBE_CONFIG_PRODUCTION` | base64-kubeconfig | Kubeconfig from Step 1.3 |
| `KUBE_CONFIG_STAGING` | base64-kubeconfig | (Optional) Staging kubeconfig |
| `PRODUCTION_SERVER_IP` | EC2-public-ip | Production server IP |
| `STAGING_SERVER_IP` | EC2-public-ip | (Optional) Staging server IP |

### 2.2 Get Docker Hub Token

1. Go to https://hub.docker.com/settings/security
2. Click "New Access Token"
3. Name: "GitHub Actions"
4. Access permissions: Read, Write, Delete
5. Copy the token and add to GitHub Secrets

## Step 3: Update Configuration

### 3.1 Update K8s Service Manifests

Edit all files in `k8s/services/*.yaml`:

Replace `${DOCKER_REGISTRY}/${IMAGE_TAG}` pattern with your Docker Hub username.

Example in `k8s/services/gateway.yaml`:
```yaml
image: ${DOCKER_REGISTRY}/gamevault-gateway:${IMAGE_TAG}
# Will be automatically replaced during CI/CD
```

### 3.2 Update Application Configuration

For production deployment, update application.yml files in each service:

```yaml
spring:
  cloud:
    nacos:
      discovery:
        server-addr: nacos:8848  # Use service name in K8s
        namespace: prod
```

## Step 4: Build and Deploy

### Method A: Using GitHub Actions (Recommended)

#### 4.1 Push to Trigger CI/CD

```bash
# Make a commit
git add .
git commit -m "feat: configure k8s deployment"
git push origin main
```

This will:
1. Run CI workflow (build & test)
2. Run CD workflow (build images & deploy to K8s)

#### 4.2 Monitor Deployment

- Go to GitHub Actions tab
- Watch the CI and CD workflows
- Check deployment summary

### Method B: Manual Deployment

#### 4.1 Build Images Locally

```bash
# Set environment variables
export DOCKER_USERNAME=your-dockerhub-username
export IMAGE_TAG=v1.0.0

# Build all images
./scripts/build-images.sh

# Login and push
./scripts/push-images.sh
```

#### 4.2 Deploy to K8s

```bash
# Set kubeconfig (if deploying from local machine)
export KUBECONFIG=~/.kube/config

# Deploy
./scripts/deploy-local.sh
```

## Step 5: Verify Deployment

### 5.1 Check Pod Status

```bash
kubectl get pods -n gamevault
```

Expected output:
```
NAME                                  READY   STATUS    RESTARTS   AGE
postgres-xxx                          1/1     Running   0          5m
redis-xxx                             1/1     Running   0          5m
nacos-xxx                             1/1     Running   0          5m
minio-xxx                             1/1     Running   0          5m
gamevault-gateway-xxx                 1/1     Running   0          3m
gamevault-auth-xxx                    1/1     Running   0          3m
gamevault-shopping-xxx                1/1     Running   0          3m
gamevault-forum-xxx                   1/1     Running   0          3m
gamevault-developer-xxx               1/1     Running   0          3m
gamevault-social-xxx                  1/1     Running   0          3m
```

### 5.2 Check Service Status

```bash
kubectl get svc -n gamevault
```

### 5.3 Get Gateway URL

```bash
# For LoadBalancer (if available)
kubectl get svc gamevault-gateway -n gamevault

# For ClusterIP, use port-forward
kubectl port-forward -n gamevault svc/gamevault-gateway 8080:80
```

### 5.4 Test API

```bash
# Health check
curl http://<GATEWAY_IP>/actuator/health

# Or via port-forward
curl http://localhost:8080/actuator/health
```

## Step 6: Monitoring and Logs

### View Logs

```bash
# Gateway logs
kubectl logs -f deployment/gamevault-gateway -n gamevault

# Auth service logs
kubectl logs -f deployment/gamevault-auth -n gamevault

# All pods
kubectl logs -f -l app=gamevault-gateway -n gamevault --all-containers
```

### Scale Services

```bash
# Scale gateway to 3 replicas
kubectl scale deployment gamevault-gateway -n gamevault --replicas=3

# Auto-scaling is configured via HPA (Horizontal Pod Autoscaler)
kubectl get hpa -n gamevault
```

## Troubleshooting

### Pod Not Starting

```bash
# Describe pod to see events
kubectl describe pod <pod-name> -n gamevault

# Check logs
kubectl logs <pod-name> -n gamevault

# Check resource limits
kubectl top pods -n gamevault
```

### Service Connection Issues

```bash
# Check endpoints
kubectl get endpoints -n gamevault

# Test service connectivity from another pod
kubectl run -it --rm debug --image=busybox --restart=Never -n gamevault -- sh
# Inside pod:
# wget -O- http://gamevault-auth:8081/actuator/health
```

### Database Connection Errors

```bash
# Check PostgreSQL pod
kubectl exec -it <postgres-pod> -n gamevault -- psql -U gamevault_user -d gamevault_auth

# Verify secret
kubectl get secret postgres-secret -n gamevault -o yaml
```

## Updating Deployment

### Rolling Update

```bash
# Update image tag in deployment
kubectl set image deployment/gamevault-gateway \
  gateway=your-username/gamevault-gateway:v1.1.0 \
  -n gamevault

# Check rollout status
kubectl rollout status deployment/gamevault-gateway -n gamevault
```

### Rollback

```bash
# Rollback to previous version
kubectl rollout undo deployment/gamevault-gateway -n gamevault

# Rollback to specific revision
kubectl rollout undo deployment/gamevault-gateway -n gamevault --to-revision=2
```

## Backup and Restore

### Backup PostgreSQL

```bash
# Exec into PostgreSQL pod
kubectl exec -it <postgres-pod> -n gamevault -- bash

# Create backup
pg_dump -U gamevault_user gamevault_auth > backup.sql

# Copy from pod
kubectl cp gamevault/<postgres-pod>:/backup.sql ./backup.sql
```

### Restore PostgreSQL

```bash
# Copy to pod
kubectl cp ./backup.sql gamevault/<postgres-pod>:/backup.sql

# Restore
kubectl exec -it <postgres-pod> -n gamevault -- \
  psql -U gamevault_user -d gamevault_auth -f /backup.sql
```

## Security Best Practices

1. **Update Secrets**: Change default passwords in production
   ```bash
   kubectl create secret generic postgres-secret \
     --from-literal=POSTGRES_PASSWORD='your-secure-password' \
     -n gamevault --dry-run=client -o yaml | kubectl apply -f -
   ```

2. **Enable RBAC**: Use Kubernetes RBAC for access control

3. **Network Policies**: Restrict pod-to-pod communication

4. **Use HTTPS**: Configure TLS/SSL certificates

5. **Regular Updates**: Keep K3s and images updated

## Production Checklist

- [ ] Update all default passwords
- [ ] Configure persistent storage
- [ ] Set up backup strategy
- [ ] Configure monitoring (Prometheus/Grafana)
- [ ] Set up log aggregation
- [ ] Configure alerts
- [ ] Enable HTTPS/TLS
- [ ] Set resource limits
- [ ] Configure network policies
- [ ] Document disaster recovery plan

## Cost Optimization

### Single Instance Option
- Use t3.xlarge spot instance: ~$50/month (67% savings)
- Stop instance during non-business hours
- Use EBS snapshots for backups

### Multi-Instance Option
- Use spot instances for worker nodes
- Use reserved instances for master
- Configure cluster autoscaler

## Support

For issues and questions:
- GitHub Issues: https://github.com/HZHENJ/gamevault-cloud/issues
- Email: your-email@example.com

## License

[Your License]
