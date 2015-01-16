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

/**
 * Immutable class to represent transport ID. Holds references to transport
 * protocol id and transport protocol version
 * 
 * @author Andrew Shvayka
 *
 */
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
