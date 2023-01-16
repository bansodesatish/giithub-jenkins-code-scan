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
                    '''
                    
                synopsys_detect detectProperties: '--blackduck.offline.mode=true --detect.accuracy.required=NONE --detect.source.path="${WORKSPACE}/lambda" --detect.detector.search.depth=2 --detect.python.python3=true --detect.pip.requirements.path="${WORKSPACE}/requirements.txt" --detect.tools.excluded="SIGNATURE_SCAN" --logging.level.detect=DEBUG --logging.level.com.synopsys.integration=DEBUG --detect.cleanup=false', downloadStrategyOverride: [$class: 'ScriptOrJarDownloadStrategy']
                    
                }  
            }
        }
    }
}
