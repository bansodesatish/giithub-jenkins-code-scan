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
                - name: blackduck
                  image: bansodesatish/blackduck
                  cammand: 
                  - cat
                  tty: true
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
                container('blackduck') {
                    sh '''
                    echo "pipeline working...."
                    which python3
                    '''
                    
                synopsys_detect detectProperties: '--blackduck.offline.mode=true --detect.source.path="${WORKSPACE}/lambda/pokemon" --detect.detector.search.depth=0 --detect.python.python3=true  --detect.pip.requirements.path="${WORKSPACE}/requirements.txt" --detect.tools.excluded="SIGNATURE_SCAN" --logging.level.detect=DEBUG --logging.level.com.synopsys.integration=DEBUG --detect.cleanup=false', downloadStrategyOverride: [$class: 'ScriptOrJarDownloadStrategy']
                    
                }  
            }
        }
    }
}
// --detect.accuracy.required=NONE
// --detect.python.python3=true