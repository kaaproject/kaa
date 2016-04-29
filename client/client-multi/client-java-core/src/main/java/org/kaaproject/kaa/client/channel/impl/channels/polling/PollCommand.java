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

package org.kaaproject.kaa.client.channel.impl.channels.polling;

import java.util.LinkedHashMap;
import java.util.Map;

import org.kaaproject.kaa.client.channel.ChannelDirection;
import org.kaaproject.kaa.client.channel.IPTransportInfo;
import org.kaaproject.kaa.client.transport.AbstractHttpClient;
import org.kaaproject.kaa.common.TransportType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PollCommand implements Command {

    public static final Logger LOG = LoggerFactory //NOSONAR
            .getLogger(PollCommand.class);

    private final AbstractHttpClient httpClient;
    private final RawDataProcessor processor;
    private final Map<TransportType, ChannelDirection> transportTypes;
    private final IPTransportInfo serverInfo;
    private volatile boolean canceled = false;

    public PollCommand(AbstractHttpClient client, RawDataProcessor processor, Map<TransportType, ChannelDirection> transportTypes, IPTransportInfo serverInfo) {
        this.httpClient = client;
        this.serverInfo = serverInfo;
        this.processor = processor;
        this.transportTypes = transportTypes;
    }

    @Override
    public void execute() {
        try {
            if (httpClient != null ) {
                LinkedHashMap<String, byte[]> request = processor.createRequest(transportTypes); //NOSONAR
                if (request != null && !canceled) {
                    byte[] responseDataRaw = httpClient.executeHttpRequest("", request, false);
                    processor.onResponse(responseDataRaw);
                }
            } else {
                LOG.warn("Unable to execute http request, http client is null.");
            }
        } catch (Exception e) {
            if (!canceled) {
                LOG.error("Server failed {}", e);
            } else {
                LOG.debug("PollCommand execution aborted");
            }
            processor.onServerError(serverInfo);
        }
    }

    @Override
    public void cancel() {
        canceled = true;
        if (httpClient != null && httpClient.canAbort()) {
            httpClient.abort();
        }
    }

}
