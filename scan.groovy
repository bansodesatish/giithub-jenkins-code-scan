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
                    which pip3
                    which pipenv
                    '''
                    script{
                        env.PYTHON_3_PATH=sh (returnStdout: true, script: 'which python3').trim()
                        env.PIP_3_PATH=sh (returnStdout: true, script: 'which pip3').trim()
                    }
                    synopsys_detect detectProperties: '''
                    --blackduck.offline.mode=true \
                    --detect.project.application.id=462841 \
                    --detect.project.version.name="${BUILD_NUMBER}" \
                    --detect.project.tags=internal,contractores,notdistributed,bottomzone,middlezone,nonmodifiedoss,mexux \
                    --detect.source.path="${WORKSPACE}/lambda/pokemon" \
                    --detect.detector.search.depth=0 --detect.python.python3=true \
                    --detect.python.path=${PYTHON_3_PATH} \
                    --detect.pip.path=${PIP_3_PATH}  \
                    --detect.pip.requirements.path="${WORKSPACE}/requirements.txt" \
                    --detect.tools.excluded="SIGNATURE_SCAN" \
                    --logging.level.detect=TRACE  \
                    --detect.cleanup=true \
                    --logging.level.com.synopsys.integration=TRACE \
                    --detect.bdio.output.path="${WORKSPACE}" \
                    ---detect.bom.aggregate.name="${BUILD_NUMBER}_bom" \
                    ''', downloadStrategyOverride: [$class: 'ScriptOrJarDownloadStrategy']
                    
                }  
            }
        }
    }
}
// --detect.accuracy.required=NONE
// --detect.python.python3=true  --detect.cleanup=false --logging.level.com.synopsys.integration=DEBUG