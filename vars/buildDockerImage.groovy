def call(String imageTag) {
    dir("${env.WORKSPACE}") {
        sh "ls -lh target/*.jar || (echo 'JAR file not found!' && exit 1)"
        sh "docker build --no-cache -t ${imageTag} ."
    }
}