def call(Map config = [:]) {
    def workingDir = config.workingDir ?: '.'
    dir(workingDir) {
        echo "Building application..."
        sh 'mvn clean package -DskipTests'
    }
}
