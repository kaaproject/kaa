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

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Bohdan Khablenko
 * @author Andrew Shvayka
 *
 * @since v0.9.0
 */
public class CredentialsDto implements HasId, Serializable {

    private static final long serialVersionUID = 1000L;

    @JsonProperty("credentialsId")
    private String id;

    private byte[] credentialsBody;
    private CredentialsStatus status;

    public CredentialsDto() {
        this(null, CredentialsStatus.AVAILABLE);
    }

    public CredentialsDto(byte[] credentialsBody, CredentialsStatus status) {
        this(null, credentialsBody, status);
    }

    public CredentialsDto(String credentialsId, byte[] credentialsBody, CredentialsStatus status) {
        this.id = credentialsId;
        this.credentialsBody = credentialsBody != null ? Arrays.copyOf(credentialsBody, credentialsBody.length) : null;
        this.status = status;
    }

    @Override
    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public byte[] getCredentialsBody() {
        return this.credentialsBody;
    }

    public void setCredentialsBody(byte[] credentialsBody) {
        this.credentialsBody = credentialsBody;
    }

    public CredentialsStatus getStatus() {
        return this.status;
    }

    public void setStatus(CredentialsStatus status) {
        this.status = status;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(this.credentialsBody);
        result = prime * result + ((this.id == null) ? 0 : this.id.hashCode());
        result = prime * result + ((this.status == null) ? 0 : this.status.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        CredentialsDto other = (CredentialsDto) obj;
        if (!Arrays.equals(this.credentialsBody, other.credentialsBody)) {
            return false;
        }
        if (this.id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!this.id.equals(other.id)) {
            return false;
        }
        if (this.status != other.status) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("CredentialsDto [id=");
        builder.append(this.id);
        builder.append(", credentialsBody=");
        builder.append(Arrays.toString(this.credentialsBody));
        builder.append(", status=");
        builder.append(this.status);
        builder.append("]");
        return builder.toString();
    }
}
