# Dự toán chi phí 1 tháng

Theo bài giảng Cloud Database (mục 13.2): chi phí = compute theo thời gian chạy + storage + backup ngoài phần được bao gồm + IP/truyền dữ liệu và khoản khác.

**Nền tảng:** AWS (tài khoản Free plan: credit 100 USD + tối đa 100 USD, 6 tháng)  **Region:** Asia Pacific (Sydney) `ap-southeast-2`
**Ngày tra cứu giá:** 12/11/2026, bằng AWS Pricing API (`aws pricing get-products`) và trang giá AWS  **Người tra cứu:** _(điền)_

## Giả định

| Giả định | Giá trị |
|---|---|
| Loại VM | EC2 `c7i-flex.large` (2 vCPU, 4 GB RAM), Linux, on-demand |
| Ổ đĩa | 1 volume EBS gp3 30 GB (gốc của VM, chứa cả volume Docker) |
| Số giờ VM chạy / tháng | Kịch bản A: 730 giờ (chạy liên tục). Kịch bản B: khoảng 40 giờ (chỉ bật khi demo/kiểm thử) |
| Dung lượng dữ liệu (MySQL + MinIO) | < 1 GB (dữ liệu demo) |
| Lưu lượng ra Internet | < 5 GB / tháng, nằm trong 100 GB miễn phí mỗi tháng của AWS |
| Log CloudWatch | < 100 MB / tháng, giữ 7 ngày, nằm trong mức miễn phí 5 GB |
| Sao lưu | `scripts/backup.sh` lưu trên chính VM (không tính thêm) |
| IP công khai | 1 IPv4 tự cấp (đổi mỗi lần Start), chỉ tính khi đang gắn với VM đang chạy |

## Bảng chi phí

| Hạng mục | Đơn giá | Kịch bản A (730 giờ) | Kịch bản B (40 giờ) | Nguồn |
|---|---|---|---|---|
| EC2 `c7i-flex.large` | 0,11072 USD/giờ | 80,83 | 4,43 | AWS Pricing API |
| EBS gp3 30 GB | 0,096 USD/GB-tháng | 2,88 | 2,88 (vẫn tính khi VM tắt) | AWS Pricing API |
| IPv4 công khai | 0,005 USD/giờ | 3,65 | 0,20 | https://aws.amazon.com/vpc/pricing/ |
| Truyền dữ liệu ra Internet | 100 GB đầu miễn phí | 0 | 0 | https://aws.amazon.com/ec2/pricing/on-demand/ |
| CloudWatch Logs | 5 GB đầu miễn phí | 0 | 0 | https://aws.amazon.com/cloudwatch/pricing/ |
| **Tổng trước credit** | | **≈ 87,36 USD** | **≈ 7,51 USD** | |
| Credit Free plan | | trừ vào credit | trừ vào credit | |
| **Tiền thật phải trả** | | 0 khi còn credit | 0 khi còn credit | |

Nhóm dùng kịch bản B: chỉ Start VM khi demo/kiểm thử, Stop ngay sau đó.

## Kiểm soát chi phí

- [ ] Đã tạo budget alert (đề xuất ngưỡng 10 USD), có ảnh chụp: **chủ tài khoản AWS làm**
- [ ] Ghi ngày bắt đầu dùng credit và ngày hết hạn
- [x] Tắt VM khi không dùng (EBS vẫn tính khi VM tắt)
- [ ] Sau khi chấm: xóa VM, ổ EBS, snapshot, log group `/qlsv/app`, IAM role `qlsv-ec2-logs`, deploy key GitHub (README mục 4c.7)

Ghi chú: budget alert chỉ là cảnh báo, không tự ngắt chi phí.
