def call(Map config = [:]) {
    def workingDir = config.workingDir ?: '.'
    def manifestPath = config.manifestPath ?: 'manifests/deployment.yaml'
    def kubeconfigCredentialsId = config.kubeconfigCredentialsId
    def serverIp = config.serverIp

    dir(workingDir) {
        echo "Applying manifests to Kubernetes cluster..."
        if (kubeconfigCredentialsId) {
            withCredentials([file(credentialsId: kubeconfigCredentialsId, variable: 'KUBECONFIG')]) {
                def serverOverride = serverIp ? "--server=https://${serverIp}:8443 --insecure-skip-tls-verify=true" : ""
                sh "kubectl apply -f ${manifestPath} --kubeconfig=\$KUBECONFIG ${serverOverride}"
            }
        } else {
            sh "kubectl apply -f ${manifestPath} --kubeconfig=/home/jenkins/kubeconfig"
        }
    }
}
