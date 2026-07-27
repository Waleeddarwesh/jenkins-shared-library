def call() {
    echo "Deploying application..."
    withCredentials([
        file(
            credentialsId: 'kubeconfig',
            variable: 'KUBECONFIG'
        )
    ]) {
        sh "kubectl apply -f manifests/deployment.yaml --kubeconfig=\$KUBECONFIG"
    }
}
