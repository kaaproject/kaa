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
package org.kaaproject.kaa.server.operations.pojo.sync;  

public class UserClientSync {
   private org.kaaproject.kaa.server.operations.pojo.sync.UserAttachRequest userAttachRequest;
   private java.util.List<org.kaaproject.kaa.server.operations.pojo.sync.EndpointAttachRequest> endpointAttachRequests;
   private java.util.List<org.kaaproject.kaa.server.operations.pojo.sync.EndpointDetachRequest> endpointDetachRequests;

  /**
   * Default constructor.  Note that this does not initialize fields
   * to their default values from the schema.  If that is desired then
   * one should use {@link \#newBuilder()}. 
   */
  public UserClientSync() {}

  /**
   * All-args constructor.
   */
  public UserClientSync(org.kaaproject.kaa.server.operations.pojo.sync.UserAttachRequest userAttachRequest, java.util.List<org.kaaproject.kaa.server.operations.pojo.sync.EndpointAttachRequest> endpointAttachRequests, java.util.List<org.kaaproject.kaa.server.operations.pojo.sync.EndpointDetachRequest> endpointDetachRequests) {
    this.userAttachRequest = userAttachRequest;
    this.endpointAttachRequests = endpointAttachRequests;
    this.endpointDetachRequests = endpointDetachRequests;
  }

  /**
   * Gets the value of the 'userAttachRequest' field.
   */
  public org.kaaproject.kaa.server.operations.pojo.sync.UserAttachRequest getUserAttachRequest() {
    return userAttachRequest;
  }

  /**
   * Sets the value of the 'userAttachRequest' field.
   * @param value the value to set.
   */
  public void setUserAttachRequest(org.kaaproject.kaa.server.operations.pojo.sync.UserAttachRequest value) {
    this.userAttachRequest = value;
  }

  /**
   * Gets the value of the 'endpointAttachRequests' field.
   */
  public java.util.List<org.kaaproject.kaa.server.operations.pojo.sync.EndpointAttachRequest> getEndpointAttachRequests() {
    return endpointAttachRequests;
  }

  /**
   * Sets the value of the 'endpointAttachRequests' field.
   * @param value the value to set.
   */
  public void setEndpointAttachRequests(java.util.List<org.kaaproject.kaa.server.operations.pojo.sync.EndpointAttachRequest> value) {
    this.endpointAttachRequests = value;
  }

  /**
   * Gets the value of the 'endpointDetachRequests' field.
   */
  public java.util.List<org.kaaproject.kaa.server.operations.pojo.sync.EndpointDetachRequest> getEndpointDetachRequests() {
    return endpointDetachRequests;
  }

  /**
   * Sets the value of the 'endpointDetachRequests' field.
   * @param value the value to set.
   */
  public void setEndpointDetachRequests(java.util.List<org.kaaproject.kaa.server.operations.pojo.sync.EndpointDetachRequest> value) {
    this.endpointDetachRequests = value;
  }

@Override
public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((endpointAttachRequests == null) ? 0 : endpointAttachRequests.hashCode());
    result = prime * result + ((endpointDetachRequests == null) ? 0 : endpointDetachRequests.hashCode());
    result = prime * result + ((userAttachRequest == null) ? 0 : userAttachRequest.hashCode());
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
    UserClientSync other = (UserClientSync) obj;
    if (endpointAttachRequests == null) {
        if (other.endpointAttachRequests != null) {
            return false;
        }
    } else if (!endpointAttachRequests.equals(other.endpointAttachRequests)) {
        return false;
    }
    if (endpointDetachRequests == null) {
        if (other.endpointDetachRequests != null) {
            return false;
        }
    } else if (!endpointDetachRequests.equals(other.endpointDetachRequests)) {
        return false;
    }
    if (userAttachRequest == null) {
        if (other.userAttachRequest != null) {
            return false;
        }
    } else if (!userAttachRequest.equals(other.userAttachRequest)) {
        return false;
    }
    return true;
}
}
