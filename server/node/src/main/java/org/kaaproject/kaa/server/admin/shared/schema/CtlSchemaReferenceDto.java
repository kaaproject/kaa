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

package org.kaaproject.kaa.server.admin.shared.schema;

import java.io.Serializable;

import org.kaaproject.kaa.common.dto.ctl.CTLSchemaMetaInfoDto;

public class CtlSchemaReferenceDto implements Serializable 
{
    private static final long serialVersionUID = -6668598697779214725L;

    private CTLSchemaMetaInfoDto metaInfo;
    private int version;
    
    public CtlSchemaReferenceDto() {
        super();
    }
    
    public CtlSchemaReferenceDto(CTLSchemaMetaInfoDto metaInfo, int version) {
        super();
        this.metaInfo = metaInfo;
        this.version = version;
    }

    public CTLSchemaMetaInfoDto getMetaInfo() {
        return metaInfo;
    }

    public void setMetaInfo(CTLSchemaMetaInfoDto metaInfo) {
        this.metaInfo = metaInfo;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((metaInfo == null) ? 0 : metaInfo.hashCode());
        result = prime * result + version;
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
        CtlSchemaReferenceDto other = (CtlSchemaReferenceDto) obj;
        if (metaInfo == null) {
            if (other.metaInfo != null)
                return false;
        } else if (!metaInfo.equals(other.metaInfo))
            return false;
        if (version != other.version)
            return false;
        return true;
    }

}
