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

import org.kaaproject.kaa.common.hash.EndpointObjectHash;

public final class RouteTableAddress {
    private final EndpointObjectHash endpointKey;
    private final String serverId;
    private final String applicationToken;

    public RouteTableAddress(EndpointObjectHash endpointKey, String applicationToken) {
        this(endpointKey, applicationToken, null);
    }

    public RouteTableAddress(EndpointObjectHash endpointKey, String applicationToken, String serverId) {
        super();
        this.endpointKey = endpointKey;
        this.applicationToken = applicationToken;
        this.serverId = serverId;
    }

    public EndpointObjectHash getEndpointKey() {
        return endpointKey;
    }

    public String getServerId() {
        return serverId;
    }

    public String getApplicationToken() {
		return applicationToken;
	}

	public boolean isLocal(){
        return serverId == null;
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ ((applicationToken == null) ? 0 : applicationToken.hashCode());
		result = prime * result
				+ ((endpointKey == null) ? 0 : endpointKey.hashCode());
		result = prime * result
				+ ((serverId == null) ? 0 : serverId.hashCode());
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
		RouteTableAddress other = (RouteTableAddress) obj;
		if (applicationToken == null) {
			if (other.applicationToken != null) {
				return false;
			}
		} else if (!applicationToken.equals(other.applicationToken)) {
			return false;
		}
		if (endpointKey == null) {
			if (other.endpointKey != null) {
				return false;
			}
		} else if (!endpointKey.equals(other.endpointKey)) {
			return false;
		}
		if (serverId == null) {
			if (other.serverId != null) {
				return false;
			}
		} else if (!serverId.equals(other.serverId)) {
			return false;
		}
		return true;
	}

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "RouteTableAddress [endpointKey=" + endpointKey + ", serverId=" + serverId + ", applicationToken=" + applicationToken + "]";
    }


}
