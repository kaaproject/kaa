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

public class EcfInfoDto implements Serializable {

    private static final long serialVersionUID = 1400506296720691592L;
    
    private String ecfId;
    private String ecfName;
    private int version;
    
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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((ecfId == null) ? 0 : ecfId.hashCode());
        result = prime * result + ((ecfName == null) ? 0 : ecfName.hashCode());
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
        EcfInfoDto other = (EcfInfoDto) obj;
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
        if (version != other.version) {
            return false;
        }
        return true;
    }
    

}
