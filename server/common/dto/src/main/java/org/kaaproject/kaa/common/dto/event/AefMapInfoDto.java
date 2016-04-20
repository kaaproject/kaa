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

public class AefMapInfoDto extends EcfInfoDto {

    private static final long serialVersionUID = -3070865577878738788L;
    
    private String aefMapId;

    public String getAefMapId() {
        return aefMapId;
    }

    public void setAefMapId(String aefMapId) {
        this.aefMapId = aefMapId;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result
                + ((aefMapId == null) ? 0 : aefMapId.hashCode());
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
        AefMapInfoDto other = (AefMapInfoDto) obj;
        if (aefMapId == null) {
            if (other.aefMapId != null) {
                return false;
            }
        } else if (!aefMapId.equals(other.aefMapId)) {
            return false;
        }
        return true;
    }

    
    
}
