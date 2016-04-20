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


public final class RouteTableKey implements Serializable{

    /**
     *
     */
    private static final long serialVersionUID = 3363397161525196106L;


    private final String appToken;
    private final EventClassFamilyVersion ecfVersion;

    public RouteTableKey(String appToken, EventClassFamilyVersion ecfVersion) {
        super();
        this.appToken = appToken;
        this.ecfVersion = ecfVersion;
    }

    public String getAppToken() {
        return appToken;
    }

    public EventClassFamilyVersion getEcfVersion() {
        return ecfVersion;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((appToken == null) ? 0 : appToken.hashCode());
        result = prime * result + ((ecfVersion == null) ? 0 : ecfVersion.hashCode());
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
        RouteTableKey other = (RouteTableKey) obj;
        if (appToken == null) {
            if (other.appToken != null) {
                return false;
            }
        } else if (!appToken.equals(other.appToken)) {
            return false;
        }
        if (ecfVersion == null) {
            if (other.ecfVersion != null) {
                return false;
            }
        } else if (!ecfVersion.equals(other.ecfVersion)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("RouteKey [appToken=");
        builder.append(appToken);
        builder.append(", ecfVersion=");
        builder.append(ecfVersion);
        builder.append("]");
        return builder.toString();
    }

}
