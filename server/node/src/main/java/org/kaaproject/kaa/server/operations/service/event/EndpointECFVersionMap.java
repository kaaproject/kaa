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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kaaproject.kaa.common.hash.EndpointObjectHash;

public class EndpointECFVersionMap {

    private final Map<EndpointObjectHash, Map<String, Integer>> map;

    public EndpointECFVersionMap() {
        super();
        map = new HashMap<EndpointObjectHash, Map<String, Integer>>();
    }

    public void put(EndpointObjectHash endpoint, List<EventClassFamilyVersion> versions) {
        Map<String, Integer> innerMap = new HashMap<>();
        for (EventClassFamilyVersion ecfVersion : versions) {
            innerMap.put(ecfVersion.getEcfId(), ecfVersion.getVersion());
        }
        map.put(endpoint, innerMap);
    }

    public Integer get(EndpointObjectHash endpoint, String ecfId) {
        Map<String, Integer> ecfVersions = map.get(endpoint);
        if (ecfVersions != null) {
            return ecfVersions.get(ecfId);
        } else {
            return null;
        }
    }

    public boolean remove(EndpointObjectHash endpoint){
        return map.remove(endpoint) != null;
    }
}
