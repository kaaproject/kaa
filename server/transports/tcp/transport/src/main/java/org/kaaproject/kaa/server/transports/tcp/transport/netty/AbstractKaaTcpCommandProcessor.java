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

package org.kaaproject.kaa.server.transports.tcp.transport.netty;

import org.kaaproject.kaa.common.channels.protocols.kaatcp.messages.MqttFrame;
import org.kaaproject.kaa.server.common.server.KaaCommandProcessor;

public abstract class AbstractKaaTcpCommandProcessor implements KaaCommandProcessor<MqttFrame, MqttFrame> {

    /** Time of SYNC processing */
    private long syncTime = 0;

    /** integer representing ID of HTTP request */
    private int commandId;

    private MqttFrame kaaTcpRequest;
    private MqttFrame kaaTcpResponse;

    @Override
    public int getCommandId() {
        return commandId;
    }

    @Override
    public void setCommandId(int commandId) {
        this.commandId = commandId;
    }

    @Override
    public long getSyncTime() {
        return syncTime;
    }

    @Override
    public void setSyncTime(long syncTime) {
        this.syncTime = syncTime;
    }

    @Override
    public MqttFrame getResponse() {
        return kaaTcpResponse;
    }

    @Override
    public void setResponse(MqttFrame response) {
        this.kaaTcpResponse = response;
    }

    @Override
    public MqttFrame getRequest() {
        return kaaTcpRequest;
    }

    @Override
    public void setRequest(MqttFrame request) {
        this.kaaTcpRequest = request;
    }

}
