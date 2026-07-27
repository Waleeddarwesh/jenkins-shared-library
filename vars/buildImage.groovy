def call() {
    echo "Building Docker image..."
    sh 'docker build -t myapp:${BUILD_NUMBER} .'
}
