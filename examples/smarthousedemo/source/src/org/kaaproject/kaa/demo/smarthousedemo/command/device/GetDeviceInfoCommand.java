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
import org.kaaproject.kaa.demo.smarthouse.device.DeviceInfoRequest;
import org.kaaproject.kaa.demo.smarthouse.device.DeviceInfoResponse;
import org.kaaproject.kaa.demo.smarthousedemo.command.EndpointCommandKey;
import org.kaaproject.kaa.demo.smarthousedemo.concurrent.BlockingCallable;

/** The implementation of the get device info command.
 */
public class GetDeviceInfoCommand extends AbstractDeviceCommand<DeviceInfoResponse> {

    public GetDeviceInfoCommand(Map<EndpointCommandKey, BlockingCallable<?>> commandMap,
            DeviceEventClassFamily devices, String endpontKey) {
        super(commandMap, devices, endpontKey, DeviceInfoResponse.class);
    }

    @Override
    protected void executeDeviceCommand() {
        DeviceInfoRequest request = new DeviceInfoRequest();
        devices.sendEvent(request, endpontKey);
    }
}
