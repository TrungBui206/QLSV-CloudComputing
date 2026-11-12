# Kết quả thử nghiệm trên Kubernetes

Thực hiện ngày 12/11/2026 trên cluster AWS Sydney (ap-southeast-2): 3 máy EC2 `c7i-flex.large`
(2 vCPU, 4 GB RAM), Ubuntu 24.04, Kubernetes v1.35.10, containerd 2.2.1, CNI Flannel.
Lệnh chạy từ AWS CloudShell. Thời gian ghi theo UTC.

## 1. Cluster

`kubectl get nodes -o wide`:

| Node | Trạng thái | Vai trò | IP nội bộ |
|---|---|---|---|
| cp | Ready | control-plane | 172.31.10.181 |
| worker1 | Ready | | 172.31.15.169 |
| worker2 | Ready | | 172.31.3.212 |

## 2. Image trên Docker Hub

`trung296/qlsv-backend` và `trung296/qlsv-frontend`, mỗi repo có tag `v1` và `v2` (kiểm tra qua API Docker Hub).

## 3. Triển khai

Sau `kubectl apply`, mỗi Deployment frontend, backend có 2 Pod, mỗi worker một Pod:

| Pod | Node |
|---|---|
| backend-…-58sw6 | worker2 |
| backend-…-5hcx5 | worker1 |
| frontend-…-8k574 | worker1 |
| frontend-…-msj99 | worker2 |
| mysql-… | worker1 |
| minio-… | worker1 |

Truy cập từ ngoài cluster (CloudShell) qua NodePort 30080:

| Kiểm tra | Kết quả |
|---|---|
| `GET /login.html` qua IP công khai của cp, worker1, worker2 | 200 cả ba |
| `GET /health` | `{"status":"UP"}` cả ba |
| `GET /api/students` không token | 401 |
| Đăng nhập `admin` qua `/api/auth/login`, rồi `GET /api/students` | Trả danh sách sinh viên demo |
| 6 lần `GET /backend-info` | Trả lời luân phiên từ 2 Pod backend trên worker1 và worker2 |

## 4. Scale lên 4 replicas

`kubectl -n qlsv scale deploy/frontend --replicas=4`: 4 Pod Running, 2 trên worker1, 2 trên worker2.

## 5. Cập nhật và rollback

| Bước | Kết quả |
|---|---|
| `set image ... qlsv-frontend:v2` | Rolling update thay lần lượt 4 Pod; `/frontend-info` trả `"version":"v2"` |
| `rollout history` | Revision 1 `frontend v1`, revision 2 `frontend v2` |
| `rollout undo` | Image trở lại `trung296/qlsv-frontend:v1`; `/frontend-info` trả `"version":"v1"` |

Ghi nhận: ngay sau rollback, 1 trong 4 request kiểm tra không có phản hồi trong 5 giây (một Pod cũ đang dừng).
Không lặp lại ở các request sau.

## 6. Tắt worker2

Trạng thái trước: frontend 4 Pod (2 mỗi worker), backend 2 Pod (1 mỗi worker).
Lệnh: `aws ec2 stop-instances` cho `k8s-worker2` lúc 07:52:25. Mỗi 15–20 giây gửi 5 request `/backend-info`
qua IP của worker1.

| Thời điểm | Request thành công | worker2 |
|---|---|---|
| 07:52:54 | 1/5 | Ready |
| 07:53:19 | 3/5 | NotReady |
| 07:53:39 | 5/5 | NotReady |
| 07:53:59 – 07:55:20 | 5/5 | NotReady |

Trạng thái Pod lúc 07:55:
- Pod trên worker2 chuyển `Terminating`; Kubernetes tạo Pod thay thế trên worker1.
- frontend: 4 Pod Running trên worker1. backend: 2 Pod Running trên worker1.
- mysql, minio: không đổi (vốn ở worker1).

Giải thích:
1. Máy tắt, kubelet trên worker2 ngừng gửi heartbeat. Sau khoảng 40 giây (thời gian chờ mặc định của
   node controller) control plane đánh dấu worker2 `NotReady`.
2. Trong khoảng này Service vẫn chuyển một phần request tới Pod trên worker2, nên có request lỗi (1/5, 3/5).
   Đây là thời gian gián đoạn một phần, khoảng 1 phút.
3. Khi node NotReady, Pod trên đó không còn Ready nên bị loại khỏi Service; mọi request đi tới Pod trên worker1.
4. Sau 30 giây (`tolerationSeconds: 30` trong YAML; mặc định là 300 giây), Pod trên worker2 bị đánh dấu xóa và
   Deployment tạo Pod mới trên worker1 để giữ đủ số replicas. Pod cũ ở trạng thái `Terminating` cho tới khi
   worker2 quay lại, vì control plane không liên lạc được kubelet để xác nhận đã dừng.
5. `topologySpreadConstraints` dùng `nodeTaintsPolicy: Honor` nên bỏ qua node hỏng, cho phép dồn Pod sang worker1.

Nếu tắt worker1 thay vì worker2, ứng dụng sẽ ngừng hoạt động vì MySQL và MinIO chỉ có 1 bản trên worker1.

## 7. Bật lại worker2

Sau khi bật, worker2 trở lại `Ready` trong khoảng 1 phút. Pod **không** tự chia lại; chạy
`kubectl -n qlsv rollout restart deploy/backend deploy/frontend` để chia lại mỗi worker 1 Pod. IP công khai của
worker2 đổi (3.27.141.25 → 16.176.6.188).

## Giới hạn

- Chưa kiểm thử tải.
- MySQL, MinIO một bản, dữ liệu trên đĩa của worker1: mất worker1 là mất dịch vụ.
- Truy cập bằng IP và cổng 30080, chưa có Ingress, tên miền, HTTPS.
- `curl localhost:30080` chạy ngay trên máy cp bị treo; truy cập qua IP công khai hoạt động bình thường.
  Chưa tìm nguyên nhân.
