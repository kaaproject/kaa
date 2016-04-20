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

public class PageLinkDto implements Serializable {

    private static final long serialVersionUID = 3270625120957514502L;

    public static final String ENDPOINT_GROUP_ID = "endpointGroupId";
    public static final String LIMIT = "limit";
    public static final String EQ = "=";
    public static final String AMP = "&";
    public static final String OFFSET = "offset";

    private String endpointGroupId;
    private String limit;
    private String offset;
    private String next;
    private String applicationId;

    public PageLinkDto() {
    }

    public PageLinkDto(String endpointGroupId, String limit, String offset) {
        this.endpointGroupId = endpointGroupId;
        this.limit = limit;
        this.offset = offset;
        this.next = null;
        this.applicationId = null;
    }

    public String getLimit() {
        return limit;
    }

    public void setLimit(String limit) {
        this.limit = limit;
    }

    public String getOffset() {
        return offset;
    }

    public void setOffset(String offset) {
        this.offset = offset;
    }

    public String getNext() {
        return next;
    }

    public void setNext(String next) {
        this.next = next;
    }

    public String getEndpointGroupId() {
        return endpointGroupId;
    }

    public void setEndpointGroupId(String endpointGroupId) {
        this.endpointGroupId = endpointGroupId;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public String getNextUrlPart() {
        String nextUrlPart = null;
        if (limit != null && offset != null) {
            StringBuilder sb = new StringBuilder();
            sb.append(ENDPOINT_GROUP_ID).append(EQ).append(endpointGroupId).append(AMP).append(LIMIT).append(EQ).append(limit)
                    .append(AMP).append(OFFSET).append(EQ).append(offset);
            nextUrlPart = sb.toString();
        }
        return nextUrlPart;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((applicationId == null) ? 0 : applicationId.hashCode());
        result = prime * result + ((endpointGroupId == null) ? 0 : endpointGroupId.hashCode());
        result = prime * result + ((limit == null) ? 0 : limit.hashCode());
        result = prime * result + ((next == null) ? 0 : next.hashCode());
        result = prime * result + ((offset == null) ? 0 : offset.hashCode());
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
        PageLinkDto other = (PageLinkDto) obj;
        if (applicationId == null) {
            if (other.applicationId != null) {
                return false;
            }
        } else if (!applicationId.equals(other.applicationId)) {
            return false;
        }
        if (endpointGroupId == null) {
            if (other.endpointGroupId != null) {
                return false;
            }
        } else if (!endpointGroupId.equals(other.endpointGroupId)) {
            return false;
        }
        if (limit == null) {
            if (other.limit != null) {
                return false;
            }
        } else if (!limit.equals(other.limit)) {
            return false;
        }
        if (next == null) {
            if (other.next != null) {
                return false;
            }
        } else if (!next.equals(other.next)) {
            return false;
        }
        if (offset == null) {
            if (other.offset != null) {
                return false;
            }
        } else if (!offset.equals(other.offset)) {
            return false;
        }
        return true;
    }
}
