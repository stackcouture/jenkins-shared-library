def call(String imageTag) {
    sh "docker build --no-cache -t ${imageTag} ."
}