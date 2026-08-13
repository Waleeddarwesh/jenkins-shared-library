<div align="center">

# ⚙️ Jenkins Shared Library

**A highly reusable, parameterized repository of Groovy components and standardized pipeline stages for enterprise CI/CD workflows.**

</div>

---

## 📌 Overview

This repository serves as a **Jenkins Shared Library**, enabling developers and DevOps engineers to write modular, DRY (Don't Repeat Yourself) Jenkins pipelines. 

In this updated version, the library abstracts a complete **9-stage CI/CD pipeline** into a single orchestrator step: `microservicePipeline`. It includes integrated security scanning with Trivy, quality gating with SonarQube, Docker building with OCI labels, ECR pushing via temporary AWS credentials, and a pure GitOps handoff using Kustomize and ArgoCD.

---

## 📂 Repository Structure

```text
jenkins-shared-library/
├── vars/
│   ├── microservicePipeline.groovy   # The main orchestrator — 9 stages
│   ├── runUnitTests.groovy           # Language-aware unit testing
│   ├── sonarQubeScan.groovy          # Code analysis + blocking quality gate
│   ├── dockerBuildImage.groovy       # Docker build + OCI provenance labels
│   ├── trivyScan.groovy              # Security gate (fails on fixable CRITICAL)
│   ├── ecrPush.groovy                # ECR login, push, and verify
│   ├── updateManifests.groovy        # Kustomize image update (no regex/sed)
│   └── pushManifests.groovy          # Git commit + push — CI→CD handoff
└── README.md
```

---

## 🚀 How to Use (Quick Start)

### 1. Import the Library

To use this shared library in your Jenkins pipeline, import it at the very top of your `Jenkinsfile` using the `@Library` annotation:

```groovy
@Library('shared-library') _
```
*(Note: The trailing underscore `_` is required).*

### 2. Pipeline Example

Because all logic is abstracted into `vars/`, your application `Jenkinsfile` only needs to provide configuration. The `microservicePipeline` step will automatically execute the entire 9-stage process.

```groovy
#!/usr/bin/env groovy

@Library('shared-library') _

microservicePipeline(
    serviceName: 'ivolve-frontend',
    sourceDir: 'src/frontend',
    language: 'node', // 'node', 'python', or 'java'

    ecrRegistry: '991216470475.dkr.ecr.us-east-1.amazonaws.com',
    awsRegion: 'us-east-1',

    gitRepo: 'github.com/WaleedDarwesh/CloudDevOpsProject.git',
    gitBranch: 'main',
    manifestDir: '04-Kubernetes/manifests',

    runSonar: true,
    failOnCritical: true
)
```

---

## 🛠 Available Functions (API)

While `microservicePipeline` is the main entry point, it orchestrates the following individual functions which can also be used standalone:

### `microservicePipeline(Map config)`
The master pipeline orchestrator. Executes Checkout → Unit Tests → SonarQube → Build Image → Trivy Scan → Push to ECR → Clean Local Image → Update Manifests → Push to Git.

### `runUnitTests(String language, String sourceDir)`
Runs language-aware, containerized tests. Automatically detects the language (`node`, `python`, `java`) and runs the appropriate test commands inside an ephemeral Docker container.

### `sonarQubeScan(String projectKey, String sourceDir, String language, String version)`
Executes a SonarQube scan and utilizes `waitForQualityGate()` to block the pipeline if code quality standards are not met.

### `dockerBuildImage(String image, String context, String commit, String buildNumber)`
Builds the Docker image and automatically injects standard `org.opencontainers.image.*` provenance labels.

### `trivyScan(String image, boolean failOnCritical, String reportPrefix)`
A two-pass container security scanner. Pass 1 generates a full report. Pass 2 explicitly fails the pipeline if any **fixable CRITICAL** vulnerabilities are found, ensuring vulnerable images are blocked *before* they are pushed.

### `ecrPush(String image, String registry, String region)`
Authenticates with AWS ECR using an IAM instance profile (zero static credentials), pushes the image with 3 retries, and verifies the image digest.

### `updateManifests(String manifestDir, String serviceName, String newTag)`
Uses `kustomize edit set image` to safely update the deployment manifest for ArgoCD, avoiding the severe risks of using `sed` on YAML files.

### `pushManifests(String gitRepo, String gitBranch, String manifestDir)`
Commits the updated manifests with `[skip ci]` to prevent infinite build loops, and pushes to GitHub using `git pull --rebase` to elegantly handle concurrent pipeline races.

---

## 🔄 The GitOps Handoff

Unlike traditional CI/CD pipelines, this library **never** runs `kubectl apply`. 

Instead, at the end of the pipeline, Jenkins commits the new Docker image tag to the deployment repository. ArgoCD, watching that repository, will detect the new commit and automatically reconcile the cluster state. This ensures that **Git remains the single source of truth** for what is deployed in your cluster.
