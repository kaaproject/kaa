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

package org.kaaproject.kaa.common.dto.admin;

import org.kaaproject.kaa.common.dto.KaaAuthorityDto;


public class TenantUserDto extends UserDto {

    private static final long serialVersionUID = 1685963821728067967L;

    private String tenantName;

    public TenantUserDto() {
    }

    public TenantUserDto(String externalUid,
            String username,
            String firstName,
            String lastName,
            String mail,
            KaaAuthorityDto authority) {
        super(externalUid, username, firstName, lastName, mail, authority);
    }

    public void setTenantName(String tenantName) {
        this.tenantName = tenantName;
    }

    public String getTenantName() {
        return tenantName;
    }

    @Override
    public String toString() {
        return "TenantUserDto [tenantName=" + tenantName + ", toString()=" + super.toString() + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getTenantId() == null) ? 0 : getTenantId().hashCode());
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
        TenantUserDto other = (TenantUserDto) obj;
        if (getTenantId() == null) {
            if (other.getTenantId() != null) {
                return false;
            }
        } else if (!getTenantId().equals(other.getTenantId())) {
            return false;
        }
        return true;
    }


}
