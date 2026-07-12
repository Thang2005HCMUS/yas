# CI/CD cơ bản — developer build không dùng ArgoCD

Tài liệu này triển khai phần 6 điểm của đồ án bằng GitHub Actions + Jenkins + Helm. Nó độc lập với `ci/Jenkinsfile.argocd`: Jenkins ở đây deploy trực tiếp bằng Helm vào một namespace developer và xóa namespace khi test xong.

## 1. Những gì được tự động hóa

| Yêu cầu | Cách triển khai |
|---|---|
| Image mặc định | Mọi image deployable của YAS được push từ `main` với tag `main` và `latest`. |
| Image theo branch | Mỗi push lên branch kích hoạt `.github/workflows/dockerhub-branch-images.yaml`; image được tag bằng 12 ký tự đầu của commit SHA. |
| CD cho developer | Jenkins job `developer_build` nhận branch cho từng service. Mặc định tất cả là `main`; service được đổi branch sẽ deploy image SHA của HEAD branch đó. |
| Truy cập test | Tất cả YAS service được expose `NodePort`. Job ghi URL và dòng `/etc/hosts` vào `developer-access.txt`. |
| Xóa deployment | Jenkins job `developer_cleanup` xóa namespace `developer` hoặc `developer-*`. |

Ví dụ: developer có branch `dev_tax_service`. Sau khi push branch và workflow Docker Hub thành công, chạy `developer_build`, nhập `TAX_BRANCH=dev_tax_service`, giữ mọi parameter khác là `main`. Jenkins resolve HEAD SHA của branch đó, deploy `yas-tax:<sha12>`, và deploy mọi service khác bằng `yas-<service>:main`.

## 2. Prerequisites chỉ làm một lần

### Kubernetes cluster

Minikube được đề chấp nhận như một master/worker local cluster. Máy chạy cluster cần đủ tài nguyên cho dependencies của YAS:

```bash
minikube start --disk-size=40000mb --memory=16g
minikube addons enable ingress
```

Cài `kubectl`, Helm và cấu hình Jenkins agent có quyền dùng cùng kubeconfig/context với cluster. Jenkins agent cần `git`, `kubectl`, Helm và quyền tạo/xóa namespace `developer*`.

### Dependency của YAS

Trước lần deploy developer đầu tiên, triển khai các dependency mà application cần: PostgreSQL, Kafka, Elasticsearch, Keycloak, Redis và các operator tương ứng. Có sẵn script nền tại `k8s/deploy/setup-cluster.sh`; mặc định script **không** cài Observability. Chỉ đặt `INSTALL_OBSERVABILITY=true` khi nhóm chủ động muốn cài Grafana/Prometheus/Loki/Tempo.

Sau dependencies, chạy configuration chart một lần (hoặc để job deploy chạy theo tài liệu cluster hiện hành):

```bash
helm dependency build k8s/charts/yas-configuration
helm upgrade --install yas-configuration k8s/charts/yas-configuration \
  --namespace developer --create-namespace
```

> `developer_build` hiện deploy chart `yas-configuration` trước toàn bộ application. Dependency dùng chung nên nên được cài ngoài namespace developer để không bị cleanup job xóa.

## 3. GitHub Actions CI và Docker Hub

Tạo repository secrets:

- `DOCKERHUB_USERNAME`: Docker Hub account/namespace, ví dụ `thang2005hcmus`.
- `DOCKERHUB_TOKEN`: Docker Hub access token có quyền push.

Workflow mới build 20 image YAS deployable trong mỗi push nhằm đảm bảo branch có tag SHA cho service được developer chọn. Với push vào `main`, mỗi image có ba tag: `<sha12>`, `main`, `latest`.

Kiểm tra workflow thành công và Docker Hub trước khi chạy CD. Nếu chưa có image `<sha12>`, Kubernetes sẽ báo `ImagePullBackOff`.

## 4. Tạo hai Jenkins job

### Cách A — Job DSL seed job

1. Cài plugin **Job DSL** trong Jenkins.
2. Tạo seed job chạy file `ci/jenkins-jobs.groovy` từ SCM.
3. Tạo Jenkins credential `github-credentials` để clone repository. Nếu image Docker Hub private, tạo thêm credential username/password có ID `dockerhub-credentials`.
4. Chạy seed job. Jenkins tạo `developer_build` và `developer_cleanup`.

### Cách B — Tạo thủ công

Tạo hai loại job **Pipeline, Pipeline script from SCM**, trỏ repository YAS (branch `main`):

| Job name | Script path |
|---|---|
| `developer_build` | `ci/Jenkinsfile.developer-build` |
| `developer_cleanup` | `ci/Jenkinsfile.developer-cleanup` |

Chạy job một lần để Jenkins nạp các parameters. Cấu hình credentials khi checkout private repository.

## 5. Chạy demo cho developer

1. Developer push branch, ví dụ `dev_tax_service`.
2. Chờ workflow **Docker Hub branch images** thành công.
3. Mở Jenkins `developer_build` → **Build with Parameters**.
4. Giữ `DEPLOY_NAMESPACE=developer`; giữ tất cả `<SERVICE>_BRANCH=main`, chỉ đặt `TAX_BRANCH=dev_tax_service`.
5. Nếu Docker Hub repository private, bật `CREATE_IMAGE_PULL_SECRET`.
6. Cuối log/job artifact có `developer-access.txt`, ví dụ:

```text
Add this line to the developer workstation /etc/hosts:
192.168.49.2 developer.yas.local

tax                  http://developer.yas.local:30xxx
storefront-ui        http://developer.yas.local:30yyy
```

Developer thêm dòng hosts trên máy của họ, sau đó truy cập URL NodePort tương ứng. Có thể lấy lại port bằng:

```bash
kubectl get svc -n developer
kubectl get pods -n developer
```

Khi test xong, chạy `developer_cleanup` với cùng `DEPLOY_NAMESPACE`. Script chỉ cho phép xóa `developer` hoặc `developer-*`, không thể xóa `dev`, `staging` hay namespace chung do nhập nhầm.

## 6. Bằng chứng cần chụp trong báo cáo

1. Cluster Minikube/Kubernetes có node `Ready`.
2. GitHub Actions branch CI: commit SHA và Docker Hub push thành công.
3. Docker Hub: `yas-tax:<sha12>` và image `main`/`latest`.
4. Jenkins `developer_build`: `TAX_BRANCH=dev_tax_service`, các branch khác `main`.
5. Log Helm/kubectl: service trong namespace `developer` và image tag của `tax` là SHA branch, service khác là `main`.
6. `developer-access.txt`, `/etc/hosts` và trình duyệt/API truy cập được qua NodePort.
7. Jenkins `developer_cleanup` và namespace developer đã biến mất.

## 7. Giới hạn có chủ đích

- Workflow không deploy Grafana, Prometheus, Loki hay Tempo; đề ghi rõ Observability không bắt buộc cho phần này.
- `developer_build` direct-deploy bằng Helm, không ArgoCD, để đáp ứng đúng mục developer CD và cleanup của phần 6 điểm.
- ArgoCD Dev/Staging tiếp tục nằm ở `ci/Jenkinsfile.argocd` và không bị sửa bởi workflow này.
