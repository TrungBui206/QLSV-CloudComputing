# Kiến trúc triển khai (giai đoạn giữa kỳ)

Một máy ảo EC2 (Ubuntu) trên AWS chạy 3 container bằng Docker Compose. Chỉ cổng 80 (HTTP) mở cho Internet; cổng 22 (SSH) chỉ cho dịch vụ EC2 Instance Connect hoặc IP của nhóm.

```mermaid
flowchart LR
    U[Người dùng / trình duyệt] -->|HTTP :80| FW{{Firewall / Security Group<br/>mở 80 cho mọi nơi<br/>22 chỉ EC2 Instance Connect / IP nhóm}}
    Dev[Thành viên nhóm] -->|SSH :22| FW
    FW --> VM

    subgraph VM[EC2 Ubuntu - AWS Sydney ap-southeast-2]
        direction LR
        APP[app<br/>Spring Boot + giao diện tĩnh<br/>cổng 8082 trong container<br/>publish 80]
        DB[(mysql 8.4<br/>volume mysql-db)]
        S3[(minio<br/>volume minio-data)]
        APP -->|JDBC mysql:3306<br/>user qlsv_app| DB
        APP -->|S3 API minio:9000| S3
    end

    GH[GitHub repo<br/>nhánh cloud] -->|git pull bằng deploy key chỉ đọc| VM
```

| Thành phần | Vai trò | Mở ra Internet? |
|---|---|---|
| `app` | REST API, xác thực JWT, phục vụ giao diện HTML/JS, endpoint `/health` | Có, qua cổng 80 |
| `mysql` | Lưu khoa, lớp, sinh viên, niên khóa, tài khoản, metadata tài liệu | Không |
| `minio` | Lưu file tài liệu tải lên | Không (Console chỉ trên 127.0.0.1, xem qua SSH tunnel) |
| `.env` trên VM | Mật khẩu DB, khóa JWT, khóa MinIO | Không, quyền 600, không nằm trong Git |

Luồng một request: trình duyệt → firewall (cổng 80) → container `app` → kiểm tra JWT và quyền → truy vấn `mysql` bằng tài khoản riêng `qlsv_app` (không phải root) → trả JSON. Mỗi request được ghi một dòng log (`docker compose logs app`).

Hướng mở rộng cuối kỳ (chưa làm): tách MySQL sang database được quản lý (ví dụ Amazon RDS) trong mạng riêng; HTTPS bằng reverse proxy; đưa log lên dịch vụ log của nhà cung cấp.
