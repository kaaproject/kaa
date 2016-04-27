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

public class ApplicationDto implements HasId, Serializable {

    private static final long serialVersionUID = -5596816884035813927L;

    private String id;
    private String applicationToken;
    private String name;
    private int sequenceNumber;
    private String tenantId;
    private String credentialsServiceName;

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    public String getApplicationToken() {
        return applicationToken;
    }

    public void setApplicationToken(String applicationToken) {
        this.applicationToken = applicationToken;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(int sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getCredentialsServiceName() {
        return this.credentialsServiceName;
    }

    public void setCredentialsServiceName(String credentialsServiceName) {
        this.credentialsServiceName = credentialsServiceName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ApplicationDto that = (ApplicationDto) o;

        if (sequenceNumber != that.sequenceNumber) {
            return false;
        }
        if (applicationToken != null ? !applicationToken.equals(that.applicationToken) : that.applicationToken != null) {
            return false;
        }
        if (tenantId != null ? !tenantId.equals(that.tenantId) : that.tenantId != null) {
            return false;
        }
        if (name != null ? !name.equals(that.name) : that.name != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = applicationToken != null ? applicationToken.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + sequenceNumber;
        result = 31 * result + (tenantId != null ? tenantId.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ApplicationDto{" + "id='" + id + '\'' + ", applicationToken='" + applicationToken + '\'' + ", name='" + name + '\''
                + ", sequenceNumber=" + sequenceNumber + ", tenantId='" + tenantId + ", credentialsServiceName='" + credentialsServiceName + '\'' + '}';
    }
}
