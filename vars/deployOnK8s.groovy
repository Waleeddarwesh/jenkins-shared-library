def call() {
    echo "Deploying application..."
    withCredentials([
        file(
            credentialsId: 'kubeconfig-creds',
            variable: 'KUBECONFIG'
        )
    ]) {
        sh "kubectl apply -f manifests/deployment.yaml --kubeconfig=\$KUBECONFIG"
    }
}
