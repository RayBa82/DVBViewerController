pipeline {
    agent any
    options {
        // Stop the build early in case of compile or test failures
        skipStagesAfterUnstable()
    }
    stages {
        stage('Compile') {
            steps {
                // Compile the app and its dependencies
                sh './gradlew dvbViewerController:compileProductionDebugSources'
            }
        }
        stage('Unit test') {
            steps {
                // Compile and run the unit tests for the app and its dependencies
                sh './gradlew dvbViewerController:test'

                // Analyse the test results and update the build result as appropriate
                junit '**/TEST-*.xml'
            }
        }
        stage('Build APK') {
            steps {

                configFileProvider(
                        [configFile(fileId: 'DVBViewer google-services.json', variable: 'GSERVICE_JSON')]) {
                    sh 'cp $GSERVICE_JSON dvbViewerController/src/release/google-services.json'
                }
                configFileProvider(
                        [configFile(fileId: 'signing.properties', variable: 'SIGNING_PROPS')]) {
                    sh 'cp $SIGNING_PROPS keystore/signing.properties'
                }
                // Finish building and packaging the APK
                sh './gradlew assembleRelease'

                // Archive the APKs so that they can be downloaded from Jenkins
                archiveArtifacts '**/*.apk'
            }
        }
        stage('Static analysis') {
            steps {
                // Run Lint and analyse the results
                sh './gradlew lintDebug'
                androidLint pattern: '**/lint-results-*.xml'
            }
        }
    }
}