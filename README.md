<div align="center">

# ⚙️ Jenkins Shared Library

**A highly reusable, parameterized repository of Groovy components and standardized pipeline stages for enterprise CI/CD workflows.**

</div>

---

## 📌 Overview

This repository serves as a **Jenkins Shared Library**, enabling developers and DevOps engineers to write modular, DRY (Don't Repeat Yourself) Jenkins pipelines. By abstracting complex pipeline logic into standardized, version-controlled Groovy scripts, multiple projects can reuse the same deployment patterns.

This library has been specifically designed to be **highly dynamic**. All functions accept optional named arguments (via a `Map`), allowing them to be customized for any project structure or authentication mechanism.

---

## 📂 Repository Structure

```text
jenkins-shared-library/
│
├── vars/
│   ├── buildApp.groovy        # Compiles & packages Java applications via Maven
│   ├── buildImage.groovy      # Builds and pushes Docker images to Docker Hub
│   └── deployOnK8s.groovy     # Deploys applications to Kubernetes clusters
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

### 2. Invoke the Functions
Because the functions accept dynamic arguments, you can pass custom configurations without altering the underlying shared library code:

```groovy
@Library('shared-library') _

pipeline {
    agent any

    stages {
        stage('Compile & Test') {
            steps {
                buildApp(workingDir: 'path/to/project')
            }
        }
        
        stage('Dockerize & Push') {
            steps {
                buildImage(
                    workingDir: 'path/to/project',
                    imageName: 'my-dockerhub-user/myapp',
                    dockerCredentialsId: 'dockerhub-credentials-id'
                )
            }
        }
        
        stage('Deploy to Cluster') {
            steps {
                deployOnK8s(
                    workingDir: 'path/to/project',
                    manifestPath: 'manifests/deployment.yaml',
                    kubeconfigCredentialsId: 'kubeconfig-credentials-id',
                    serverIp: '172.18.0.2'
                )
            }
        }
    }
}
```

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

### `deployOnK8s(Map config = [:])`
Applies Kubernetes infrastructure-as-code manifests to deploy the application onto an active Kubernetes cluster.
* **`workingDir`** *(Optional)*: The directory from which to run `kubectl`. Defaults to `.`.
* **`manifestPath`** *(Optional)*: The path to the Kubernetes manifest file. Defaults to `manifests/deployment.yaml`.
* **`kubeconfigCredentialsId`** *(Optional)*: The Jenkins Credentials ID containing a Secret File with your `kubeconfig`. If omitted, defaults to using `/home/jenkins/kubeconfig`.
* **`serverIp`** *(Optional)*: If provided, dynamically overrides the API server endpoint (`--server=https://<serverIp>:8443`) when applying the manifest. This is incredibly useful for solving Docker/Minikube internal networking loops without hardcoding IPs in the `kubeconfig` file.

**Under the hood:** `kubectl apply -f <manifestPath> --kubeconfig=$KUBECONFIG`

---

> 💡 **Best Practice:** Always test modifications to these shared library functions on a separate branch before merging to `main`, as changes here will instantly affect all dependent CI/CD pipelines!
