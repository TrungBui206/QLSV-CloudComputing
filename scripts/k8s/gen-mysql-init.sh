#!/usr/bin/env bash
# Đóng gói các file db/init/*.sql thành ConfigMap k8s/03-mysql-init-configmap.yaml.
# MySQL nạp các file này khi thư mục dữ liệu còn trống (lần chạy đầu tiên).
# Chạy lại script mỗi khi sửa db/init:  ./scripts/k8s/gen-mysql-init.sh
set -euo pipefail
cd "$(dirname "$0")/../.."
OUT=k8s/03-mysql-init-configmap.yaml
{
  echo "# TẠO TỰ ĐỘNG từ db/init bằng scripts/k8s/gen-mysql-init.sh. Không sửa tay."
  echo "apiVersion: v1"
  echo "kind: ConfigMap"
  echo "metadata:"
  echo "  name: mysql-init"
  echo "  namespace: qlsv"
  echo "data:"
  for f in db/init/*.sql; do
    echo "  $(basename "$f"): |"
    tr -d '\r' < "$f" | sed -e 's/[[:space:]]*$//' -e 's/^/    /' -e 's/^    $//'
  done
} > "$OUT"
echo "Đã tạo $OUT"
