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

package org.kaaproject.kaa.common.dto;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class AbstractEndpointProfilesDto implements Serializable {

    private static final long serialVersionUID = 3355356067164498361L;

    @JsonIgnore
    protected PageLinkDto pageLinkDto;
    protected String next;

    public PageLinkDto getPageLinkDto() {
        return pageLinkDto;
    }

    public void setPageLinkDto(PageLinkDto pageLinkDto) {
        this.pageLinkDto = pageLinkDto;
    }

    public String getNext() {
        return next;
    }

    public void setNext(String next) {
        this.next = next;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((next == null) ? 0 : next.hashCode());
        result = prime * result + ((pageLinkDto == null) ? 0 : pageLinkDto.hashCode());
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
        AbstractEndpointProfilesDto other = (AbstractEndpointProfilesDto) obj;
        if (next == null) {
            if (other.next != null) {
                return false;
            }
        } else if (!next.equals(other.next)) {
            return false;
        }
        if (pageLinkDto == null) {
            if (other.pageLinkDto != null) {
                return false;
            }
        } else if (!pageLinkDto.equals(other.pageLinkDto)) {
            return false;
        }
        return true;
    }
}
