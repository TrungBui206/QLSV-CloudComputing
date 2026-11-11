#!/usr/bin/env bash
# Tạo file .env cho server từ .env.example với mật khẩu/khóa NGẪU NHIÊN.
# Dùng:  bash scripts/gen-env.sh [PUBLIC_PORT]      (mặc định 80)
# Không ghi đè .env đã có. File .env được đặt quyền 600 (chỉ chủ sở hữu đọc được).
set -euo pipefail

cd "$(dirname "$0")/.."

PUBLIC_PORT="${1:-80}"

if [ -f .env ]; then
  echo ".env da ton tai, khong ghi de. Xoa hoac doi ten file cu neu muon tao lai." >&2
  exit 1
fi
if [ ! -f .env.example ]; then
  echo "Khong tim thay .env.example (hay chay script tu thu muc project)." >&2
  exit 1
fi
command -v openssl >/dev/null 2>&1 || { echo "Can openssl de sinh khoa." >&2; exit 1; }

rand() {
  # chuoi chi gom chu va so, do dai $1
  local out=""
  while [ "${#out}" -lt "$1" ]; do
    out="${out}$(openssl rand -base64 48 | tr -dc 'A-Za-z0-9')"
  done
  printf '%s' "${out:0:$1}"
}

umask 077
cp .env.example .env

set_kv() {
  # thay gia tri cua dong KEY=... ; them moi neu chua co
  if grep -q "^$1=" .env; then
    sed -i "s|^$1=.*|$1=$2|" .env
  else
    printf '%s=%s\n' "$1" "$2" >> .env
  fi
}

set_kv DB_HOST mysql
set_kv DB_PASSWORD "$(rand 24)"
set_kv MYSQL_ROOT_PASSWORD "$(rand 28)"
set_kv AUTH_JWT_SECRET "$(openssl rand -base64 32)"
set_kv MINIO_ENDPOINT http://minio:9000
set_kv MINIO_ACCESS_KEY "qlsv$(rand 8)"
set_kv MINIO_SECRET_KEY "$(rand 24)"
set_kv APP_BOOTSTRAP_ADMIN_PASSWORD "$(rand 16)"
set_kv APP_BOOTSTRAP_LECTURER_PASSWORD "$(rand 16)"
set_kv PUBLIC_PORT "$PUBLIC_PORT"
set_kv JPA_SHOW_SQL false

chmod 600 .env
echo ">> Da tao .env (quyen 600), PUBLIC_PORT=$PUBLIC_PORT."
echo ">> Khong in mat khau ra man hinh. Xem bang: cat .env (chi tren server, khong chup man hinh)."
echo ">> Tai khoan dang nhap web lay tu du lieu demo: README muc 5."
echo ">> Buoc tiep theo: bash scripts/deploy.sh"
