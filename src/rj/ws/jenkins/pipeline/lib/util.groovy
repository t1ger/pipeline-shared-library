package rj.ws.jenkins.pipeline.lib

import groovy.json.JsonSlurper

def BuildDir(workspace,src_type,tag_name,module_name) {
    def srcDir = workspace
    if(src_type == "git") {
        buildDir = "${workspace}"
        if(module_name){
            srcDir = "${workspace}/${module_name}"
        }else{
            srcDir = "${workspace}"
        }
        
    }else{
        if(tag_name == "null") {
            def srcTmp = repo_url.split("/")[-1]
            srcDir = "${workspace}/${srcTmp}"
        }else{
            srcDir = "${workspace}/${tag_name}"
        }
    }
    buildDir = srcDir
    return [buildDir,srcDir]
}


def get_java(build_dir,apollo_token,apollo_env,apollo_appid,apollo_namespace,config_file){
  def work_dir="${WORKSPACE}"
  sh """
 echo ${apollo_token}
 curl -s http://cf.company.com.cn/openapi/v1/envs/${apollo_env}/apps/${apollo_appid}/clusters/default/namespaces/${apollo_namespace} \
 -H "Accept-Encoding:json" \
 -H "authorization:${apollo_token}"|jq -jr '.items[] | ("\\(.key) \\(.value)\n")' >${work_dir}/data.json
""" 
cf_contents = readFile ("${work_dir}/data.json").split('\n')


cf_contents.each { line, count ->
def key = line.split(' ')[0]
def value    = line.split(' ')[1].replaceAll(/\&/,/\\&/).replaceAll(/\{/,/\\{/).replaceAll(/\}/,/\\}/)
 println ' you are parsing line : ' + key+ '='+ value
 sh "sed -i -- 's%${key}%${value}%'  ${config_file}" 
}

cf = readFile ("${work_dir}/${config_file}").replaceAll(/\[(.*)\]/,'$1')
println cf
writeFile file: "${work_dir}/${config_file}", text: cf
 
}

def get_nodejs(build_dir,apollo_token,apollo_env,apollo_appid,apollo_namespace,config_file){
 def work_dir="${WORKSPACE}"
 sh """
 curl -s http://cf.company.com.cn/openapi/v1/envs/${apollo_env}/apps/${apollo_appid}/clusters/default/namespaces/${apollo_namespace} \
 -H "Accept-Encoding:json" \
 -H "authorization:${apollo_token}"|jq -jr '.items[] | ("\\(.key) \\(.value)\n")' >${work_dir}/data.json
""" 
cf_contents = readFile ("${work_dir}/data.json").split('\n')

cf_contents.each { line, count ->
def key = line.split(' ')[0]
def value    = line.split(' ')[1].replaceAll(/\&/,/\\&/).replaceAll(/\{/,/\\{/).replaceAll(/\}/,/\\}/)
 println ' you are parsing line : ' + key+ '='+ value
 sh "sed -i -- 's%${key}%${value}%'  ${config_file}" 
}
}

//@NonCPS 
def get_php(build_dir,apollo_token,apollo_env,apollo_appid,apollo_namespace,config_file){
def work_dir="${WORKSPACE}"

sh """
 echo ${apollo_token}
 curl -s http://cf.company.com.cn/openapi/v1/envs/${apollo_env}/apps/${apollo_appid}/clusters/default/namespaces/${apollo_namespace} \
 -H "Accept-Encoding:json" \
 -H "authorization:${apollo_token}"|jq -jr '.items[] | ("\\(.key) #\\(.value)#\n")' >${work_dir}/data.json
"""
cf_contents = readFile ("${work_dir}/data.json").split('\n')

cf_contents.each { line, count ->
def key = line.split(' ')[0]
def value    = line.split(' ')[1].replaceAll(/\&/,/\\&/).replaceAll(/\{/,/\\{/).replaceAll(/\}/,/\\}/)
 println ' you are parsing line : ' + key+ '='+ value
 
//  sh 'sed -i \'s!^\$' + key + '[[:blank:]]*=[[:blank:]]*.*!\$'+key+'=\''+value+'\';!\'  ' + config_file 
sh "sed -i -- 's%^\$${key}[[:blank:]]*=[[:blank:]]*.*%\$${key}=${value};%'  ${config_file}" 
}
cf = readFile ("${work_dir}/${config_file}").replaceAll(~/#/,/\'/)
println cf
writeFile file: "${work_dir}/${config_file}", text: cf

}
