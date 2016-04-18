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

public class EventClassFamilyVersionStateDto implements Serializable {

    private static final long serialVersionUID = -1658174097110691624L;

    private String ecfId;
    private int version;

    public EventClassFamilyVersionStateDto() {
        super();
    }

    protected EventClassFamilyVersionStateDto(String ecfId, int version) {
        super();
        this.ecfId = ecfId;
        this.version = version;
    }

    public void setEcfId(String ecfId) {
        this.ecfId = ecfId;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getEcfId() {
        return ecfId;
    }

    public int getVersion() {
        return version;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((ecfId == null) ? 0 : ecfId.hashCode());
        result = prime * result + version;
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
        EventClassFamilyVersionStateDto other = (EventClassFamilyVersionStateDto) obj;
        if (ecfId == null) {
            if (other.ecfId != null) {
                return false;
            }
        } else if (!ecfId.equals(other.ecfId)) {
            return false;
        }
        if (version != other.version) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("EventClassFamilyStateDto [ecfId=");
        builder.append(ecfId);
        builder.append(", version=");
        builder.append(version);
        builder.append("]");
        return builder.toString();
    }
}
