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
    environment {
        BLACKDUCK_PROJECT_APPLICATION_NAME="123489"
        BLACKDUCK_PROJECT_VERSION_NAME="${BUILD_NUBMER}"
        BLACKDUCK_PROJECT_TAGS="internal"
        BLACKDUCK_SOURCE_PATH="${WORKSPACE}/lambdas/pokemon"
        BLACKDUCK_SEARCH_DEPTH="0"
        BLACKDUCK_PIP_REQUIREMENTS_PATH="${WORKSPACE}/lambdas/requirements.txt"
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
                    python3 -m pip install -r ${env.BLACKDUCK_PIP_REQUIREMENTS_PATH}
                    '''
                    script{
                        env.PYTHON_3_PATH=sh (returnStdout: true, script: 'which python3').trim()
                        env.PIP_3_PATH=sh (returnStdout: true, script: 'which pip3').trim()
                    }
                    synopsys_detect detectProperties: '''
                    --blackduck.offline.mode=true \
                    --detect.project.application.id=${env.BLACKDUCK_PROJECT_APPLICATION_NAME} \
                    --detect.project.version.name="${env.BLACKDUCK_PROJECT_VERSION_NAME}" \
                    --detect.project.tags=${env.BLACKDUCK_PROJECT_TAGS} \
                    --detect.source.path="${env.BLACKDUCK_SOURCE_PATH}" \
                    --detect.detector.search.depth=${env.BLACKDUCK_SEARCH_DEPTH} \
                    --detect.python.python3=true \
                    --detect.python.path=${PYTHON_3_PATH} \
                    --detect.pip.path=${PIP_3_PATH}  \
                    --detect.pip.requirements.path="${env.BLACKDUCK_PIP_REQUIREMENTS_PATH}" \
                    --detect.tools.excluded="SIGNATURE_SCAN" \
                    --logging.level.detect=TRACE  \
                    --logging.level.com.synopsys.integration=TRACE \
                    --detect.bdio.output.path="${WORKSPACE}" \
                    --detect.bom.aggregate.name="${BUILD_NUMBER}_bom" \
                    --detect.cleanup=true \
                    ''', downloadStrategyOverride: [$class: 'ScriptOrJarDownloadStrategy']
                    
                }  
            }
        }
    }
}
// --detect.accuracy.required=NONE
// --detect.python.python3=true  --detect.cleanup=false --logging.level.com.synopsys.integration=DEBUG