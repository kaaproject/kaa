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
package org.kaaproject.kaa.server.common.server.kaatcp;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.junit.Test;
import org.kaaproject.kaa.common.channels.protocols.kaatcp.KaaTcpProtocolException;
import org.kaaproject.kaa.common.channels.protocols.kaatcp.listeners.PingResponseListener;
import org.kaaproject.kaa.common.channels.protocols.kaatcp.messages.Disconnect;
import org.kaaproject.kaa.common.channels.protocols.kaatcp.messages.Disconnect.DisconnectReason;
import org.kaaproject.kaa.common.channels.protocols.kaatcp.messages.MessageFactory;
import org.kaaproject.kaa.common.channels.protocols.kaatcp.messages.MessageType;
import org.kaaproject.kaa.common.channels.protocols.kaatcp.messages.MqttFrame;
import org.kaaproject.kaa.common.channels.protocols.kaatcp.messages.PingRequest;
import org.kaaproject.kaa.common.channels.protocols.kaatcp.messages.PingResponse;
import org.kaaproject.kaa.server.common.server.BadRequestException;
import org.kaaproject.kaa.server.common.server.Config;
import org.kaaproject.kaa.server.common.server.KaaCommandProcessor;
import org.kaaproject.kaa.server.common.server.KaaCommandProcessorFactory;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KaaTcpServerIT {

    private static final int TIMEOUT = 10000;
    private static final int TEST_PORT = 9997;
    private static final String TEST_HOST = "localhost";
    private static final Logger LOG = LoggerFactory.getLogger(KaaTcpServerIT.class);

    @Test
    public void basicKaaTcpServerTest() throws Exception{
        Config config = new Config();
        config.setBindInterface(TEST_HOST);
        config.setPort(TEST_PORT);
        KaaCommandProcessorFactory<MqttFrame, MqttFrame> factory = getCommandFactory();

        List<KaaCommandProcessorFactory> commands = new ArrayList<>();
        commands.add(factory);
        config.setCommandList(commands);
        NettyKaaTcpServer server = createServer(config);

        PingResponseListener listener = Mockito.mock(PingResponseListener.class);
        final MessageFactory messageFactory = new MessageFactory();
        messageFactory.registerMessageListener(listener);
        try{
            LOG.debug("Initializing TCP server");
            server.init();
            LOG.debug("Starting TCP server");
            server.start();

            final Socket socket = new Socket(TEST_HOST, TEST_PORT);

            Runnable readTask = createReader(messageFactory, socket);

            Thread reader = new Thread(readTask);
            reader.start();

            PingRequest request = new PingRequest();
            socket.getOutputStream().write(request.getFrame().array());

            Mockito.verify(listener,  Mockito.timeout(TIMEOUT).atLeastOnce()).onMessage(Mockito.any(PingResponse.class));

            PingResponse response = new PingResponse();
            socket.getOutputStream().write(response.getFrame().array());
            reader.interrupt();
            socket.close();
        }finally{
            LOG.debug("Shutdown TCP server");
            server.shutdown();
        }

    }

    @Test
    public void basicKaaTcpServerDisconnectTest() throws Exception{
        Config config = new Config();
        config.setBindInterface(TEST_HOST);
        config.setPort(TEST_PORT);
        KaaCommandProcessorFactory<MqttFrame, MqttFrame> factory = getCommandFactory();

        List<KaaCommandProcessorFactory> commands = new ArrayList<>();
        commands.add(factory);
        config.setCommandList(commands);
        NettyKaaTcpServer server = createServer(config);

        PingResponseListener listener = Mockito.mock(PingResponseListener.class);
        final MessageFactory messageFactory = new MessageFactory();
        messageFactory.registerMessageListener(listener);
        try{
            LOG.debug("Initializing TCP server");
            server.init();
            LOG.debug("Starting TCP server");
            server.start();

            final Socket socket = new Socket(TEST_HOST, TEST_PORT);

            Runnable readTask = createReader(messageFactory, socket);

            Thread reader = new Thread(readTask);
            reader.start();

            PingRequest request = new PingRequest();
            socket.getOutputStream().write(request.getFrame().array());

            Mockito.verify(listener,  Mockito.timeout(TIMEOUT).atLeastOnce()).onMessage(Mockito.any(PingResponse.class));

            Disconnect disconnect = new Disconnect(DisconnectReason.BAD_REQUEST);
            socket.getOutputStream().write(disconnect.getFrame().array());
            reader.interrupt();
            socket.close();
        }finally{
            LOG.debug("Shutdown TCP server");
            server.shutdown();
        }

    }

    protected Runnable createReader(final MessageFactory messageFactory, final Socket socket) {
        Runnable readTask =  new Runnable() {
            byte [] buffer = new byte[1024];

            @Override
            public void run() {
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        LOG.info("reading data from stream using [{}] byte buffer", buffer.length);
                        int size = socket.getInputStream().read(buffer);
                        LOG.info("read data {} bytes from stream", size);
                        if (size > 0) {
                            messageFactory.getFramer().pushBytes(Arrays.copyOf(buffer, size));
                        }else if(size == -1){
                            LOG.info("received end of stream", size);
                            break;
                        }
                    } catch (IOException | KaaTcpProtocolException | RuntimeException e) {
                        if (!Thread.currentThread().isInterrupted()) {
                            LOG.error("Failed to read from the socket for channel" ,e);
                        } else {
                            LOG.info("Socket connection was interrupted");
                        }
                    }
                }
                LOG.info("Read Task is interrupted");
            }
        };
        return readTask;
    }

    protected NettyKaaTcpServer createServer(Config config) {
        NettyKaaTcpServer server = new NettyKaaTcpServer(config, new AbstractKaaTcpServerInitializer() {

            @Override
            protected SimpleChannelInboundHandler<AbstractKaaTcpCommandProcessor> getMainHandler(UUID uuid) {
                return new SimpleChannelInboundHandler<AbstractKaaTcpCommandProcessor>() {

                    @Override
                    protected void channelRead0(ChannelHandlerContext ctx, AbstractKaaTcpCommandProcessor msg) throws Exception {
                        MqttFrame frame =  msg.getRequest();
                        LOG.debug("Received frame: {}", frame);
                        msg.setRequest(frame);
                        if(msg.getSyncTime() == 0){
                            msg.setSyncTime(System.currentTimeMillis());
                        }
                        if(frame.getMessageType() == MessageType.PINGREQ){
                            ctx.writeAndFlush(new PingResponse());
                        }else if(frame.getMessageType() == MessageType.PINGRESP){
                            throw new RuntimeException("not supported command");
                        }else{
                            throw new BadRequestException("not supported command");
                        }
                    }

                };
            }
        });
        return server;
    }

    protected KaaCommandProcessorFactory<MqttFrame, MqttFrame> getCommandFactory() {
        KaaCommandProcessorFactory<MqttFrame, MqttFrame> factory = new KaaCommandProcessorFactory<MqttFrame, MqttFrame>() {

            @Override
            public String getCommandName() {
                return KaaTcpDecoder.KAA_TCP_COMMAND_NAME;
            }

            @Override
            public KaaCommandProcessor<MqttFrame, MqttFrame> createCommandProcessor() {
                return new AbstractKaaTcpCommandProcessor() {

                    @Override
                    public String getName() {
                        return KaaTcpDecoder.KAA_TCP_COMMAND_NAME;
                    }
                };
            }
        };
        return factory;
    }
}
