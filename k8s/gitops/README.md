# ArgoCD GitOps for YAS dev and staging

This folder implements the advanced assignment requirement: ArgoCD handles both
`dev` and `staging` deployments while Jenkins only builds images, pushes them to
Docker Hub, and updates the GitOps image tags.

## One-time setup

```bash
kubectl create namespace argocd --dry-run=client -o yaml | kubectl apply -f -
kubectl apply --server-side --force-conflicts -n argocd -f https://raw.githubusercontent.com/argoproj/argo-cd/stable/manifests/install.yaml

kubectl create namespace dev --dry-run=client -o yaml | kubectl apply -f -
kubectl create namespace staging --dry-run=client -o yaml | kubectl apply -f -

# Commit this GitOps folder on a feature branch and open a pull request first.
# For a pre-merge demo, temporarily change `targetRevision` in the two
# Application manifests from `main` to the feature branch name before applying.
# After the PR is merged, keep `targetRevision: main`.
kubectl apply -f k8s/gitops/applications/dev.yaml
kubectl apply -f k8s/gitops/applications/staging.yaml
```

Open the ArgoCD UI:

```bash
kubectl -n argocd port-forward svc/argocd-server 8080:443
kubectl -n argocd get secret argocd-initial-admin-secret -o jsonpath='{.data.password}' | base64 -d; echo
```

## Jenkins flow

Use `ci/Jenkinsfile.argocd`.

- Commit to `main`: Jenkins builds `docker.io/<namespace>/yas-*:SHORT_SHA`,
  updates `k8s/gitops/envs/dev/values/images.yaml`, and pushes the GitOps commit.
- Git tag `vX.Y.Z`: Jenkins builds `docker.io/<namespace>/yas-*:vX.Y.Z`,
  updates `k8s/gitops/envs/staging/values/images.yaml`, and pushes the GitOps commit.
- ArgoCD watches `main` and syncs the new values into the matching namespace.

Required Jenkins credentials:

- `dockerhub-credentials`: username/password or token for Docker Hub.
- `github-credentials`: GitHub username/token with permission to push the PR branch.
  Git checkout should use the same credential.
  In a protected-branch workflow, the Jenkinsfile pushes image-tag updates back
  to the current feature branch, then the team merges through a pull request.

Required Jenkins tools:

- Docker CLI
- `yq`
- Docker access from the Jenkins agent. The Java build runs in
  `maven:3.9.11-eclipse-temurin-25` because the current root `pom.xml` compiles
  with Java 25.
- Node build support is handled inside the UI Dockerfiles.
- `yq` is optional. If Jenkins does not have `yq`, `ci/update-gitops-images.sh`
  runs `mikefarah/yq:4` with Docker.

## Evidence commands for screenshots

```bash
kubectl get applications -n argocd
kubectl get pods -n dev -o wide
kubectl get pods -n staging -o wide
kubectl get deploy -n dev -o jsonpath='{range .items[*]}{.metadata.name}{"  "}{.spec.template.spec.containers[0].image}{"\n"}{end}'
kubectl get deploy -n staging -o jsonpath='{range .items[*]}{.metadata.name}{"  "}{.spec.template.spec.containers[0].image}{"\n"}{end}'
```

Report image asset:

- `docs/images/argocd-dev-staging-flow.svg`
