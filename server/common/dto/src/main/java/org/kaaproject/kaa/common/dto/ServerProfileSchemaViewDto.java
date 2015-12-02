/*
 * Copyright 2014-2015 CyberVision, Inc.
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

import org.kaaproject.kaa.common.dto.ctl.CTLSchemaDto;

import java.io.Serializable;

public class ServerProfileSchemaViewDto implements HasId, Serializable {

    private static final long serialVersionUID = 1L;

    private ServerProfileSchemaDto profileSchemaDto;
    private CTLSchemaDto ctlSchemaDto;

    public ServerProfileSchemaViewDto() {
    }

    public ServerProfileSchemaViewDto(ServerProfileSchemaDto profileSchemaDto, CTLSchemaDto ctlSchemaDto) {
        this.profileSchemaDto = profileSchemaDto;
        this.ctlSchemaDto = ctlSchemaDto;
    }

    public ServerProfileSchemaDto getProfileSchemaDto() {
        return profileSchemaDto;
    }

    public void setProfileSchemaDto(ServerProfileSchemaDto profileSchemaDto) {
        this.profileSchemaDto = profileSchemaDto;
    }

    public CTLSchemaDto getCtlSchemaDto() {
        return ctlSchemaDto;
    }

    public void setCtlSchemaDto(CTLSchemaDto ctlSchemaDto) {
        this.ctlSchemaDto = ctlSchemaDto;
    }

    @Override
    public String getId() {
        return profileSchemaDto.getId();
    }

    @Override
    public void setId(String id) {
        profileSchemaDto.setId(id);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ServerProfileSchemaViewDto that = (ServerProfileSchemaViewDto) o;

        if (profileSchemaDto != null ? !profileSchemaDto.equals(that.profileSchemaDto) : that.profileSchemaDto != null)
            return false;
        return !(ctlSchemaDto != null ? !ctlSchemaDto.equals(that.ctlSchemaDto) : that.ctlSchemaDto != null);

    }

    @Override
    public int hashCode() {
        int result = profileSchemaDto != null ? profileSchemaDto.hashCode() : 0;
        result = 31 * result + (ctlSchemaDto != null ? ctlSchemaDto.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ServerProfileSchemaViewDto{" +
                "profileSchemaDto=" + profileSchemaDto +
                ", ctlSchemaDto=" + ctlSchemaDto +
                '}';
    }
}
