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

import java.util.List;
import org.kaaproject.kaa.server.common.zk.gen.TransportMetaData;

/**
 * Represents listener to transport state updates.
 * 
 * @author Andrew Shvayka
 *
 */
public interface TransportUpdateListener {

    /**
     * Notify about the initialized transports.
     * @param msList a list of the initialized transport meta data
     */
    void onTransportsStarted(List<TransportMetaData> mdList);

}
