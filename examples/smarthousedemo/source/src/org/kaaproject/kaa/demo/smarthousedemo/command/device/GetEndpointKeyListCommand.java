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

import java.util.Arrays;
import java.util.List;

import org.kaaproject.kaa.client.KaaClient;
import org.kaaproject.kaa.client.event.FetchEventListeners;
import org.kaaproject.kaa.demo.smarthouse.device.DeviceInfoRequest;
import org.kaaproject.kaa.demo.smarthousedemo.command.AbstractClientCommand;
import org.kaaproject.kaa.demo.smarthousedemo.exception.CommandException;

import android.util.Log;

/** Implementation of get list of endpoint keys command.
 */
public class GetEndpointKeyListCommand extends AbstractClientCommand<List<String>> {

        public GetEndpointKeyListCommand(KaaClient client) {
            super(client);
        }

        @Override
        protected void executeAsync() {
            List<String> fqns = Arrays.asList(DeviceInfoRequest.class.getName());
            client.findEventListeners(fqns, new FetchEventListeners() {
                @Override
                public void onEventListenersReceived(
                        List<String> endpontKeys) {
                    Log.d("Kaa", "Got list of attached enpoint keys, size " + endpontKeys.size());
                    onComplete(endpontKeys);
                }
    
                @Override
                public void onRequestFailed() {
                    Log.e("Kaa", "Unable to get list of available endpoint keys!");
                    onException(new CommandException("Unable to get list of endpoint keys!"));
                }
            });
        }
    }