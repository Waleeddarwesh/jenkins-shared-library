def call(Map config = [:]) {
    def workingDir = config.workingDir ?: '.'
    def manifestPath = config.manifestPath ?: 'manifests/deployment.yaml'
    def imageName = config.imageName ?: 'myapp'
    def imageTag = config.imageTag ?: env.BUILD_NUMBER
    def githubCredentialsId = config.githubCredentialsId
    def gitEmail = config.gitEmail ?: 'jenkins@ivolve.local'
    def gitUser = config.gitUser ?: 'Jenkins CI'

    dir(workingDir) {
        echo "GitOps Action: Updating image tag in ${manifestPath} to ${imageName}:${imageTag}..."
        
        // Update manifest file image tag using sed
        sh "sed -i 's|image: .*|image: ${imageName}:${imageTag}|g' ${manifestPath}"
        
        if (githubCredentialsId) {
            echo "Pushing updated deployment manifest to GitHub for ArgoCD auto-synchronization..."
            withCredentials([usernamePassword(credentialsId: githubCredentialsId, passwordVariable: 'GITHUB_TOKEN', usernameVariable: 'GITHUB_USER')]) {
                sh """
                git config user.email "${gitEmail}"
                git config user.name "${gitUser}"
                git add ${manifestPath}
                git commit --amend --no-edit
                git push --force 
            }
        } else {
            echo "⚠️ Notice: githubCredentialsId not specified. Manifest updated locally only."
        }
    }
}
