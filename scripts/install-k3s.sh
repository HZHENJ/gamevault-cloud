#!/bin/bash

# ==========================================
# K3s Installation Script for EC2
# GameVault Cloud Deployment
# ==========================================

set -e

echo "=========================================="
echo "Installing K3s on EC2"
echo "=========================================="

# Check if running as root
if [ "$EUID" -ne 0 ]; then
  echo "Please run as root (use sudo)"
  exit 1
fi

# Update system
echo "Updating system packages..."
apt-get update
apt-get upgrade -y

# Install required packages
echo "Installing required packages..."
apt-get install -y curl wget git apt-transport-https ca-certificates software-properties-common

# Install K3s
echo "Installing K3s..."
curl -sfL https://get.k3s.io | sh -s - \
  --write-kubeconfig-mode 644 \
  --disable traefik \
  --node-taint CriticalAddonsOnly=true:NoExecute

# Wait for K3s to be ready
echo "Waiting for K3s to be ready..."
sleep 10

# Verify installation
echo "Verifying K3s installation..."
kubectl get nodes

# Configure kubectl for non-root user
if [ -n "$SUDO_USER" ]; then
  echo "Configuring kubectl for user $SUDO_USER..."
  mkdir -p /home/$SUDO_USER/.kube
  cp /etc/rancher/k3s/k3s.yaml /home/$SUDO_USER/.kube/config
  chown -R $SUDO_USER:$SUDO_USER /home/$SUDO_USER/.kube
  chmod 600 /home/$SUDO_USER/.kube/config
fi

# Install Helm
echo "Installing Helm..."
curl https://raw.githubusercontent.com/helm/helm/main/scripts/get-helm-3 | bash

# Create kubeconfig for remote access
echo ""
echo "=========================================="
echo "K3s Installation Complete!"
echo "=========================================="
echo ""
echo "To access from remote machine:"
echo "1. Copy /etc/rancher/k3s/k3s.yaml to your local machine"
echo "2. Replace 'server: https://127.0.0.1:6443' with your EC2 public IP"
echo "3. Save as ~/.kube/config"
echo ""
echo "To get kubeconfig (base64 encoded for GitHub Secrets):"
echo "cat /etc/rancher/k3s/k3s.yaml | base64 -w 0"
echo ""

# Display cluster info
kubectl cluster-info
