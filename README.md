# Hệ thống Quản lý Sinh viên (QuanLySinhVien_HibernateDAO)

Ứng dụng web quản lý Khoa, Lớp học, Sinh viên, Niên khóa và Tài liệu.
Backend Spring Boot (REST + Hibernate DAO tự viết), frontend HTML/JS tĩnh,
xác thực bằng JWT và phân quyền theo 3 vai trò ADMIN / LECTURER / STUDENT.

---

## 1. Công nghệ

| Thành phần | Công nghệ |
|---|---|
| Ngôn ngữ / build | Java 21, Maven (dùng Maven Wrapper có sẵn: `mvnw.cmd` trên Windows, `./mvnw` trên macOS/Linux) |
| Framework | Spring Boot 4.1.1 (Web MVC, Data JPA, Validation, Security) |
| Truy cập dữ liệu | Hibernate DAO tự viết (`IGenericDao`, `HibernateGenericDao`, `HibernatePaginationSortingDao`) |
| CSDL | MySQL (chạy thật), H2 (chỉ dùng khi chạy test) |
| Lưu trữ tệp | MinIO |
| Xác thực | JWT (OAuth2 Resource Server, HS256) |
| Ánh xạ | MapStruct, Lombok, Builder |
| Tài liệu API | springdoc-openapi (Swagger UI) |
| Kiểm thử, chất lượng | JUnit 5, Mockito, JaCoCo, SonarQube |

---

## 2. Kiến trúc

Dự án chia **theo tầng** và **theo nghiệp vụ**. Luồng xử lý: `REST -> Service -> DAO -> CSDL`.
Mỗi tầng gồm interface + thư mục `impl`.

```
src/main/java/hd/quanlysinhvien_hibernatedao
├── rest/            (interface)  + rest/impl/      : nhận request, @PreAuthorize phân quyền
├── service/         (interface)  + service/impl/   : nghiệp vụ, kiểm tra dữ liệu, giao dịch
├── dao/             (interface)  + dao/impl/       : truy vấn Hibernate/JPA
├── hibernateDao/                                   : DAO generic dùng chung
├── entity/                                         : thực thể JPA
├── dto/request, dto/response                       : dữ liệu vào/ra API
├── mapper/                                         : MapStruct (entity <-> DTO)
├── wrapper/                                        : ApiResponse, ApiError
├── common/          exception, handler (GlobalExceptionHandler), util
├── config/          SecurityConfig, MinioConfig, OpenApiConfig, BootstrapUsersConfig ...
└── security/
    ├── authorization/   Role, AuthorizationPolicies
    ├── jwt/             tạo/kiểm tra JWT
    ├── handler/         trả 401/403 dạng JSON
    └── scope/           phạm vi dữ liệu (STUDENT chỉ xem hồ sơ của mình)

src/main/resources/static      : giao diện (login.html + 5 trang nghiệp vụ)
  ├── menu.js       menu, nút Đăng xuất, nút biểu tượng dùng chung
  ├── role-ui.js    ẩn/hiện nút theo vai trò (khớp với backend)
  └── role-ui.css   ẩn mặc định nút quản trị cho tới khi xác định vai trò
```

**Quy ước bắt buộc khi sửa hoặc thêm code:** đặt đúng tầng, đúng nghiệp vụ.
Quy tắc nghiệp vụ nằm ở Service, truy vấn nằm ở DAO, phân quyền theo vai trò nằm ở REST (`@PreAuthorize`).

---

## 3. Chuẩn bị môi trường

1. **JDK 21** và **MySQL** đang chạy (mặc định `127.0.0.1:3306`). Tạo sẵn database:
   ```sql
   CREATE DATABASE educaze_hibernate CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```
   Bảng được Hibernate tự tạo/cập nhật (`ddl-auto=update`).
2. **MinIO** đang chạy (mặc định `http://localhost:9000`, bucket `student-documents`).
   Chỉ cần cho chức năng Tài liệu; các nghiệp vụ khác vẫn chạy khi không có MinIO.
3. **Biến môi trường** (đặt trong *Run Configuration* của IntelliJ hoặc biến hệ thống):

| Biến | Bắt buộc | Ý nghĩa |
|---|---|---|
| `DB_USER` | không (mặc định `root`) | Tài khoản MySQL |
| `DB_PASSWORD` | **có** | Mật khẩu MySQL |
| `AUTH_JWT_SECRET` | **có** | Khóa ký JWT, chuỗi **Base64** của ít nhất **32 byte** |
| `MINIO_ENDPOINT` | không (mặc định `http://localhost:9000`) | Địa chỉ MinIO |
| `MINIO_ACCESS_KEY`, `MINIO_SECRET_KEY` | **có** (khi dùng Tài liệu) | Khóa truy cập MinIO |
| `MINIO_BUCKET` | không (mặc định `student-documents`) | Tên bucket |
| `APP_BOOTSTRAP_ENABLED` | không (mặc định `false`) | `true` để tự tạo tài khoản `admin` và `lecturer` khi khởi động |
| `APP_BOOTSTRAP_ADMIN_PASSWORD`, `APP_BOOTSTRAP_LECTURER_PASSWORD` | khi bật bootstrap | Mật khẩu ban đầu, 12–72 ký tự |

Tạo nhanh một `AUTH_JWT_SECRET` hợp lệ bằng PowerShell:
```powershell
[Convert]::ToBase64String((1..32 | ForEach-Object { Get-Random -Maximum 256 }))
```
macOS / Linux:
```bash
openssl rand -base64 32
```

> Không đưa mật khẩu, khóa hay token thật vào mã nguồn hoặc README.

---

## 4. Chạy ứng dụng

- **IntelliJ:** chạy lớp `QuanLySinhVienHibernateDaoApplication` (đã khai báo biến môi trường).
- **Dòng lệnh:**
  ```powershell
  .\mvnw.cmd spring-boot:run      # Windows
  ```
  ```bash
  ./mvnw spring-boot:run          # macOS / Linux
  ```

Sau khi thấy `Started QuanLySinhVienHibernateDaoApplication`:

| Địa chỉ | Nội dung |
|---|---|
| http://localhost:8082/login.html | Trang đăng nhập |
| http://localhost:8082/swagger-ui/index.html | Swagger UI (đường dẫn mặc định của springdoc) |

---

## 4b. Chạy bằng Docker (môn Cloud Computing, nhánh `cloud`)

Không cần cài JDK, MySQL hay MinIO, chỉ cần **Docker Desktop**. `docker-compose.yml` chạy 3 container:
`app` (image build từ `Dockerfile`), `mysql` (MySQL 8.4) và `minio`.

1. Tạo file cấu hình từ mẫu rồi điền giá trị thật (**không commit `.env`**):
   ```powershell
   copy .env.example .env      # Windows (PowerShell)
   ```
   ```bash
   cp .env.example .env        # macOS / Linux
   ```
   - `DB_HOST` giữ là `mysql` (tên service trong compose). `DB_USER` **không** được là `root`.
   - `AUTH_JWT_SECRET`: chuỗi Base64 ≥ 32 byte (lệnh tạo ở mục 3).
   - `MINIO_SECRET_KEY` ≥ 8 ký tự; mật khẩu bootstrap 12–72 ký tự.
2. Build và chạy:
   ```powershell
   docker compose up -d --build
   docker compose logs -f app      # đợi "Started QuanLySinhVienHibernateDaoApplication"
   ```
3. Kiểm tra:
   - http://localhost:8082/health → `{"status":"UP"}`
   - http://localhost:8082/login.html
4. Dừng: `docker compose down` (giữ dữ liệu) hoặc `docker compose down -v` (xóa cả dữ liệu).

**Dữ liệu mẫu:** các file trong `db/init/` (`01-schema.sql`, `02-data.sql`) được MySQL nạp **tự động ở lần khởi tạo đầu tiên**
(khi volume `mysql-db` còn trống). Tài khoản demo và mật khẩu: xem mục 5.
Muốn nạp lại từ đầu: `docker compose down -v` (xóa cả dữ liệu đang có) rồi `up` lại.

**Log request:** mỗi request được ghi một dòng `METHOD /đường-dẫn -> mã trạng thái (thời gian ms)` vào log ứng dụng
(`docker compose logs app`), không ghi query string hay body. File tĩnh và Swagger được bỏ qua.

**macOS:**
- Cài **Docker Desktop for Mac** đúng chip (Apple Silicon hoặc Intel), mở Docker Desktop trước khi chạy lệnh.
- Đặt project trong thư mục người dùng (`/Users/<tên>/...`): Docker Desktop mặc định chỉ cho container đọc thư mục trong `/Users`, cần cho volume `./db/init`.
- Các lệnh `docker compose ...` giống hệt Windows; chạy trong Terminal hoặc Terminal của IntelliJ.
- Nếu `./mvnw` báo `permission denied` (bản clone cũ): chạy `chmod +x mvnw` một lần.
- Nếu một image báo không có bản cho kiến trúc máy (`no matching manifest for linux/arm64`), thêm `platform: linux/amd64` vào service đó trong `docker-compose.yml` (chạy giả lập, chậm hơn).

**Lưu ý:**
- Image `minio/minio` đã bị gỡ khỏi Docker Hub (09/2026), compose dùng `cgr.dev/chainguard/minio:latest`.
- Tài liệu trong dữ liệu mẫu chỉ có metadata; file thật không có trong MinIO mới nên tải xuống sẽ lỗi. Tải lên file mới để thử.
- Bản Docker và bản chạy bằng IntelliJ dùng **hai database khác nhau** và cùng cổng 8082: không chạy cả hai cùng lúc.

---

## 4c. Triển khai lên cloud (VM Ubuntu + Docker Compose)

Kiến trúc giai đoạn giữa kỳ: **một VM** chạy `docker compose` (app + MySQL + MinIO), chỉ mở cổng HTTP ra Internet.
Cách làm không phụ thuộc nhà cung cấp (AWS, Azure, Google Cloud...): chỉ khác ở bước tạo VM và firewall.
Sơ đồ: [`docs/architecture.md`](docs/architecture.md). Dự toán chi phí: [`docs/cost-estimate.md`](docs/cost-estimate.md).

### 1. Tạo VM

| Thông số | Khuyến nghị |
|---|---|
| Hệ điều hành | Ubuntu Server 24.04 LTS (hoặc 22.04 LTS) |
| CPU / RAM | 2 vCPU, **4 GB RAM** (app + MySQL + MinIO chạy cùng máy; VM 1–2 GB không đủ) |
| Ổ đĩa | 20–30 GB |
| Region | Nhóm đang dùng AWS Sydney (`ap-southeast-2`) |
| Đăng nhập | SSH key (không dùng mật khẩu) |

Nhóm đang dùng AWS EC2 `c7i-flex.large` (2 vCPU, 4 GB, thuộc gói Free Tier). Chỉ bật máy khi cần demo/kiểm thử; mỗi lần Start lại, IP công khai sẽ đổi.

### 2. Firewall (Security Group / Network Security Group)

| Cổng | Nguồn được phép | Mục đích |
|---|---|---|
| 22 (SSH) | Prefix list `com.amazonaws.ap-southeast-2.ec2-instance-connect` (mở terminal trên trình duyệt bằng EC2 Instance Connect) và/hoặc **IP của nhóm** (`<IP>/32`) | Quản trị |
| 80 (HTTP) | `0.0.0.0/0` | Người dùng truy cập web |
| 443 (HTTPS) | `0.0.0.0/0` | Chỉ khi làm HTTPS (điểm cộng cuối kỳ) |

**Không mở** 3306 (MySQL), 9000/9001 (MinIO), 8082. MySQL và MinIO chỉ nói chuyện với app qua mạng nội bộ của compose.

### 3. Cài Docker trên VM

```bash
ssh ubuntu@<IP-cong-khai>                 # user mặc định tùy image: ubuntu (AWS), azureuser (Azure)...
sudo apt-get update && sudo apt-get install -y git
```

Repo là **private**, nên VM cần quyền đọc repo. Cách khuyên dùng là **deploy key chỉ đọc**:

```bash
ssh-keygen -t ed25519 -C "qlsv-vm" -f ~/.ssh/qlsv_deploy -N ""
cat ~/.ssh/qlsv_deploy.pub
```

Chép khóa công khai vào GitHub: repo → **Settings → Deploy keys → Add deploy key**, **không** tick "Allow write access". Sau đó:

```bash
cat >> ~/.ssh/config <<'EOF'
Host github.com
  IdentityFile ~/.ssh/qlsv_deploy
EOF
git clone -b cloud git@github.com:TrungBui206/QLSV-CloudComputing.git qlsv
cd qlsv
bash scripts/server-setup.sh              # cài Docker Engine + Compose, tạo swap nếu RAM < 4 GB
exit                                      # đăng xuất rồi SSH lại để dùng docker không cần sudo
```

### 4. Tạo cấu hình và triển khai

```bash
ssh ubuntu@<IP-cong-khai>
cd qlsv
bash scripts/gen-env.sh                   # tạo .env với mật khẩu ngẫu nhiên, PUBLIC_PORT=80
bash scripts/deploy.sh                    # pull nhánh cloud, build, chạy, chờ /health = UP
```

Kiểm tra từ máy ngoài: `http://<IP-cong-khai>/health` trả `{"status":"UP"}`, rồi đăng nhập bằng tài khoản demo (mục 5).

- `.env` chỉ nằm trên VM (quyền 600). Không commit, không chụp màn hình, không dùng lại mật khẩu ở máy cá nhân.
- Dữ liệu demo trong `db/init/` được nạp ở lần chạy đầu (volume `mysql-db` còn trống).

### 5. Vận hành

| Việc | Lệnh (chạy trong thư mục `qlsv` trên VM) |
|---|---|
| Cập nhật bản mới từ nhánh `cloud` | `bash scripts/deploy.sh` (giữ nguyên dữ liệu) |
| Trạng thái container | `docker compose ps` |
| Xem log app (có log từng request) | `docker compose logs -f --tail 100 app` |
| Khởi động lại app (kiểm thử T4) | `docker compose restart app` |
| Xem MinIO Console | Trên máy cá nhân: `ssh -L 9001:127.0.0.1:9001 ubuntu@<IP>` rồi mở `http://localhost:9001` |
| Dừng ứng dụng | `docker compose down` (giữ dữ liệu) |
| Xóa sạch dữ liệu | `docker compose down -v` (**mất toàn bộ dữ liệu**, nạp lại dữ liệu demo ở lần chạy sau) |

Log của Docker được giới hạn 3 file × 10 MB mỗi container để không làm đầy ổ đĩa VM.

### 6. Dọn tài nguyên sau khi chấm

Xóa trên trang quản trị cloud: VM, ổ đĩa (volume/disk) gắn với VM, IP tĩnh (nếu có), snapshot/backup đã tạo, rồi xóa deploy key trên GitHub.
Kiểm tra lại trang chi phí sau 1–2 ngày để chắc không còn tài nguyên tính phí.

---

## 5. Tài khoản thử nghiệm

### Khi chạy bằng Docker (dữ liệu demo trong `db/init/`)

Dữ liệu demo **hoàn toàn giả**: 5 khoa, 2 niên khóa, 3 lớp, 18 sinh viên (email `@example.com`), không có tài liệu.
Ba tài khoản demo có sẵn để giảng viên kiểm thử:

| Vai trò | Tên đăng nhập | Mật khẩu | Ghi chú |
|---|---|---|---|
| ADMIN | `admin` | `Admin@Demo2026` | Toàn quyền |
| LECTURER | `lecturer` | `Lecturer@Demo2026` | Thêm/sửa lớp, sinh viên; tải lên tài liệu |
| STUDENT | `student01` | `Student@Demo2026` | Email `an.nguyen@example.com`, chỉ xem được hồ sơ của sinh viên Nguyễn Văn An |

Vì `admin`/`lecturer` đã có trong dữ liệu demo, biến `APP_BOOTSTRAP_*` **không** đổi mật khẩu của hai tài khoản này.
Đây là mật khẩu demo dùng chung; khi triển khai thật nên đổi sau khi đăng nhập hoặc thay dữ liệu demo.

### Khi chạy bằng IntelliJ với MySQL trên máy (database trống)

| Vai trò | Cách có tài khoản |
|---|---|
| ADMIN | `admin`, tạo tự động khi `APP_BOOTSTRAP_ENABLED=true` (mật khẩu = `APP_BOOTSTRAP_ADMIN_PASSWORD`) |
| LECTURER | `lecturer`, tạo tự động cùng lúc (mật khẩu = `APP_BOOTSTRAP_LECTURER_PASSWORD`) |
| STUDENT | Tự đăng ký qua API (tài khoản mới luôn có vai trò STUDENT) |

Đăng ký STUDENT (Postman):
```
POST http://localhost:8082/api/auth/register
Content-Type: application/json

{ "username": "student02", "email": "binh.tran@example.com", "password": "<mật khẩu tối thiểu 8 ký tự>" }
```
Tên `admin` và `lecturer` được dành riêng, không đăng ký được. STUDENT chỉ xem được hồ sơ sinh viên có email trùng email tài khoản.

---

## 6. Phân quyền

✔ = được phép, ✘ = bị chặn (backend trả **403**; giao diện ẩn nút tương ứng).

| Nghiệp vụ | Thao tác | ADMIN | LECTURER | STUDENT |
|---|---|:-:|:-:|:-:|
| Khoa | Xem | ✔ | ✔ | ✔ |
| | Thêm / Sửa / Xóa | ✔ | ✘ | ✘ |
| Lớp học | Xem | ✔ | ✔ | ✔ |
| | Thêm / Sửa | ✔ | ✔ | ✘ |
| | Xóa | ✔ | ✘ | ✘ |
| Sinh viên | Xem | ✔ (tất cả) | ✔ (tất cả) | ✔ **chỉ hồ sơ của mình** |
| | Thêm / Sửa | ✔ | ✔ | ✘ |
| | Xóa | ✔ | ✘ | ✘ |
| Niên khóa | Xem | ✔ | ✔ | ✔ |
| | Thêm / Sửa / Xóa | ✔ | ✘ | ✘ |
| Tài liệu | Xem / Tải xuống | ✔ | ✔ | ✔ |
| | Tải lên | ✔ | ✔ | ✘ |
| | Xóa | ✔ | ✘ | ✘ |

**Phạm vi dữ liệu của STUDENT:** STUDENT chỉ thấy hồ sơ sinh viên có **email trùng với email tài khoản**
(không phân biệt hoa thường). Xem hồ sơ khác trả **403**. Xử lý tại `security/scope/StudentDataScope`
và `StudentServiceImpl`.

---

## 7. Một số quy tắc nghiệp vụ

- **Lớp học:** mã lớp là UUID do máy chủ tự sinh. Form tạo mới không có ô nhập mã; khi sửa, mã chỉ đọc.
  Tên lớp không trùng (không phân biệt hoa thường).
- **Khoa / Niên khóa:** mã không trùng; niên khóa phải có năm kết thúc ≥ năm bắt đầu, năm trong khoảng 1900–2100.
- **Sinh viên:** email không trùng; ngày sinh phải ở quá khứ; không xóa được khoa còn sinh viên (409).
- **Tài liệu:** chỉ nhận `pdf, doc, docx, xls, xlsx, jpg, jpeg`; tối đa 10 MB/tệp, 50 MB/lần.
  Tải lên trùng tên trong cùng mã hồ sơ sẽ tạo **phiên bản mới**; bản cũ chuyển sang không hoạt động.
  Xóa là **xóa mềm** (đánh dấu `DELETED`), tệp vẫn được giữ trong MinIO.

---

## 8. Kiểm thử và chất lượng

```powershell
# Toàn bộ test + báo cáo coverage (target/site/jacoco/index.html)
.\mvnw.cmd clean verify        # Windows; macOS/Linux: ./mvnw clean verify

# Phân tích SonarQube (SonarQube chạy tại 127.0.0.1:9000)
.\mvnw.cmd sonar:sonar "-Dsonar.projectKey=QuanLySinhVien_HibernateDAO" "-Dsonar.host.url=http://127.0.0.1:9000" "-Dsonar.token=<TOKEN>"
```

Test dùng profile `test` với H2 trong bộ nhớ, **không cần MySQL hay MinIO**.

Kết quả lần chạy ngày 02/11/2026:

| Hạng mục | Kết quả |
|---|---|
| JUnit | 851 test, 0 lỗi |
| JaCoCo | 95,8% instruction, 87,6% branch |
| SonarQube | Quality Gate **Passed**; coverage 92,9%; trùng lặp 1,2%; 0 bug, 0 lỗ hổng, 0 code smell, 0 security hotspot |

---

## 9. Rủi ro và giới hạn đã biết

1. **Đăng ký công khai + khớp theo email:** ai cũng tự đăng ký được tài khoản STUDENT và email chưa được xác minh.
   Người đăng ký bằng email của một sinh viên khác sẽ xem được hồ sơ của sinh viên đó.
   Hướng khắc phục: đóng đăng ký công khai, hoặc thêm bước xác minh email.
2. **Xóa mềm tài liệu:** bản ghi chỉ được đánh dấu xóa và tệp vẫn nằm trong MinIO. Cần dọn định kỳ nếu muốn giải phóng dung lượng.
3. **`SecurityConfig` dùng `anyRequest().permitAll()`:** các đường dẫn `/api/...` hiện có đều đã khai báo yêu cầu đăng nhập,
   nhưng endpoint mới thêm sau này phải được khai báo thêm, nếu không sẽ mặc định mở.
4. **Mã trạng thái khi xóa chưa thống nhất:** xóa Lớp trả `200` kèm thông báo; xóa Khoa, Sinh viên, Niên khóa, Tài liệu trả `204`.
