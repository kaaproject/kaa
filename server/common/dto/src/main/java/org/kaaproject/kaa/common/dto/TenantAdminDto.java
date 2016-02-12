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

package org.kaaproject.kaa.common.dto;

import java.io.Serializable;

public class TenantAdminDto implements HasId, Serializable {

    private static final long serialVersionUID = 3723067385520113281L;
    
    private TenantDto tenant = new TenantDto();
    private String userId;
    private String username;
    private String externalUid;

    public String getId() {
        return tenant.getId();
    }

    public void setId(String id) {
        tenant.setId(id);
    }

    public String getName() {
        return tenant.getName();
    }

    public void setName(String name) {
        tenant.setName(name);
    }
    
    public TenantDto getTenant() {
        return tenant;
    }
    
    public void setTenant(TenantDto tenant) {
        this.tenant = tenant;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getExternalUid() {
        return externalUid;
    }

    public void setExternalUid(String externalUid) {
        this.externalUid = externalUid;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TenantAdminDto)) {
            return false;
        }

        TenantAdminDto that = (TenantAdminDto) o;

        if (tenant.getId() != null ? !tenant.getId().equals(that.tenant.getId()) : that.tenant.getId() != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return tenant.getId() != null ? tenant.getId().hashCode() : 0;
    }

    @Override
    public String toString() {
        return "TenantAdminDto [tenant=" + tenant + ", userId=" + userId
                + ", username=" + username + ", externalUid=" + externalUid
                + "]";
    }
 
}
