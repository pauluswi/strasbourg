# Strasbourg Deployment on AWS (EKS)

This guide documents the baseline deployment path created for Phase 7.

## What is included

- `Dockerfile` for building the Quarkus app image.
- Raw Kubernetes manifests in `k8s/`.
- Helm chart in `helm/strasbourg`.
- GitHub Actions:
  - `.github/workflows/ci.yml`
  - `.github/workflows/cd-aws-eks.yml`

## 1. Prerequisites

- AWS account and EKS cluster.
- ECR repository for the app image.
- OIDC trust between GitHub Actions and AWS IAM role.
- Kubernetes metrics-server (if using HPA).

## 2. Required GitHub configuration

### Repository Secrets

- `AWS_DEPLOY_ROLE_ARN`
- `DB_USERNAME`
- `DB_PASSWORD`

### Repository Variables

- `AWS_REGION` (example: `ap-southeast-1`)
- `ECR_REPOSITORY` (example: `strasbourg`)
- `EKS_CLUSTER_NAME` (example: `strasbourg-eks`)
- `EKS_NAMESPACE` (example: `strasbourg`)
- `HELM_RELEASE_NAME` (example: `strasbourg`)

## 3. Local build and run (container)

```bash
docker build -t strasbourg:local .
docker run --rm -p 8080:8080 \
  -e QUARKUS_PROFILE=prod \
  -e DB_JDBC_URL=jdbc:postgresql://host.docker.internal:5432/strasbourg \
  -e DB_USERNAME=strasbourg \
  -e DB_PASSWORD=strasbourg \
  strasbourg:local
```

## 4. Kubernetes deployment options

### Option A — Raw manifests

```bash
kubectl apply -f k8s/namespace.yaml
kubectl -n strasbourg apply -f k8s/secret.example.yaml
kubectl -n strasbourg apply -f k8s/configmap.yaml
kubectl -n strasbourg apply -f k8s/deployment.yaml
kubectl -n strasbourg apply -f k8s/service.yaml
kubectl -n strasbourg apply -f k8s/ingress.yaml
kubectl -n strasbourg apply -f k8s/hpa.yaml
```

### Option B — Helm

```bash
helm upgrade --install strasbourg ./helm/strasbourg --namespace strasbourg --create-namespace
```

Set image repository/tag and DB values via `--set` or a values override file.

## 5. CI/CD behavior

- `ci.yml`: runs Maven tests on pull requests and pushes to `main`.
- `cd-aws-eks.yml`: on push to `main` (or manual dispatch):
  - runs tests,
  - builds Docker image,
  - pushes to ECR,
  - creates/updates DB secret in namespace,
  - deploys/updates release via Helm.

## 6. Notes

- Local/default app profile uses H2; container/Kubernetes deployment is configured for `prod` profile and PostgreSQL env vars.
- Kafka is still mocked in the current showcase runtime.

