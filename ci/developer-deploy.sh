#!/usr/bin/env bash
set -Eeuo pipefail

# Deploy every YAS application into one disposable developer namespace.  Images
# use :main by default; a non-main branch resolves to that branch's HEAD SHA-12.

: "${DOCKERHUB_NAMESPACE:?Set DOCKERHUB_NAMESPACE to the Docker Hub account name}"
: "${DEPLOY_NAMESPACE:?Set DEPLOY_NAMESPACE (for example developer)}"

IMAGE_PULL_SECRET="${IMAGE_PULL_SECRET:-}"
ACCESS_HOST="${ACCESS_HOST:-developer.yas.local}"
REMOTE_URL="${REMOTE_URL:-$(git config --get remote.origin.url)}"

if [[ ! "$DEPLOY_NAMESPACE" =~ ^[a-z0-9]([-a-z0-9]*[a-z0-9])?$ ]]; then
  echo "DEPLOY_NAMESPACE must be a valid Kubernetes namespace: $DEPLOY_NAMESPACE" >&2
  exit 2
fi

if [[ -z "$REMOTE_URL" ]]; then
  echo "Unable to determine the Git remote; set REMOTE_URL explicitly." >&2
  exit 2
fi

resolve_image_tag() {
  local branch="$1"
  local sha

  if [[ "$branch" == "main" ]]; then
    printf '%s\n' main
    return
  fi

  if [[ ! "$branch" =~ ^[A-Za-z0-9][A-Za-z0-9._/-]*$ ]] || [[ "$branch" == *".."* ]]; then
    echo "Invalid branch name: $branch" >&2
    return 2
  fi

  sha="$(git ls-remote "$REMOTE_URL" "refs/heads/$branch" | awk 'NR == 1 { print $1 }')"
  if [[ ! "$sha" =~ ^[0-9a-f]{40}$ ]]; then
    echo "Branch '$branch' was not found on origin. Push it and wait for the Docker Hub CI workflow first." >&2
    return 1
  fi
  printf '%s\n' "${sha:0:12}"
}

deploy_chart() {
  local release="$1"
  local value_root="$2"
  local image_name="$3"
  local branch_variable="$4"
  local branch="${!branch_variable:-main}"
  local image_tag
  local -a values

  image_tag="$(resolve_image_tag "$branch")"
  echo "Deploying $release from branch '$branch' with image tag '$image_tag'"

  helm dependency build "k8s/charts/$release"
  values=(
    --set-string "$value_root.image.repository=docker.io/$DOCKERHUB_NAMESPACE/$image_name"
    --set-string "$value_root.image.tag=$image_tag"
    --set "$value_root.service.type=NodePort"
  )

  if [[ "$value_root" == "backend" ]]; then
    # Prometheus is intentionally outside this project requirement and the CRD
    # is normally absent on the developer cluster.
    values+=(--set backend.serviceMonitor.enabled=false)
  fi
  if [[ -n "$IMAGE_PULL_SECRET" ]]; then
    values+=(--set-string "$value_root.imagePullSecrets[0].name=$IMAGE_PULL_SECRET")
  fi

  helm upgrade --install "$release" "k8s/charts/$release" \
    --namespace "$DEPLOY_NAMESPACE" --create-namespace --wait --timeout 10m \
    "${values[@]}"
}

helm repo add stakater https://stakater.github.io/stakater-charts --force-update
helm repo update stakater
helm dependency build k8s/charts/yas-configuration
helm upgrade --install yas-configuration k8s/charts/yas-configuration \
  --namespace "$DEPLOY_NAMESPACE" --create-namespace

deploy_chart backoffice-ui ui yas-backoffice BACKOFFICE_BRANCH
deploy_chart backoffice-bff backend yas-backoffice-bff BACKOFFICE_BFF_BRANCH
deploy_chart cart backend yas-cart CART_BRANCH
deploy_chart customer backend yas-customer CUSTOMER_BRANCH
deploy_chart inventory backend yas-inventory INVENTORY_BRANCH
deploy_chart location backend yas-location LOCATION_BRANCH
deploy_chart media backend yas-media MEDIA_BRANCH
deploy_chart order backend yas-order ORDER_BRANCH
deploy_chart payment backend yas-payment PAYMENT_BRANCH
deploy_chart payment-paypal backend yas-payment-paypal PAYMENT_PAYPAL_BRANCH
deploy_chart product backend yas-product PRODUCT_BRANCH
deploy_chart promotion backend yas-promotion PROMOTION_BRANCH
deploy_chart rating backend yas-rating RATING_BRANCH
deploy_chart recommendation backend yas-recommendation RECOMMENDATION_BRANCH
deploy_chart sampledata backend yas-sampledata SAMPLEDATA_BRANCH
deploy_chart search backend yas-search SEARCH_BRANCH
deploy_chart storefront-ui ui yas-storefront STOREFRONT_BRANCH
deploy_chart storefront-bff backend yas-storefront-bff STOREFRONT_BFF_BRANCH
deploy_chart tax backend yas-tax TAX_BRANCH
deploy_chart webhook backend yas-webhook WEBHOOK_BRANCH

# Swagger is part of the developer environment but is not a YAS image built by
# the branch CI workflow.
helm upgrade --install swagger-ui k8s/charts/swagger-ui \
  --namespace "$DEPLOY_NAMESPACE" --create-namespace --wait --timeout 5m \
  --set service.type=NodePort

node_ip="$(kubectl get nodes -o jsonpath='{.items[0].status.addresses[?(@.type=="InternalIP")].address}')"
if [[ -z "$node_ip" ]]; then
  node_ip="$(kubectl get nodes -o jsonpath='{.items[0].status.addresses[?(@.type=="ExternalIP")].address}')"
fi

access_file="${ACCESS_FILE:-developer-access.txt}"
{
  echo "Deployment namespace: $DEPLOY_NAMESPACE"
  echo "Add this line to the developer workstation /etc/hosts:"
  echo "$node_ip $ACCESS_HOST"
  echo
  echo "NodePort access URLs:"
  for service in backoffice-ui backoffice-bff cart customer inventory location media order payment payment-paypal product promotion rating recommendation sampledata search storefront-ui storefront-bff tax webhook swagger-ui; do
    node_port="$(kubectl get service "$service" -n "$DEPLOY_NAMESPACE" -o jsonpath='{.spec.ports[?(@.name=="http")].nodePort}')"
    printf '%-20s http://%s:%s\n' "$service" "$ACCESS_HOST" "$node_port"
  done
} | tee "$access_file"

kubectl get pods,services -n "$DEPLOY_NAMESPACE"
