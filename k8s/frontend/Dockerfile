# Frontend của QLSV: nginx phục vụ các trang tĩnh, chuyển /api sang backend.
# Build từ THƯ MỤC GỐC của repo:
#   docker build -f k8s/frontend/Dockerfile --build-arg APP_VERSION=v1 -t <user>/qlsv-frontend:v1 .
FROM nginx:1.28-alpine

ARG APP_VERSION=dev
ENV APP_VERSION=${APP_VERSION} \
    API_HOST=backend \
    API_PORT=8082 \
    POD_NAME=local \
    NODE_NAME=local

# Image nginx tự thay ${...} trong file .template bằng biến môi trường khi container khởi động
COPY k8s/frontend/default.conf.template /etc/nginx/templates/default.conf.template
COPY src/main/resources/static/ /usr/share/nginx/html/
COPY k8s/frontend/cluster.html k8s/frontend/version-badge.js /usr/share/nginx/html/

EXPOSE 8080
