package org.kaaproject.kaa.client.channel;

import org.kaaproject.kaa.common.endpoint.gen.ProtocolMetaData;

public class GenericTransportInfo implements ServerInfo {

    protected final ServerType serverType;
    protected final TransportId transportId;
    protected final ProtocolMetaData md;
    
    public GenericTransportInfo(ServerType serverType, ProtocolMetaData md) {
        super();
        this.serverType = serverType;
        this.md = md;
        this.transportId = new TransportId(md.getProtocolId(), md.getProtocolVersion());
    }

    @Override
    public ServerType getServerType() {
        return serverType;
    }

    @Override
    public TransportId getTransportId() {
        return transportId;
    }
    

    @Override
    public int getAccessPointId() {
        return md.getAccessPointId();
    }

    @Override
    public byte[] getConnectionInfo() {
        return md.getConnectionInfo().array();
    }    

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((md == null) ? 0 : md.hashCode());
        result = prime * result + ((serverType == null) ? 0 : serverType.hashCode());
        result = prime * result + ((transportId == null) ? 0 : transportId.hashCode());
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
        GenericTransportInfo other = (GenericTransportInfo) obj;
        if (md == null) {
            if (other.md != null)
                return false;
        } else if (!md.equals(other.md))
            return false;
        if (serverType != other.serverType)
            return false;
        if (transportId == null) {
            if (other.transportId != null)
                return false;
        } else if (!transportId.equals(other.transportId))
            return false;
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("GenericTransportInfo [serverType=");
        builder.append(serverType);
        builder.append(", transportId=");
        builder.append(transportId);
        builder.append(", md=");
        builder.append(md);
        builder.append("]");
        return builder.toString();
    }
}
