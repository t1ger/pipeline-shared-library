Jenkins Pipeline Shared Library
========================================

This project is intended for use with [Jenkins](https://jenkins.io/) and Global Pipeline Libraries through the 
[Pipeline Shared Groovy Libraries Plugin](https://wiki.jenkins.io/display/JENKINS/Pipeline+Shared+Groovy+Libraries+Plugin).

A common scenario when developing Jenkins [declarative pipelines](https://jenkins.io/doc/book/pipeline/syntax/), is
to bundle common custom pipeline tasks in a shared library so that all Jenkins pipeline configurations in an organisation
can leverage from them without the need to reimplement the same logic.

This project provides a project template for developing shared Jenkins pipeline libraries as specified in the Jenkins
[documentation](https://jenkins.io/doc/book/pipeline/shared-libraries/). The project is setup using Gradle which enables you to develop and unit
test your custom Jenkins pipeline library code.

Requirements
---
[Apache Groovy](http://groovy-lang.org/)

Install
---
    git clone https://github.com/t1ger/pipeline-shared-library.git
    cd pipeline-shared-library
    ./gradlew build test

Install the shared library as described in the Jenkins [shared library documentation](https://jenkins.io/doc/book/pipeline/shared-libraries/#using-libraries).

Structure
---
The project contains an example pipeline method _maintainer_, which allows you to output the project maintainer in the console log.
This is only used as an example. Adapt and add new classes according to your needs. 

    ├── src                       (your source code classes goes here)
    │   └── rj.ws.jenkins.pipeline.lib
    │       └── Constants.groovy  (example Groovy class)
    |       |__ build.groovy      (your build library )         
    ├── test                      (your unit test classes goes here)
    │   └── MaintainerTest.groovy (example unit test class)
    └── vars                      (your shared library classes goes here)
        └── maintainer.groovy     (logic for your custom method - filename to match Jenkins pipeline step name)
        |__ build.groovy

Example usage in a Jenkins Script pipeline:
```
/**
 * Library name should match the name configured in Jenkins > Configure system > Global Pipeline Libraries.
 * Annotation can be omitted if configured to be loaded implicitly.
 */
node('109'){
        @Library('pipeline-shared-library') _
                def map = [:]
                map.put('BRANCH','master')
                map.put('MODULE_NAME','')
                map.put('TAG_NAME','')
                map.put('SRC_TYPE','git')
                map.put('REPO_URL','git@github.com.cn:nw_code/search.git')
                map.put('CRED_ID','c5cb1287-dabc-43e6-abe4-05a9a7d99990')
                map.put('TARGET_HOSTS','172.16.90.111')
                map.put('APP_LANG','java')
                map.put('IS_COMPILE','true')
                map.put('VERSION_MAJOR','6')
                map.put('VERSION_MINOR','2.0')
                map.put('BUILD_TYPE','mvn')
                map.put('BUILD_COMMAND','mvn clean package -Dmaven.test.skip=true')
                map.put('POM_PATH','pom.xml')
                map.put('PROJECT_NAME','Search')
                map.put('STOP_COMMAND','init.d stop')
                map.put('START_COMMAND','init.d start')
                map.put('TARGET_DIR','/opt/webapps')
                map.put('TARGET_NAME','')
                map.put('TARGET_USER','')
                map.put('EMAIL','')
                build(map)
}
```
Considerations
----
For many use cases there is a benefit in providing a custom and simplified DSL to create Jenkins pipelines, instead of
requiring repetitive pipeline configurations for each project. So instead of each project specifying a full configuration
such as in the example above, the pipeline itself can be extracted to a pipeline method (residing in the _vars_ directory).

Example:
```vars/build.groovy```

```
#!groovy

def call(body) {
    def config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()
    
    pipeline {
        agent any
        stages {
            stage('Commit stage') {
                steps {
                    maintainer config.maintainer
                }
            }
        }
    }
}
```
... which allows your pipeline configuration (e.g. in a Jenkinsfile) to look like:
```
continuousDeliveryPipeline {
    maintainer = 'Pipe Line'
}
```
Configuration
----
The library name used in the pipeline script must match what is configured as the library name in Jenkins > Configure system > Global Pipeline Libraries.

Contact and feedback
----
Feel free to open an issue or pull request if you find areas of improvement.

Happy pipelining!
