# So sánh phương án database và lưu file – QLSV Cloud

## 1. Hiện trạng

Ứng dụng Spring Boot dùng MySQL 8.4 (dữ liệu nghiệp vụ, tài khoản, metadata tài liệu) và MinIO (nội dung file tải lên),
chạy bằng Docker Compose trên **một máy ảo AWS EC2** ở region Sydney (`ap-southeast-2`). Dữ liệu nằm trong named volume trên ổ EBS của VM.

## 2. Database: MySQL trên VM hay Amazon RDS

| Tiêu chí | MySQL trong container trên EC2 (đang dùng) | Amazon RDS for MySQL |
|---|---|---|
| Vận hành | Nhóm tự cập nhật, theo dõi, xử lý sự cố | AWS lo vá lỗi, sao lưu tự động, failover (nếu bật Multi-AZ) |
| Chi phí thêm | 0 (dùng chung VM và ổ đĩa đã có) | Thêm tiền DB instance + storage + backup vượt mức miễn phí |
| Sao lưu | `scripts/backup.sh` (mysqldump), tự lên lịch | Automated backup, snapshot, khôi phục theo thời điểm |
| Sửa ứng dụng | Không | Chỉ đổi biến `DB_HOST`, `DB_USER`, `DB_PASSWORD` trong `.env`; cần kiểm thử lại kết nối |
| Bảo mật mạng | Cổng 3306 không publish, chỉ app trong mạng compose truy cập | Đặt trong subnet riêng, Security Group chỉ cho EC2 truy cập |
| Phù hợp | Môn học, demo, dữ liệu nhỏ | Hệ thống chạy lâu dài, cần độ sẵn sàng cao |

## 3. Lưu file: MinIO trên VM hay Amazon S3

| Tiêu chí | MinIO trong container (đang dùng) | Amazon S3 |
|---|---|---|
| Triển khai | Đã tích hợp, chạy cùng compose | Dịch vụ quản lý, không cần container |
| Chi phí thêm | 0 (dùng ổ đĩa VM) | Theo dung lượng, số request và truyền dữ liệu |
| Bảo vệ dữ liệu | Mất VM là mất file nếu không sao lưu | Độ bền cao, có versioning, replication |
| Sửa ứng dụng | Không | Đổi `MINIO_ENDPOINT` và khóa truy cập; cần kiểm thử upload/download (API tương thích S3) |
| Phù hợp | Demo, phát triển | Lưu trữ lâu dài |

## 4. Chi phí 1 tháng (phương án đang dùng)

Giá on-demand region Sydney, tra bằng AWS Pricing API ngày 12/11/2026 (chi tiết: [`cost-estimate.md`](cost-estimate.md)).

| Hạng mục | Chạy liên tục 730 giờ | Chỉ bật khi cần (khoảng 40 giờ) |
|---|---|---|
| EC2 `c7i-flex.large` (0,11072 USD/giờ) | 80,83 USD | 4,43 USD |
| EBS gp3 30 GB (0,096 USD/GB-tháng) | 2,88 USD | 2,88 USD (tính cả khi VM tắt) |
| IPv4 công khai (0,005 USD/giờ khi gắn) | 3,65 USD | 0,20 USD |
| **Tổng** | **≈ 87,36 USD** | **≈ 7,51 USD** |

Phương án tách dịch vụ (EC2 + RDS + S3) cộng thêm tiền RDS instance, RDS storage và S3 vào bảng trên; nhóm chưa tính số cụ thể vì không dùng ở giữa kỳ.

## 5. Lựa chọn

Giữ **MySQL + MinIO trong Docker Compose trên một EC2** cho phạm vi môn học:

1. Tận dụng ứng dụng đã Docker hóa, không sửa mã nguồn.
2. Không phát sinh chi phí dịch vụ ngoài VM; phù hợp với dữ liệu demo nhỏ.
3. Đã có sao lưu/khôi phục kiểm chứng trên AWS (`backup-restore-test.md`).

## 6. Hạn chế và hướng phát triển

- Một VM là điểm lỗi đơn; volume không thay thế bản sao lưu độc lập.
- Bản sao lưu đang nằm trên chính VM; cần chép ra S3 hoặc máy khác.
- Sao lưu chưa gồm file trong MinIO.
- Khi cần chạy lâu dài: chuyển MySQL sang RDS và MinIO sang S3 (chỉ đổi biến môi trường), kiểm thử lại T3, T4.

## 7. Nguồn

- AWS Pricing API (`aws pricing get-products`, region `ap-southeast-2`), tra ngày 12/11/2026
- Amazon RDS for MySQL pricing: https://aws.amazon.com/rds/mysql/pricing/
- Amazon S3 pricing: https://aws.amazon.com/s3/pricing/
- Amazon VPC pricing (IPv4 công khai): https://aws.amazon.com/vpc/pricing/
