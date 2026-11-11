#!/usr/bin/env bash
# Cài Docker Engine + Docker Compose plugin + git trên VM Ubuntu (22.04 / 24.04 LTS).
# Chạy MỘT lần sau khi SSH vào VM:   bash scripts/server-setup.sh
# Nguồn cách cài: Docker Docs - Install Docker Engine on Ubuntu (apt repository).
set -euo pipefail

if [ "$(id -u)" -ne 0 ]; then SUDO=sudo; else SUDO=""; fi

# shellcheck disable=SC1091
. /etc/os-release
if [ "${ID:-}" != "ubuntu" ]; then
  echo "Script chi ho tro Ubuntu (dang chay: ${ID:-khong ro})." >&2
  exit 1
fi

if command -v docker >/dev/null 2>&1 && docker compose version >/dev/null 2>&1; then
  echo "Docker va Docker Compose da duoc cai, bo qua buoc cai dat."
else
  echo ">> Cai Docker Engine tu kho apt chinh thuc cua Docker"
  $SUDO apt-get update
  $SUDO apt-get install -y ca-certificates curl git
  $SUDO install -m 0755 -d /etc/apt/keyrings
  $SUDO curl -fsSL https://download.docker.com/linux/ubuntu/gpg -o /etc/apt/keyrings/docker.asc
  $SUDO chmod a+r /etc/apt/keyrings/docker.asc
  echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.asc] https://download.docker.com/linux/ubuntu ${UBUNTU_CODENAME:-$VERSION_CODENAME} stable" \
    | $SUDO tee /etc/apt/sources.list.d/docker.list >/dev/null
  $SUDO apt-get update
  $SUDO apt-get install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin
fi

$SUDO systemctl enable --now docker

TARGET_USER="${SUDO_USER:-$(id -un)}"
if [ "$TARGET_USER" != "root" ] && ! id -nG "$TARGET_USER" | grep -qw docker; then
  $SUDO usermod -aG docker "$TARGET_USER"
  echo ">> Da them $TARGET_USER vao nhom docker. DANG XUAT roi SSH lai de dung lenh docker khong can sudo."
fi

# VM it RAM: tao swap 2 GB de buoc build Maven trong Docker khong bi het bo nho
MEM_KB=$(awk '/MemTotal/ {print $2}' /proc/meminfo)
if [ "$MEM_KB" -lt 4194304 ] && [ "$(swapon --show --noheadings | wc -l)" -eq 0 ]; then
  echo ">> RAM duoi 4 GB va chua co swap: tao /swapfile 2 GB"
  $SUDO fallocate -l 2G /swapfile
  $SUDO chmod 600 /swapfile
  $SUDO mkswap /swapfile >/dev/null
  $SUDO swapon /swapfile
  grep -q '^/swapfile ' /etc/fstab || echo '/swapfile none swap sw 0 0' | $SUDO tee -a /etc/fstab >/dev/null
fi

docker --version || $SUDO docker --version
docker compose version || $SUDO docker compose version
echo ">> Xong. Buoc tiep theo: bash scripts/gen-env.sh"
