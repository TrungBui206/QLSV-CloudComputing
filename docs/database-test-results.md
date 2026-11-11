# Kết quả kiểm thử database (T3, T4)

## Môi trường

| | Local | Cloud |
|---|---|---|
| Máy | Windows 11, Docker Desktop | AWS EC2 `c7i-flex.large`, Ubuntu 24.04, Sydney |
| Dữ liệu ban đầu | Dữ liệu demo, 18 sinh viên | Dữ liệu demo, 18 sinh viên |
| Cách kiểm tra | Giao diện web + truy vấn SQL | API qua URL công khai + truy vấn SQL trên server |

## T3 – Thêm, sửa, xóa

| Thao tác | Local | Cloud |
|---|---|---|
| Thêm | ID 19, tổng 18 → 19 | `POST /api/students` 201, ID 19, tổng 18 → 19 |
| Sửa | Tên đổi thành "Sinh Vien Kiem Thu T3 Updated" | `PUT /api/students/19` 200, SQL thấy tên mới |
| Xóa | Truy vấn ID 19 rỗng, tổng về 18 | `DELETE /api/students/19` 204, `GET` trả 404, tổng về 18 |
| **Đánh giá** | **Đạt** | **Đạt** |

## T4 – Dữ liệu còn sau khi khởi động lại

| Tình huống | Local | Cloud |
|---|---|---|
| Restart container app | `docker restart qlsv-git-app-1`: ID 19 và tên đã sửa còn nguyên | `docker compose restart app`: `/health` UP, SQL vẫn 19 sinh viên, ID 19 giữ tên đã sửa |
| Restart cả VM | Không áp dụng | `sudo reboot`: các container tự chạy lại (`restart: unless-stopped`), API trả ID 19 với tên đã sửa, tổng 19 |
| **Đánh giá** | **Đạt** | **Đạt** |

Sau kiểm thử, bản ghi ID 19 được xóa để database trở về dữ liệu demo.
