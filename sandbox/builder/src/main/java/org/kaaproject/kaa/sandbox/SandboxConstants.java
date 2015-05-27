/*
 * Copyright 2014 CyberVision, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kaaproject.kaa.sandbox;

public interface SandboxConstants {

    public static final String DISTRO_PATH_VAR = "\\$\\{distro_path\\}";
    public static final String PACKAGE_NAME_VAR = "\\$\\{package_name\\}";
    public static final String SERVICE_NAME_VAR = "\\$\\{service_name\\}";
    
    public static final String WEB_ADMIN_PORT_VAR = "\\$\\{web_admin_port\\}";
    public static final String SSH_FORWARD_PORT_VAR = "\\$\\{ssh_forward_port\\}";
    
    public static final String STOP_SERVICES_VAR = "\\$\\{stop_services\\}";
    public static final String SET_NEW_HOSTS = "\\$\\{set_new_hosts\\}";
    public static final String START_SERVICES_VAR = "\\$\\{start_services\\}";
    
    public static final String DEFAULT_HOST = "127.0.0.1";
    
    public static final int DEFAULT_SSH_FORWARD_PORT = 2222;
    public static final int DEFAULT_WEB_ADMIN_PORT = 8080;
    
    public static final String SSH_USERNAME = "kaa";
    public static final String SSH_PASSWORD = "kaa";
    public static final String SHARED_FOLDER = "kaa-shared";
    public static final String DISTRO_PATH = "/"+SHARED_FOLDER+"/distro";
    public static final String DEMO_PROJECTS = "demo_projects";
    public static final String DEMO_PROJECTS_PATH = "/"+SHARED_FOLDER+"/"+DEMO_PROJECTS;    
    public static final String DEMO_PROJECTS_XML = "demo_projects.xml";
    public static final String SANDBOX_PATH = "/"+SHARED_FOLDER+"/sandbox";
    
    public static final String SANDBOX_SPLASH_PY_TEMPLATE = "scripts/sandbox-splash.py.template";
    public static final String SANDBOX_SPLASH_PY = "sandbox-splash.py";

    public static final String CHANGE_KAA_HOST_TEMPLATE = "scripts/change_kaa_host.sh.template";
    public static final String CHANGE_KAA_HOST = "change_kaa_host.sh";
    
    public static final String SANDBOX_FOLDER = "/usr/lib/kaa-sandbox";
    public static final String ADMIN_FOLDER = "/usr/lib/kaa-admin";

    public static final String LOG_DUMP_LOCATION = "sandbox/builder/target/sandbox_logs/";
}
