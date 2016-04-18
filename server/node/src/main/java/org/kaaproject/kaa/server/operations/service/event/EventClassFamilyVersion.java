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

package org.kaaproject.kaa.server.operations.service.event;

import java.io.Serializable;

public final class EventClassFamilyVersion implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 7302504644662229284L;


    private final String ecfId;
    private final int version;

    public EventClassFamilyVersion(String ecfId, int version) {
        super();
        this.ecfId = ecfId;
        this.version = version;
    }

    public String getEcfId() {
        return ecfId;
    }

    public int getVersion() {
        return version;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((ecfId == null) ? 0 : ecfId.hashCode());
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
        EventClassFamilyVersion other = (EventClassFamilyVersion) obj;
        if (ecfId == null) {
            if (other.ecfId != null) {
                return false;
            }
        } else if (!ecfId.equals(other.ecfId)) {
            return false;
        }
        if (version != other.version) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("EventClassFamilyVersion [ecfId=");
        builder.append(ecfId);
        builder.append(", version=");
        builder.append(version);
        builder.append("]");
        return builder.toString();
    }
}
