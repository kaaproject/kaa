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
package org.kaaproject.kaa.common.channels;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.kaaproject.kaa.common.TransportType;
import org.kaaproject.kaa.common.bootstrap.gen.ChannelType;

/**
 * Abstract Channel Class, implements base methods to check supported transports
 * for specific Channels.
 * @author Andrey Panasenko
 *
 */
public abstract class Channel {

    /**
     * Return List of supported transports for Channel
     * @return List<TransportTypes> of supported transports.
     */
    public abstract List<TransportType> getSupportedTransports();

    /**
     * Return AVRO ChannelType for Channel instance
     * @return ChannelType of Channel
     */
    public abstract ChannelType getChannelType();

    /**
     * Check if specified TransportType is supported by Channel
     * @param type TransportTypes
     * @return boolean, true if specified type is supported by Channel
     */
    public abstract boolean isTransportTypeSupported(TransportType type);

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "HttpChannel [" + getChannelType() + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getChannelType() == null) ? 0 : getChannelType().hashCode());
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
        Channel other = (Channel) obj;
        if (getChannelType() != other.getChannelType()) {
            return false;
        }
        return true;
    }

    protected static boolean isTransportTypeSupported(TransportType[] supported, TransportType type) {
        boolean isSupported = false;
        for (int i = 0; i < supported.length; i++) {
            if (type == supported[i]) {
                isSupported = true;
                break;
            }
        }
        return isSupported;
    }

    protected static List<TransportType> listTransportTypesFromArray(TransportType[] supportedTransports) {
        return new ArrayList<>(Arrays.asList(supportedTransports)); //NOSONAR
    }
}
