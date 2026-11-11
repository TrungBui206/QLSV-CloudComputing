# Kịch bản demo chức năng (2–3 phút)

Chuẩn bị: ứng dụng đang chạy với dữ liệu demo (`db/init/`), mở sẵn trang `/login.html`.
Tài khoản: xem README mục 5. Thay `<URL>` bằng `http://localhost:8082` (local) hoặc URL công khai trên cloud.

| # | Thời lượng | Thao tác | Điểm cần nói |
|---|---|---|---|
| 1 | 15 s | Mở `<URL>/health` | Trả `{"status":"UP"}`, dùng cho giám sát và kiểm tra sau triển khai (T5) |
| 2 | 20 s | Đăng nhập `admin`, mở trang **Khoa**, **Sinh viên** | Dữ liệu demo nạp tự động từ `db/init/` khi khởi tạo database |
| 3 | 40 s | Thêm sinh viên mới → sửa tên → xóa | CRUD; dữ liệu lưu trong MySQL (T3) |
| 4 | 20 s | Tìm kiếm theo tên, lọc theo khoa, đổi trang | Tìm kiếm, lọc, phân trang, sắp xếp |
| 5 | 20 s | Thêm sinh viên với email sai hoặc ngày sinh ở tương lai | Ứng dụng báo lỗi rõ ràng, không dừng (T6) |
| 6 | 20 s | Đăng xuất, đăng nhập `lecturer` | Không thấy nút xóa khoa/sinh viên; backend trả 403 nếu gọi thẳng API |
| 7 | 20 s | Đăng xuất, đăng nhập `student01` | Chỉ thấy hồ sơ của chính mình (Nguyễn Văn An) |
| 8 | 15 s | Mở log ứng dụng (`docker compose logs app` hoặc trang log trên cloud) | Thấy từng request vừa thao tác, ví dụ `POST /api/students -> 201` (T7) |

Phương án dự phòng: nếu mạng hoặc cloud lỗi lúc thuyết trình, chạy bản Docker trên máy hoặc mở video demo quay sẵn.
