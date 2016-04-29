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

package org.kaaproject.kaa.server.appenders.flume.appender.client;

import java.io.IOException;

import org.apache.flume.Channel;
import org.apache.flume.ChannelSelector;
import org.apache.flume.Context;
import org.apache.flume.channel.ChannelProcessor;
import org.apache.flume.channel.MemoryChannel;
import org.apache.flume.channel.MultiplexingChannelSelector;
import org.apache.flume.conf.Configurables;
import org.apache.flume.source.AvroSource;

import com.google.common.collect.Lists;

public class FlumeSourceRunner {

    private static FlumeSourceRunner INSTANCE;
    
    private AvroSource flumeSource;
    
    public synchronized static FlumeSourceRunner getInstance() throws Exception {
        if (INSTANCE == null) {
            INSTANCE = new FlumeSourceRunner();
        }
        return INSTANCE;
    }
    
    private FlumeSourceRunner() {
    }

    public synchronized boolean isRunning() {
        return flumeSource != null;
    }

    public synchronized void stopFlumeSource() throws Exception {
        if (flumeSource == null) {
            throw new Exception("Flume source not found");
        }
        flumeSource.stop();
        flumeSource = null;
    }

    public synchronized void startFlumeSource(String name, String bindHost, int port) throws Exception {
        if (flumeSource != null) {
            throw new Exception("Flume source is already running");
        }
        flumeSource = new AvroSource();
        flumeSource.setName(name);
        
        Channel channel = new MemoryChannel();
        
        Context context = prepareContext(bindHost, port);
        
        Configurables.configure(flumeSource, context);
        Configurables.configure(channel, context);
        
        ChannelSelector cs = new MultiplexingChannelSelector();
        cs.setChannels(Lists.newArrayList(channel));

        Configurables.configure(cs, context);

        flumeSource.setChannelProcessor(new ChannelProcessor(cs));

        flumeSource.start();
    }
    
    private Context prepareContext(String bindHost, int port) throws IOException {
        Context context = new Context();

        context.put("bind", bindHost);
        context.put("port", port+"");

        // Channel parameters
        context.put("capacity", "100000000");
        context.put("transactionCapacity", "10000000");
        context.put("keep-alive", "1");

        return context;
    }
 
}
