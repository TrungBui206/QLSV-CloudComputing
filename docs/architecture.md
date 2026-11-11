# Kiến trúc triển khai (giai đoạn giữa kỳ)

Một máy ảo EC2 (Ubuntu 24.04) trên AWS region Sydney chạy 3 container bằng Docker Compose.
Chỉ cổng 80 (HTTP) mở cho Internet; cổng 22 (SSH) chỉ cho dịch vụ EC2 Instance Connect và IP của nhóm.
Log của app được gửi lên Amazon CloudWatch Logs; CPU của VM xem trên CloudWatch Metrics.

![Sơ đồ kiến trúc](architecture.png)

```mermaid
flowchart LR
    U[Người dùng / trình duyệt] -->|HTTP :80| SG{{Security Group<br/>80: mọi nơi<br/>22: EC2 Instance Connect, IP nhóm}}
    Dev[Thành viên nhóm] -->|SSH :22 qua EC2 Instance Connect| SG
    SG --> APP

    subgraph AWS[AWS ap-southeast-2 Sydney]
        subgraph VM[EC2 c7i-flex.large - Ubuntu 24.04 - EBS gp3 30 GB]
            direction LR
            APP[app<br/>Spring Boot + giao diện tĩnh<br/>cổng 8082 trong container<br/>publish 80]
            DB[(mysql 8.4<br/>volume mysql-db)]
            S3[(minio<br/>volume minio-data)]
            APP -->|JDBC mysql:3306<br/>user qlsv_app| DB
            APP -->|S3 API minio:9000| S3
        end
        CW[CloudWatch Logs<br/>/qlsv/app]
        CWM[CloudWatch Metrics<br/>CPUUtilization]
        APP -.->|log driver awslogs<br/>IAM role qlsv-ec2-logs| CW
        VM -.-> CWM
    end

    GH[GitHub repo private<br/>nhánh cloud] -->|git pull bằng deploy key chỉ đọc| VM
```

| Thành phần | Vai trò | Mở ra Internet? |
|---|---|---|
| `app` | REST API, xác thực JWT, phục vụ giao diện HTML/JS, endpoint `/health` | Có, qua cổng 80 |
| `mysql` | Lưu khoa, lớp, sinh viên, niên khóa, tài khoản, metadata tài liệu | Không (3306 không publish) |
| `minio` | Lưu file tài liệu tải lên | Không (Console chỉ trên 127.0.0.1, xem qua SSH tunnel) |
| `.env` trên VM | Mật khẩu DB, khóa JWT, khóa MinIO (sinh ngẫu nhiên bằng `scripts/gen-env.sh`) | Không, quyền 600, không nằm trong Git |
| CloudWatch Logs `/qlsv/app` | Log ứng dụng, mỗi request một dòng, giữ 7 ngày | Chỉ xem trong AWS console |
| IAM role `qlsv-ec2-logs` | Cho EC2 ghi log lên CloudWatch, không cần access key trên VM | — |

Luồng một request: trình duyệt → Security Group (cổng 80) → container `app` → kiểm tra JWT và quyền → truy vấn `mysql`
bằng tài khoản riêng `qlsv_app` (không phải root) → trả JSON; dòng log của request được gửi lên CloudWatch.

Hướng mở rộng cuối kỳ (chưa làm): tách MySQL sang Amazon RDS, file sang S3 (`storage-comparison.md`); HTTPS bằng reverse proxy và tên miền; CI/CD.
