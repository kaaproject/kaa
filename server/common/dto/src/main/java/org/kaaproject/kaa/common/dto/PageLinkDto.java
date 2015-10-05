package org.kaaproject.kaa.common.dto;

import java.io.Serializable;

public class PageLinkDto implements Serializable, HasId {

    private static final long serialVersionUID = 3270625120957514502L;

    private String id;
    private String endpointGroupId;
    private String limit;
    private String offset;
    private String next;

    public PageLinkDto() {}

    public PageLinkDto(String endpointGroupId, String limit, String offset) {
        this.endpointGroupId = endpointGroupId;
        this.limit = limit;
        this.offset = offset;
        this.next = null;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((endpointGroupId == null) ? 0 : endpointGroupId.hashCode());
        result = prime * result + ((limit == null) ? 0 : limit.hashCode());
        result = prime * result + ((next == null) ? 0 : next.hashCode());
        result = prime * result + ((offset == null) ? 0 : offset.hashCode());
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
        PageLinkDto other = (PageLinkDto) obj;
        if (endpointGroupId == null) {
            if (other.endpointGroupId != null)
                return false;
        } else if (!endpointGroupId.equals(other.endpointGroupId))
            return false;
        if (limit == null) {
            if (other.limit != null)
                return false;
        } else if (!limit.equals(other.limit))
            return false;
        if (next == null) {
            if (other.next != null)
                return false;
        } else if (!next.equals(other.next))
            return false;
        if (offset == null) {
            if (other.offset != null)
                return false;
        } else if (!offset.equals(other.offset))
            return false;
        return true;
    }
}
