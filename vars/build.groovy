#!groovy

def call(Map map){

    //project info
    def project
    def repo_url
    def branch
    def tag_name
    def module_name
    def cerd_id
    def email

    //package info
    def version_major
    def version_minor
  
    def git_version
    def commit_sum
    def target_package

    //build info
    def app_lang
    def src_type
    def build_dir
    def is_compile
  
    def src_dir
    def build_command
    def build_type
    def pom_path
    
    
    //config info
    def apollo_env
    def apollo_appid
    def apollo_namespace
    def apollo_token
    def config_file

    //deploy info
  
    String target_hosts
    def target_port
    def target_dir
    def target_user
    def stop_command
    def start_command
    

node('109'){
    stage('prepare'){
    
    //get repo_url
    if("${map.REPO_URL}"){
        repo_url = "${map.REPO_URL}"
    }else{
        echo "repo_url Error"
    }

    //get project name
    if("${map.PROJECT_NAME}"){
        project = "${map.PROJECT_NAME}"
    }else {
        def url = "${map.REPO_URL}"
        project = url.substring(url.lastIndexOf('/') + 1, url.lastIndexOf('.git'))
    }

    //get branch
    if("${map.BRANCH}"){
        branch = "${map.BRANCH}"
    }else{
        branch = 'master'
    }
    
    //get module_name
    if("${map.MODULE_NAME}"){
        module_name = "${map.MODULE_NAME}"
    }
    
    //get tag_name
    if("${map.TAG_NAME}"){
        tag_name = "${map.TAG_NAME}"
    }
    

    //get cerd_id
    if("${map.CRED_ID}"){
        cerd_id = "${map.CRED_ID}"
    }else{
        echo "cerd_id Error"
    }



    //get email
    if("${map.EMAIL}"){
        email = "${map.EMAIL}"
    }else{
        email = 'default@company.com.cn'
    }
                        
    if("${map.TARGET_PACKAGE}"){
        target_package = "${map.TARGET_PACKAGE}"
    }
    
    if("${map.VERSION_MAJOR}"){
        version_major = "${map.VERSION_MAJOR}"
    }
    
    if("${map.VERSION_MINOR}"){
        version_minor = "${map.VERSION_MINOR}"
    }
    
    
    if("${map.APP_LANG}"){
        app_lang = "${map.APP_LANG}"
    }else{
        app_lang = 'java'
    }
    

    if("${map.SRC_TYPE}"){
        src_type = "${map.SRC_TYPE}"
    }else{
        src_type = 'git'
    }
    
    if("${map.IS_COMPILE}"){
        is_compile = "${map.IS_COMPILE}"
    }else{
        is_compile = 'false'
    }
    
    if("${map.BUILD_COMMAND}"){
        build_command="${map.BUILD_COMMAND}"
    }else{
        switch ("${app_lang}"){
        case "java":
            build_command='mvn clean package'
            break;
        case "nodejs":
            build_command='yarn install && yarn run deploy'
            break;
        case "php":
            build_command='composer install'
            break;
        }
    }


    //config info
    if("${map.APOLLO_ENV}"){
        apollo_env = "${map.APOLLO_ENV}"
    }else{
        apollo_env = "PRO" 
    }
    
    if("${map.APOLLO_APPID}"){
        apollo_appid = "${map.APOLLO_APPID}"
    }else{
        apollo_appid = "ali_whistle_global_vpc" 
    }
    
    if("${map.APOLLO_NAMESPACE}"){
        apollo_namespace = "${map.APOLLO_NAMESPACE}"
    }
    
    if("${map.APOLLO_TOKEN}"){
        apollo_token = "${map.APOLLO_TOKEN}"
    }
    
    if("${map.CONFIG_FILE}"){
        config_file = "${map.CONFIG_FILE}"
    }
    
        
    //for java
    if("${map.BUILD_TYPE}"){
        build_type = "${map.BUILD_TYPE}"
    }else{
        build_type = "mvn" 
    }

    if("${map.POM_PATH}"){
        pom_path = "${map.POM_PATH}"
    }else{
        pom_path = 'pom.xml'
    }


    //get target dir
    if("${map.TARGET_DIR}"){
        target_dir = "${map.TARGET_DIR}"
    }else{
        target_dir = '/usr/local/whistle/webapps'
    }
    
    //get target hosts
    if("${map.TARGET_HOSTS}"){
        target_hosts = "${map.TARGET_HOSTS}"
    }else{
        echo "target_hosts Error"
    }
    
    //get target port
    if("${map.TARGET_PORT}"){
        target_port = "${map.TARGET_PORT}"
    }else{
        target_port = '22'
    }
    
    if("${map.TARGET_USER}"){
        target_user = "${map.TARGET_USER}"
    }else{
        target_user = 'nobody'
    }
    if("${map.STOP_COMMAND}"){
        stop_command = "${map.STOP_COMMAND}"
    }else{
        stop_command = "${target_dir}/${project}/bin/shutdown.sh"
    }
    
    if("${map.START_COMMAND}"){
        start_command = "${map.START_COMMAND}"
    }else{
        start_command = "${target_dir}/${project}/bin/startup.sh"
    }
    }

    stage('checkout code'){
        deleteDir()
        git branch: "${branch}", credentialsId: "${cerd_id}", url: "${repo_url}"
    }    

    stage('complie project'){
        
        def workspace = "${WORKSPACE}"
        def tools = new rj.ws.jenkins.pipeline.lib.util()
        build_dir = tools.BuildDir(workspace,src_type,tag_name,module_name)[0]
        
        src_dir   = tools.BuildDir(workspace,src_type,tag_name,module_name)[1] 
        
        git_version = sh(returnStdout: true, script: 'git rev-parse HEAD').trim().take(7)
        commit_sum  = sh(returnStdout: true, script: 'git rev-list HEAD --first-parent --count').trim()
        def today   = sh(script: 'date +%Y%m%d.%H%M', returnStdout:true).trim()
        target_package = "${project}_${version_major}.${version_minor}_${today}.${commit_sum}_${git_version}.tar.gz" 
        def build = new rj.ws.jenkins.pipeline.lib.build()
        
        def cf = new rj.ws.jenkins.pipeline.lib.util()
        
        
        switch ("${app_lang}"){
        case "java":
            cf.get_java(build_dir,apollo_token,apollo_env,apollo_appid,apollo_namespace,config_file)
            build.compile_java(build_dir,build_command,project,target_package)
            break;
        case "nodejs":
            cf.get_nodejs(build_dir,apollo_token,apollo_env,apollo_appid,apollo_namespace,config_file)
            build.compile_nodejs(build_dir,is_compile,build_command,target_package)
            break;
        case "php":
            cf.get_php(build_dir,apollo_token,apollo_env,apollo_appid,apollo_namespace,config_file)
            build.compile_php(build_dir,is_compile,build_command,target_package)
            break;
        }
        
        archiveArtifacts artifacts: "${target_package}", onlyIfSuccessful: true
    }
    
    stage('deploy'){
          
        def d = new rj.ws.jenkins.pipeline.lib.deploy()
        switch ("${app_lang}"){
        case "java":
            d.java_deploy(src_dir,target_user,target_hosts,target_port,target_dir,target_package,project,stop_command,start_command,git_version)
            break;
        case "nodejs":
            d.web_deploy(src_dir,target_user,target_hosts,target_port,target_dir,target_package,project,stop_command,start_command,git_version)
            break;
        case "php":
            d.web_deploy(src_dir,target_user,target_hosts,target_port,target_dir,target_package,project,stop_command,start_command,git_version)
            break;
        }
    }

    stage('notify'){
        emailext body: "CI project:${BUILD_URL}\r\n", subject: '构建结果通知[成功]', to: "${email}"
    }
    
  }
}

