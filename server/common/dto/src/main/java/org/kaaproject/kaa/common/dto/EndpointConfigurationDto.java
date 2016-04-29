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

package org.kaaproject.kaa.common.dto;

import java.io.Serializable;
import java.util.Arrays;

import static org.kaaproject.kaa.common.dto.Util.getArrayCopy;

public class EndpointConfigurationDto implements Serializable {

    private static final long serialVersionUID = 5662111748223086520L;

    private byte[] configurationHash;
    private byte[] configuration;

    public byte[] getConfigurationHash() {
        return configurationHash;
    }

    public void setConfigurationHash(byte[] configurationHash) {
        this.configurationHash = getArrayCopy(configurationHash);
    }

    public byte[] getConfiguration() {
        return configuration;
    }

    public String getConfigurationAsString() {
        return new String(configuration);
    }

    public void setConfiguration(byte[] configuration) {
        this.configuration = configuration;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        EndpointConfigurationDto that = (EndpointConfigurationDto) o;

        if (!Arrays.equals(configuration, that.configuration)) {
            return false;
        }
        if (!Arrays.equals(configurationHash, that.configurationHash)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = configurationHash != null ? Arrays.hashCode(configurationHash) : 0;
        result = 31 * result + (configuration != null ? Arrays.hashCode(configuration) : 0);
        return result;
    }

    @Override
    public String toString() {
        return "EndpointConfigurationDto{" +
                "configurationHash=" + Arrays.toString(configurationHash) +
                ", configuration=" + Arrays.toString(configuration) +
                '}';
    }
}
