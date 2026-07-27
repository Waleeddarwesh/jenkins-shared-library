<div align="center">

# ⚙️ Jenkins Shared Library

**A centralized repository of reusable Groovy components and standardized pipeline stages for enterprise CI/CD workflows.**

</div>

---

## 📌 Overview

This repository serves as a **Jenkins Shared Library**, enabling developers and DevOps engineers to write modular, DRY (Don't Repeat Yourself) Jenkins pipelines. By abstracting complex pipeline logic into standardized, version-controlled Groovy scripts, multiple projects can reuse the same deployment patterns.

---

## 📂 Repository Structure

```text
jenkins-shared-library/
│
├── vars/
│   ├── buildApp.groovy        # Compiles & packages Java applications via Maven
│   ├── buildImage.groovy      # Builds and tags Docker images
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
Once imported, you can call the custom pipeline steps directly inside your stages as if they were native Jenkins commands:

```groovy
@Library('shared-library') _

pipeline {
    agent any

    stages {
        stage('Compile & Test') {
            steps {
                buildApp()
            }
        }
        
        stage('Dockerize') {
            steps {
                buildImage()
            }
        }
        
        stage('Deploy to Cluster') {
            steps {
                deployOnK8s()
            }
        }
    }
}
```

---

## 🛠 Available Functions

### `buildApp()`
Executes a Maven build to compile and package a Java application while skipping tests for faster deployment cycles.
- **Under the hood:** `mvn clean package -DskipTests`

### `buildImage()`
Builds a Docker image of the application and dynamically tags it using the Jenkins native `$BUILD_NUMBER` variable to ensure immutable, traceable image versions.
- **Under the hood:** `docker build -t myapp:${BUILD_NUMBER} .`

### `deployOnK8s()`
Applies the infrastructure-as-code manifests located in `manifests/deployment.yaml` to deploy the application onto the active Kubernetes cluster.
- **Under the hood:** `kubectl apply -f manifests/deployment.yaml`

---

> 💡 **Best Practice:** Always test modifications to these shared library functions on a separate branch before merging to `main`, as changes here will instantly affect all dependent CI/CD pipelines!
