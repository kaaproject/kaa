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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.kaaproject.kaa.server.common.Base64Util;

public class EventStorage {

    private final Map<EndpointEvent, Set<RouteTableKey>> dataMap;

    public EventStorage() {
        super();
        dataMap = new HashMap<EndpointEvent, Set<RouteTableKey>>();
    }

    public Set<RouteTableKey> put(EndpointEvent event, Set<RouteTableKey> recipientKeys){
        return dataMap.put(event, recipientKeys);
    }

    public boolean clear(EndpointEvent event) {
        return dataMap.remove(event) != null;
    }

    public List<EndpointEvent> getEvents(RouteTableKey key) {
        return getEvents(key, null);
    }

    public List<EndpointEvent> getEvents(RouteTableKey key, RouteTableAddress targetAddress) {
        String target = null;
        if(targetAddress != null){
            target = Base64Util.encode(targetAddress.getEndpointKey().getData());
        }
        List<EndpointEvent> result = new ArrayList<>();
        for(Entry<EndpointEvent, Set<RouteTableKey>> entry : dataMap.entrySet()){
            EndpointEvent event = entry.getKey();
            if(targetAddress != null && targetAddress.getEndpointKey().equals(event.getSender())){
                continue;
            }
            if(entry.getValue().contains(key) && (event.getTarget() == null || target == null || event.getTarget().equals(target))) {
                result.add(entry.getKey());
            }
        }
        return result;
    }

}
