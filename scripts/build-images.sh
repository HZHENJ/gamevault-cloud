#!/bin/bash

# ==========================================
# Build Docker Images Script
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
echo "Building Docker Images"
echo "=========================================="
echo "Registry: $DOCKER_REGISTRY"
echo "Username: $DOCKER_USERNAME"
echo "Tag: $IMAGE_TAG"
echo ""

# Build parent project first
echo "Building parent project..."
mvn clean package -DskipTests -B

# Build and push each service
for service in "${SERVICES[@]}"; do
    echo ""
    echo "=========================================="
    echo "Building: $service"
    echo "=========================================="

    IMAGE_NAME="$DOCKER_REGISTRY/$DOCKER_USERNAME/$service:$IMAGE_TAG"
    LATEST_TAG="$DOCKER_REGISTRY/$DOCKER_USERNAME/$service:latest"

    docker build \
        -f $service/Dockerfile \
        -t $IMAGE_NAME \
        -t $LATEST_TAG \
        .

    echo "✅ Built: $IMAGE_NAME"
done

echo ""
echo "=========================================="
echo "Build Complete!"
echo "=========================================="
echo ""
echo "To push images to Docker Hub:"
echo "  docker login"
for service in "${SERVICES[@]}"; do
    echo "  docker push $DOCKER_REGISTRY/$DOCKER_USERNAME/$service:$IMAGE_TAG"
    echo "  docker push $DOCKER_REGISTRY/$DOCKER_USERNAME/$service:latest"
done

echo ""
echo "Or run: ./scripts/push-images.sh"
