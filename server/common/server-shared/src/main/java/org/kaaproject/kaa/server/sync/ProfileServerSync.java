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

package org.kaaproject.kaa.server.sync;

public final class ProfileServerSync {
    private SyncResponseStatus responseStatus;

    public ProfileServerSync() {
    }

    /**
     * All-args constructor.
     */
    public ProfileServerSync(SyncResponseStatus responseStatus) {
        this.responseStatus = responseStatus;
    }

    /**
     * Gets the value of the 'responseStatus' field.
     */
    public SyncResponseStatus getResponseStatus() {
        return responseStatus;
    }

    /**
     * Sets the value of the 'responseStatus' field.
     * 
     * @param value
     *            the value to set.
     */
    public void setResponseStatus(SyncResponseStatus value) {
        this.responseStatus = value;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ProfileServerSync that = (ProfileServerSync) o;

        if (responseStatus != that.responseStatus) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return responseStatus != null ? responseStatus.hashCode() : 0;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ProfileServerSync [responseStatus=");
        builder.append(responseStatus);
        builder.append("]");
        return builder.toString();
    }
}
