pipeline {
  agent any
  stages {
    stage('Compile') {
      steps {
        sh './gradlew dvbViewerController:compileProductionDebugSources'
      }
    }
    stage('Build APK') {
      steps {
        configFileProvider([configFile(fileId: 'DVBViewer google-services.json', variable: 'GSERVICE_JSON')]) {
          sh 'cp $GSERVICE_JSON dvbViewerController/src/release/google-services.json'
        }

        configFileProvider([configFile(fileId: 'signing.properties', variable: 'SIGNING_PROPS')]) {
          sh 'cp $SIGNING_PROPS keystore/signing.properties'
        }

        sh './gradlew assembleRelease'
        archiveArtifacts '**/*.apk'
      }
    }
    stage('Static analysis') {
      steps {
        sh './gradlew lintDebug'
        androidLint(pattern: '**/lint-results-*.xml')
      }
    }
  }
  options {
    skipStagesAfterUnstable()
  }
}