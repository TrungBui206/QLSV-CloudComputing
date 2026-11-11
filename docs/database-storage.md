# Database và lưu trữ – QLSV Cloud

## 1. Công nghệ

| Thành phần | Công nghệ | Ghi chú |
|---|---|---|
| Database | MySQL 8.4 (container `mysql`) | Database `educaze_hibernate`, ứng dụng dùng tài khoản riêng `qlsv_app` (không dùng root) |
| Lưu file | MinIO (container `minio`, API tương thích S3) | Bucket chứa file tài liệu tải lên |
| Điều phối | Docker Compose | Cùng một cấu hình cho máy cá nhân và server AWS |

## 2. Thiết kế cơ sở dữ liệu

Sơ đồ ERD: [`database-erd.png`](database-erd.png) (file MySQL Workbench: [`database-model.mwb`](database-model.mwb)).

| Bảng | Khóa chính | Ràng buộc UNIQUE | Khóa ngoại |
|---|---|---|---|
| `departments` | `department_id` | `department_code` | |
| `students` | `student_id` | `email` | `department_id` → `departments.department_id` |
| `class_info` | `id` | `name` | |
| `academic_years` | `academic_year_id` | `academic_year_code` | |
| `app_users` | `id` | `username`, `email` | |
| `document` | `id` | | |

Quan hệ khóa ngoại duy nhất trong schema: một khoa có nhiều sinh viên (`students.department_id`).
Liên kết giữa tài khoản `STUDENT` và hồ sơ sinh viên được ứng dụng xử lý theo email, không phải khóa ngoại.

## 3. Cơ chế nạp dữ liệu

- `db/init/01-schema.sql`: tạo bảng và ràng buộc.
- `db/init/02-data.sql`: dữ liệu demo hoàn toàn giả (5 khoa, 2 niên khóa, 3 lớp, 18 sinh viên, 3 tài khoản demo).
- `docker-compose.yml` mount `./db/init` vào `/docker-entrypoint-initdb.d` ở chế độ **chỉ đọc**.
- MySQL chỉ chạy các file này khi **thư mục dữ liệu còn trống** (lần khởi tạo đầu tiên của volume `mysql-db`).
  Khi volume đã có dữ liệu, sửa `db/init/` không có tác dụng; muốn nạp lại phải xóa volume (`docker compose down -v`, mất dữ liệu)
  hoặc dùng `scripts/reset-local-db` trên máy cá nhân.

## 4. Cơ chế lưu trữ

| Volume | Đường dẫn trong container | Nội dung |
|---|---|---|
| `mysql-db` | `/var/lib/mysql` | Toàn bộ dữ liệu MySQL |
| `minio-data` | `/data` | File tài liệu trong MinIO |

- Named volume độc lập với vòng đời container: xóa/tạo lại container, restart app, restart VM đều không mất dữ liệu (đã kiểm chứng, xem `database-test-results.md`).
- `docker compose down` giữ volume; `docker compose down -v` **xóa volume** và mất dữ liệu.
- Volume vẫn nằm trên ổ đĩa của chính VM, nên **không thay thế sao lưu**: mất VM hoặc ổ đĩa là mất dữ liệu.

## 5. Vì sao healthcheck MySQL dùng `127.0.0.1`

```yaml
test: ["CMD", "mysqladmin", "ping", "-h", "127.0.0.1", "-u", "root", "-p${MYSQL_ROOT_PASSWORD}"]
```

Trong lần khởi tạo đầu tiên, image MySQL chạy một **server tạm** để nạp các file `db/init/*.sql`.
Server tạm này tắt mạng TCP, chỉ nhận kết nối qua Unix socket. Nếu healthcheck dùng `localhost`,
`mysqladmin` kết nối qua socket nên báo "healthy" ngay khi server tạm chạy, trong khi dữ liệu chưa nạp xong;
app (khai báo `depends_on: condition: service_healthy`) sẽ khởi động quá sớm.
Dùng `127.0.0.1` buộc kết nối qua TCP, nên container chỉ "healthy" khi server MySQL chính thức đã sẵn sàng.

## 6. Sao lưu và khôi phục

| Script | Chức năng |
|---|---|
| `scripts/backup.sh` | `mysqldump` trong container `mysql` (tài khoản `qlsv_app`, `--single-transaction`), lưu `backups/qlsv_<ngày>_<giờ>.sql` |
| `scripts/restore.sh <file>` | Khôi phục vào database thử nghiệm `qlsv_restore_test`, không đụng database chính |
| `scripts/restore.sh --main <file>` | Khôi phục vào database chính: hỏi xác nhận `YES`, tạm dừng app, nạp lại các bảng, bật lại app |

- `backups/` nằm trong `.gitignore`: file sao lưu chứa hash mật khẩu, không commit.
- Kết quả kiểm chứng trên AWS: [`backup-restore-test.md`](backup-restore-test.md).

## 7. Giới hạn

- Bản sao lưu đang nằm trên chính VM; cần chép ra nơi khác (máy cá nhân hoặc S3) mới bảo vệ được khi mất VM.
- Sao lưu MySQL không gồm file trong MinIO (dữ liệu demo hiện không có tài liệu nên chưa ảnh hưởng).
- Chưa có lịch sao lưu tự động (cron).
