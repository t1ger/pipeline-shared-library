package rj.ws.jenkins.pipeline.lib


def java_deploy(src_dir,target_user,target_hosts,target_port,target_dir,target_package,project,stop_command,start_command,git_version){
    try{
                println('部署准备')
                
                writeFile file: 'ansible/ansible.cfg', text:'''
[defaults]
hostfile = inventory
roles_path = roles
#remote_user=root
ask_pass = False
remote_tmp = $HOME/.ansible/tmp
host_key_checking = False
system_warnings = False
#private_key_file = /home/gitlab-runner/.ssh/id_rsa
[ssh_connection]
scp_if_ssh=True
'''
                
                
            	writeFile file: 'ansible/java_deploy.yml', text: '''
---
- hosts: all
  sudo: yes
  tasks:

    - name: "create deploy project folder if not exists"
      file:
        path: "{{ target_dir }}"
        state: directory
        owner: "{{ user }}"
        group: "{{ user }}"
        
    - name: "copy project tar application"
      copy:
        src: "{{ src_dir }}/{{ target_package }}"
        dest: "{{ target_dir }}"
        owner: "{{ user }}"
        group: "{{ user }}"
        
    - name: "delete project dir if exist "
      file:
        path: "{{ target_dir }}/{{ project }}_{{ git_version }}"
        state: absent
        
    - name: "crete project dir if not exist"
      file:
        path: "{{ target_dir }}/{{ project }}_{{ git_version }}"
        state: directory
        owner: "{{ user }}"
        group: "{{ user }}"

    - name: "unarchive  project package"
      unarchive:
        src: "{{ target_dir }}/{{ target_package }}"
        dest: "{{ target_dir }}/{{ project }}_{{ git_version }}"
        copy: no
        mode: 775
    
    - name: get running processes
      shell: "if [ $(ps -ef | grep -v grep | grep -w {{ project }} -c) -gt 0 ];then echo success; else echo fail; fi;"
      register: service_exists

    - debug: var=service_exists

    - name: stop project servce if runing
      shell: "sudo {{target_dir}}/{{project}}/{{stop_command}}"
      when: service_exists.stdout == 'success'

    - name: Remember current project /project link
      stat:
        path: "{{target_dir}}/project"
      register: project_before_link

    - name: Link /project to latest build
      file:
        src: "{{target_dir}}/{{project}}_{{ git_version }}"
        dest: "{{target_dir}}/{{project}}"
        state: link

    - name: Start service
      shell: "sudo {{target_dir}}/{{project}}/{{start_command}}"
    
    - name: Get deprecated file
      find:
        paths: "{{target_dir}}"
        age: 1h
        age_stamp: atime
        patterns: "{{ project }}_*"
        file_type: file
      register: deprecated_file

    - name: clear deprecated version
      file:
        path: "{{ item['path'] }}"
        state: absent
      with_items:  "{{ deprecated_file['files'] }}"
    
    - name: Get deprecated directory
      find:
        paths: "{{target_dir}}"
        patterns: "{{ project }}_*"
        file_type: directory
      register: deprecated_directory
              
    - name: Determine old directories
      set_fact:
        old_dirs: "{{ (deprecated_directory.files|sort(attribute='mtime', reverse=True))[3:] }}"

    - name: Remove old directories
      file:
        path: "{{ item['path'] }}"
        state: absent
      with_items: "{{ old_dirs }}"
      when: deprecated_directory.matched > 3
'''
            
            def hosts=target_hosts.split(',')
            sh "echo > ansible/hosts"
            //sh "echo [product]>>ansible/hosts "
		    for(String host in hosts) {
			sh "echo ${host} ansible_ssh_user=user ansible_ssh_pass=password ansible_sudo_pass='password'>>ansible/hosts"
		    }
		    
		    echo "src=${src_dir}"
		    echo "project=${project}"
		    echo "user=${user}"
		    echo "git_version=${git_version}"
		    
		    println('部署服务')
		    
		    sh """
		    set +x
		    export ANSIBLE_HOST_KEY_CHECKING=False
		    ansible-playbook ansible/java_deploy.yml --inventory=ansible/hosts  -e src_dir='${src_dir}' -e git_version='${git_version}' \
		    -e target_dir='${target_dir}' -e target_package='${target_package}' -e project='${project}' -e user='${target_user}' \
		    -e 'start_command="${start_command}"' -e 'stop_command="${stop_command}"'
            set -x
            """
	} catch (e){
	    currentBuild.description='包发布失败'
	    error '包发布失败'
	    
	}
}





def web_deploy(src_dir,target_user,target_hosts,target_port,target_dir,target_package,project,stop_command,start_command,git_version){
     try{
                println('部署准备')
                
                writeFile file: 'ansible/ansible.cfg', text:'''
[defaults]
hostfile = inventory
roles_path = roles
#remote_user=root
ask_pass = False
remote_tmp = $HOME/.ansible/tmp
host_key_checking = False
system_warnings = False
#private_key_file = /home/gitlab-runner/.ssh/id_rsa
[ssh_connection]
scp_if_ssh=True
'''
                
                
            	writeFile file: 'ansible/web_deploy.yml', text: '''
---
- hosts: all
  sudo: yes
  tasks:
    - name: "create deploy project folder if not exists"
      file:
        path: "{{ target_dir }}"
        state: directory
        owner: "{{ user }}"
        group: "{{ user }}"
        
    - name: "copy project tar application"
      copy:
        src: "{{ src_dir }}/{{ target_package }}"
        dest: "{{ target_dir }}"
        owner: "{{ user }}"
        group: "{{ user }}"
        
    - name: "delete project dir if exist "
      file:
        path: "{{ target_dir }}/{{ project }}_{{ git_version }}"
        state: absent
        
    - name: "crete project dir if not exist"
      file:
        path: "{{ target_dir }}/{{ project }}_{{ git_version }}"
        state: directory
        owner: "{{ user }}"
        group: "{{ user }}"

    - name: "unarchive  project package"
      unarchive:
        src: "{{ target_dir }}/{{ target_package }}"
        dest: "{{ target_dir }}/{{ project }}_{{ git_version }}"
        copy: no
        mode: 775
    
    - name: get running processes
      shell: "if [ $(ps -ef | grep -v grep | grep -w {{ project }} -c) -gt 0 ];then echo success; else echo fail; fi;"
      register: service_exists

    - debug: var=service_exists

    - name: stop project servce if runing
      shell: "sudo {{target_dir}}/{{project}}/{{stop_command}}"
      when: service_exists.stdout == 'success'

    - name: Remember current project /project link
      stat:
        path: "{{target_dir}}/project"
      register: project_before_link

    - name: Link /project to latest build
      file:
        src: "{{target_dir}}/{{project}}_{{ git_version }}"
        dest: "{{target_dir}}/{{project}}"
        state: link

    - name: Start service
      shell: "sudo {{start_command}}"
      when: service_exists.stdout == 'success'
    
    - name: Get deprecated file
      find:
        paths: "{{target_dir}}"
        age: 1h
        age_stamp: atime
        patterns: "{{ project }}_*"
        file_type: file
      register: deprecated_file

    - name: clear deprecated version
      file:
        path: "{{ item['path'] }}"
        state: absent
      with_items:  "{{ deprecated_file['files'] }}"
    
    - name: Get deprecated directory
      find:
        paths: "{{target_dir}}"
        patterns: "{{ project }}_*"
        file_type: directory
      register: deprecated_directory
              
    - name: Determine old directories
      set_fact:
        old_dirs: "{{ (deprecated_directory.files|sort(attribute='mtime', reverse=True))[3:] }}"

    - name: Remove old directories
      file:
        path: "{{ item['path'] }}"
        state: absent
      with_items: "{{ old_dirs }}"
      when: deprecated_directory.matched > 3
'''
            
            def hosts=target_hosts.split(',')
            sh "echo > ansible/hosts"
            //sh "echo [product]>>ansible/hosts "
		    for(String host in hosts) {
			sh "echo ${host} ansible_ssh_user=user ansible_ssh_pass=password ansible_sudo_pass='password'>>ansible/hosts"
		    }
		    
		    echo "src=${src_dir}"
		    echo "project=${project}"
		    echo "user=${user}"
		    echo "git_version=${git_version}"
		    
		    println('部署服务')
		    
		    sh """
		    set +x
		    export ANSIBLE_HOST_KEY_CHECKING=False
		    ansible-playbook ansible/web_deploy.yml --inventory=ansible/hosts  -e src_dir='${src_dir}' -e git_version='${git_version}' \
		    -e target_dir='${target_dir}' -e target_package='${target_package}' -e project='${project}' -e user='${target_user}' \
		    -e 'start_command="${start_command}"' -e 'stop_command="${stop_command}"'
            set -x
            """
	} catch (e){
	    currentBuild.description='包发布失败'
	    error '包发布失败'
	    
	}
}
