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

public class UserAttachRequest {
   private java.lang.String userExternalId;
   private java.lang.String userAccessToken;

  /**
   * Default constructor.  Note that this does not initialize fields
   * to their default values from the schema.  If that is desired then
   * one should use {@link \#newBuilder()}. 
   */
  public UserAttachRequest() {}

  /**
   * All-args constructor.
   */
  public UserAttachRequest(java.lang.String userExternalId, java.lang.String userAccessToken) {
    this.userExternalId = userExternalId;
    this.userAccessToken = userAccessToken;
  }

  /**
   * Gets the value of the 'userExternalId' field.
   */
  public java.lang.String getUserExternalId() {
    return userExternalId;
  }

  /**
   * Sets the value of the 'userExternalId' field.
   * @param value the value to set.
   */
  public void setUserExternalId(java.lang.String value) {
    this.userExternalId = value;
  }

  /**
   * Gets the value of the 'userAccessToken' field.
   */
  public java.lang.String getUserAccessToken() {
    return userAccessToken;
  }

  /**
   * Sets the value of the 'userAccessToken' field.
   * @param value the value to set.
   */
  public void setUserAccessToken(java.lang.String value) {
    this.userAccessToken = value;
  }

@Override
public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((userAccessToken == null) ? 0 : userAccessToken.hashCode());
    result = prime * result + ((userExternalId == null) ? 0 : userExternalId.hashCode());
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
    UserAttachRequest other = (UserAttachRequest) obj;
    if (userAccessToken == null) {
        if (other.userAccessToken != null) {
            return false;
        }
    } else if (!userAccessToken.equals(other.userAccessToken)) {
        return false;
    }
    if (userExternalId == null) {
        if (other.userExternalId != null) {
            return false;
        }
    } else if (!userExternalId.equals(other.userExternalId)) {
        return false;
    }
    return true;
}

}
