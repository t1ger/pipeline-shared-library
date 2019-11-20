 
package rj.ws.jenkins.pipeline.lib

def compile_java(build_dir,build_command,project,target_package){
    sh "echo target_package=${target_package}"
    sh """
    java -version
    cd ${build_dir} && ${build_command}
    [ -f ${build_dir}/target/${project}*.tar.gz ]&& cp ${build_dir}/target/${project}*.tar.gz  ${build_dir}/${target_package}
    
    """
}

def compile_nodejs(build_dir,is_compile,build_command,target_package){
    sh "echo ${build_dir}"
    sh '''
    node -v
    yarn config set cache-folder /data/caches/yarn
    yarn config set registry "https://registry.npm.taobao.org"
    yarn config set sass_binary_site "https://npm.taobao.org/mirrors/node-sass/"
    yarn config set phantomjs_cdnurl "http://cnpmjs.org/downloads"

    npm set sass_binary_site https://npm.taobao.org/mirrors/node-sass
    '''
    
    if(Boolean.valueOf("${is_compile}")){
        def webDist=build_dir + '/dist'
        
        sh """
        hook-binary-mirror
        cd ${build_dir} && ${build_command} && cd -
        """
        
        webBuild(webDist,target_package)
    }else{
        
        sh """
        hook-binary-mirror
        cd ${build_dir} && ${build_command} && cd -
        """
        
        webBuild(buildDir,target_package)
    }
}

def compile_php(build_dir,is_compile,build_command,target_package){

    if(Boolean.valueOf("${is_compile}")){
        sh " cd ${build_dir} && ${build_command} && cd -"
        webBuild(build_dir,target_package)
    }else{
        webBuild(build_dir,target_package) 
    }
    
}


def webBuild(src_dir,target_package){
    def package_dir="${WORKSPACE}"
    sh """
        [ -d ${src_dir} ]&&cd ${src_dir}/
        [ -f *.tar.gz ]||tar -zcf ${target_package} * --exclude=*.git --exclude=ansible --exclude=*.tar.gz --exclude=data.json .
        cd -
        if [ ! -e ${package_dir}/${target_package} ]; then
        cp ${src_dir}/${target_package} ${package_dir}
        fi
    """
}
