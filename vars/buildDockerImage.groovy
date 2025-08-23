def call(String imageTag) {
    dir('java-app') {
        sh "docker build --no-cache -t ${imageTag} ."
    }
}