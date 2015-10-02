package org.kaaproject.kaa.common.dto;

import java.io.Serializable;

public class PageLinkDto implements Serializable{

    private static final long serialVersionUID = -6521135212595518900L;

    private String limit;
    private String offset;
    private String endpointKeyHash;
    private String previous;
    private String next;

    public PageLinkDto() {}

    PageLinkDto(String limit, String offset, String previous, String next) {
        this.limit = limit;
        this.offset = offset;
        this.previous = previous;
        this.next = next;
        this.endpointKeyHash = null;
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

    public String getEndpointKeyHash() {
        return endpointKeyHash;
    }

    public void setEndpointKeyHash(String endpointKeyHash) {
        this.endpointKeyHash = endpointKeyHash;
    }

    public String getPrevious() {
        return previous;
    }

    public void setPrevious(String previous) {
        this.previous = previous;
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
        result = prime * result + ((endpointKeyHash == null) ? 0 : endpointKeyHash.hashCode());
        result = prime * result + ((limit == null) ? 0 : limit.hashCode());
        result = prime * result + ((next == null) ? 0 : next.hashCode());
        result = prime * result + ((offset == null) ? 0 : offset.hashCode());
        result = prime * result + ((previous == null) ? 0 : previous.hashCode());
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
        if (endpointKeyHash == null) {
            if (other.endpointKeyHash != null)
                return false;
        } else if (!endpointKeyHash.equals(other.endpointKeyHash))
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
        if (previous == null) {
            if (other.previous != null)
                return false;
        } else if (!previous.equals(other.previous))
            return false;
        return true;
    }
}
