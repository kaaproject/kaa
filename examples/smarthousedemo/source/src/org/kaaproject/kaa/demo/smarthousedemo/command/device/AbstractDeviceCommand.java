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

package org.kaaproject.kaa.demo.smarthousedemo.command.device;

import java.util.Map;

import org.kaaproject.kaa.demo.smarthouse.device.DeviceEventClassFamily;
import org.kaaproject.kaa.demo.smarthousedemo.command.EndpointCommandKey;
import org.kaaproject.kaa.demo.smarthousedemo.concurrent.BlockingCallable;

public abstract class AbstractDeviceCommand<V> extends BlockingCallable<V> {
    
    private final Map<EndpointCommandKey, BlockingCallable<?>> commandMap;
    protected final DeviceEventClassFamily devices;
    protected final String endpontKey;
    protected final Class<V> clazz;
    
    AbstractDeviceCommand(Map<EndpointCommandKey, BlockingCallable<?>> commandMap, 
            DeviceEventClassFamily devices,
            String endpontKey, 
            Class<V> clazz) {
        this.commandMap = commandMap;
        this.devices = devices;
        this.endpontKey = endpontKey;
        this.clazz = clazz;
    }
    
    @Override
    protected void executeAsync() {
        EndpointCommandKey key = new EndpointCommandKey(clazz.getName(), endpontKey);
        commandMap.put(key, this);
        executeDeviceCommand();
    }
    
    protected abstract void executeDeviceCommand();
    
}
