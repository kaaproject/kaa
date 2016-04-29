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

package org.kaaproject.kaa.server.operations.service.cache;

import java.io.Serializable;

public final class AppSeqNumber implements Serializable{

    /**
     *
     */
    private static final long serialVersionUID = -5524478984530847211L;

    private final String tenantId;
    private final String appId;
    private final String appToken;
    private final int seqNumber;

    public AppSeqNumber(String tenantId, String appId, String appToken, int seqNumber) {
        super();
        this.tenantId = tenantId;
        this.appId = appId;
        this.appToken = appToken;
        this.seqNumber = seqNumber;
    }

    public String getTenantId() {
        return tenantId;
    }

    public String getAppId() {
        return appId;
    }

    public String getAppToken() {
        return appToken;
    }

    public int getSeqNumber() {
        return seqNumber;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((appToken == null) ? 0 : appToken.hashCode());
        result = prime * result + seqNumber;
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
        AppSeqNumber other = (AppSeqNumber) obj;
        if (appToken == null) {
            if (other.appToken != null) {
                return false;
            }
        } else if (!appToken.equals(other.appToken)) {
            return false;
        }
        if (seqNumber != other.seqNumber) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("AppSeqNumber [appId=");
        builder.append(appId);
        builder.append(", appToken=");
        builder.append(appToken);
        builder.append(", seqNumber=");
        builder.append(seqNumber);
        builder.append("]");
        return builder.toString();
    }


}
