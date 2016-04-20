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

/**
 * The Class HistoryKey is used to model key of cache entry for history of
 * application changes. Application change is a change of active profile or
 * configuration for particular application endpoint group. Contains appToken,
 * old and new app seq numbers, profile and conf versions
 *
 * @author ashvayka
 */
public final class HistoryKey implements Serializable {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 8365579177309710618L;

    /** The app token. */
    private final String appToken;

    /** The old seq number. */
    private final int oldSeqNumber;

    /** The new seq number. */
    private final int newSeqNumber;

    /** The conf schema version. */
    private final int confSchemaVersion;

    /** The profile schema version. */
    private final int endpointProfileSchemaVersion;

    /** The profile schema version. */
    private final int serverProfileSchemaVersion;

    /**
     * Instantiates a new history key.
     *
     * @param appToken the app token
     * @param oldSeqNumber the old seq number
     * @param newSeqNumber the new seq number
     * @param confSchemaVersion the conf schema version
     * @param endpointProfileSchemaVersion the profile schema version
     * @param serverProfileSchemaVersion the profile schema version
     */
    public HistoryKey(String appToken, int oldSeqNumber, int newSeqNumber, int confSchemaVersion,
            int endpointProfileSchemaVersion, int serverProfileSchemaVersion) {
        super();
        this.appToken = appToken;
        this.oldSeqNumber = oldSeqNumber;
        this.newSeqNumber = newSeqNumber;
        this.confSchemaVersion = confSchemaVersion;
        this.endpointProfileSchemaVersion = endpointProfileSchemaVersion;
        this.serverProfileSchemaVersion = serverProfileSchemaVersion;
    }

    /**
     * Gets the app token.
     *
     * @return the app token
     */
    public String getAppToken() {
        return appToken;
    }

    /**
     * Gets the old seq number.
     *
     * @return the old seq number
     */
    public int getOldSeqNumber() {
        return oldSeqNumber;
    }

    /**
     * Gets the new seq number.
     *
     * @return the new seq number
     */
    public int getNewSeqNumber() {
        return newSeqNumber;
    }

    /**
     * Gets the conf schema version.
     *
     * @return the conf schema version
     */
    public int getConfSchemaVersion() {
        return confSchemaVersion;
    }

    /**
     * Gets the profile schema version.
     *
     * @return the profile schema version
     */
    public int getEndpointProfileSchemaVersion() {
        return endpointProfileSchemaVersion;
    }

    public int getServerProfileSchemaVersion() {
        return serverProfileSchemaVersion;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((appToken == null) ? 0 : appToken.hashCode());
        result = prime * result + confSchemaVersion;
        result = prime * result + newSeqNumber;
        result = prime * result + oldSeqNumber;
        result = prime * result + endpointProfileSchemaVersion;
        result = prime * result + serverProfileSchemaVersion;
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
        HistoryKey other = (HistoryKey) obj;
        if (appToken == null) {
            if (other.appToken != null) {
                return false;
            }
        } else if (!appToken.equals(other.appToken)) {
            return false;
        }
        if (confSchemaVersion != other.confSchemaVersion) {
            return false;
        }
        if (newSeqNumber != other.newSeqNumber) {
            return false;
        }
        if (oldSeqNumber != other.oldSeqNumber) {
            return false;
        }
        if (endpointProfileSchemaVersion != other.endpointProfileSchemaVersion) {
            return false;
        }
        if (serverProfileSchemaVersion != other.serverProfileSchemaVersion) {
            return false;
        }
        return true;
    }

}
