#!/usr/bin/env bash
set -Eeuo pipefail

: "${DEPLOY_NAMESPACE:?Set DEPLOY_NAMESPACE to the developer namespace to remove}"

# Do not let a cleanup job accidentally remove shared dev/staging/production.
case "$DEPLOY_NAMESPACE" in
  developer|developer-*) ;;
  *)
    echo "Refusing to delete '$DEPLOY_NAMESPACE'. Cleanup is restricted to developer or developer-* namespaces." >&2
    exit 2
    ;;
esac

kubectl delete namespace "$DEPLOY_NAMESPACE" --ignore-not-found --wait=true
