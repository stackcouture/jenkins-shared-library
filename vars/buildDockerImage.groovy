def call(String imageTag) {
    dir("${env.WORKSPACE}/java-app") {
        sh "ls -lh target/*.jar || (echo 'JAR file not found!' && exit 1)"
        sh "docker build --no-cache -t ${imageTag} ."
    }
}