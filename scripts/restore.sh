#!/usr/bin/env bash
# Khôi phục database MySQL từ một file do scripts/backup.sh tạo ra.
#
#   bash scripts/restore.sh backups/<file>.sql          # vào database thử nghiệm qlsv_restore_test (an toàn)
#   bash scripts/restore.sh --main backups/<file>.sql   # vào database CHÍNH của ứng dụng (ghi đè dữ liệu hiện tại)
#
# Chế độ --main: hỏi xác nhận, tạm dừng container app, nạp lại toàn bộ bảng, rồi bật lại app.
set -Eeuo pipefail

cd "$(dirname "$0")/.."

MODE="test"
if [ "${1:-}" = "--main" ]; then
    MODE="main"
    shift
fi

if [ "$#" -ne 1 ] || [ ! -f "$1" ]; then
    echo "Cach dung: bash scripts/restore.sh [--main] backups/ten-file.sql" >&2
    exit 1
fi

BACKUP_FILE="$1"
TEMP_FILE="/tmp/qlsv-restore-$$.sql"

# Không cho file SQL tự chuyển sang database khác
if grep -Eiq '^[[:space:]]*(USE[[:space:]]+|CREATE[[:space:]]+DATABASE|DROP[[:space:]]+DATABASE)' "$BACKUP_FILE"; then
    echo "File SQL co lenh chuyen/tao/xoa database. Huy restore." >&2
    exit 1
fi

CONTAINER_ID=$(docker compose ps -q mysql)
if [ -z "$CONTAINER_ID" ]; then
    echo "Khong tim thay container MySQL (chay docker compose up -d truoc)." >&2
    exit 1
fi

APP_STOPPED=0
cleanup() {
    docker compose exec -T mysql rm -f "$TEMP_FILE" >/dev/null 2>&1 || true
    if [ "$APP_STOPPED" = "1" ]; then
        echo "Bat lai container app..."
        docker compose start app >/dev/null || true
    fi
}
trap cleanup EXIT

if [ "$MODE" = "main" ]; then
    echo "CANH BAO: du lieu hien tai cua database chinh se bi GHI DE bang $BACKUP_FILE."
    read -r -p "Go YES de tiep tuc: " answer
    if [ "$answer" != "YES" ]; then
        echo "Da huy."
        exit 1
    fi
    echo "Tam dung container app..."
    docker compose stop app >/dev/null
    APP_STOPPED=1
fi

echo "Dang sao chep file backup vao container MySQL..."
docker cp "$BACKUP_FILE" "${CONTAINER_ID}:${TEMP_FILE}"

docker compose exec -T mysql sh -c '
    set -e
    export MYSQL_PWD="$MYSQL_ROOT_PASSWORD"
    if [ "$2" = "main" ]; then
        TARGET="$MYSQL_DATABASE"
    else
        TARGET="qlsv_restore_test"
        mysql -u root -e "CREATE DATABASE IF NOT EXISTS qlsv_restore_test
            CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
    fi
    echo "Dang khoi phuc vao database: $TARGET"
    mysql -u root --default-character-set=utf8mb4 "$TARGET" < "$1"
' sh "$TEMP_FILE" "$MODE"

echo "Restore thanh cong (che do: $MODE)."
