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
package org.kaaproject.kaa.common.dto.user;

import java.io.Serializable;
import java.util.Arrays;

import org.kaaproject.kaa.common.dto.AbstractDetailDto;
import org.kaaproject.kaa.common.dto.HasId;

/**
 * Represents user verifier metadata.
 * 
 * @author Andrew Shvayka
 *
 */
public class UserVerifierDto extends AbstractDetailDto implements HasId, Serializable {

    private static final long serialVersionUID = -4616508101972356690L;

    private String id;
    private int verifierId;
    private String applicationId;
    private String className;
    private byte[] rawConfiguration;

    public UserVerifierDto() {
        super();
    }

    public UserVerifierDto(AbstractDetailDto detailsDto) {
        super(detailsDto);
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    public int getVerifierId() {
        return verifierId;
    }

    public void setVerifierId(int verifierId) {
        this.verifierId = verifierId;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public byte[] getRawConfiguration() {
        return rawConfiguration;
    }

    public void setRawConfiguration(byte[] rawConfiguration) {
        this.rawConfiguration = rawConfiguration;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((applicationId == null) ? 0 : applicationId.hashCode());
        result = prime * result + ((className == null) ? 0 : className.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + Arrays.hashCode(rawConfiguration);
        result = prime * result + verifierId;
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
        UserVerifierDto other = (UserVerifierDto) obj;
        if (applicationId == null) {
            if (other.applicationId != null) {
                return false;
            }
        } else if (!applicationId.equals(other.applicationId)) {
            return false;
        }
        if (className == null) {
            if (other.className != null) {
                return false;
            }
        } else if (!className.equals(other.className)) {
            return false;
        }
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        if (!Arrays.equals(rawConfiguration, other.rawConfiguration)) {
            return false;
        }
        if (verifierId != other.verifierId) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("UserVerifierDto [id=");
        builder.append(id);
        builder.append(", verifierId=");
        builder.append(verifierId);
        builder.append(", applicationId=");
        builder.append(applicationId);
        builder.append(", className=");
        builder.append(className);
        builder.append(", rawConfiguration=");
        builder.append(Arrays.toString(rawConfiguration));
        builder.append("]");
        return builder.toString();
    }
}
