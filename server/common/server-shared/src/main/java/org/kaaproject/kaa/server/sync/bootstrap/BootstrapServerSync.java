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

import java.util.Set;

/**
 * 
 * @author Andrew Shvayka
 *
 */
public final class BootstrapServerSync {
    private final int requestId;
    private final Set<ProtocolConnectionData> protocolList;

    public BootstrapServerSync(int requestId, Set<ProtocolConnectionData> protocolList) {
        super();
        this.requestId = requestId;
        this.protocolList = protocolList;
    }

    public int getRequestId() {
        return requestId;
    }

    public Set<ProtocolConnectionData> getProtocolList() {
        return protocolList;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((protocolList == null) ? 0 : protocolList.hashCode());
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
        BootstrapServerSync other = (BootstrapServerSync) obj;
        if (protocolList == null) {
            if (other.protocolList != null) {
                return false;
            }
        } else if (!protocolList.equals(other.protocolList)) {
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
        builder.append("BootstrapServerSync [requestId=");
        builder.append(requestId);
        builder.append(", protocolList=");
        builder.append(protocolList);
        builder.append("]");
        return builder.toString();
    }

}
