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
        stage('Build APK') {
            steps {

                configFileProvider(
                        [configFile(fileId: 'e7c9e865-af44-4cd7-83f1-21c92df8ac4c', variable: 'GSERVICE_JSON')]) {
                    sh 'cp $GSERVICE_JSON src/release/google-services.json'
                }
                configFileProvider(
                        [configFile(fileId: '79bae1f2-b479-4945-b208-4faced22f141', variable: 'SIGNING_PROPS')]) {
                    sh 'cp $SIGNING_PROPS ../keystore/signing.properties'
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