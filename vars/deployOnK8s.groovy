def call() {
    echo "Deploying application..."
    sh "kubectl apply -f manifests/deployment.yaml --kubeconfig=/home/jenkins/kubeconfig"
}
