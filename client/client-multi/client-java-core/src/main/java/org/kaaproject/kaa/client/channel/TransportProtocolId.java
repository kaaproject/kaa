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

package org.kaaproject.kaa.client.channel;

/**
 * Immutable class to represent transport ID. Holds references to transport
 * protocol id and transport protocol version
 *
 * @author Andrew Shvayka
 */
public final class TransportProtocolId {

  private final int id;
  private final int version;

  /**
   * All-args constructor.
   */
  public TransportProtocolId(int protocolId, int protocolVersion) {
    super();
    this.id = protocolId;
    this.version = protocolVersion;
  }

  public int getProtocolId() {
    return id;
  }

  public int getProtocolVersion() {
    return version;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + id;
    result = prime * result + version;
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
    TransportProtocolId other = (TransportProtocolId) obj;
    if (id != other.id) {
      return false;
    }
    if (version != other.version) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return "TransportProtocolId [id=" + id + ", version=" + version + "]";
  }
}
