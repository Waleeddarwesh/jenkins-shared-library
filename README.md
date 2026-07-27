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

---

## 🚨 Troubleshooting & Lessons Learned

During the initial setup of this shared library and Jenkins agent pipeline, several complex issues were encountered and resolved. These are documented below for future reference:

### 1. Maven Build Failure (`MissingProjectException`)
- **Problem:** `mvn clean package` failed with the error `there is no POM in this directory`. Jenkins was executing the shared library functions at the root of the Git workspace, rather than inside the application subfolder (`Lab23-Jenkins-Shared-Library`), meaning Maven couldn't find the `pom.xml`.
- **Solution:** Modified the `Jenkinsfile` to wrap the shared library function calls inside a `dir('04-Jenkins/Lab23-Jenkins-Shared-Library')` block. This guarantees that regardless of where the Jenkins Agent checks out the repository, the Groovy functions always execute relative to the actual Java application source code.

### 2. Kubernetes Credential Parsing (`error: tls: failed to parse private key`)
- **Problem:** `kubectl apply` inside the Jenkins pipeline crashed with a parsing error. This was caused by the `minikube-flat-config.yaml` file being edited and saved using a Windows text editor (like VS Code), which secretly added Windows-style carriage returns (`CRLF`) and a Byte Order Mark (BOM) to the Base64 keys. The Linux-based `kubectl` inside the Jenkins agent failed to parse these invisible characters.
- **Solution:** A clean, pristine configuration file was generated programmatically bypassing Windows text editors, completely enforcing Unix-style (`LF`) line endings. This ensured the Base64 PEM keys could be correctly parsed by Linux containers.

### 3. Docker Networking & Minikube (`Connection Refused`)
- **Problem:** The Jenkins agent could not reach the Minikube cluster using `https://host.docker.internal:50742`. Recent versions of Docker Desktop and Minikube bind the API server exclusively to `127.0.0.1` on the Windows host. Therefore, traffic escaping the Jenkins agent container via `host.docker.internal` (which maps to the host's external network IP) was immediately dropped by the host, as nothing was listening on that interface.
- **Solution:** 
  1. **Direct Network Peering:** Attached the `jenkins-agent` Docker container directly to the Minikube bridge network using `docker network connect minikube jenkins-agent`.
  2. **Direct IP Addressing:** Updated the `kubeconfig` Server URL to target Minikube's internal container IP directly (e.g., `https://192.168.49.2:8443`), completely bypassing the Windows host network and firewall.
  3. **Hardcoded Injection:** To prevent further configuration drift from the Jenkins Credentials UI, the `withCredentials` block was bypassed, and the `kubeconfig` was injected directly into the agent container's disk (`/home/jenkins/kubeconfig`), which the shared library now explicitly references.
