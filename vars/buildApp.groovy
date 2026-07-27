def call() {
    echo "Building application..."
    sh 'mvn clean package -DskipTests'
}
