# Jenkins Shared Library

A centralized Jenkins Shared Library containing reusable Groovy scripts for standardizing CI/CD pipelines.

## Contents
- `buildApp.groovy`: Compiles and packages Java applications using Maven.
- `buildImage.groovy`: Builds Docker images and tags them dynamically.
- `deployOnK8s.groovy`: Deploys applications to Kubernetes via `kubectl apply`.
