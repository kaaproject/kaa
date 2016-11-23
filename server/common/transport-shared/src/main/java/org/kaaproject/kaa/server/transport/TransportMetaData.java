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

package org.kaaproject.kaa.server.transport;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a transport meta data that includes supported versions and the
 * connection info.
 *
 * @author Andrew Shvayka
 */
public class TransportMetaData implements Serializable {

  /**
   * Generated value.
   */
  private static final long serialVersionUID = 9208273898021695583L;

  private final int minSupportedVersion;
  private final int maxSupportedVersion;
  private final List<byte[]> defaultConnectionInfoList;
  private final Map<Integer, List<byte[]>> versionSpecificConnectionInfoList;

  /**
   * Create new instance of <code>TransportMetaData</code>.
   *
   * @param minSupportedVersion is minimal supported version
   * @param maxSupportedVersion is maximum  supported version
   * @param defaultConnectionInfoList is default connection info list
   */
  public TransportMetaData(int minSupportedVersion,
                           int maxSupportedVersion,
                           List<byte[]> defaultConnectionInfoList) {
    super();
    this.minSupportedVersion = minSupportedVersion;
    this.maxSupportedVersion = maxSupportedVersion;
    this.defaultConnectionInfoList = defaultConnectionInfoList;
    this.versionSpecificConnectionInfoList = new HashMap<>();
  }

  /**
   * Populates the connection information for each client version. The
   * connection information is serialized. It is the responsibility of the
   * transport developer to serialize the data on the server and deserialize
   * it on the client
   *
   * @param version        the client version
   * @param connectionInfo the connection data
   */
  public void addConnectionInfo(int version, byte[] connectionInfo) {
    if (!versionSpecificConnectionInfoList.containsKey(version)) {
      versionSpecificConnectionInfoList.put(version, new ArrayList<>());
    }
    versionSpecificConnectionInfoList.get(version).add(connectionInfo);
  }

  /**
   * Returns the serialized connection data for the specified transport
   * versions. Returns the default connection data if there is no overridden
   * configuration info for the specified version of the transport.
   *
   * @param version the serialized connection info for the specified version
   * @return serialized connection info list for the specified version
   */
  public List<byte[]> getConnectionInfoList(int version) {
    if (versionSpecificConnectionInfoList.containsKey(version)) {
      return versionSpecificConnectionInfoList.get(version);
    } else {
      return defaultConnectionInfoList;
    }
  }

  /**
   * Returns the minimum supported version of this transport.
   *
   * @return the minimum supported version
   */
  public int getMinSupportedVersion() {
    return minSupportedVersion;
  }

  /**
   * Returns the maximum supported version of this transport.
   *
   * @return the maximum supported version
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
    builder.append(versionSpecificConnectionInfoList);
    builder.append("]");
    return builder.toString();
  }

  ;
}
