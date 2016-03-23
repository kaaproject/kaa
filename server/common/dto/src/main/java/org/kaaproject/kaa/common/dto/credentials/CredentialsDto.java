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
import java.util.Arrays;

import org.kaaproject.kaa.common.dto.HasId;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Bohdan Khablenko
 * @author Andrew Shvayka
 *
 * @since v0.9.0
 */
@JsonInclude(Include.NON_NULL)
public final class CredentialsDto implements HasId, Serializable {

    private static final long serialVersionUID = 1L;

    @JsonProperty("credentialsId")
    private String id;

    @JsonProperty("credentials")
    private byte[] credentials;

    @JsonProperty("status")
    private CredentialsStatus status;
    
    public CredentialsDto() {
        super();
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    public byte[] getCredentials() {
        return credentials;
    }

    public void setCredentials(byte[] credentials) {
        this.credentials = credentials;
    }

    public CredentialsStatus getStatus() {
        return status;
    }

    public void setStatus(CredentialsStatus status) {
        this.status = status;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(credentials);
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((status == null) ? 0 : status.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        CredentialsDto other = (CredentialsDto) obj;
        if (!Arrays.equals(credentials, other.credentials))
            return false;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (status != other.status)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "CredentialsDto [id=" + id + ", credentials=" + Arrays.toString(credentials) + ", status=" + status + "]";
    }

}
