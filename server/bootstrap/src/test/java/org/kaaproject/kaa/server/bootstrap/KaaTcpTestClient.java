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
package org.kaaproject.kaa.server.bootstrap;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import org.kaaproject.kaa.common.bootstrap.gen.ChannelType;
import org.kaaproject.kaa.common.bootstrap.gen.HTTPComunicationParameters;
import org.kaaproject.kaa.common.bootstrap.gen.HTTPLPComunicationParameters;
import org.kaaproject.kaa.common.bootstrap.gen.KaaTCPComunicationParameters;
import org.kaaproject.kaa.common.bootstrap.gen.OperationsServer;
import org.kaaproject.kaa.common.bootstrap.gen.OperationsServerList;
import org.kaaproject.kaa.common.bootstrap.gen.SupportedChannel;
import org.kaaproject.kaa.common.channels.protocols.kaatcp.KaaTcpProtocolException;
import org.kaaproject.kaa.common.channels.protocols.kaatcp.listeners.BootstrapResponseListener;
import org.kaaproject.kaa.common.channels.protocols.kaatcp.messages.BootstrapResolve;
import org.kaaproject.kaa.common.channels.protocols.kaatcp.messages.BootstrapResponse;
import org.kaaproject.kaa.common.channels.protocols.kaatcp.messages.BootstrapResponse.OperationsServerRecord;
import org.kaaproject.kaa.common.channels.protocols.kaatcp.messages.BootstrapResponse.SupportedChannelRecord;
import org.kaaproject.kaa.common.channels.protocols.kaatcp.messages.BootstrapResponse.SupportedChannelType;
import org.kaaproject.kaa.common.channels.protocols.kaatcp.messages.MessageFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Andrey Panasenko
 *
 */
public class KaaTcpTestClient implements Runnable, BootstrapResponseListener {
    /** The Constant logger. */
    protected static final Logger logger = LoggerFactory
            .getLogger(KaaTcpTestClient.class);
    
    private final Socket clientSocket;
    
    private final DataInputStream inReader;
    
    private final DataOutputStream outWriter;
    
    private String applicationToken;
    
    private MessageFactory factory;
    
    private boolean frameReceived = false;
    
    private OperationsServerList opServerList;
    
    private HttpActivity activity;
    
    private Exception exception;
    
    private boolean connected = false;
    /**
     * @throws IOException 
     * @throws UnknownHostException 
     * 
     */
    public KaaTcpTestClient(String hostName, int port, String applicationToken, HttpActivity activity) throws UnknownHostException, IOException {
        clientSocket = new Socket(hostName, port);
        connected = true;
        inReader = new DataInputStream(clientSocket.getInputStream());
        outWriter = new DataOutputStream(clientSocket.getOutputStream());
        this.applicationToken = applicationToken;
        this.activity = activity;
        factory = new MessageFactory();
        factory.registerMessageListener(this);
        logger.info("KaaTcp Client Connection to {}:{} initialized...",hostName,port);
    }

    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {
        try {
            BootstrapResolve resolve = new BootstrapResolve(applicationToken);
            outWriter.write(resolve.getFrame().array());
            byte[] buff = new byte[1024];
            int c = 0;
            while(!frameReceived) {
                c = inReader.read(buff);
                byte[] buffer = Arrays.copyOf(buff, c);
                if (c > 0) {
                    factory.getFramer().pushBytes(buffer);
                }
            }
            logger.debug("KaaTcp message got, wait socket close");
            c = inReader.read();
            if (c == -1) {
                connected = false;
            }
            logger.debug("KaaTcp socket close, read {} bytes", c);
        } catch (IOException e) {
            logger.error("KaaTcp IOException", e);
            exception = e;
        } catch (KaaTcpProtocolException e) {
            logger.error("KaaTcp KaatcpProtocolException", e);
            exception = e;
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            
            if (opServerList != null) {
                activity.httpRequestComplete(exception, 0, opServerList);
            } else {
                if (exception != null) {
                    activity.httpRequestComplete(exception, 0, null);
                } else {
                    exception = new Exception("Error KaaTcp BootstrapResolve not parsed");
                    activity.httpRequestComplete(exception, 0, null);
                }
            }
        }
    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.common.channels.protocols.kaatcp.listeners.MessageListener#onMessage(org.kaaproject.kaa.common.channels.protocols.kaatcp.messages.MqttFrame)
     */
    @Override
    public void onMessage(BootstrapResponse message) {
        logger.debug("Bootstrap Response message received");
        frameReceived = true;
        opServerList = new OperationsServerList();
        List<OperationsServer> opList = new LinkedList<>();
        
        for(OperationsServerRecord record : message.getOperationsServers().values()) {
            List<SupportedChannel> supportedChannelsArray = new LinkedList<>();
            for(SupportedChannelRecord chRecord : record.supportedChannelsList) {
                SupportedChannel suppChannel = new SupportedChannel();
                if (chRecord.supportedChannelType == SupportedChannelType.HTTP) {
                    suppChannel.setChannelType(ChannelType.HTTP);
                    suppChannel.setCommunicationParameters(new HTTPComunicationParameters(chRecord.hostName, chRecord.port));
                } else if (chRecord.supportedChannelType == SupportedChannelType.HTTPLP) {
                    suppChannel.setChannelType(ChannelType.HTTP_LP);
                    suppChannel.setCommunicationParameters(new HTTPLPComunicationParameters(chRecord.hostName, chRecord.port));
                } else if (chRecord.supportedChannelType == SupportedChannelType.KAATCP) {
                    suppChannel.setChannelType(ChannelType.KAATCP);
                    suppChannel.setCommunicationParameters(new KaaTCPComunicationParameters(chRecord.hostName, chRecord.port));
                } else {
                    logger.error("Error unknown ChannelType");
                    exception = new Exception("Error unknown ChannelType");
                    continue;
                }
                supportedChannelsArray.add(suppChannel);
            }
            OperationsServer server = new OperationsServer(
                    record.name, 
                    record.priority, 
                    ByteBuffer.wrap(record.publicKey), 
                    supportedChannelsArray);
            opList.add(server);
        }
        opServerList.setOperationsServerArray(opList );
        
    }

    public boolean isConnected() {
        return connected;
    }
}
