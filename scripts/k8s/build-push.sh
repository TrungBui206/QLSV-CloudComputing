#!/usr/bin/env bash
# Build image frontend và backend với tag v1, v2 rồi đẩy lên Docker Hub.
# Đăng nhập trước bằng: docker login
# Chạy từ thư mục gốc repo:  ./scripts/k8s/build-push.sh <tên-đăng-nhập-docker-hub> [v1 v2 ...]
set -euo pipefail
cd "$(dirname "$0")/../.."
HUB_USER="${1:?Cach dung: build-push.sh <ten-dang-nhap-docker-hub> [v1 v2 ...]}"
shift
VERSIONS=("$@")
[ ${#VERSIONS[@]} -eq 0 ] && VERSIONS=(v1 v2)

for V in "${VERSIONS[@]}"; do
  echo "== Build $V"
  docker build --build-arg APP_VERSION="$V" -t "$HUB_USER/qlsv-backend:$V" .
  docker build --build-arg APP_VERSION="$V" -f k8s/frontend/Dockerfile -t "$HUB_USER/qlsv-frontend:$V" .
done
for V in "${VERSIONS[@]}"; do
  echo "== Push $V"
  docker push "$HUB_USER/qlsv-backend:$V"
  docker push "$HUB_USER/qlsv-frontend:$V"
done
echo "Xong. Image: $HUB_USER/qlsv-backend và $HUB_USER/qlsv-frontend, tag: ${VERSIONS[*]}"
