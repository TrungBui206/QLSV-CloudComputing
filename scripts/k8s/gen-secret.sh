#!/usr/bin/env bash
# Tạo k8s/01-secret.yaml với mật khẩu ngẫu nhiên (file này KHÔNG commit).
# Chạy từ thư mục gốc repo:  ./scripts/k8s/gen-secret.sh
set -euo pipefail
cd "$(dirname "$0")/../.."
OUT=k8s/01-secret.yaml

if [ -f "$OUT" ] && [ "${1:-}" != "--force" ]; then
  echo "$OUT đã có, giữ nguyên. Đổi mật khẩu khi MySQL đã có dữ liệu sẽ làm backend không đăng nhập được database."
  echo "Muốn tạo lại: ./scripts/k8s/gen-secret.sh --force"
  exit 0
fi

rand() { openssl rand -hex "$1"; }
umask 077
cat > "$OUT" <<YAML
# Tạo bởi scripts/k8s/gen-secret.sh. KHÔNG commit file này.
apiVersion: v1
kind: Secret
metadata:
  name: qlsv-secret
  namespace: qlsv
type: Opaque
stringData:
  DB_USER: qlsv_app
  DB_PASSWORD: "$(rand 16)"
  MYSQL_ROOT_PASSWORD: "$(rand 16)"
  AUTH_JWT_SECRET: "$(openssl rand -base64 32)"
  MINIO_ACCESS_KEY: "qlsv$(rand 4)"
  MINIO_SECRET_KEY: "$(rand 16)"
YAML
echo "Đã tạo $OUT"
