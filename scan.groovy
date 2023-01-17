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
        BLACKDUCK_OFFLINE_MODE="true" // Offline Mode: This can disable any Black Duck communication - if true, Detect will not upload BDIO files, it will not check policies, and it will not download and install the signature scanner.
        BLACKDUCK_PROJECT_NAME="Blackduck_Application_Scan" // Project Name: An override for the name to use for the Black Duck project
        BLACKDUCK_PROJECT_APPLICATION_ID="123489" // Application ID: Sets the 'Application ID' project setting. 
        BLACKDUCK_PROJECT_VERSION_NAME="${BUILD_NUBMER}" // Version Name: An override for the version to use for the Black Duck project.
        BLACKDUCK_PROJECT_TAGS="internal" // Project Tags: A comma-separated list of tags to add to the project.
        BLACKDUCK_SOURCE_PATH="${WORKSPACE}" // /lambdas/pokemon" // Source Path: The path to the project directory to inspect.
        BLACKDUCK_SEARCH_DEPTH="3" // Detector Search Depth: Depth of subdirectories within the source directory to which Detect will search for files that indicate whether a detector applies.
        BLACKDUCK_DETECTOR_SEARCH_CONTINUE="true" //If true, the bom tool search will continue to look for nested bom tools of the same type to the maximum search depth, see the detailed help for more information. If true, Detect will find Maven projects that are in subdirectories of a Maven project and Gradle projects that are in subdirectories of Gradle projects, etc. If false, Detect will only find bom tools in subdirectories of a project if they are of a different type such as an Npm project in a subdirectory of a Gradle project.
        BLACKDUCK_DETECTOR_SEARCH_EXCLUSION_DEFAULT="true" // If true, these directories will be excluded from the detector search: bin, build, .git, .gradle, node_modules, out, packages, target.
        BLACKDUCK_PIP_REQUIREMENTS_PATH="${WORKSPACE}/lambdas/requirements.txt" // PIP Requirements Path: A comma-separated list of paths to requirements.txt files.
        BLACKDUCK_TOOLS_EXCLUDED="SIGNATURE_SCAN" // Acceptable Values: BAZEL, DETECTOR, DOCKER, SIGNATURE_SCAN, BINARY_SCAN, POLARIS, NONE, ALL
        BLACKDUCK_LEVEL_DETECT="DEBUG" // Acceptable Values: TRACE, DEBUG, INFO, WARN, ERROR, FATAL, OFF
        BLACKDUCK_LEVEL_COM_SYNOPSIS_INTEGRATION="DEBUG" // Acceptable Values: TRACE, DEBUG, INFO, WARN, ERROR, FATAL, OFF
        BLACKDUCK_BDIO_OUTPUT_PATH="${WORKSPACE}" // BDIO Output Directory: The path to the output directory for all BDIO files.
        BLACKDUCK_BDIO_AGGREGATE_NAME="${BUILD_NUMBER}_bom" // Aggregate BDIO File Name: If set, this will aggregate all the BOMs to create a single BDIO file with the name provided.
    }
    stages {
        stage('Build image and push it to dev') {
            steps {
                container('blackduck') {
                    sh '''
                    echo "installing pip dependencies...."
                    python3 -m pip install -r ${BLACKDUCK_PIP_REQUIREMENTS_PATH}
                    '''
                    script{
                        env.PYTHON_3_PATH=sh (returnStdout: true, script: 'which python3').trim()
                        env.PIP_3_PATH=sh (returnStdout: true, script: 'which pip3').trim()
                    }
                    synopsys_detect detectProperties: '''
                    --blackduck.offline.mode=${BLACKDUCK_OFFLINE_MODE} \
                    --detect.project.name=${BLACKDUCK_PROJECT_NAME} \
                    --detect.project.application.id=${BLACKDUCK_PROJECT_APPLICATION_ID} \
                    --detect.project.version.name="${BLACKDUCK_PROJECT_VERSION_NAME}" \
                    --detect.project.tags=${BLACKDUCK_PROJECT_TAGS} \
                    --detect.source.path="${BLACKDUCK_SOURCE_PATH}" \
                    --detect.detector.search.continue="${BLACKDUCK_DETECTOR_SEARCH_CONTINUE}" \
                    --detect.detector.search.exclusion.defaults="${BLACKDUCK_DETECTOR_SEARCH_EXCLUSION_DEFAULT}" \
                    --detect.detector.search.depth=${BLACKDUCK_SEARCH_DEPTH} \
                    --detect.python.python3=true \
                    --detect.python.path=${PYTHON_3_PATH} \
                    --detect.pip.path=${PIP_3_PATH}  \
                    --detect.pip.requirements.path="${BLACKDUCK_PIP_REQUIREMENTS_PATH},${WORKSPACE}/layers/hello_world_layers/requirements.txt" \
                    --detect.tools.excluded="${BLACKDUCK_TOOLS_EXCLUDED}" \
                    --logging.level.detect=${BLACKDUCK_LEVEL_DETECT}  \
                    --logging.level.com.synopsys.integration=${BLACKDUCK_LEVEL_COM_SYNOPSIS_INTEGRATION} \
                    --detect.bdio.output.path="${BLACKDUCK_BDIO_OUTPUT_PATH}" \
                    --detect.bom.aggregate.name="${BLACKDUCK_BDIO_AGGREGATE_NAME}" \
                    --detect.cleanup=true \
                    ''', downloadStrategyOverride: [$class: 'ScriptOrJarDownloadStrategy']
                    
                }  
            }
        }
    }
}