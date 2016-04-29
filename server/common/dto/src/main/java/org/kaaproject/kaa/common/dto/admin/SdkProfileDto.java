/*
 * Copyright 2014-2016 CyberVision, Inc.
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

package org.kaaproject.kaa.common.dto.admin;

import java.util.List;

import org.kaaproject.kaa.common.dto.HasId;

public class SdkProfileDto extends SdkTokenDto implements HasId {

    private static final long serialVersionUID = 2433663439327120870L;

    private String id;
    private String applicationId;
    private String token;
    private String createdUsername;
    private Long createdTime;
    private Integer endpointCount = 0;

    public SdkProfileDto() {
        super();
    }

    public SdkProfileDto(String applicationId, Integer configurationSchemaVersion,
                            Integer profileSchemaVersion, Integer notificationSchemaVersion,
                            Integer logSchemaVersion,
                            List<String> aefMapIds,
                            String defaultVerifierToken, String applicationToken,
                            String createdUsername, Long createdTime, String name) {
        super(configurationSchemaVersion, 
                profileSchemaVersion, 
                notificationSchemaVersion, 
                logSchemaVersion,
                aefMapIds,
                defaultVerifierToken,
                applicationToken, name);
        this.applicationId = applicationId;
        this.createdUsername = createdUsername;
        this.createdTime = createdTime;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getCreatedUsername() {
        return createdUsername;
    }

    public void setCreatedUsername(String createdUsername) {
        this.createdUsername = createdUsername;
    }

    public Long getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Long createdTime) {
        this.createdTime = createdTime;
    }

    public Integer getEndpointCount() {
        return endpointCount;
    }

    public void setEndpointCount(Integer endpointCount) {
        this.endpointCount = endpointCount;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }
    
    public SdkTokenDto toSdkTokenDto() {
        return new SdkTokenDto(configurationSchemaVersion, 
                profileSchemaVersion, 
                notificationSchemaVersion, 
                logSchemaVersion, 
                aefMapIds, 
                defaultVerifierToken, 
                applicationToken, 
                name);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result
                + ((applicationId == null) ? 0 : applicationId.hashCode());
        result = prime * result
                + ((createdTime == null) ? 0 : createdTime.hashCode());
        result = prime * result
                + ((createdUsername == null) ? 0 : createdUsername.hashCode());
        result = prime * result
                + ((endpointCount == null) ? 0 : endpointCount.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((token == null) ? 0 : token.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        SdkProfileDto other = (SdkProfileDto) obj;
        if (applicationId == null) {
            if (other.applicationId != null) {
                return false;
            }
        } else if (!applicationId.equals(other.applicationId)) {
            return false;
        }
        if (createdTime == null) {
            if (other.createdTime != null) {
                return false;
            }
        } else if (!createdTime.equals(other.createdTime)) {
            return false;
        }
        if (createdUsername == null) {
            if (other.createdUsername != null) {
                return false;
            }
        } else if (!createdUsername.equals(other.createdUsername)) {
            return false;
        }
        if (endpointCount == null) {
            if (other.endpointCount != null) {
                return false;
            }
        } else if (!endpointCount.equals(other.endpointCount)) {
            return false;
        }
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        if (token == null) {
            if (other.token != null) {
                return false;
            }
        } else if (!token.equals(other.token)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("SdkProfileDto [id=");
        builder.append(id);
        builder.append(", applicationId=");
        builder.append(applicationId);
        builder.append(", token=");
        builder.append(token);
        builder.append(", createdUsername=");
        builder.append(createdUsername);
        builder.append(", createdTime=");
        builder.append(createdTime);
        builder.append(", endpointCount=");
        builder.append(endpointCount);
        builder.append("]");
        return builder.toString();
    }

}
