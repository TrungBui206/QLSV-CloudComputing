# QLSV trên Kubernetes (bài giữa kỳ CKA 2026)

Ứng dụng QLSV (Spring Boot + MySQL + MinIO) đóng gói bằng Docker và chạy trên cluster Kubernetes gồm
1 control plane (`cp`) và 2 worker (`worker1`, `worker2`), dựng bằng kubeadm trên 3 máy AWS EC2.
Kết quả thử nghiệm: [docs/kubernetes-test-results.md](../docs/kubernetes-test-results.md).

## Kiến trúc

```
Trình duyệt ──► http://<IP công khai của node bất kỳ>:30080   (Service frontend, NodePort)
                     │
          ┌──────────┴──────────┐
          ▼                     ▼
   frontend (nginx) × 2   rải trên worker1 + worker2
          │  /api, /health, /backend-info  ──►  Service backend (ClusterIP :8082)
          ▼
   backend (Spring Boot) × 2   rải trên worker1 + worker2
          │
          ├──► Service mysql (ClusterIP :3306) ──► MySQL × 1  (ghim vào worker1, dữ liệu hostPath)
          └──► Service minio (ClusterIP :9000) ──► MinIO × 1  (ghim vào worker1, dữ liệu hostPath)
```

| Thành phần | Image | Kiểu | Replicas |
|---|---|---|---|
| frontend | `trung296/qlsv-frontend:v1`, `:v2` (nhóm build) | Deployment + Service NodePort 30080 | 2 (thử scale 4) |
| backend | `trung296/qlsv-backend:v1`, `:v2` (nhóm build) | Deployment + Service ClusterIP | 2 |
| mysql | `mysql:8.4` | Deployment + Service ClusterIP | 1 |
| minio | `cgr.dev/chainguard/minio:latest` | Deployment + Service ClusterIP | 1 |

**Vì sao MySQL và MinIO chỉ chạy 1 bản:** đây là thành phần có trạng thái. Hai Pod MySQL độc lập là hai database
khác nhau, dữ liệu không đồng bộ; nhân bản đúng cần cấu hình replication, nằm ngoài phạm vi bài. Frontend và backend
không giữ trạng thái (đăng nhập bằng JWT, mọi Pod backend dùng chung khóa ký trong Secret) nên chạy được nhiều bản.
Vì dữ liệu nằm trên đĩa của `worker1`, khi thử tắt node phải tắt **`worker2`**.

**Vì sao MinIO dùng tag `latest`:** Chainguard chỉ phát hành miễn phí tag này. Quy định "không dùng `latest`" của đề
áp dụng cho image nhóm tự build (frontend, backend), luôn gắn `v1`, `v2`.

## Các file

| File | Nội dung |
|---|---|
| `k8s/00-namespace.yaml` | Namespace `qlsv` |
| `k8s/01-secret.example.yaml` | Mẫu Secret. File thật `01-secret.yaml` sinh bằng `scripts/k8s/gen-secret.sh`, không commit |
| `k8s/02-configmap.yaml` | Cấu hình backend (địa chỉ MySQL, MinIO, cổng) |
| `k8s/03-mysql-init-configmap.yaml` | Dữ liệu demo (`db/init/*.sql`), sinh bằng `scripts/k8s/gen-mysql-init.sh` |
| `k8s/04-mysql.yaml`, `k8s/05-minio.yaml` | Database và kho file |
| `k8s/06-backend.yaml`, `k8s/07-frontend.yaml` | Ứng dụng, 2 replicas, chia đều 2 worker (`topologySpreadConstraints`) |
| `k8s/frontend/` | Dockerfile của frontend (nginx), cấu hình nginx, trang `/cluster.html` |
| `Dockerfile` (gốc repo) | Image backend, dùng chung với bản Docker Compose |
| `scripts/k8s/` | Script tạo máy, cài node, init cluster, build và push image |

## Chạy lại từ đầu

### 1. Build và đẩy image (máy có Docker)

```bash
docker login -u <tên-docker-hub>
./scripts/k8s/build-push.sh <tên-docker-hub>          # Windows: scripts\k8s\build-push.cmd <tên-docker-hub>
```
Script build `qlsv-backend` và `qlsv-frontend` với tag `v1` và `v2`. Hai phiên bản chỉ khác biến `APP_VERSION`,
hiện ở nhãn góc dưới phải mỗi trang (v1 màu xanh dương, v2 màu xanh lá) và ở `/cluster.html`.
Đổi `trung296/` trong `k8s/06-backend.yaml`, `k8s/07-frontend.yaml` nếu dùng tài khoản khác.

### 2. Tạo 3 máy EC2 và cài node (AWS CloudShell, region Sydney)

Tải repo (hoặc thư mục `k8s/` và `scripts/k8s/`) lên CloudShell, rồi:
```bash
cd scripts/k8s
./aws-create-nodes.sh          # Security Group k8s-midterm-sg + 3 máy c7i-flex.large; mỗi máy tự chạy node-setup.sh
./aws-node.sh status           # chờ 2-3 phút cho cài đặt xong
```
`node-setup.sh`: đặt hostname, tắt swap, bật `br_netfilter` và `ip_forward`, cài containerd (cgroup systemd),
kubeadm, kubelet, kubectl v1.35.

Security Group: các node gọi nhau tự do (cùng nhóm), cổng 30080 mở cho mọi nơi, SSH chỉ từ EC2 Instance Connect
và IP của CloudShell (`aws-node.sh` tự thêm).

### 3. Dựng cluster

```bash
./aws-node.sh cp 'sudo bash -s' < cp-init.sh                     # kubeadm init + Flannel
JOIN=$(./aws-node.sh cp sudo kubeadm token create --print-join-command)
./aws-node.sh worker1 "sudo $JOIN"
./aws-node.sh worker2 "sudo $JOIN"
./aws-node.sh cp kubectl get nodes                               # 3 node Ready
```

### 4. Triển khai ứng dụng (trên `cp`, trong thư mục repo)

```bash
./scripts/k8s/gen-secret.sh                 # tạo k8s/01-secret.yaml, mật khẩu ngẫu nhiên
kubectl apply -f k8s/                        # bỏ qua 01-secret.example.yaml nếu đã có 01-secret.yaml (xem dưới)
kubectl -n qlsv get pods -o wide
```
`kubectl apply -f k8s/` cũng áp dụng `01-secret.example.yaml` (mật khẩu mẫu). Để tránh, áp dụng từng file:
```bash
for f in 00-namespace 01-secret 02-configmap 03-mysql-init-configmap 04-mysql 05-minio 06-backend 07-frontend; do
  kubectl apply -f k8s/$f.yaml; done
```
Mở `http://<IP công khai của cp, worker1 hoặc worker2>:30080/login.html`. Tài khoản demo: README gốc, mục 5.

## Lệnh vận hành

```bash
# Scale lên 4 replicas
kubectl -n qlsv scale deploy/frontend --replicas=4
kubectl -n qlsv get pods -l app=frontend -o wide

# Cập nhật lên v2 (rolling update, không gián đoạn: maxUnavailable 0)
kubectl -n qlsv set image deploy/frontend frontend=trung296/qlsv-frontend:v2
kubectl -n qlsv annotate deploy/frontend kubernetes.io/change-cause="frontend v2" --overwrite
kubectl -n qlsv rollout status deploy/frontend
kubectl -n qlsv rollout history deploy/frontend

# Rollback về phiên bản trước
kubectl -n qlsv rollout undo deploy/frontend

# Tắt worker2 (từ CloudShell) và quan sát
aws ec2 stop-instances --instance-ids <id k8s-worker2>
kubectl get nodes -w
kubectl -n qlsv get pods -o wide -w

# Sau khi bật lại worker2: chia lại Pod cho đều 2 worker
kubectl -n qlsv rollout restart deploy/backend deploy/frontend
```

Bật, tắt cả cluster để tiết kiệm chi phí: `./scripts/k8s/aws-node.sh start` / `stop`.
IP công khai đổi sau mỗi lần bật, xem bằng `./scripts/k8s/aws-node.sh status`.

## Dọn tài nguyên sau khi chấm

Terminate 3 máy `k8s-cp`, `k8s-worker1`, `k8s-worker2` (ổ đĩa xóa theo), rồi xóa Security Group `k8s-midterm-sg`.
Xóa image trên Docker Hub nếu không dùng nữa.
