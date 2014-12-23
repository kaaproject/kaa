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
package org.kaaproject.kaa.server.operations.pojo.sync;

import java.nio.ByteBuffer;

public class ConfigurationClientSync {
    private int appStateSeqNumber;
    private ByteBuffer configurationHash;

    public ConfigurationClientSync() {
    }

    /**
     * All-args constructor.
     */
    public ConfigurationClientSync(int appStateSeqNumber, ByteBuffer configurationHash) {
        this.appStateSeqNumber = appStateSeqNumber;
        this.configurationHash = configurationHash;
    }

    /**
     * Gets the value of the 'appStateSeqNumber' field.
     */
    public int getAppStateSeqNumber() {
        return appStateSeqNumber;
    }

    /**
     * Sets the value of the 'appStateSeqNumber' field.
     * 
     * @param value
     *            the value to set.
     */
    public void setAppStateSeqNumber(int value) {
        this.appStateSeqNumber = value;
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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + appStateSeqNumber;
        result = prime * result + ((configurationHash == null) ? 0 : configurationHash.hashCode());
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
        if (appStateSeqNumber != other.appStateSeqNumber) {
            return false;
        }
        if (configurationHash == null) {
            if (other.configurationHash != null) {
                return false;
            }
        } else if (!configurationHash.equals(other.configurationHash)) {
            return false;
        }
        return true;
    }
}
