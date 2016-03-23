/**
 *  Copyright 2014-2016 CyberVision, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.kaaproject.kaa.common.dto.credentials;

import java.io.Serializable;
import java.util.Optional;

import org.kaaproject.kaa.common.dto.HasId;

/**
 * @author Bohdan Khablenko
 * @author Andrew Shvayka
 *
 * @since v0.9.0
 */
public class EndpointRegistrationInfoDto implements HasId, Serializable {

    private static final long serialVersionUID = 1L;
    
    private String id;
    
    private String applicationId;
    
    /**
     * This is optional
     */
    private String credentialsId;
    
    private String endpointId;

    private Integer serverProfileVersion;
    
    private String serverProfileBody;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public String getCredentialsId() {
        return credentialsId;
    }

    public void setCredentialsId(String credentialsId) {
        this.credentialsId = credentialsId;
    }

    public String getEndpointId() {
        return endpointId;
    }

    public void setEndpointId(String endpointId) {
        this.endpointId = endpointId;
    }

    public Optional<Integer> getServerProfileVersion() {
        return Optional.ofNullable(serverProfileVersion);
    }

    public void setServerProfileVersion(Integer serverProfileVersion) {
        this.serverProfileVersion = serverProfileVersion;
    }

    public Optional<String> getServerProfileBody() {
        return Optional.ofNullable(serverProfileBody);
    }

    public void setServerProfileBody(String serverProfileBody) {
        this.serverProfileBody = serverProfileBody;
    }
}
