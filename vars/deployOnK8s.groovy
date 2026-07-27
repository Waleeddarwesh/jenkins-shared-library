def call(Map config = [:]) {
    def workingDir = config.workingDir ?: '.'
    def manifestPath = config.manifestPath ?: 'manifests/deployment.yaml'

    dir(workingDir) {
        echo "Deploying application from ${manifestPath}..."
        sh "kubectl apply -f ${manifestPath} --kubeconfig=/home/jenkins/kubeconfig"
    }
}
