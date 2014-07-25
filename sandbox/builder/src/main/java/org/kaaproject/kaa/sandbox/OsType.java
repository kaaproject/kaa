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

public enum OsType {
    
    UBUNTU("dpkg -i ${distro_path}/${package_name}.deb",
           "service ${service_name} start",
           "service ${service_name} stop");
    
    String installPackageTemplate;
    String startServiceTemplate;
    String stopServiceTemplate;
    
    OsType(String _installPackageTemplate, String _startServiceTemplate, String _stopServiceTemplate) {
        installPackageTemplate = _installPackageTemplate;
        startServiceTemplate = _startServiceTemplate;
        stopServiceTemplate = _stopServiceTemplate;
    }
    
    public String getInstallPackageTemplate() {
        return installPackageTemplate;
    }

    public String getStartServiceTemplate() {
        return startServiceTemplate;
    }
    
    public String getStopServiceTemplate() {
        return stopServiceTemplate;
    }
    
}
