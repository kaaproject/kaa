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

package org.kaaproject.kaa.server.sync.bootstrap;

import java.util.List;

/**
 * 
 * @author Andrew Shvayka
 *
 */
public final class BootstrapClientSync {

    private final int requestId;
    private final List<ProtocolVersionId> keys;

    public BootstrapClientSync(int requestId, List<ProtocolVersionId> keys) {
        super();
        this.requestId = requestId;
        this.keys = keys;
    }

    public int getRequestId() {
        return requestId;
    }

    public List<ProtocolVersionId> getKeys() {
        return keys;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((keys == null) ? 0 : keys.hashCode());
        result = prime * result + requestId;
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
        BootstrapClientSync other = (BootstrapClientSync) obj;
        if (keys == null) {
            if (other.keys != null) {
                return false;
            }
        } else if (!keys.equals(other.keys)) {
            return false;
        }
        if (requestId != other.requestId) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("BootstrapClientSync [requestId=");
        builder.append(requestId);
        builder.append(", keys=");
        builder.append(keys);
        builder.append("]");
        return builder.toString();
    }
}
