# Triển Khai Service Mesh Cho YAS

Tài liệu này mô tả cách triển khai Istio Service Mesh cho một phần ứng dụng YAS trên Kubernetes. Phạm vi demo gồm các service `product`, `cart`, `order`, `tax` và một service phụ `yas-fault` dùng để kiểm thử retry khi backend trả lỗi `500`.

Các manifest trong thư mục này cấu hình:

- mTLS bắt buộc trong namespace `yas`.
- AuthorizationPolicy để giới hạn service/pod được phép gọi nhau.
- VirtualService để bật retry policy cho service thử lỗi `yas-fault`.
- Các pod curl dùng để kiểm thử trường hợp được phép và bị chặn.

## 1. Chuẩn Bị Cluster Và Istio

Khởi động Minikube và cài Istio:

```sh
minikube start --memory=16g --disk-size=40000mb
minikube addons enable ingress
kubectl config use-context minikube

ISTIO_TMP=/tmp/yas-istio-demo
mkdir -p "$ISTIO_TMP"
cd "$ISTIO_TMP"
curl -L https://istio.io/downloadIstio | ISTIO_VERSION=1.30.2 sh -
export PATH="$ISTIO_TMP/istio-1.30.2/bin:$PATH"

istioctl x precheck
istioctl install --set profile=demo -y
```

Cài Kiali và Prometheus để quan sát topology:

```sh
kubectl apply -f istio-1.30.2/samples/addons/prometheus.yaml
kubectl apply -f istio-1.30.2/samples/addons/kiali.yaml
kubectl rollout status deploy/kiali -n istio-system
```

Quay lại thư mục repo:

```sh
cd /home/jason/Projects/Uni/Year3Sem2/DevOps/yas
```

## 2. Triển Khai YAS Slice

Tạo namespace `yas` và bật Istio sidecar injection:

```sh
kubectl apply -f k8s/service-mesh/00-namespace.yaml
```

Cài PostgreSQL tối thiểu cho các service backend:

```sh
helm repo add postgres-operator-charts https://opensource.zalando.com/postgres-operator/charts/postgres-operator
helm repo update
helm upgrade --install postgres-operator postgres-operator-charts/postgres-operator \
  --namespace postgres --create-namespace --wait
kubectl wait --for=condition=Established crd/postgresqls.acid.zalan.do --timeout=180s
helm upgrade --install postgres k8s/deploy/postgres/postgresql \
  --namespace postgres --create-namespace --wait
```

Triển khai cấu hình chung của YAS:

```sh
helm dependency build k8s/charts/yas-configuration
helm upgrade --install yas-configuration k8s/charts/yas-configuration \
  --namespace yas --create-namespace --wait
```

Triển khai các service dùng trong demo:

```sh
for chart in product cart order tax; do
  helm dependency build "k8s/charts/$chart"
  helm upgrade --install "$chart" "k8s/charts/$chart" \
    --namespace yas --create-namespace --wait \
    --set backend.serviceMonitor.enabled=false
done
```

Các service trên dùng public image có sẵn trong Helm chart:

- `ghcr.io/nashtech-garage/yas-product:latest`
- `ghcr.io/nashtech-garage/yas-cart:latest`
- `ghcr.io/nashtech-garage/yas-order:latest`
- `ghcr.io/nashtech-garage/yas-tax:latest`

Nếu pod được tạo trước khi namespace có label injection, restart lại deployment:

```sh
kubectl rollout restart deploy/product deploy/cart deploy/order deploy/tax -n yas
kubectl rollout status deploy/product -n yas
kubectl rollout status deploy/cart -n yas
kubectl rollout status deploy/order -n yas
kubectl rollout status deploy/tax -n yas
```

## 3. Áp Dụng Cấu Hình Service Mesh

Áp dụng manifest mTLS, authorization, retry và các pod kiểm thử:

```sh
kubectl apply -f k8s/service-mesh/10-mtls.yaml
kubectl apply -f k8s/service-mesh/20-authz.yaml
kubectl apply -f k8s/service-mesh/30-retry.yaml
kubectl apply -f k8s/service-mesh/40-test-clients.yaml
kubectl apply -f k8s/service-mesh/50-non-mesh-client.yaml
```

Chờ các pod kiểm thử sẵn sàng:

```sh
kubectl wait --for=condition=Ready pod/mesh-curl-allowed -n yas --timeout=180s
kubectl wait --for=condition=Ready pod/mesh-curl-denied -n yas --timeout=180s
kubectl wait --for=condition=Ready pod/non-mesh-curl -n mesh-negative-test --timeout=180s
kubectl rollout status deploy/yas-fault -n yas
```

Kiểm tra các resource Istio đã được tạo:

```sh
kubectl get peerauthentication,destinationrule,authorizationpolicy,virtualservice -n yas
```

## 4. Kiểm Tra mTLS

Kiểm tra các pod trong namespace `yas` đã được inject sidecar:

```sh
kubectl get pods -n yas
```

Kiểm tra certificate của workload trong mesh:

```sh
istioctl proxy-config secret mesh-curl-allowed.yas
```

Kết quả cần có các secret/certificate như `default` và `ROOTCA` ở trạng thái `ACTIVE`.

Kiểm tra outbound cluster từ pod test tới `product` có cấu hình TLS:

```sh
istioctl proxy-config cluster mesh-curl-allowed.yas \
  --fqdn product.yas.svc.cluster.local -o json | grep -E '"transportSocket"|"sni"|"tlsCertificateSdsSecretConfigs"'
```

Kiểm tra pod không thuộc mesh bị chặn khi gọi service trong namespace `yas`:

```sh
kubectl exec -n mesh-negative-test non-mesh-curl -c curl -- \
  curl -sS -i --max-time 5 http://product.yas/product/actuator/health/readiness
```

Request từ pod này sẽ fail, timeout hoặc bị reset vì namespace `yas` đang bật STRICT mTLS.

## 5. Kiểm Tra AuthorizationPolicy

Pod `mesh-curl-allowed` được phép gọi `product`:

```sh
kubectl exec -n yas mesh-curl-allowed -c curl -- \
  curl -sS -o /tmp/product.out -w "HTTP %{http_code}\n" \
  http://product.yas/product/storefront/products/featured
kubectl exec -n yas mesh-curl-allowed -c curl -- cat /tmp/product.out
```

Pod `mesh-curl-denied` không nằm trong allow-list nên bị Envoy chặn:

```sh
kubectl exec -n yas mesh-curl-denied -c curl -- \
  curl -sS -i http://product.yas/product/storefront/products/featured
```

Kết quả mong đợi là `HTTP/1.1 403 Forbidden` hoặc nội dung có `RBAC: access denied`.

## 6. Kiểm Tra Retry Policy

Service `yas-fault` dùng image `kennethreitz/httpbin`. Endpoint `/status/500` luôn trả HTTP 500 để tạo lỗi có kiểm soát. VirtualService `yas-fault-retry` cấu hình retry 3 lần cho nhóm lỗi `5xx`.

Xem retry counter trước khi gọi endpoint lỗi:

```sh
kubectl exec -n yas mesh-curl-allowed -c istio-proxy -- \
  pilot-agent request GET stats | grep upstream_rq_retry
```

Gọi endpoint trả lỗi 500:

```sh
kubectl exec -n yas mesh-curl-allowed -c curl -- \
  curl -sS -o /dev/null -w "HTTP %{http_code}\n" http://yas-fault.yas/status/500
```

Xem lại retry counter:

```sh
kubectl exec -n yas mesh-curl-allowed -c istio-proxy -- \
  pilot-agent request GET stats | grep upstream_rq_retry
```

Nếu policy hoạt động, counter `upstream_rq_retry` hoặc counter theo cluster `outbound|80||yas-fault.yas.svc.cluster.local` sẽ tăng sau request.

## 7. Quan Sát Topology Trên Kiali

Tạo traffic để Kiali hiển thị các cạnh giữa client và service:

```sh
for i in $(seq 1 30); do
  kubectl exec -n yas mesh-curl-allowed -c curl -- curl -sS -o /dev/null http://product.yas/product/storefront/products/featured
  kubectl exec -n yas mesh-curl-allowed -c curl -- curl -sS -o /dev/null http://order.yas/order/storefront/orders/completed
  kubectl exec -n yas mesh-curl-allowed -c curl -- curl -sS -o /dev/null http://cart.yas/cart/actuator/health/readiness
  kubectl exec -n yas mesh-curl-allowed -c curl -- curl -sS -o /dev/null http://tax.yas/tax/actuator/health/readiness
  kubectl exec -n yas mesh-curl-allowed -c curl -- curl -sS -o /dev/null http://yas-fault.yas/status/500 || true
done
```

Mở Kiali:

```sh
istioctl dashboard kiali
```

Trong giao diện Kiali, chọn:

- Namespace: `yas`
- View: `Traffic Graph`
- Graph type: `App graph` hoặc `Workload graph`
- Time range: `Last 5m`

Topology cần thể hiện các node chính như `mesh-curl-allowed`, `product`, `cart`, `order`, `tax` và `yas-fault`. Các cạnh có biểu tượng khóa thể hiện traffic đang đi qua mTLS. Màu xanh biểu thị request thành công; màu cam/đỏ biểu thị request lỗi, ví dụ `yas-fault` trả 500 để kiểm thử retry.
