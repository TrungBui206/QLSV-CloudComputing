# Dự toán chi phí 1 tháng

Theo bài giảng Cloud Database (mục 13.2): chi phí = compute theo thời gian chạy + storage + backup ngoài phần được bao gồm + IP/truyền dữ liệu và khoản khác. Giá và chính sách miễn phí phải tra tại thời điểm tạo tài nguyên.

**Nền tảng:** AWS (tài khoản Free plan: credit $100 + tối đa $100, 6 tháng)  **Region:** Asia Pacific (Sydney) `ap-southeast-2`  **Ngày tra cứu giá:** 11/11/2026  **Người tra cứu:** _(điền)_

## Giả định

| Giả định | Giá trị |
|---|---|
| Số giờ VM chạy / tháng | 730 giờ (chạy liên tục) — hoặc ghi số giờ thực tế nếu tắt VM khi không dùng |
| Dung lượng dữ liệu (MySQL + MinIO) | < 1 GB (dữ liệu demo) |
| Lưu lượng ra Internet | < 5 GB / tháng (demo, chấm bài) |
| Backup | Script `mysqldump` lưu trên chính VM (không tính thêm) |
| Số IP công khai | 1 |

## Bảng chi phí

| Hạng mục | Cấu hình | Đơn giá | Số lượng | Thành tiền (USD) | Nguồn đơn giá |
|---|---|---|---|---|---|
| Compute (VM) | EC2 `c7i-flex.large` (2 vCPU / 4 GB), Linux | 0.11072 USD/giờ | 730 giờ | 80.83 | Bảng giá hiển thị khi tạo instance trên EC2 console |
| Ổ đĩa | _(loại, 30 GB)_ | _(USD/GB-tháng)_ | 30 GB | | |
| IP công khai | 1 địa chỉ IPv4 | 0.005 USD/giờ _(xác nhận lại)_ | 730 giờ | 3.65 | https://aws.amazon.com/vpc/pricing/ |
| Truyền dữ liệu ra Internet | | _(USD/GB)_ | 5 GB | | |
| **Tổng trước credit** | | | | | |
| Credit / gói miễn phí áp dụng | Credit Free plan của AWS | | | _(trừ)_ | |
| **Tổng phải trả** | | | | | |

Ghi chú: nếu chỉ bật máy khi cần (ví dụ 40 giờ/tháng) thì compute ≈ 40 × 0.11072 ≈ 4.43 USD; khi máy tắt vẫn tính ổ đĩa EBS.

## Kiểm soát chi phí

- [ ] Đã tạo budget alert (ngưỡng: ____ USD), có ảnh chụp
- [ ] Đã ghi ngày bắt đầu dùng credit và ngày hết hạn
- [ ] Tắt VM khi không dùng lâu (lưu ý: IP công khai và ổ đĩa có thể vẫn tính phí khi VM tắt)
- [ ] Sau khi chấm: xóa VM, ổ đĩa, IP, snapshot (README mục 4c.6)

Ghi chú: budget alert chỉ là cảnh báo, không tự ngắt chi phí.
