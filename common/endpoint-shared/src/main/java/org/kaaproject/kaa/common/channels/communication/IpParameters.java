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

package org.kaaproject.kaa.common.channels.communication;

/**
 * IpParameters Class.
 * Base type for IP oriented Channels - IpParameters consists
 * HostName String and Integer port
 *
 * @author Andrey Panasenko
 */
public class IpParameters {
  private String hostName;
  private int port;

  /**
   * HostName getter.
   *
   * @return String HostName
   */
  public String getHostName() {
    return hostName;
  }

  /**
   * HostName setter.
   *
   * @param hostName the host name
   */
  public void setHostName(String hostName) {
    this.hostName = hostName;
  }

  /**
   * Port getter.
   *
   * @return int port
   */
  public int getPort() {
    return port;
  }

  /**
   * Port setter.
   *
   * @param port the port
   */
  public void setPort(int port) {
    this.port = port;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "IpParameters [hostName=" + hostName + ", port=" + port + "]";
  }

  /* (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((hostName == null) ? 0 : hostName.hashCode());
    result = prime * result + port;
    return result;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#equals(java.lang.Object)
   */
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
    IpParameters other = (IpParameters) obj;
    if (hostName == null) {
      if (other.hostName != null) {
        return false;
      }
    } else if (!hostName.equals(other.hostName)) {
      return false;
    }
    if (port != other.port) {
      return false;
    }
    return true;
  }
}
