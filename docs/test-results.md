# Kết quả kiểm thử

Môi trường: Docker local (`docker compose up -d --build`), dữ liệu demo `db/init/`, ngày 10/11/2026.
Bản cloud: Người 4 bổ sung sau khi triển khai.

| Mã | Tình huống | Kết quả mong đợi | Local | Cloud | Minh chứng |
|---|---|---|---|---|---|
| T1 | Mở URL công khai | Trang web hiển thị bình thường | — | | |
| T2 | Đăng ký và đăng nhập | Người dùng truy cập được chức năng cá nhân | Đạt (3 tài khoản demo) | | |
| T3 | Thêm, sửa, xóa | Dữ liệu thay đổi đúng trong database | | | |
| T4 | Khởi động lại ứng dụng | Dữ liệu vẫn còn | | | |
| T5 | Gọi `/health` | Trả về trạng thái thành công | Đạt (`{"status":"UP"}`) | | |
| T6 | Gửi dữ liệu không hợp lệ | Báo lỗi phù hợp, không bị dừng | Đạt (chi tiết bên dưới) | | |
| T7 | Xem log | Tìm được request hoặc lỗi thử nghiệm | | | |

## T2: tài khoản demo (local)

| Tài khoản | Đăng nhập | Kiểm tra thêm |
|---|---|---|
| `admin` | 200 | Thấy 5 khoa, 3 lớp, 18 sinh viên, 2 niên khóa, 0 tài liệu |
| `lecturer` | 200 | |
| `student01` | 200 | `GET /api/students` chỉ trả 1 hồ sơ (Nguyễn Văn An); xem hồ sơ của mình 200, hồ sơ người khác 403 |

## T6: dữ liệu không hợp lệ (local)

Gọi API bằng tài khoản `admin` (trừ các ca đăng ký/đăng nhập). Sau toàn bộ các ca, `/health` vẫn trả `UP`.

| # | Ca kiểm thử | Request | HTTP | Thông báo trả về |
|---|---|---|---|---|
| 1 | Email sinh viên sai định dạng | `POST /api/students` | 400 | `email: must be a well-formed email address` |
| 2 | Ngày sinh ở tương lai | `POST /api/students` | 400 | `dob: must be a past date` |
| 3 | Thiếu tên sinh viên | `POST /api/students` | 400 | `name: must not be blank` |
| 4 | Email sinh viên đã tồn tại | `POST /api/students` | 409 | `Email đã tồn tại: an.nguyen@example.com` |
| 5 | Khoa không tồn tại | `POST /api/students` (`departmentId: 999`) | 400 | `Department không tồn tại: 999` |
| 6 | Mã khoa trùng | `POST /api/departments` (`code: CNTT`) | 409 | `Mã khoa đã tồn tại: CNTT` |
| 7 | Năm kết thúc nhỏ hơn năm bắt đầu | `POST /api/academic-years` | 400 | `Năm kết thúc không được nhỏ hơn năm bắt đầu` |
| 8 | JSON hỏng | `POST /api/students` | 400 | `Nội dung JSON không hợp lệ` |
| 9 | Xóa khoa còn sinh viên | `DELETE /api/departments/1` | 409 | `Khoa đang có sinh viên, không thể xóa` |
| 10 | Mật khẩu đăng ký dưới 8 ký tự | `POST /api/auth/register` | 400 | `password: size must be between 8 and 72` |
| 11 | Sai mật khẩu | `POST /api/auth/login` | 401 | `Tài khoản hoặc mật khẩu không đúng` |
| 12 | Không gửi token | `GET /api/students` | 401 | `Bạn chưa đăng nhập hoặc phiên đăng nhập không hợp lệ` |
| 13 | Sai phương thức HTTP | `GET /api/academic-years` (chỉ nhận `POST`) | 405 | `Phương thức GET không được hỗ trợ cho đường dẫn này` (trước khi sửa trả nhầm 500; đã thêm handler `HttpRequestMethodNotSupportedException`) |

Ghi chú: thông báo validation của Bean Validation (ca 1–3, 10) đang là tiếng Anh mặc định.
