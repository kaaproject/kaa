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

package org.kaaproject.kaa.server.sync.bootstrap;

import java.util.Arrays;

/**
 * 
 * @author Andrew Shvayka
 *
 */
public final class ProtocolConnectionData {
    private final int accessPointId;
    private final ProtocolVersionId protocolVersionId;
    private final byte[] connectionData;

    public ProtocolConnectionData(int accessPointId, ProtocolVersionId protocolVersionId, byte[] connectionData) {
        super();
        this.accessPointId = accessPointId;
        this.protocolVersionId = protocolVersionId;
        this.connectionData = connectionData;
    }

    public int getAccessPointId() {
        return accessPointId;
    }

    public int getProtocolId() {
        return protocolVersionId.getProtocolId();
    }

    public int getProtocolVersion() {
        return protocolVersionId.getVersion();
    }

    public byte[] getConnectionData() {
        return connectionData;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + accessPointId;
        result = prime * result + Arrays.hashCode(connectionData);
        result = prime * result + ((protocolVersionId == null) ? 0 : protocolVersionId.hashCode());
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
        ProtocolConnectionData other = (ProtocolConnectionData) obj;
        if (accessPointId != other.accessPointId) {
            return false;
        }
        if (!Arrays.equals(connectionData, other.connectionData)) {
            return false;
        }
        if (protocolVersionId == null) {
            if (other.protocolVersionId != null) {
                return false;
            }
        } else if (!protocolVersionId.equals(other.protocolVersionId)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ProtocolConnectionData [accessPointId=");
        builder.append(accessPointId);
        builder.append(", protocolVersionId=");
        builder.append(protocolVersionId);
        builder.append(", connectionData=");
        builder.append(Arrays.toString(connectionData));
        builder.append("]");
        return builder.toString();
    }

}
