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

package org.kaaproject.kaa.server.transports.http.transport;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.kaaproject.kaa.server.common.server.AbstractNettyServer;
import org.kaaproject.kaa.server.common.server.CommandFactory;
import org.kaaproject.kaa.server.common.server.KaaCommandProcessorFactory;
import org.kaaproject.kaa.server.transport.AbstractKaaTransport;
import org.kaaproject.kaa.server.transport.SpecificTransportContext;
import org.kaaproject.kaa.server.transport.TransportLifecycleException;
import org.kaaproject.kaa.server.transport.http.config.gen.AvroHttpConfig;
import org.kaaproject.kaa.server.transports.http.transport.commands.LongSyncCommandFactory;
import org.kaaproject.kaa.server.transports.http.transport.commands.SyncCommandFactory;
import org.kaaproject.kaa.server.transports.http.transport.netty.AbstractCommand;
import org.kaaproject.kaa.server.transports.http.transport.netty.DefaultHttpServerInitializer;
import org.kaaproject.kaa.server.transports.http.transport.netty.RequestDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of Kaa http transport
 * 
 * @author Andrew Shvayka
 *
 */
public class HttpTransport extends AbstractKaaTransport<AvroHttpConfig> {

    private static final Logger LOG = LoggerFactory.getLogger(HttpTransport.class);
    private static final int SUPPORTED_VERSION = 1;

    private AbstractNettyServer netty;

    @Override
    public void init(SpecificTransportContext<AvroHttpConfig> context) throws TransportLifecycleException {
        AvroHttpConfig configuration = context.getConfiguration();
        configuration.setBindInterface(replaceProperty(configuration.getBindInterface(), BIND_INTERFACE_PROP_NAME, context
                .getCommonProperties().getProperty(BIND_INTERFACE_PROP_NAME, LOCALHOST)));
        configuration.setPublicInterface(replaceProperty(configuration.getPublicInterface(), PUBLIC_INTERFACE_PROP_NAME, context
                .getCommonProperties().getProperty(PUBLIC_INTERFACE_PROP_NAME, LOCALHOST)));
        List<KaaCommandProcessorFactory<HttpRequest, HttpResponse>> processors = new ArrayList<KaaCommandProcessorFactory<HttpRequest, HttpResponse>>();
        processors.add(new SyncCommandFactory());
        processors.add(new LongSyncCommandFactory());
        final CommandFactory<HttpRequest, HttpResponse> factory = new CommandFactory<>(processors);
        final int maxBodySize = configuration.getMaxBodySize();

        this.netty = new AbstractNettyServer(configuration.getBindInterface(), configuration.getBindPort()) {

            @Override
            protected ChannelInitializer<SocketChannel> configureInitializer() throws Exception {
                return new DefaultHttpServerInitializer() {
                    @Override
                    protected SimpleChannelInboundHandler<AbstractCommand> getMainHandler(UUID uuid) {
                        return new HttpHandler(uuid, HttpTransport.this.handler);
                    }

                    @Override
                    public int getClientMaxBodySize() {
                        return maxBodySize;
                    }

                    @Override
                    protected ChannelHandler getRequestDecoder() {
                        return new RequestDecoder(factory);
                    }
                };
            }
        };
    }

    @Override
    public void start() {
        LOG.info("Initializing netty");
        netty.init();
        LOG.info("Starting netty");
        netty.start();
    }

    @Override
    public void stop() {
        LOG.info("Stopping netty");
        netty.shutdown();
    }

    @Override
    public Class<AvroHttpConfig> getConfigurationClass() {
        return AvroHttpConfig.class;
    }

    @Override
    protected ByteBuffer getSerializedConnectionInfo() {
        byte[] interfaceData = toUTF8Bytes(context.getConfiguration().getPublicInterface());
        byte[] publicKeyData = context.getServerKey().getEncoded();
        ByteBuffer buf = ByteBuffer.wrap(new byte[SIZE_OF_INT * 3 + interfaceData.length + publicKeyData.length]);
        buf.putInt(publicKeyData.length);
        buf.put(publicKeyData);
        buf.putInt(interfaceData.length);
        buf.put(interfaceData);
        buf.putInt(context.getConfiguration().getPublicPort());
        return buf;
    }

    @Override
    protected int getMinSupportedVersion() {
        return SUPPORTED_VERSION;
    }

    @Override
    protected int getMaxSupportedVersion() {
        return SUPPORTED_VERSION;
    }
}
