#!/usr/bin/env bash
set -euo pipefail

usage() {
  echo "Usage: $0 <dev|staging> <dockerhub-namespace> <image-tag>" >&2
}

if [ "$#" -ne 3 ]; then
  usage
  exit 2
fi

env_name="$1"
dockerhub_namespace="$2"
image_tag="$3"
values_file="k8s/gitops/envs/${env_name}/values/images.yaml"

if [ "$env_name" != "dev" ] && [ "$env_name" != "staging" ]; then
  usage
  exit 2
fi

run_yq() {
  if command -v yq >/dev/null 2>&1; then
    yq "$@"
  elif command -v docker >/dev/null 2>&1 && [ -f /.dockerenv ]; then
    docker run --rm --volumes-from "$HOSTNAME" -u "$(id -u):$(id -g)" -w "$PWD" mikefarah/yq:4 "$@"
  elif command -v docker >/dev/null 2>&1; then
    docker run --rm -v "$PWD:/workdir" -u "$(id -u):$(id -g)" -w /workdir mikefarah/yq:4 "$@"
  else
    echo "Either yq or docker is required to update ${values_file}" >&2
    exit 1
  fi
}

update_backend() {
  chart="$1"
  image="$2"
  run_yq -i ".\"${chart}\".image.repository = \"docker.io/${dockerhub_namespace}/${image}\"" "$values_file"
  run_yq -i ".\"${chart}\".image.tag = \"${image_tag}\"" "$values_file"
}

update_ui() {
  chart="$1"
  image="$2"
  run_yq -i ".\"${chart}\".image.repository = \"docker.io/${dockerhub_namespace}/${image}\"" "$values_file"
  run_yq -i ".\"${chart}\".image.tag = \"${image_tag}\"" "$values_file"
}

update_backend backoffice-bff yas-backoffice-bff
update_ui backoffice-ui yas-backoffice
update_backend cart yas-cart
update_backend customer yas-customer
update_backend inventory yas-inventory
update_backend location yas-location
update_backend media yas-media
update_backend order yas-order
update_backend payment yas-payment
update_backend payment-paypal yas-payment-paypal
update_backend product yas-product
update_backend promotion yas-promotion
update_backend rating yas-rating
update_backend recommendation yas-recommendation
update_backend sampledata yas-sampledata
update_backend search yas-search
update_backend storefront-bff yas-storefront-bff
update_ui storefront-ui yas-storefront
update_backend tax yas-tax
update_backend webhook yas-webhook

echo "Updated ${values_file} to docker.io/${dockerhub_namespace}/yas-*: ${image_tag}"
