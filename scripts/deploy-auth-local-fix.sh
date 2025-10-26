#!/bin/bash

# 头像上传修复 - 本地存储方案部署脚本
# 只需添加文件访问接口，无需修改上传逻辑

set -e

# 颜色输出
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

DOCKERHUB_USERNAME="hzhenj"
IMAGE_TAG="latest"

echo -e "${GREEN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${GREEN}🔧 头像访问修复 - 本地存储方案${NC}"
echo -e "${GREEN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo ""

# 检查目录
if [ ! -f "pom.xml" ]; then
    echo -e "${RED}❌ 请在 gamevault-cloud 目录下执行此脚本${NC}"
    exit 1
fi

# 步骤1: 构建
echo -e "${YELLOW}📦 [1/3] 构建 Auth 服务...${NC}"
cd gamevault-auth
mvn clean package -DskipTests

if [ $? -ne 0 ]; then
    echo -e "${RED}❌ Maven 构建失败${NC}"
    exit 1
fi
echo -e "${GREEN}✅ 构建成功${NC}"
cd ..

# 步骤2: Docker镜像
echo ""
echo -e "${YELLOW}🐳 [2/3] 构建 Docker 镜像...${NC}"
docker build -t ${DOCKERHUB_USERNAME}/gamevault-auth:${IMAGE_TAG} \
    -f gamevault-auth/Dockerfile \
    gamevault-auth/

if [ $? -ne 0 ]; then
    echo -e "${RED}❌ Docker 构建失败${NC}"
    exit 1
fi
echo -e "${GREEN}✅ 镜像构建成功${NC}"

# 步骤3: 推送
echo ""
echo -e "${YELLOW}📤 [3/3] 推送到 Docker Hub...${NC}"
docker push ${DOCKERHUB_USERNAME}/gamevault-auth:${IMAGE_TAG}

if [ $? -ne 0 ]; then
    echo -e "${RED}❌ 推送失败${NC}"
    exit 1
fi
echo -e "${GREEN}✅ 推送成功${NC}"

# 完成
echo ""
echo -e "${GREEN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${GREEN}✨ 本地构建完成！${NC}"
echo -e "${GREEN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo ""
echo -e "${YELLOW}📋 接下来在 EC2 上执行：${NC}"
echo ""
echo -e "${GREEN}# 1. SSH 到服务器${NC}"
echo "   ssh ubuntu@<你的EC2-IP>"
echo ""
echo -e "${GREEN}# 2. 更新 Auth 服务${NC}"
echo "   sudo docker stop gamevault_auth_cloud"
echo "   sudo docker rm gamevault_auth_cloud"
echo "   sudo docker pull ${DOCKERHUB_USERNAME}/gamevault-auth:${IMAGE_TAG}"
echo "   cd ~/gamevault-cloud"
echo "   export DOCKERHUB_USERNAME=${DOCKERHUB_USERNAME}"
echo "   sudo -E docker-compose up -d auth"
echo ""
echo -e "${GREEN}# 3. 查看日志${NC}"
echo "   sudo docker logs -f gamevault_auth_cloud"
echo ""
echo -e "${GREEN}# 4. 测试文件访问${NC}"
echo "   curl -I http://localhost:8081/uploads/avatars/test.jpg"
echo ""
echo -e "${GREEN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo ""
echo -e "${YELLOW}💡 修改内容：${NC}"
echo "   ✅ 新增 FileAccessController - 提供文件HTTP访问"
echo "   ✅ 修改 SecurityConfig - 放行 /uploads/** 路径"
echo "   ❌ 无需修改上传逻辑"
echo "   ❌ 无需修改配置文件"
echo ""

