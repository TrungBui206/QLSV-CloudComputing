# Log và metric trên AWS

## 1. Log ứng dụng trên Amazon CloudWatch Logs

| Thành phần | Cấu hình |
|---|---|
| Log driver | `awslogs` cho container `app` (file `docker-compose.aws.yml`, bật bằng `COMPOSE_FILE` trong `.env` trên server) |
| Log group / stream | `/qlsv/app` / `app`, region `ap-southeast-2`, giữ 7 ngày |
| Quyền | IAM role `qlsv-ec2-logs` (policy `CloudWatchAgentServerPolicy`) gắn vào EC2 qua instance profile; không lưu access key trên VM |
| Nội dung | Toàn bộ log Spring Boot, gồm một dòng cho mỗi request: `METHOD /đường-dẫn -> mã trạng thái (thời gian ms)` |

Docker vẫn giữ bản sao log trên máy, nên `docker compose logs app` trên VM vẫn dùng được.

**Kiểm chứng (12/11/2026):** sau khi gọi API từ máy ngoài, truy vấn `aws logs filter-log-events --log-group-name /qlsv/app --filter-pattern RequestLoggingFilter` trả về:

```
GET /api/students -> 200 (44 ms)
POST /api/auth/login -> 200 (88 ms)
DELETE /api/students/19 -> 204 (90 ms)
GET /api/students/999999 -> 404 (25 ms)
GET /api/students -> 200 (28 ms)
```

Dòng `404` là request lỗi thử nghiệm (T7).

**Chụp ảnh cho báo cáo:** CloudWatch → Log groups → `/qlsv/app` → stream `app` → ô lọc gõ `RequestLoggingFilter`
(hoặc Logs Insights với truy vấn `fields @timestamp, @message | filter @message like /->/ | sort @timestamp desc | limit 20`).

## 2. Metric

| Metric | Nguồn | Xem ở đâu |
|---|---|---|
| `CPUUtilization` của EC2 | EC2 basic monitoring (miễn phí, chu kỳ 5 phút) | EC2 → Instances → `qlsv-server` → tab **Monitoring**, hoặc CloudWatch → Metrics → EC2 → Per-Instance Metrics |
| Network in/out, Status check | EC2 basic monitoring | Cùng tab Monitoring |
| `/health` của ứng dụng | Spring Actuator | `http://<IP>/health` |

Đã xác nhận CloudWatch có dữ liệu `CPUUtilization` của `i-0bede4b3e49ac7d77` trong lúc kiểm thử.
RAM không có sẵn trong basic monitoring; muốn có cần cài CloudWatch Agent (role `qlsv-ec2-logs` đã đủ quyền).

## 3. Giới hạn

- Chưa có alarm tự động (ví dụ CPU > 80 % hoặc `/health` lỗi).
- Chỉ log của `app` lên CloudWatch; log MySQL và MinIO vẫn chỉ trên VM.
