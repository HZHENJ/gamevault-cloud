#!/bin/bash

# ==========================================
# Push Docker Images Script
# GameVault Cloud Microservices
# ==========================================

set -e

DOCKER_REGISTRY=${DOCKER_REGISTRY:-"docker.io"}
DOCKER_USERNAME=${DOCKER_USERNAME:-"your-dockerhub-username"}
IMAGE_TAG=${IMAGE_TAG:-"latest"}

SERVICES=(
    "gamevault-gateway"
    "gamevault-auth"
    "gamevault-shopping"
    "gamevault-forum"
    "gamevault-developer"
    "gamevault-social"
)

echo "=========================================="
echo "Pushing Docker Images"
echo "=========================================="
echo "Registry: $DOCKER_REGISTRY"
echo "Username: $DOCKER_USERNAME"
echo "Tag: $IMAGE_TAG"
echo ""

# Login to Docker Hub
echo "Logging in to Docker Hub..."
docker login $DOCKER_REGISTRY

# Push each service
for service in "${SERVICES[@]}"; do
    echo ""
    echo "Pushing: $service"

    docker push "$DOCKER_REGISTRY/$DOCKER_USERNAME/$service:$IMAGE_TAG"
    docker push "$DOCKER_REGISTRY/$DOCKER_USERNAME/$service:latest"

    echo "✅ Pushed: $service"
done

echo ""
echo "=========================================="
echo "Push Complete!"
echo "=========================================="
