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

package org.kaaproject.kaa.demo.smarthousedemo.command.music;

import java.util.Map;

import org.kaaproject.kaa.demo.smarthouse.music.MusicEventClassFamily;
import org.kaaproject.kaa.demo.smarthouse.music.PlayListRequest;
import org.kaaproject.kaa.demo.smarthouse.music.PlayListResponse;
import org.kaaproject.kaa.demo.smarthousedemo.command.EndpointCommandKey;
import org.kaaproject.kaa.demo.smarthousedemo.concurrent.BlockingCallable;

/** Implementation of get list of song info command.
 */
public class GetPlayListCommand extends AbstractPlayerCommand<PlayListResponse> {

    public GetPlayListCommand(Map<EndpointCommandKey, BlockingCallable<?>> commandMap,
            MusicEventClassFamily players, String endpontKey) {
        super(commandMap, players, endpontKey, PlayListResponse.class);
    }

    @Override
    protected void executePlayerCommand() {
        PlayListRequest request = new PlayListRequest();
        players.sendEvent(request, endpontKey);
    }
}