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

package org.kaaproject.kaa.common.dto;

import static org.kaaproject.kaa.common.dto.Util.getArrayCopy;

import java.io.Serializable;
import java.util.Arrays;

public class EndpointConfigurationDto implements HasId, Serializable {

    private static final long serialVersionUID = 5662111748223086520L;

    private String id;
    private byte[] configurationHash;
    private byte[] configuration;

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

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

//    public BaseData getBaseConfiguration(BaseSchema schema) {
//        return new BaseData(schema, new String(configuration));
//    }

    public void setConfiguration(byte[] configuration) {
        this.configuration = configuration;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((configuration == null) ? 0 : configuration.hashCode());
        result = prime * result + Arrays.hashCode(configurationHash);
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof EndpointConfigurationDto)) {
            return false;
        }

        EndpointConfigurationDto that = (EndpointConfigurationDto) o;
        if (!Arrays.equals(configuration, that.configuration)) {
            return false;
        }

        return Arrays.equals(configurationHash, that.configurationHash);
    }

    @Override
    public String toString() {
        return "EndpointConfigurationDto{" +
                "id='" + id + '\'' +
                ", configurationHash='" + configurationHash + '\'' +
                ", configuration='" + configuration + '\'' +
                '}';
    }
}
