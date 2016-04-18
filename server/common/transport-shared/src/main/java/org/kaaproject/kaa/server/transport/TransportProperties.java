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

package org.kaaproject.kaa.server.transport;

import java.util.Properties;
import java.util.Map.Entry;

/**
 * Stores common properties for all transports. Majority of these properties are
 * related to the runtime environment or specific to the host.
 * 
 * @author Andrew Shvayka
 *
 */
public class TransportProperties extends Properties {

    /**
     * 
     */
    private static final long serialVersionUID = -3398931583634951967L;

    private static final String FILTER_PREFIX = "transport_";

    public TransportProperties(Properties source) {
        super();
        for (Entry<Object, Object> entry : source.entrySet()) {
            if (entry.getKey().toString().startsWith(FILTER_PREFIX)) {
                this.put(entry.getKey(), entry.getValue());
            }
        }
    }
}
