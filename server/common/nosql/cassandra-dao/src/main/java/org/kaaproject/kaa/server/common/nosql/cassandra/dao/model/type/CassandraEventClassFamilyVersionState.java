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

package org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.type;

import static org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraModelConstants.EVENT_CLASS_FAMILY_VERSION_STATE_ECF_ID_PROPERTY;
import static org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraModelConstants.EVENT_CLASS_FAMILY_VERSION_STATE_ECF_VERSION_PROPERTY;
import static org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraModelConstants.EVENT_CLASS_FAMILY_VERSION_STATE_USER_TYPE_NAME;

import java.io.Serializable;

import org.kaaproject.kaa.common.dto.EventClassFamilyVersionStateDto;
import org.kaaproject.kaa.server.common.dao.model.ToDto;

import com.datastax.driver.mapping.annotations.Field;
import com.datastax.driver.mapping.annotations.Transient;
import com.datastax.driver.mapping.annotations.UDT;

@UDT(name = EVENT_CLASS_FAMILY_VERSION_STATE_USER_TYPE_NAME)
public final class CassandraEventClassFamilyVersionState implements ToDto<EventClassFamilyVersionStateDto>, Serializable {

    @Transient
    private static final long serialVersionUID = 3766947955702551264L;

    @Field(name = EVENT_CLASS_FAMILY_VERSION_STATE_ECF_ID_PROPERTY)
    private String ecfId;
    @Field(name = EVENT_CLASS_FAMILY_VERSION_STATE_ECF_VERSION_PROPERTY)
    private int version;

    public CassandraEventClassFamilyVersionState() {
    }

    public CassandraEventClassFamilyVersionState(EventClassFamilyVersionStateDto dto) {
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
        CassandraEventClassFamilyVersionState other = (CassandraEventClassFamilyVersionState) obj;
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
