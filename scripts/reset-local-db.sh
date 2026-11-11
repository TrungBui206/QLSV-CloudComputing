#!/usr/bin/env bash
# Tạo lại database LOCAL từ dữ liệu demo trong db/init (macOS / Linux).
# Dùng khi không đăng nhập được bằng tài khoản demo (README mục 5): thường do volume
# mysql-db được tạo từ trước khi có dữ liệu demo, nên tài khoản admin trong đó mang mật khẩu cũ.
# CẢNH BÁO: xóa toàn bộ dữ liệu local của MySQL và MinIO. Không chạy trên server cloud.
set -euo pipefail

cd "$(dirname "$0")/.."

if [ ! -f .env ]; then
  echo "Chua co file .env. Tao theo README muc 4b (cp .env.example .env roi dien gia tri)." >&2
  exit 1
fi

echo "Thao tac nay XOA toan bo du lieu local (MySQL + MinIO) roi nap lai du lieu demo."
read -r -p "Go YES de tiep tuc: " answer
if [ "$answer" != "YES" ]; then
  echo "Da huy."
  exit 1
fi

docker compose down -v
docker compose up -d --build

PORT="$(grep -E '^PUBLIC_PORT=' .env | cut -d= -f2 | tr -d '\r' || true)"
PORT="${PORT:-8082}"

echo ">> Doi app san sang tai http://localhost:${PORT}/health (toi da 3 phut)"
for _ in $(seq 1 36); do
  if curl -fsS "http://localhost:${PORT}/health" >/dev/null 2>&1; then
    echo ">> App da san sang: http://localhost:${PORT}/login.html"
    echo ">> Dang nhap bang tai khoan demo trong README muc 5 (admin / lecturer / student01)."
    exit 0
  fi
  sleep 5
done

echo "App chua san sang sau 3 phut. Xem log: docker compose logs --tail 50 app" >&2
exit 1
