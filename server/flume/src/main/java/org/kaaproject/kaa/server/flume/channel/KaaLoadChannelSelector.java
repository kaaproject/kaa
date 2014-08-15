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
package org.kaaproject.kaa.server.flume.channel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.flume.Channel;
import org.apache.flume.Context;
import org.apache.flume.Event;
import org.apache.flume.channel.AbstractChannelSelector;

public class KaaLoadChannelSelector extends AbstractChannelSelector {

    private List<Channel> allChannels = null;   
    private List<Channel> optionalChannels = new ArrayList<Channel>();
    
    private int currentChannel = 0;
    
    @Override
    public List<Channel> getRequiredChannels(Event event) {
        Channel channel = allChannels.get(currentChannel);
        currentChannel++;
        if (currentChannel == allChannels.size()) {
            currentChannel = 0;
        }
        return Collections.singletonList(channel);
    }

    @Override
    public List<Channel> getOptionalChannels(Event event) {
        return optionalChannels;
    }

    @Override
    public void configure(Context context) {
        allChannels = getAllChannels();
    }

}
