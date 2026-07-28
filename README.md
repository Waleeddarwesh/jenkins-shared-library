<div align="center">

# ⚙️ Jenkins Shared Library

**A highly reusable, parameterized repository of Groovy components and standardized pipeline stages for enterprise CI/CD workflows.**

</div>

---

## 📌 Overview

This repository serves as a **Jenkins Shared Library**, enabling developers and DevOps engineers to write modular, DRY (Don't Repeat Yourself) Jenkins pipelines. By abstracting complex pipeline logic into standardized, version-controlled Groovy scripts, multiple projects can reuse the same deployment patterns.

This library has been specifically designed to be **highly dynamic**. All functions accept optional named arguments (via a `Map`), allowing them to be customized for any project structure or authentication mechanism.

The library supports **two deployment strategies**:

- **Traditional CI/CD**: Jenkins builds the application, creates Docker images, and deploys directly to Kubernetes using `kubectl apply`.
- **GitOps with ArgoCD**: Jenkins builds the application, creates Docker images, updates the Kubernetes manifest in Git, and pushes the change. ArgoCD detects the commit and automatically synchronizes the cluster.

---

## 📂 Repository Structure

```text
jenkins-shared-library/
│
├── vars/
│   ├── buildApp.groovy            # Compiles & packages Java applications via Maven
│   ├── buildImage.groovy          # Builds and pushes Docker images to Docker Hub
│   ├── deployOnK8s.groovy         # Deploys applications directly to Kubernetes (Traditional CI/CD)
│   └── updateGitOpsRepo.groovy    # Updates manifests & pushes to Git for ArgoCD (GitOps CD)
│
└── README.md
```

---

## 🚀 How to Use (Quick Start)

### 1. Import the Library

To use this shared library in your Jenkins pipeline, import it at the very top of your `Jenkinsfile` using the `@Library` annotation:

```groovy
@Library('shared-library') _
```

### 2. Pipeline Examples

#### Traditional CI/CD Pipeline (Direct Kubernetes Deployment)

```groovy
@Library('shared-library') _

pipeline {
    agent {
        label 'devops-agent'
    }

    stages {
        stage('Build Application') {
            steps {
                buildApp(workingDir: '<PATH_TO_APPLICATION>')
            }
        }

        stage('Build & Push Docker Image') {
            steps {
                buildImage(
                    workingDir: '<PATH_TO_APPLICATION>',
                    imageName: '<YOUR_DOCKERHUB_USERNAME>/<IMAGE_NAME>',
                    dockerCredentialsId: 'dockerhub'
                )
            }
        }

        stage('Deploy to Kubernetes') {
            steps {
                deployOnK8s(
                    workingDir: '<PATH_TO_APPLICATION>',
                    manifestPath: 'manifests/deployment.yaml',
                    kubeconfigCredentialsId: 'kubeconfig'
                )
            }
        }
    }

    post {
        always {
            echo 'Pipeline execution completed.'
            cleanWs()
        }

        success {
            echo 'Application deployed successfully.'
        }

        failure {
            echo 'Pipeline execution failed.'
        }
    }
}
```

---

#### GitOps Pipeline (ArgoCD Deployment)

```groovy
@Library('shared-library') _

pipeline {
    agent {
        label 'devops-agent'
    }

    stages {
        stage('Build Application') {
            steps {
                buildApp(workingDir: '<PATH_TO_APPLICATION>')
            }
        }

        stage('Build & Push Docker Image') {
            steps {
                buildImage(
                    workingDir: '<PATH_TO_APPLICATION>',
                    imageName: '<YOUR_DOCKERHUB_USERNAME>/<IMAGE_NAME>',
                    dockerCredentialsId: 'dockerhub'
                )
            }
        }

        stage('Update GitOps Repository') {
            steps {
                updateGitOpsRepo(
                    workingDir: '<PATH_TO_APPLICATION>',
                    manifestPath: 'manifests/deployment.yaml',
                    imageName: '<YOUR_DOCKERHUB_USERNAME>/<IMAGE_NAME>',
                    githubCredentialsId: 'github-creds'
                )
            }
        }
    }

    post {
        always {
            echo 'Pipeline execution completed.'
            cleanWs()
        }

        success {
            echo 'GitOps repository updated successfully.'
            echo 'ArgoCD will automatically synchronize the new application version to Kubernetes.'
        }

        failure {
            echo 'Pipeline execution failed.'
        }
    }
}
```

> ⚠️ **Key Difference:** In a GitOps pipeline, Jenkins **never** runs `kubectl apply`. Instead, Jenkins updates the manifest in Git, and ArgoCD handles the actual Kubernetes deployment.

---

## 🛠 Available Functions

### `buildApp(Map config = [:])`
Executes a Maven build to compile and package a Java application while skipping tests for faster deployment cycles.
* **`workingDir`** *(Optional)*: The directory where the `pom.xml` is located. Defaults to `.` (workspace root).

**Under the hood:** `mvn clean package -DskipTests`

---

### `buildImage(Map config = [:])`
Builds a Docker image and dynamically tags it using the Jenkins `$BUILD_NUMBER` variable. If credentials are provided, it automatically pushes the image to Docker Hub.
* **`workingDir`** *(Optional)*: The directory containing the `Dockerfile`. Defaults to `.`.
* **`imageName`** *(Optional)*: The base name of the image. Defaults to `myapp`.
* **`imageTag`** *(Optional)*: The tag for the image. Defaults to `env.BUILD_NUMBER`.
* **`dockerCredentialsId`** *(Optional)*: The Jenkins Credentials ID containing your Docker Hub username and password. If provided, the function will authenticate and `docker push` the image.

**Under the hood:** `docker build -t <imageName>:<imageTag> .` and optional `docker login` + `docker push`.

---

### `deployOnK8s(Map config = [:])`  *(Traditional CI/CD)*
Applies Kubernetes manifests directly to a running cluster using `kubectl apply`. Used in traditional CI/CD pipelines where Jenkins manages the deployment.
* **`workingDir`** *(Optional)*: The directory from which to run `kubectl`. Defaults to `.`.
* **`manifestPath`** *(Optional)*: The path to the Kubernetes manifest file. Defaults to `manifests/deployment.yaml`.
* **`kubeconfigCredentialsId`** *(Optional)*: The Jenkins Credentials ID containing a Secret File with your `kubeconfig`. If omitted, defaults to using `/home/jenkins/kubeconfig`.
* **`serverIp`** *(Optional)*: If provided, dynamically overrides the API server endpoint (`--server=https://<serverIp>:8443`) when applying the manifest. This is useful for solving Docker/Minikube internal networking issues without hardcoding IPs in the `kubeconfig` file.

**Under the hood:** `kubectl apply -f <manifestPath> --kubeconfig=$KUBECONFIG`

---

### `updateGitOpsRepo(Map config = [:])`  *(GitOps with ArgoCD)*
Updates the image tag in the Kubernetes deployment manifest using `sed`, then commits and pushes the change back to GitHub. This triggers ArgoCD auto-synchronization — ArgoCD detects the new commit and deploys the updated application to Kubernetes.
* **`workingDir`** *(Optional)*: Working directory containing the manifest. Defaults to `.`.
* **`manifestPath`** *(Optional)*: Path to the deployment manifest. Defaults to `manifests/deployment.yaml`.
* **`imageName`** *(Optional)*: Docker image repository name. Defaults to `myapp`.
* **`imageTag`** *(Optional)*: New image tag to write into the manifest. Defaults to `env.BUILD_NUMBER`.
* **`githubCredentialsId`** *(Optional)*: Jenkins Credentials ID (Username with Password or PAT) for GitHub commit & push access.
* **`gitEmail`** *(Optional)*: Git commit author email. Defaults to `jenkins@ivolve.local`.
* **`gitUser`** *(Optional)*: Git commit author name. Defaults to `Jenkins CI`.

**Under the hood:** `sed -i` to update image tag → `git add` → `git commit` → `git push`

---

## 🔄 Deployment Strategy Comparison

| Aspect | Traditional (`deployOnK8s`) | GitOps (`updateGitOpsRepo`) |
|--------|----------------------------|-----------------------------|
| **Who deploys?** | Jenkins via `kubectl apply` | ArgoCD via Git reconciliation |
| **Cluster access** | Jenkins needs `kubeconfig` | Jenkins only needs GitHub access |
| **Source of truth** | Jenkins pipeline | Git repository |
| **Rollback** | Manual `kubectl` or re-run pipeline | `git revert` triggers ArgoCD auto-sync |
| **Audit trail** | Jenkins build logs | Git commit history |
| **Used in** | Lab 23 (Traditional CI/CD) | Lab 24 (GitOps with ArgoCD) |

---

> 💡 **Best Practice:** Always test modifications to these shared library functions on a separate branch before merging to `main`, as changes here will instantly affect all dependent CI/CD pipelines!
