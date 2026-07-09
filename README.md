### Deploy CRD
```YAML
apiVersion: argoproj.io/v1alpha1
kind: Application
metadata:
  name: yas-crds
  finalizers:
    - resources-finalizer.argocd.argoproj.io
  namespace: argocd
spec:
  destination:
    namespace: yas-crds
    server: https://kubernetes.default.svc
  source:
    path: crds
    repoURL: https://github.com/Thang2005HCMUS/Yas.git
    targetRevision: deploy-observability-infra
  sources: []
  project: default
  syncPolicy:
    automated:
      prune: true
      selfHeal: true
      enabled: true
```
### Deploy Infra

```YAML
apiVersion: argoproj.io/v1alpha1
kind: Application
metadata:
  name: yas-Infra
  finalizers:
    - resources-finalizer.argocd.argoproj.io
  namespace: argocd
spec:
  destination:
    namespace: yas-infra
    server: https://kubernetes.default.svc
  source:
    path: deploy-infra
    repoURL: https://github.com/Thang2005HCMUS/Yas.git
    targetRevision: deploy-observability-infra
  sources: []
  project: default
  syncPolicy:
    automated:
      prune: true
      selfHeal: true
      enabled: true
```
### Deploy Observability
```YAML
apiVersion: argoproj.io/v1alpha1
kind: Application
metadata:
  name: yas-observability
  finalizers:
    - resources-finalizer.argocd.argoproj.io
  namespace: argocd
spec:
  destination:
    namespace: yas-observability
    server: https://kubernetes.default.svc
  source:
    path: observability-applications
    repoURL: https://github.com/Thang2005HCMUS/Yas.git
    targetRevision: deploy-observability-infra
  sources: []
  project: default
  syncPolicy:
    automated:
      prune: true
      selfHeal: true
      enabled: true
```

### Deploy cluster-configuration 
```YAML
apiVersion: argoproj.io/v1alpha1
kind: Application
metadata:
  name: yas-configuration
  finalizers:
    - resources-finalizer.argocd.argoproj.io
  namespace: argocd
spec:
  destination:
    namespace: yas-configuration
    server: https://kubernetes.default.svc
  source:
    path: application-config
    repoURL: https://github.com/Thang2005HCMUS/Yas.git
    targetRevision: deploy-observability-infra
  sources: []
  project: default
  syncPolicy:
    automated:
      prune: true
      selfHeal: true
      enabled: true
```
### Deploy microservices

```YAML
apiVersion: argoproj.io/v1alpha1
kind: Application
metadata:
  name: yas-microservices
  namespace: argocd
  finalizers:
    - resources-finalizer.argocd.argoproj.io
spec:
  project: default
  source:
    repoURL: https://github.com/Thang2005HCMUS/Yas.git
    targetRevision: deploy-observability-infra  # Hoặc thay bằng branch chứa file của bạn (ví dụ: master/main)
    path: yas-services    # Thư mục chứa file ApplicationSet bạn đã lưu
  destination:
    server: https://kubernetes.default.svc
    namespace: argocd     # ApplicationSet bắt buộc phải nằm ở namespace argocd để hoạt động
  syncPolicy:
    automated:
      prune: true
      selfHeal: true
    syncOptions:
      - CreateNamespace=true
```


