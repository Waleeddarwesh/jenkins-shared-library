def call(Map config = [:]) {
    def workingDir = config.workingDir ?: '.'
    def imageName = config.imageName ?: 'myapp'
    def imageTag = config.imageTag ?: env.BUILD_NUMBER
    def dockerCredentialsId = config.dockerCredentialsId

    dir(workingDir) {
        echo "Building Docker image ${imageName}:${imageTag}..."
        sh "docker build -t ${imageName}:${imageTag} ."
        
        if (dockerCredentialsId) {
            echo "Pushing image to Docker Hub..."
            withCredentials([usernamePassword(credentialsId: dockerCredentialsId, passwordVariable: 'DOCKER_PASS', usernameVariable: 'DOCKER_USER')]) {
                sh """
                echo "\$DOCKER_PASS" | docker login -u "\$DOCKER_USER" --password-stdin
                docker push ${imageName}:${imageTag}
                """
            }
        }
    }
}
