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
package org.kaaproject.kaa.server.transport;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * A meta data information about transport that includes supported versions and
 * connection info.
 * 
 * @author Andrew Shvayka
 *
 */
public class TransportMetaData implements Serializable {

    /**
     * Generated value
     */
    private static final long serialVersionUID = 9208273898021695583L;

    private final int minSupportedVersion;
    private final int maxSupportedVersion;
    private final byte[] defaultConnectionInfo;
    private final Map<Integer, byte[]> versionSpecificConnectionInfo;

    public TransportMetaData(int minSupportedVersion, int maxSupportedVersion, byte[] defaultConnectionInfo) {
        super();
        this.minSupportedVersion = minSupportedVersion;
        this.maxSupportedVersion = maxSupportedVersion;
        this.defaultConnectionInfo = defaultConnectionInfo;
        this.versionSpecificConnectionInfo = new HashMap<Integer, byte[]>();
    }

    /**
     * Populates connection information for each client version. Connection
     * information is serialized. It is a responsibility of transport developer
     * to serialize data on server and deserialize on the client
     * 
     * @param version
     *            of the client
     * @param connectionInfo
     *            connection data
     */
    public void setConnectionInfo(int version, byte[] connectionInfo) {
        this.versionSpecificConnectionInfo.put(version, connectionInfo);
    }

    /**
     * Returns serialized connection info for specified transport versions.
     * Returns default connection info if there is no overridden configuration
     * info for specified version of the transport.
     * 
     * @param version
     *            - specific version of the transport
     * @return serialized connection info for the specified version
     */
    public byte[] getConnectionInfo(int version) {
        if (versionSpecificConnectionInfo.containsKey(version)) {
            return versionSpecificConnectionInfo.get(version);
        } else {
            return defaultConnectionInfo;
        }
    }

    /**
     * Returns minimum supported version of this transport.
     * @return minimum supported version.
     */
    public int getMinSupportedVersion() {
        return minSupportedVersion;
    }

    /**
     * Returns maximum supported version of this transport.
     * @return maximum supported version.
     */
    public int getMaxSupportedVersion() {
        return maxSupportedVersion;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("TransportConnectionInfo [minSupportedVersion=");
        builder.append(minSupportedVersion);
        builder.append(", maxSupportedVersion=");
        builder.append(maxSupportedVersion);
        builder.append(", clientConnectionInfo=");
        builder.append(versionSpecificConnectionInfo);
        builder.append("]");
        return builder.toString();
    };
}
