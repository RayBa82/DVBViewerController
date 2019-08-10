pipeline {
    agent any
    options {
        // Stop the build early in case of compile or test failures
        skipStagesAfterUnstable()
    }
    stages {
        stage('Prepare') {
            steps {
                sh 'mkdir -p dvbViewerController/src/release && cp /var/signing/google-services.json dvbViewerController/src/release/google-services.json'
                sh 'cp /var/signing/signing.properties keystore/signing.properties'
            }
        }
        stage('Compile') {
            steps {
                // Compile the app and its dependencies
                sh './gradlew dvbViewerController:compileProductionReleaseSources'
            }
        }
        stage('Unit test') {
            steps {
                // Compile and run the unit tests for the app and its dependencies
                sh './gradlew dvbViewerController:testProductionReleaseUnitTest'

                // Analyse the test results and update the build result as appropriate
                junit '**/TEST-*.xml'
            }
        }
        stage('Build APK') {
            steps {
                // Finish building and packaging the APK
                sh './gradlew dvbViewerController:assembleProductionRelease'

                // Archive the APKs so that they can be downloaded from Jenkins
                archiveArtifacts '**/*.apk'
            }
        }
        stage('Static analysis') {
            steps {
                // Run Lint and analyse the results
                sh './gradlew lintProductionRelease'
                androidLint pattern: '**/lint-results-*.xml'
            }
        }
        stage('Deploy') {
            when {
                // Only execute this stage when building from the `beta` branch
                branch 'beta'
            }
            steps {
                // Upload the APK to Google Play
                androidApkUpload apkFilesPattern: '**/*.apk', deobfuscationFilesPattern: '**/mapping.txt', googleCredentialsId: 'DVBViewerController', trackName: 'beta'
            }
        }
    }
}