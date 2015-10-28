/*
 * Copyright 2015 CyberVision, Inc.
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

import java.io.Serializable;
import java.util.List;

public class EndpointProfilesPageDto implements Serializable {

    private static final long serialVersionUID = 6368165337840879484L;

    private List<EndpointProfileBodyDto> endpointProfilesBody;
    private List<EndpointProfileDto> endpointProfiles;
    private PageLinkDto pageLinkDto;

    public EndpointProfilesPageDto() {}

    public EndpointProfilesPageDto(List<EndpointProfileDto> endpointProfiles,
            List<EndpointProfileBodyDto> endpointProfilesBodyDto, PageLinkDto pageLinkDto) {
       this.endpointProfiles = endpointProfiles;
       this.endpointProfilesBody = endpointProfilesBodyDto;
       this.pageLinkDto = pageLinkDto;
    }

    public List<EndpointProfileDto> getEndpointProfiles() {
        return endpointProfiles;
    }

    public void setEndpointProfiles(List<EndpointProfileDto> endpointProfiles) {
        this.endpointProfiles = endpointProfiles;
    }

    public PageLinkDto getPageLinkDto() {
        return pageLinkDto;
    }

    public void setPageLinkDto(PageLinkDto pageLinkDto) {
        this.pageLinkDto = pageLinkDto;
    }

    public boolean hasEndpointProfiles() {
        boolean result = false;
        if (endpointProfiles != null) {
            result = !endpointProfiles.isEmpty();
        }
        return result;
    }

    public List<EndpointProfileBodyDto> getEndpointProfilesBody() {
        return endpointProfilesBody;
    }

    public void setEndpointProfilesBody(List<EndpointProfileBodyDto> endpointProfilesBody) {
        this.endpointProfilesBody = endpointProfilesBody;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((endpointProfiles == null) ? 0 : endpointProfiles.hashCode());
        result = prime * result + ((endpointProfilesBody == null) ? 0 : endpointProfilesBody.hashCode());
        result = prime * result + ((pageLinkDto == null) ? 0 : pageLinkDto.hashCode());
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
        EndpointProfilesPageDto other = (EndpointProfilesPageDto) obj;
        if (endpointProfiles == null) {
            if (other.endpointProfiles != null)
                return false;
        } else if (!endpointProfiles.equals(other.endpointProfiles))
            return false;
        if (endpointProfilesBody == null) {
            if (other.endpointProfilesBody != null)
                return false;
        } else if (!endpointProfilesBody.equals(other.endpointProfilesBody))
            return false;
        if (pageLinkDto == null) {
            if (other.pageLinkDto != null)
                return false;
        } else if (!pageLinkDto.equals(other.pageLinkDto))
            return false;
        return true;
    }
}
