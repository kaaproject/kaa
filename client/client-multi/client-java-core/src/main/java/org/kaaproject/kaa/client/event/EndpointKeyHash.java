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

package org.kaaproject.kaa.client.event;

/**
 * Represents endpoint key hash returned from OPS after it was successfully attached.
 */
public class EndpointKeyHash {

  private String keyHash;

  public EndpointKeyHash(String keyHash) {
    this.keyHash = keyHash;
  }

  public String getKeyHash() {
    return keyHash;
  }

  public void setKeyHash(String keyHash) {
    this.keyHash = keyHash;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((keyHash == null) ? 0 : keyHash.hashCode());
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
    EndpointKeyHash other = (EndpointKeyHash) obj;
    if (keyHash == null) {
      if (other.keyHash != null) {
        return false;
      }
    } else if (!keyHash.equals(other.keyHash)) {
      return false;
    }
    return true;
  }

  public String toString() {
    return keyHash;
  }
}
