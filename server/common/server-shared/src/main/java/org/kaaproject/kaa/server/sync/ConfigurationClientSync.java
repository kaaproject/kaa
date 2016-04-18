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

import java.nio.ByteBuffer;

public final class ConfigurationClientSync {
    private ByteBuffer configurationHash;
    private boolean resyncOnly;

    public ConfigurationClientSync() {
    }

    /**
     * All-args constructor.
     */
    public ConfigurationClientSync(ByteBuffer configurationHash, boolean resyncOnly) {
        this.configurationHash = configurationHash;
        this.resyncOnly = resyncOnly;
    }

    /**
     * Gets the value of the 'configurationHash' field.
     */
    public ByteBuffer getConfigurationHash() {
        return configurationHash;
    }

    /**
     * Sets the value of the 'configurationHash' field.
     * 
     * @param value
     *            the value to set.
     */
    public void setConfigurationHash(ByteBuffer value) {
        this.configurationHash = value;
    }

    /**
     * Indicates if client is interested only in resync delta encoded using base schema.
     * 
     * @return value
     *             the value of the flag
     */
    public boolean isResyncOnly() {
        return resyncOnly;
    }

    /**
     * Sets that client is interested only in resync delta encoded using base schema
     * @param resyncOnly
     */
    public void setResyncOnly(boolean resyncOnly) {
        this.resyncOnly = resyncOnly;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((configurationHash == null) ? 0 : configurationHash.hashCode());
        result = prime * result + (resyncOnly ? 1231 : 1237);
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
        ConfigurationClientSync other = (ConfigurationClientSync) obj;
        if (configurationHash == null) {
            if (other.configurationHash != null) {
                return false;
            }
        } else if (!configurationHash.equals(other.configurationHash)) {
            return false;
        }
        if (resyncOnly != other.resyncOnly) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ConfigurationClientSync [configurationHash=");
        builder.append(configurationHash);
        builder.append(", resyncOnly=");
        builder.append(resyncOnly);
        builder.append("]");
        return builder.toString();
    }
}
