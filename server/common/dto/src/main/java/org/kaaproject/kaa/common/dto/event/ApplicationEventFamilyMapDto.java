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

package org.kaaproject.kaa.common.dto.event;

import java.io.Serializable;
import java.util.List;

import org.kaaproject.kaa.common.dto.HasId;

public class ApplicationEventFamilyMapDto implements HasId, Serializable {

    private static final long serialVersionUID = 1290579880736903999L;

    private String id;
    private String applicationId;
    private String ecfId;
    private String ecfName;
    private int version;
    private String createdUsername;
    private long createdTime;
    private List<ApplicationEventMapDto> eventMaps;

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

    public String getEcfId() {
        return ecfId;
    }

    public void setEcfId(String ecfId) {
        this.ecfId = ecfId;
    }

    public String getEcfName() {
        return ecfName;
    }

    public void setEcfName(String ecfName) {
        this.ecfName = ecfName;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }
    
    public List<ApplicationEventMapDto> getEventMaps() {
        return eventMaps;
    }

    public void setEventMaps(List<ApplicationEventMapDto> eventMaps) {
        this.eventMaps = eventMaps;
    }
    
    public String getCreatedUsername() {
        return createdUsername;
    }

    public void setCreatedUsername(String createdUsername) {
        this.createdUsername = createdUsername;
    }

    public long getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(long createdTime) {
        this.createdTime = createdTime;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((applicationId == null) ? 0 : applicationId.hashCode());
        result = prime * result + (int) (createdTime ^ (createdTime >>> 32));
        result = prime * result
                + ((createdUsername == null) ? 0 : createdUsername.hashCode());
        result = prime * result + ((ecfId == null) ? 0 : ecfId.hashCode());
        result = prime * result + ((ecfName == null) ? 0 : ecfName.hashCode());
        result = prime * result
                + ((eventMaps == null) ? 0 : eventMaps.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
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
        ApplicationEventFamilyMapDto other = (ApplicationEventFamilyMapDto) obj;
        if (applicationId == null) {
            if (other.applicationId != null) {
                return false;
            }
        } else if (!applicationId.equals(other.applicationId)) {
            return false;
        }
        if (createdTime != other.createdTime) {
            return false;
        }
        if (createdUsername == null) {
            if (other.createdUsername != null) {
                return false;
            }
        } else if (!createdUsername.equals(other.createdUsername)) {
            return false;
        }
        if (ecfId == null) {
            if (other.ecfId != null) {
                return false;
            }
        } else if (!ecfId.equals(other.ecfId)) {
            return false;
        }
        if (ecfName == null) {
            if (other.ecfName != null) {
                return false;
            }
        } else if (!ecfName.equals(other.ecfName)) {
            return false;
        }
        if (eventMaps == null) {
            if (other.eventMaps != null) {
                return false;
            }
        } else if (!eventMaps.equals(other.eventMaps)) {
            return false;
        }
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        return version == other.version;
    }

    @Override
    public String toString() {
        return "ApplicationEventFamilyMapDto [id=" + id + ", applicationId="
                + applicationId + ", ecfId=" + ecfId + ", ecfName=" + ecfName
                + ", version=" + version + ", createdUsername="
                + createdUsername + ", createdTime=" + createdTime
                + ", eventMaps=" + eventMaps + "]";
    }

}
