/*
 * Copyright 2014 CyberVision, Inc.
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
package org.kaaproject.kaa.client.channel;

import org.kaaproject.kaa.common.endpoint.gen.ProtocolMetaData;

/**
 * Generic implementation of {@link TransportConnectionInfo} that is based on {@link ProtocolMetaData}
 * @author Andrew Shvayka
 *
 */
public class GenericTransportInfo implements TransportConnectionInfo {

    protected final ServerType serverType;
    protected final TransportProtocolId transportId;
    protected final ProtocolMetaData md;
    
    public GenericTransportInfo(ServerType serverType, ProtocolMetaData md) {
        super();
        this.serverType = serverType;
        this.md = md;
        this.transportId = new TransportProtocolId(md.getProtocolId(), md.getProtocolVersion());
    }

    @Override
    public ServerType getServerType() {
        return serverType;
    }

    @Override
    public TransportProtocolId getTransportId() {
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
