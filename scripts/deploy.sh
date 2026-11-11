#!/usr/bin/env bash
# Cập nhật mã nguồn và (re)deploy ứng dụng trên server bằng docker compose.
# Dùng:  bash scripts/deploy.sh [nhánh]      (mặc định: cloud)
# Script KHÔNG xóa volume, nên dữ liệu trong database được giữ nguyên.
set -euo pipefail

cd "$(dirname "$0")/.."

BRANCH="${1:-cloud}"

if [ ! -f .env ]; then
  echo "Chua co .env. Chay truoc: bash scripts/gen-env.sh" >&2
  exit 1
fi

echo ">> Lay ma nguon moi nhat cua nhanh $BRANCH"
git fetch origin
git checkout "$BRANCH"
git pull --ff-only origin "$BRANCH"

echo ">> Build image va khoi dong container"
docker compose up -d --build

PORT="$(grep -E '^PUBLIC_PORT=' .env | cut -d= -f2 || true)"
PORT="${PORT:-8082}"

echo ">> Doi app san sang tai http://127.0.0.1:${PORT}/health (toi da 3 phut)"
for _ in $(seq 1 36); do
  if curl -fsS "http://127.0.0.1:${PORT}/health" >/dev/null 2>&1; then
    echo ">> /health: $(curl -fsS "http://127.0.0.1:${PORT}/health")"
    docker compose ps
    echo ">> Deploy xong. Kiem tra tu may ngoai: http://<IP-cong-khai>/health"
    exit 0
  fi
  sleep 5
done

echo "App chua san sang sau 3 phut. 50 dong log cuoi cua app:" >&2
docker compose logs --tail 50 app >&2
exit 1
