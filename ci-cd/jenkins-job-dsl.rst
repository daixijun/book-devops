Jenkins Job DSL
===============

Podstawy składni Job DSL
------------------------
Jedyne wymagane to nazwa ``Job``:

:DSL Methods:
    .. code-block:: groovy

        job('my-job')

:Job:
    .. code-block:: groovy

        job(String name, Closure closure = null)
        freeStyleJob(String name, Closure closure = null)
        buildFlowJob(String name, Closure closure = null)
        ivyJob(String name, Closure closure = null)
        matrixJob(String name, Closure closure = null)
        mavenJob(String name, Closure closure = null)
        multiJob(String name, Closure closure = null)
        workflowJob(String name, Closure closure = null)
        multibranchWorkflowJob(String name, Closure closure = null)

    .. code-block:: groovy

        def myJob = freeStyleJob('SimpleJob')
        myJob.with {
            description 'A Simple Job'
        }

:View:
    .. code-block:: groovy

        listView(String name, Closure closure = null)
        sectionedView(String name, Closure closure = null)
        nestedView(String name, Closure closure = null)
        deliveryPipelineView(String name, Closure closure = null)
        buildPipelineView(String name, Closure closure = null)
        buildMonitorView(String name, Closure closure = null)
        categorizedJobsView(String name, Closure closure = null)

:Folder:
    .. code-block:: groovy

        folder(String name, Closure closure = null)

    .. code-block:: groovy

        folder('project-a')
        freeStyleJob('project-a/compile')
        listView('project-a/pipeline')
        folder('project-a/testing')

:Config:
    .. code-block:: groovy

        configFiles(Closure configFilesClosure = null)

:Queue:
    .. code-block:: groovy

        queue(String jobName)
        queue(Job job)

:Reading from workspace:
    .. code-block:: groovy

        InputStream streamFileFromWorkspace(String filePath)
        String readFileFromWorkspace(String filePath)
        String readFileFromWorkspace(String jobName, String filePath)

    .. code-block:: groovy

        job('example') {
            steps {
                shell(readFileFromWorkspace('build.sh'))
            }
        }

        job('acme-tests') {
            description(readFileFromWorkspace('acme-tests', 'README.txt'))
        }

:Logging:
    .. code-block:: groovy

        out.println('Hello from a Job DSL script!')
        println('Hello from a Job DSL script!')

    .. code-block:: groovy

        import java.util.logging.Logger

        Logger logger = Logger.getLogger('org.example.jobdsl')
        logger.info('Hello from a Job DSL script!')

:Confiugure:
    .. code-block:: groovy

        job('example') {
            ...
            configure { project ->
                project / buildWrappers / EnvInjectPasswordWrapper {
                    injectGlobalPasswords(true)
                }
            }
        }

Przykłady Job DSL
-----------------

.. code-block:: groovy

    job('DSL-Tutorial-1-Test') {
        scm {
            git('git://github.com/quidryan/aws-sdk-test.git')
        }
        triggers {
            scm('H/15 * * * *')
        }
        steps {
            maven('-e clean test')
        }
    }

.. code-block:: groovy

    def project = 'quidryan/aws-sdk-test'
    def branchApi = new URL("https://api.github.com/repos/${project}/branches")
    def branches = new groovy.json.JsonSlurper().parse(branchApi.newReader())

    branches.each {
        def branchName = it.name
        def jobName = "${project}-${branchName}".replaceAll('/','-')

        job(jobName) {
            scm {
                git("git://github.com/${project}.git", branchName)
            }
            steps {
                maven("test -Dproject.name=${project}/${branchName}")
            }
        }
    }

.. code-block:: groovy

    def giturl = 'https://github.com/quidryan/aws-sdk-test.git'

    for(i in 0..10) {
        job("DSL-Tutorial-1-Test-${i}") {
            scm {
                git(giturl)
            }
            steps {
                maven("test -Dtest.suite=${i}")
            }
        }
    }