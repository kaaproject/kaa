package org.kaaproject.kaa.client.channel;

public final class TransportId {

    private final int protocolId;
    private final int protocolVersion;

    public TransportId(int protocolId, int protocolVersion) {
        super();
        this.protocolId = protocolId;
        this.protocolVersion = protocolVersion;
    }

    public int getProtocolId() {
        return protocolId;
    }

    public int getProtocolVersion() {
        return protocolVersion;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + protocolId;
        result = prime * result + protocolVersion;
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
        TransportId other = (TransportId) obj;
        if (protocolId != other.protocolId)
            return false;
        if (protocolVersion != other.protocolVersion)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "TransportId [protocolId=" + protocolId + ", protocolVersion=" + protocolVersion + "]";
    }

}
