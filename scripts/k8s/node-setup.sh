#!/usr/bin/env bash
# Chuẩn bị MỘT máy Ubuntu 24.04 làm node Kubernetes (chạy trên cả 3 máy, bằng root):
#   sudo ./node-setup.sh cp        # hoặc worker1, worker2
# Việc làm: đặt hostname, tắt swap, bật module mạng, cài containerd, kubeadm, kubelet, kubectl.
set -euo pipefail

NAME="${1:?Cach dung: node-setup.sh <cp|worker1|worker2>}"
K8S_MINOR="${K8S_MINOR:-v1.35}"
export DEBIAN_FRONTEND=noninteractive

echo "== [1/5] Hostname: $NAME"
hostnamectl set-hostname "$NAME"
# Trên AWS, cloud-init đặt lại hostname mỗi lần bật máy nếu không khóa lại
echo "preserve_hostname: true" > /etc/cloud/cloud.cfg.d/99-preserve-hostname.cfg
grep -qE "\s$NAME$" /etc/hosts || echo "127.0.1.1 $NAME" >> /etc/hosts

echo "== [2/5] Tắt swap, bật module và sysctl cho mạng container"
swapoff -a
sed -i '/\sswap\s/s/^/#/' /etc/fstab
printf 'overlay\nbr_netfilter\n' > /etc/modules-load.d/k8s.conf
modprobe overlay
modprobe br_netfilter
cat > /etc/sysctl.d/k8s.conf <<SYSCTL
net.bridge.bridge-nf-call-iptables  = 1
net.bridge.bridge-nf-call-ip6tables = 1
net.ipv4.ip_forward                 = 1
SYSCTL
sysctl --system > /dev/null

echo "== [3/5] Cài containerd (container runtime)"
apt-get update -qq
apt-get install -y -qq containerd apt-transport-https ca-certificates curl gpg
mkdir -p /etc/containerd
containerd config default > /etc/containerd/config.toml
# kubelet dùng cgroup driver systemd nên containerd phải dùng cùng loại
sed -i 's/SystemdCgroup = false/SystemdCgroup = true/' /etc/containerd/config.toml
systemctl restart containerd
systemctl enable containerd > /dev/null

echo "== [4/5] Cài kubeadm, kubelet, kubectl ($K8S_MINOR)"
mkdir -p /etc/apt/keyrings
curl -fsSL "https://pkgs.k8s.io/core:/stable:/$K8S_MINOR/deb/Release.key" \
  | gpg --dearmor --yes -o /etc/apt/keyrings/kubernetes-apt-keyring.gpg
echo "deb [signed-by=/etc/apt/keyrings/kubernetes-apt-keyring.gpg] https://pkgs.k8s.io/core:/stable:/$K8S_MINOR/deb/ /" \
  > /etc/apt/sources.list.d/kubernetes.list
apt-get update -qq
apt-get install -y -qq kubelet kubeadm kubectl
apt-mark hold kubelet kubeadm kubectl > /dev/null
systemctl enable kubelet > /dev/null

echo "== [5/5] Xong: $(kubeadm version -o short), containerd $(containerd --version | awk '{print $3}')"
touch /var/lib/k8s-node-setup.done
