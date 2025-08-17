def call(String imageTag) {
    echo "Building Docker image: ${imageTag}"
    sh "docker build -t ${imageTag} ."
    echo "Docker image built successfully."
}