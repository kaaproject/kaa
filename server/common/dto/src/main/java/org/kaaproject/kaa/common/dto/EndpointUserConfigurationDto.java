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

package org.kaaproject.kaa.common.dto;

import java.io.Serializable;

public class EndpointUserConfigurationDto implements Serializable {

    private static final long serialVersionUID = -1463982688020241482L;

    private String userId;
    private String appToken;
    private Integer schemaVersion;
    private String body;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getAppToken() {
        return appToken;
    }

    public void setAppToken(String appToken) {
        this.appToken = appToken;
    }

    public Integer getSchemaVersion() {
        return schemaVersion;
    }

    public void setSchemaVersion(Integer schemaVersion) {
        this.schemaVersion = schemaVersion;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        EndpointUserConfigurationDto that = (EndpointUserConfigurationDto) o;

        if (appToken != null ? !appToken.equals(that.appToken) : that.appToken != null) {
            return false;
        }
        if (body != null ? !body.equals(that.body) : that.body != null) {
            return false;
        }
        if (schemaVersion != null ? !schemaVersion.equals(that.schemaVersion) : that.schemaVersion != null) {
            return false;
        }
        if (userId != null ? !userId.equals(that.userId) : that.userId != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = userId != null ? userId.hashCode() : 0;
        result = 31 * result + (appToken != null ? appToken.hashCode() : 0);
        result = 31 * result + (schemaVersion != null ? schemaVersion.hashCode() : 0);
        result = 31 * result + (body != null ? body.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "EndpointUserConfigurationDto{" + "userId='" + userId + '\'' + ", appToken='" + appToken + '\'' + ", schemaVersion="
                + schemaVersion + ", body='" + body + '\'' + '}';
    }
}
