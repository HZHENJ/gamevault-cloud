#!/bin/bash

# ==========================================
# Local K8s Deployment Script
# GameVault Cloud Microservices
# ==========================================

set -e

NAMESPACE="gamevault"
DOCKER_REGISTRY=${DOCKER_REGISTRY:-"docker.io"}
DOCKER_USERNAME=${DOCKER_USERNAME:-"your-dockerhub-username"}
IMAGE_TAG=${IMAGE_TAG:-"latest"}

echo "=========================================="
echo "Deploying GameVault Cloud to Kubernetes"
echo "=========================================="
echo "Registry: $DOCKER_REGISTRY"
echo "Username: $DOCKER_USERNAME"
echo "Tag: $IMAGE_TAG"
echo ""

# Check if kubectl is available
if ! command -v kubectl &> /dev/null; then
    echo "kubectl not found. Please install kubectl first."
    exit 1
fi

# Create namespace
echo "Creating namespace..."
kubectl apply -f k8s/namespace.yaml

# Deploy infrastructure
echo "Deploying infrastructure components..."
kubectl apply -f k8s/infrastructure/postgres.yaml
kubectl apply -f k8s/infrastructure/redis.yaml
kubectl apply -f k8s/infrastructure/nacos.yaml
kubectl apply -f k8s/infrastructure/minio.yaml

# Wait for infrastructure to be ready
echo "Waiting for infrastructure to be ready..."
echo "This may take a few minutes..."

kubectl wait --for=condition=ready pod -l app=postgres -n $NAMESPACE --timeout=300s || true
kubectl wait --for=condition=ready pod -l app=redis -n $NAMESPACE --timeout=300s || true
kubectl wait --for=condition=ready pod -l app=nacos -n $NAMESPACE --timeout=300s || true
kubectl wait --for=condition=ready pod -l app=minio -n $NAMESPACE --timeout=300s || true

# Run MinIO init job
echo "Initializing MinIO buckets..."
kubectl delete job minio-init -n $NAMESPACE --ignore-not-found=true
kubectl apply -f k8s/infrastructure/minio.yaml

# Update service manifests with correct image names
echo "Updating service manifests..."
export DOCKER_REGISTRY="$DOCKER_REGISTRY/$DOCKER_USERNAME"
export IMAGE_TAG="$IMAGE_TAG"

for file in k8s/services/*.yaml; do
    envsubst < $file | kubectl apply -f -
done

# Wait for services to be ready
echo "Waiting for services to be ready..."
kubectl wait --for=condition=available deployment --all -n $NAMESPACE --timeout=600s

# Display deployment status
echo ""
echo "=========================================="
echo "Deployment Status"
echo "=========================================="
kubectl get pods -n $NAMESPACE
echo ""
kubectl get svc -n $NAMESPACE
echo ""

# Get Gateway URL
GATEWAY_IP=$(kubectl get svc gamevault-gateway -n $NAMESPACE -o jsonpath='{.status.loadBalancer.ingress[0].ip}' 2>/dev/null || echo "")
if [ -z "$GATEWAY_IP" ]; then
    GATEWAY_IP=$(kubectl get svc gamevault-gateway -n $NAMESPACE -o jsonpath='{.spec.clusterIP}')
    echo "Gateway ClusterIP: $GATEWAY_IP"
    echo "Note: Access via kubectl port-forward:"
    echo "  kubectl port-forward -n $NAMESPACE svc/gamevault-gateway 8080:80"
else
    echo "Gateway LoadBalancer IP: $GATEWAY_IP"
    echo "Access at: http://$GATEWAY_IP"
fi

echo ""
echo "Deployment complete!"
