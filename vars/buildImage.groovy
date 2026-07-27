def call(Map config = [:]) {
    def workingDir = config.workingDir ?: '.'
    def imageName = config.imageName ?: 'myapp'
    def imageTag = config.imageTag ?: env.BUILD_NUMBER

    dir(workingDir) {
        echo "Building Docker image ${imageName}:${imageTag}..."
        sh "docker build -t ${imageName}:${imageTag} ."
    }
}
