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
import java.util.Map;

public class EventDeliveryTable {

    private static enum DeliveryState {
        PENDING, 
        DELIVERED
    }

    Map<EndpointEvent, Map<RouteTableAddress, DeliveryState>> data;

    public EventDeliveryTable() {
        super();
        this.data = new HashMap<>();
    }

    public void registerDeliveryAttempt(EndpointEvent event, RouteTableAddress... addresses){
        Map<RouteTableAddress, DeliveryState> attempts = data.get(event);
        if(attempts == null){
            attempts = new HashMap<>();
            data.put(event, attempts);
        }
        for(RouteTableAddress address : addresses){
            attempts.put(address, DeliveryState.PENDING);
        }
    }

    public boolean isDeliveryStarted(EndpointEvent event, RouteTableAddress address){
        Map<RouteTableAddress, DeliveryState> attempts = data.get(event);
        return attempts != null && attempts.containsKey(address);
    }

    public boolean clear(EndpointEvent event) {
        return data.remove(event) != null;
    }

    public void registerDeliverySuccess(EndpointEvent event, RouteTableAddress address) {
        Map<RouteTableAddress, DeliveryState> attempts = data.get(event);
        if(attempts == null){
            attempts = new HashMap<>();
            data.put(event, attempts);
        }
        attempts.put(address, DeliveryState.DELIVERED);
    }

    public void registerDeliveryFailure(EndpointEvent event, RouteTableAddress address) {
        Map<RouteTableAddress, DeliveryState> attempts = data.get(event);
        if(attempts != null){
            attempts.remove(address);
        }
    }
}
