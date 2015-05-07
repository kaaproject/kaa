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

public enum KaaPackage {

    BOOTSTRAP("bootstrap", "kaa-bootstrap", "/etc/kaa-bootstrap/conf/bootstrap-server.properties", new String[] { "transport.bindInterface", "transport.publicInterface" }), 
    CONTROL("control", "kaa-control", "/etc/kaa-control/conf/control-server.properties", new String[] {}), 
    OPERATIONS("operations", "kaa-operations", "/etc/kaa-operations/conf/operations-server.properties", new String[] { "transport.bindInterface", "transport.publicInterface" }), 
    ADMIN("admin", "kaa-admin", "/etc/kaa-admin/conf/admin-server.properties", new String[] {});

    String packageName;
    String serviceName;
    String propertiesFile;
    String[] hostProperties;

    KaaPackage(String _packageName, String _serviceName, String _propertiesFile, String[] _hostProperties) {
        packageName = _packageName;
        serviceName = _serviceName;
        propertiesFile = _propertiesFile;
        hostProperties = _hostProperties;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getPropertiesFile() {
        return propertiesFile;
    }

    public String[] getHostProperties() {
        return hostProperties;
    }

}
