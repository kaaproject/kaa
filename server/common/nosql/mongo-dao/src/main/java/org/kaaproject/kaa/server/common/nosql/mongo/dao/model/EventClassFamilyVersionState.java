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

package org.kaaproject.kaa.server.common.nosql.mongo.dao.model;

import org.kaaproject.kaa.common.dto.EventClassFamilyVersionStateDto;
import org.kaaproject.kaa.server.common.dao.model.ToDto;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;

import static org.kaaproject.kaa.server.common.nosql.mongo.dao.model.MongoModelConstants.ECF_ID;
import static org.kaaproject.kaa.server.common.nosql.mongo.dao.model.MongoModelConstants.EVENT_CLASS_FAMILY_VERSION;

public final class EventClassFamilyVersionState implements ToDto<EventClassFamilyVersionStateDto>, Serializable {

    private static final long serialVersionUID = 3766947955702551264L;

    @Field(ECF_ID)
    private String ecfId;
    @Field(EVENT_CLASS_FAMILY_VERSION)
    private int version;

    public EventClassFamilyVersionState() {
    }

    public EventClassFamilyVersionState(EventClassFamilyVersionStateDto dto) {
        this.ecfId = dto.getEcfId();
        this.version = dto.getVersion();
    }

    public String getEcfId() {
        return ecfId;
    }

    public void setEcfId(String ecfId) {
        this.ecfId = ecfId;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    @Override
    public EventClassFamilyVersionStateDto toDto() {
        EventClassFamilyVersionStateDto dto = new EventClassFamilyVersionStateDto();
        dto.setEcfId(ecfId);
        dto.setVersion(version);
        return dto;
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
        EventClassFamilyVersionState other = (EventClassFamilyVersionState) obj;
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
        return "EventClassFamilyVersionState [ecfId=" + ecfId + ", version=" + version + "]";
    }


}
