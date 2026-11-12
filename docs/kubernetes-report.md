# Báo cáo bài giữa kỳ CKA 2026: triển khai QLSV bằng Docker và Kubernetes

Cập nhật: 12/11/2026. Hạn nộp: 19/11/2026.
Tài liệu liên quan: [hướng dẫn chạy](../k8s/README.md), [kết quả thử nghiệm](kubernetes-test-results.md).

## 1. Tóm tắt

Nhóm dùng lại ứng dụng QLSV (Spring Boot, MySQL, MinIO), tách thành frontend (nginx) và backend (Spring Boot),
đóng gói bằng Docker, đẩy lên Docker Hub, rồi chạy trên cluster Kubernetes 3 máy AWS EC2 dựng bằng kubeadm.
Bốn thao tác vận hành của đề (scale, cập nhật, rollback, tắt worker) đã thử và ghi lại kết quả ngày 12/11/2026.

## 2. Đối chiếu với đề bài

| Yêu cầu | Điểm | Trạng thái | Bằng chứng |
|---|---|---|---|
| Dockerfile cho frontend và backend, image có tag phiên bản trên registry | 2 | Xong | `Dockerfile`, `k8s/frontend/Dockerfile`; `trung296/qlsv-backend`, `trung296/qlsv-frontend`, tag `v1`, `v2` |
| Cluster 1 control plane + 2 worker bằng kubeadm, có CNI, 3 node Ready | 2 | Xong | `kubectl get nodes`: cp, worker1, worker2 Ready (v1.35.10, Flannel) |
| Namespace riêng; Deployment ≥ 2 replicas rải 2 worker; Service; YAML + `kubectl apply` | 2,5 | Xong | Namespace `qlsv`; frontend, backend mỗi worker 1 Pod; NodePort 30080; `k8s/*.yaml` |
| Scale 4; cập nhật rồi rollback; tắt 1 worker và giải thích | 1,5 | Xong | `docs/kubernetes-test-results.md`, mục 4–7 |
| Báo cáo PDF, demo, trả lời câu hỏi | 2 | **Chưa xong** | Xem mục 4 |

## 3. Đã làm

**Docker**
- Backend: dùng `Dockerfile` hiện có (build 2 tầng, chạy bằng user thường), thêm `ARG APP_VERSION`.
- Frontend: `k8s/frontend/Dockerfile`, nginx phục vụ các trang tĩnh trong `src/main/resources/static`
  và chuyển `/api` sang backend. Không sửa mã giao diện.
- Backend thêm `GET /info` (phiên bản, tên Pod, tên node) bằng cấu hình Actuator, không sửa mã Java.
- 4 image trên Docker Hub: `qlsv-backend:v1`, `:v2`, `qlsv-frontend:v1`, `:v2`.

**Cluster**
- 3 máy EC2 `c7i-flex.large` (2 vCPU, 4 GB), Ubuntu 24.04, region Sydney, Security Group `k8s-midterm-sg`.
- Cài containerd, kubeadm, kubelet, kubectl v1.35 bằng `scripts/k8s/node-setup.sh`.
- `kubeadm init` trên cp, cài Flannel, `kubeadm join` trên 2 worker.

**Triển khai**
- 7 file YAML: Namespace, Secret (sinh ngẫu nhiên, không commit), ConfigMap, dữ liệu demo, MySQL, MinIO, backend, frontend.
- Frontend và backend: 2 replicas, `topologySpreadConstraints` ép chia đều 2 worker, có readiness và liveness probe.
- Service: frontend NodePort 30080; backend, mysql, minio ClusterIP.
- Kiểm tra từ ngoài cluster: trang đăng nhập 200 qua IP của cả 3 node; đăng nhập `admin` và lấy danh sách sinh viên được;
  request luân phiên giữa 2 Pod backend trên 2 worker.

**Vận hành** (chi tiết và mốc thời gian ở `kubernetes-test-results.md`)
- Scale frontend lên 4: mỗi worker 2 Pod.
- Cập nhật frontend lên `v2` rồi `rollout undo` về `v1`.
- Tắt worker2: khoảng 1 phút đầu có request lỗi, sau đó toàn bộ request thành công; Pod được tạo lại trên worker1.
- Bật lại worker2: node về Ready; chia lại Pod bằng `rollout restart`.

## 4. Chưa làm và giới hạn

**Sản phẩm nộp còn thiếu**
- Báo cáo PDF (tối đa 10 trang): sơ đồ cluster, các bước, ảnh chụp kết quả, khó khăn.
- Slide và kịch bản thuyết trình 8–10 phút.
- Ảnh chụp màn hình các bước (hiện chỉ có kết quả dạng chữ).

**Chưa kiểm chứng**
- Chưa chạy lại bộ test (`mvn verify`) sau khi sửa `application.properties`.
- Cập nhật và rollback mới thử trên frontend; backend `v2` chưa triển khai lần nào.
- Scale mới thử trên frontend.
- Chưa kiểm thử tải.
- Chưa thử tạo lại cluster từ đầu lần thứ hai bằng tài liệu hướng dẫn.

**Giới hạn thiết kế**
- MySQL và MinIO chạy 1 bản, ghim vào worker1, dữ liệu trên đĩa của worker1 (hostPath). Mất worker1 là mất dịch vụ
  và có thể mất dữ liệu. Chưa dùng PersistentVolume, StatefulSet hay replication.
- `v2` chỉ khác `v1` ở nhãn phiên bản, không có tính năng mới.
- Truy cập bằng IP và cổng 30080; chưa có Ingress, tên miền, HTTPS. IP công khai đổi sau mỗi lần bật máy.
- Một control plane duy nhất: cp hỏng thì không điều khiển được cluster (Pod đang chạy vẫn chạy).
- Image MinIO dùng tag `latest` vì nhà phát hành chỉ cung cấp miễn phí tag này.
- `curl localhost:30080` chạy ngay trên máy cp bị treo; truy cập qua IP công khai bình thường. Chưa rõ nguyên nhân.

## 5. Khó khăn đã gặp

| Khó khăn | Nguyên nhân | Cách xử lý |
|---|---|---|
| Ứng dụng có MySQL, không nhân bản như web | Thành phần có trạng thái | Chạy 1 bản, ghim worker1, nêu rõ giới hạn |
| Pod có thể dồn về một worker sau rolling update | Ràng buộc chia đều tính chung cả Pod cũ và mới | Thêm `matchLabelKeys: pod-template-hash` |
| Node chết thì Pod mới không xếp lịch được | Ràng buộc chia đều vẫn tính node hỏng | `nodeTaintsPolicy: Honor` |
| Chờ 5 phút Pod mới được dời khỏi node chết | `tolerationSeconds` mặc định 300 | Đặt 30 giây |
| Hostname máy AWS bị đặt lại khi bật máy | cloud-init | `preserve_hostname: true` |
| `docker push` lỗi `TLS handshake timeout`, `anonymous token` | Chưa `docker login`, mạng chậm | Đăng nhập trong cmd rồi push lại |
| Pull Request xung đột | Nhánh tạo từ nhánh cũ đã squash-merge | Cherry-pick commit lên `cloud` mới nhất |

## 6. Trình bày với thầy (8–10 phút)

| Phút | Nội dung | Cho xem |
|---|---|---|
| 0–1 | Đề bài, ứng dụng QLSV, phân công | Slide |
| 1–2,5 | Docker: 2 Dockerfile, cách đặt tag | `Dockerfile`, `k8s/frontend/Dockerfile`, trang Docker Hub có `v1`, `v2` |
| 2,5–4 | Cluster: 3 máy, các bước kubeadm, CNI | Sơ đồ; EC2 Console 3 máy; `kubectl get nodes -o wide` |
| 4–5,5 | Triển khai: YAML, Namespace, Deployment, Service | `k8s/06-backend.yaml`; `kubectl -n qlsv get deploy,svc,pods -o wide`; web chạy trên điện thoại |
| 5,5–8,5 | Vận hành: 4 thao tác | Demo trực tiếp, xem dưới |
| 8,5–10 | Khó khăn, giới hạn, hỏi đáp | Slide |

**Lệnh demo** (chạy trên cp; mở sẵn trang `http://<IP>:30080/cluster.html` bên cạnh):
```bash
kubectl get nodes -o wide
kubectl -n qlsv get pods -o wide

kubectl -n qlsv scale deploy/frontend --replicas=4
kubectl -n qlsv get pods -l app=frontend -o wide            # 2 Pod mỗi worker

kubectl -n qlsv set image deploy/frontend frontend=trung296/qlsv-frontend:v2
kubectl -n qlsv rollout status deploy/frontend              # nhãn trên trang đổi sang v2, màu xanh lá
kubectl -n qlsv rollout history deploy/frontend
kubectl -n qlsv rollout undo deploy/frontend                # nhãn về v1, màu xanh dương

# Tắt worker2 trên EC2 Console (Instance state > Stop), rồi:
kubectl get nodes -w                                         # khoảng 40 giây sau: worker2 NotReady
kubectl -n qlsv get pods -o wide                             # Pod mới xuất hiện trên worker1
```

**Lưu ý khi demo**
- Chỉ tắt **worker2**. Tắt worker1 thì mất MySQL và ứng dụng ngừng.
- Truy cập web bằng IP của **worker1 hoặc cp** trong lúc tắt worker2.
- Tắt worker mất khoảng 2 phút mới thấy đủ kết quả. Nên làm bước này trước rồi quay lại giải thích, hoặc quay video dự phòng.
- Bật cluster trước 15 phút (`./scripts/k8s/aws-node.sh start`), lấy IP mới bằng `aws-node.sh status`,
  kiểm tra `kubectl get nodes` và chạy `kubectl -n qlsv rollout restart deploy/backend deploy/frontend` nếu Pod lệch về một worker.
- Chuẩn bị ảnh chụp hoặc video cho từng bước, phòng khi mạng hoặc AWS trục trặc.

## 7. Câu hỏi thầy có thể hỏi

| Câu hỏi | Ý trả lời |
|---|---|
| Vì sao MySQL chỉ 1 replica? | Có trạng thái; 2 Pod độc lập là 2 database khác nhau. Nhân bản đúng cần replication hoặc dịch vụ ngoài (RDS). Nhóm chọn 1 bản và nêu rõ đây là điểm lỗi đơn. |
| Làm sao Pod rải đều 2 worker? | `topologySpreadConstraints` theo `kubernetes.io/hostname`, `maxSkew: 1`, `DoNotSchedule`. |
| Tắt worker thì chuyện gì xảy ra? | Kubelet ngừng heartbeat, khoảng 40 giây sau node NotReady, Pod bị loại khỏi Service, sau `tolerationSeconds` Pod bị xóa và Deployment tạo Pod mới ở node còn lại. |
| Vì sao Pod cũ kẹt ở Terminating? | Control plane không liên lạc được kubelet của node đã tắt để xác nhận Pod đã dừng. |
| Rolling update có gián đoạn không? | `maxUnavailable: 0`, `maxSurge: 1`: Pod mới Ready rồi mới bỏ Pod cũ. Lúc thử có 1 request không phản hồi ngay sau rollback. |
| Bật lại worker, Pod có tự quay về không? | Không. Scheduler chỉ quyết định lúc tạo Pod. Dùng `rollout restart` để chia lại. |
| NodePort khác ClusterIP, Ingress thế nào? | ClusterIP chỉ trong cluster; NodePort mở cổng trên mọi node; Ingress định tuyến HTTP theo tên miền, cần thêm controller. |
| CNI để làm gì? | Cấp IP cho Pod và nối mạng Pod giữa các node. Chưa cài thì node ở NotReady. |
| Vì sao không dùng tag `latest`? | Không biết đang chạy bản nào, không rollback chính xác được, node có thể dùng bản cache cũ. |
| Mật khẩu để ở đâu? | Secret `qlsv-secret`, sinh ngẫu nhiên bằng script, không commit. Secret chỉ mã hóa base64, chưa mã hóa khi lưu. |

## 8. Tài nguyên và chi phí

| Tài nguyên | Chi tiết |
|---|---|
| EC2 | `k8s-cp`, `k8s-worker1`, `k8s-worker2`, loại `c7i-flex.large`, ổ gp3 20 GB mỗi máy |
| Security Group | `k8s-midterm-sg` |
| Docker Hub | `trung296/qlsv-backend`, `trung296/qlsv-frontend` |

Khi bật: 3 × 0,11072 = khoảng 0,33 USD/giờ, cộng IPv4 công khai 3 × 0,005 USD/giờ.
Khi tắt: chỉ còn ổ đĩa, 60 GB × 0,096 = khoảng 5,76 USD/tháng. Giá Sydney tra ngày 12/11/2026.
Sau khi chấm: Terminate 3 máy và xóa Security Group.
