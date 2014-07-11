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

package org.kaaproject.kaa.client.configuration.delta;

import java.util.UUID;

/**
 * Id which is used to identify delta objects and subscribe for their updates
 *
 * @author Yaroslav Zeygerman
 *
 */
public final class DeltaHandlerId implements Comparable<DeltaHandlerId> {
    private final Long handlerId;

    public DeltaHandlerId(UUID uuid) {
        long result = 0;
        if (uuid.version() == 1) {
            // Time-based uuid
            result = (uuid.timestamp() << 4) | (uuid.node() & 0xFF);
        } else {
            result = (uuid.getMostSignificantBits() << 32) | (uuid.getLeastSignificantBits() & 0xFFFFFFFF);
        }
        this.handlerId = new Long(result);
    }

    public DeltaHandlerId(long handlerId) {
        this.handlerId = new Long(handlerId);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((handlerId == null) ? 0 : handlerId.hashCode());
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
        DeltaHandlerId other = (DeltaHandlerId) obj;
        if (handlerId == null) {
            if (other.handlerId != null) {
                return false;
            }
        } else if (!handlerId.equals(other.handlerId)) {
            return false;
        }
        return true;
    }

    @Override
    public int compareTo(DeltaHandlerId obj) {
        return handlerId.compareTo(obj.handlerId);
    }

    @Override
    public String toString() {
        return handlerId.toString();
    }
}
