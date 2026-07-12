// Run this file from a Jenkins Job DSL seed job. It creates the two jobs
// required by the project; their parameters are declared in each Jenkinsfile.
def repository = 'https://github.com/Thang2005HCMUS/yas.git'
def credentialsId = 'github-credentials'

['developer_build': 'ci/Jenkinsfile.developer-build',
 'developer_cleanup': 'ci/Jenkinsfile.developer-cleanup'].each { jobName, pipelinePath ->
  pipelineJob(jobName) {
    description("Managed by ci/jenkins-jobs.groovy. ${jobName == 'developer_build' ? 'Deploy a disposable developer environment.' : 'Delete a disposable developer environment.'}")
    definition {
      cpsScm {
        scm {
          git {
            remote {
              url(repository)
              credentials(credentialsId)
            }
            branches('*/main')
            extensions {
              cleanBeforeCheckout()
            }
          }
        }
        scriptPath(pipelinePath)
        lightweight(false)
      }
    }
  }
}
