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

package org.kaaproject.kaa.common.dto.ctl;

import java.io.Serializable;
import java.util.Set;

import org.kaaproject.kaa.common.dto.HasId;

/**
 * A Common Type Library schema.
 *
 * @since v0.8.0
 */
public class CTLSchemaDto implements HasId, Serializable {

    private static final long serialVersionUID = -7601241323233814152L;

    private String id;
    private CTLSchemaMetaInfoDto metaInfo;
    private Integer version;
    private String body;
    private String defaultRecord;
    private String createdUsername;
    private long createdTime;    
    private Set<CTLSchemaDto> dependencySet;

    public CTLSchemaDto() {
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    public CTLSchemaMetaInfoDto getMetaInfo() {
        return metaInfo;
    }

    public void setMetaInfo(CTLSchemaMetaInfoDto metaInfo) {
        this.metaInfo = metaInfo;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getDefaultRecord() {
        return defaultRecord;
    }

    public void setDefaultRecord(String defaultRecord) {
        this.defaultRecord = defaultRecord;
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

    public Set<CTLSchemaDto> getDependencySet() {
        return dependencySet;
    }

    public void setDependencySet(Set<CTLSchemaDto> dependencySet) {
        this.dependencySet = dependencySet;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((dependencySet == null) ? 0 : dependencySet.hashCode());
        result = prime * result + ((metaInfo == null) ? 0 : metaInfo.hashCode());
        result = prime * result + ((version == null) ? 0 : version.hashCode());
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
        CTLSchemaDto other = (CTLSchemaDto) obj;
        if (dependencySet == null) {
            if (other.dependencySet != null)
                return false;
        } else if (!dependencySet.equals(other.dependencySet))
            return false;
        if (metaInfo == null) {
            if (other.metaInfo != null)
                return false;
        } else if (!metaInfo.equals(other.metaInfo))
            return false;
        if (version == null) {
            if (other.version != null)
                return false;
        } else if (!version.equals(other.version))
            return false;
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("CTLSchemaDto [id=");
        builder.append(id);
        builder.append(", metaInfo=");
        builder.append(metaInfo);
        builder.append(", version=");
        builder.append(version);
        builder.append(", body=");
        builder.append(body);
        builder.append(", defaultRecord=");
        builder.append(defaultRecord);
        builder.append(", createdUsername=");
        builder.append(createdUsername);
        builder.append(", createdTime=");
        builder.append(createdTime);
        builder.append(", dependencySet=");
        builder.append(dependencySet);
        builder.append("]");
        return builder.toString();
    }
    
}
