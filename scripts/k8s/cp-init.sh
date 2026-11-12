#!/usr/bin/env bash
# Chạy trên máy cp (sau node-setup.sh), bằng root:  sudo ./cp-init.sh
# Việc làm: kubeadm init, cấp kubeconfig cho user ubuntu, cài plugin mạng Flannel, in lệnh join.
set -euo pipefail

POD_CIDR="10.244.0.0/16"                      # dải IP của Pod, khớp cấu hình mặc định của Flannel
IP="$(hostname -I | awk '{print $1}')"        # IP nội bộ của cp

kubeadm init \
  --apiserver-advertise-address "$IP" \
  --pod-network-cidr "$POD_CIDR" \
  --node-name cp

# Cho user ubuntu dùng kubectl
mkdir -p /home/ubuntu/.kube
cp /etc/kubernetes/admin.conf /home/ubuntu/.kube/config
chown -R ubuntu:ubuntu /home/ubuntu/.kube

# Plugin mạng (CNI): chưa có thì các node ở trạng thái NotReady
export KUBECONFIG=/etc/kubernetes/admin.conf
kubectl apply -f https://github.com/flannel-io/flannel/releases/latest/download/kube-flannel.yml

echo
echo "== Chạy lệnh sau (bằng sudo) trên worker1 và worker2:"
kubeadm token create --print-join-command
