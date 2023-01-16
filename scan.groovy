pipeline {
    agent {
        kubernetes {
            label 'kube-agent'
            yaml '''
            apiVersion: v1
            kind: Pod
            spec:
                serviceAccountName: jenkins-admin
                containers:
                - name: chaos-builder
                  image: jenkinsxio/builder-base:0.1.275
                  command:
                  - cat
                  tty: true
                  volumeMounts:
                  - name: docker
                    mountPath: /var/run/docker.sock
                volumes:
                - name: docker
                  hostPath:
                    path: /var/run/docker.sock
            '''
        }
    }
    stages {
        stage('Build image and push it to dev') {
            steps {
                container('chaos-builder') {
                    sh '''
                    echo "pipeline working...."
                    curl -h
                    '''
                    
                }  
                synopsys_detect detectProperties: '--blackduck.offline.mode=true --detect.detector.buildless=true --detect.spurce.path="${WORKSPACE}/lambda" --detect.pipi.requirements.path="${WORKSPACE}/requirements.txt"', downloadStrategyOverride: [$class: 'ScriptOrJarDownloadStrategy']
            }
        }
    }
}
