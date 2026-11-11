# Kiểm thử sao lưu và khôi phục – QLSV Cloud

## Môi trường

- AWS EC2 `c7i-flex.large`, Ubuntu 24.04, region Sydney (`ap-southeast-2`); Docker Compose (app + MySQL 8.4 + MinIO)
- Database `educaze_hibernate`, dữ liệu demo ban đầu 18 sinh viên
- Thời gian: 12/11/2026 (khoảng 03:50–04:05 giờ Việt Nam)

## Quy trình và kết quả

| Bước | Thao tác | Kết quả |
|---|---|---|
| 1 | Thêm sinh viên thử nghiệm qua API (`POST /api/students`), rồi sửa tên | Tạo ID 19, sửa tên thành "Sinh Vien Kiem Thu Cloud Updated"; SQL: 19 sinh viên |
| 2 | `bash scripts/backup.sh` | Tạo `backups/qlsv_20261111_205851.sql` (12 011 byte, giờ UTC) |
| 3 | Xóa sinh viên ID 19 qua API (`DELETE /api/students/19`) | HTTP 204; SQL: 18 sinh viên, không còn ID 19 |
| 4 | `echo YES \| bash scripts/restore.sh --main backups/qlsv_20261111_205851.sql` | Script dừng app, nạp lại database chính, bật lại app |
| 5 | Kiểm tra lại | SQL: 19 sinh viên, ID 19 đã trở lại; `/health` = UP |
| 6 | `bash scripts/restore.sh backups/qlsv_20261111_205851.sql` (chế độ thử nghiệm) | Database `qlsv_restore_test`: 6 bảng, 19 sinh viên; database chính không bị đụng |
| 7 | Dọn dẹp: xóa lại sinh viên ID 19 | Database trở về 18 sinh viên như dữ liệu demo |

**Kết luận:** sao lưu → xóa bản ghi → khôi phục → bản ghi trở lại: **Đạt** trên AWS.

## Giới hạn

- File sao lưu lưu trên chính VM; chưa chép ra nơi lưu trữ khác.
- Không gồm nội dung file trong MinIO.
