def call(Map config = [:]) {
    def workingDir = config.workingDir ?: '.'
    def manifestPath = config.manifestPath ?: 'manifests/deployment.yaml'
    def kubeconfigCredentialsId = config.kubeconfigCredentialsId
    def serverIp = config.serverIp

    dir(workingDir) {
        echo "Deploying application from ${manifestPath}..."
        
        if (kubeconfigCredentialsId) {
            withCredentials([file(credentialsId: kubeconfigCredentialsId, variable: 'KUBECONFIG')]) {
                // If a server IP is provided, use it to override the API server endpoint dynamically
                def serverOverride = serverIp ? "--server=https://${serverIp}:8443 --insecure-skip-tls-verify=true" : ""
                sh "kubectl apply -f ${manifestPath} --kubeconfig=\$KUBECONFIG ${serverOverride}"
            }
        } else {
            // Fallback for hardcoded/local environments
            sh "kubectl apply -f ${manifestPath} --kubeconfig=/home/jenkins/kubeconfig"
        }
    }
}
