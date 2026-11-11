#!/usr/bin/env bash
# Sao lưu database MySQL của ứng dụng ra file .sql trong thư mục backups/.
# Dùng:  bash scripts/backup.sh
# File backup chứa dữ liệu tài khoản (hash mật khẩu): KHÔNG commit, backups/ đã nằm trong .gitignore.
set -Eeuo pipefail

cd "$(dirname "$0")/.."

mkdir -p backups

TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
BACKUP_FILE="backups/qlsv_${TIMESTAMP}.sql"
TEMP_FILE="${BACKUP_FILE}.tmp"

# Xóa file tạm nếu backup lỗi giữa chừng
trap 'rm -f "$TEMP_FILE"' EXIT

echo "Dang sao luu MySQL..."

# mysqldump chạy trong container mysql, dùng tài khoản ứng dụng (không dùng root)
docker compose exec -T mysql sh -c '
    export MYSQL_PWD="$MYSQL_PASSWORD"
    exec mysqldump \
        -u "$MYSQL_USER" \
        --single-transaction \
        --quick \
        --no-tablespaces \
        --default-character-set=utf8mb4 \
        "$MYSQL_DATABASE"
' > "$TEMP_FILE"

if [ ! -s "$TEMP_FILE" ]; then
    echo "Backup that bai: file rong." >&2
    exit 1
fi

mv "$TEMP_FILE" "$BACKUP_FILE"
echo "Backup thanh cong: $BACKUP_FILE ($(wc -c < "$BACKUP_FILE") byte)"
