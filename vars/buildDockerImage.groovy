def call(String imageTag) {
    dir('source') {
        sh "docker build --no-cache -t ${imageTag} ."
    }
}
