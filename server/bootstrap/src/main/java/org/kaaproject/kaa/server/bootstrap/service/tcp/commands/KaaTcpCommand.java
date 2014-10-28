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

package org.kaaproject.kaa.server.bootstrap.service.tcp.commands;

import java.util.concurrent.Callable;

import org.kaaproject.kaa.common.bootstrap.gen.ChannelType;
import org.kaaproject.kaa.common.bootstrap.gen.HTTPComunicationParameters;
import org.kaaproject.kaa.common.bootstrap.gen.HTTPLPComunicationParameters;
import org.kaaproject.kaa.common.bootstrap.gen.KaaTCPComunicationParameters;
import org.kaaproject.kaa.common.bootstrap.gen.OperationsServer;
import org.kaaproject.kaa.common.bootstrap.gen.SupportedChannel;
import org.kaaproject.kaa.common.channels.protocols.kaatcp.messages.BootstrapResolve;
import org.kaaproject.kaa.common.channels.protocols.kaatcp.messages.BootstrapResponse;
import org.kaaproject.kaa.common.channels.protocols.kaatcp.messages.BootstrapResponse.PublicKeyType;
import org.kaaproject.kaa.common.channels.protocols.kaatcp.messages.BootstrapResponse.SupportedChannelType;
import org.kaaproject.kaa.common.channels.protocols.kaatcp.messages.BootstrapResponse.UnknownOperationsServerExceptions;
import org.kaaproject.kaa.common.channels.protocols.kaatcp.messages.Disconnect;
import org.kaaproject.kaa.common.channels.protocols.kaatcp.messages.Disconnect.DisconnectReason;
import org.kaaproject.kaa.common.channels.protocols.kaatcp.messages.KaaSync;
import org.kaaproject.kaa.common.channels.protocols.kaatcp.messages.KaaSyncMessageType;
import org.kaaproject.kaa.common.channels.protocols.kaatcp.messages.MessageType;
import org.kaaproject.kaa.common.channels.protocols.kaatcp.messages.MqttFrame;
import org.kaaproject.kaa.server.bootstrap.service.OperationsServerListService;
import org.kaaproject.kaa.server.common.server.kaatcp.AbstractKaaTcpCommandProcessor;

/**
 * KaaTcpCommand Class.
 * Process BootstrapResolve message and produce BootstrapResponse message.
 * @author Andrey Panasenko
 *
 */
public class KaaTcpCommand extends AbstractKaaTcpCommandProcessor implements Callable<KaaTcpCommand> {

    static final String KAA_TCP = "KaaTcp";

    private OperationsServerListService operationsServerListService;
    
    /**
     * Default Constructor.
     * @param operationsServerListService OperationsServerListService
     */
    public KaaTcpCommand(OperationsServerListService operationsServerListService) {
        this.operationsServerListService = operationsServerListService;
    }

    /*
     * (non-Javadoc)
     * @see org.kaaproject.kaa.server.common.server.KaaCommandProcessor#getName()
     */
    @Override
    public String getName() {
        return KAA_TCP;
    }

    /* (non-Javadoc)
     * @see java.util.concurrent.Callable#call()
     */
    @Override
    public KaaTcpCommand call() throws Exception {
        if (getRequest() != null) {
            if (getRequest().getMessageType() == MessageType.KAASYNC) {
                KaaSync kaaSync = (KaaSync)getRequest();
                if (kaaSync.getKaaSyncMessageType() == KaaSyncMessageType.BOOTSTRAP) {
                    BootstrapResolve resolve = (BootstrapResolve) kaaSync;
                    int messageId = resolve.getMessageId();
                    setResponse(getNewBootstrapResponse(messageId));
                    
                } else {
                    setResponse(new Disconnect(DisconnectReason.BAD_REQUEST));
                }
            } else {
                setResponse(new Disconnect(DisconnectReason.BAD_REQUEST));
            }
        } else {
            setResponse(new Disconnect(DisconnectReason.BAD_REQUEST));
        }
        return this;
    }

    /**
     * Pack and return list of Operations servers.
     * @param messageId int
     * @return MqttFrame of BootstrapResponse message
     * @throws UnknownOperationsServerExceptions 
     */
    private MqttFrame getNewBootstrapResponse(int messageId) throws UnknownOperationsServerExceptions {
        BootstrapResponse response = new BootstrapResponse();
        response.setMessageId(messageId);
        for(OperationsServer server : operationsServerListService.getOpsServerList().getOperationsServerArray()){
            response.addOperationsServer(
                    server.getName(), 
                    server.getPriority(), 
                    PublicKeyType.RSA_PKSC8, 
                    server.getPublicKey().array());
            for(SupportedChannel ch : server.getSupportedChannelsArray()) {
                
                SupportedChannelType supportedChannelType;
                String hostName;
                int port;                
                if (ch.getChannelType() == ChannelType.HTTP) {
                    supportedChannelType = SupportedChannelType.HTTP;
                    HTTPComunicationParameters params = (HTTPComunicationParameters)ch.getCommunicationParameters();
                    hostName = params.getHostName();
                    port = params.getPort();
                } else if (ch.getChannelType() == ChannelType.HTTP_LP) {
                    supportedChannelType = SupportedChannelType.HTTPLP;
                    HTTPLPComunicationParameters params = (HTTPLPComunicationParameters)ch.getCommunicationParameters();
                    hostName = params.getHostName();
                    port = params.getPort();
                } else if (ch.getChannelType() == ChannelType.KAATCP) {
                    supportedChannelType = SupportedChannelType.KAATCP;
                    KaaTCPComunicationParameters params = (KaaTCPComunicationParameters)ch.getCommunicationParameters();
                    hostName = params.getHostName();
                    port = params.getPort();
                } else {
                    supportedChannelType = SupportedChannelType.UNUSED;
                    hostName = "";
                    port = 0;
                }

                response.addSupportedChannel(server.getName(), supportedChannelType, hostName, port);
            }
        }
        return response;
    }
    
}
