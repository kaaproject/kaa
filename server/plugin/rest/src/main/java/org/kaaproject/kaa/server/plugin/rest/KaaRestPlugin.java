/*
 * Copyright 2014-2015 CyberVision, Inc.
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

package org.kaaproject.kaa.server.plugin.rest;

import java.io.IOException;
import java.util.List;

import org.kaaproject.kaa.common.avro.AvroByteArrayConverter;
import org.kaaproject.kaa.server.common.core.plugin.def.Plugin;
import org.kaaproject.kaa.server.common.core.plugin.def.PluginExecutionContext;
import org.kaaproject.kaa.server.common.core.plugin.def.PluginInitContext;
import org.kaaproject.kaa.server.common.core.plugin.instance.KaaPlugin;
import org.kaaproject.kaa.server.common.core.plugin.instance.KaaPluginMessage;
import org.kaaproject.kaa.server.common.core.plugin.instance.PluginLifecycleException;
import org.kaaproject.kaa.server.plugin.rest.gen.HttpSchemaType;
import org.kaaproject.kaa.server.plugin.rest.gen.KaaRestPluginConfig;
import org.kaaproject.kaa.server.plugin.rest.gen.KaaRestPluginItemConfig;
import org.kaaproject.kaa.server.plugin.rest.gen.RequestParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestTemplate;

@Plugin(KaaRestPluginDef.class)
public class KaaRestPlugin implements KaaPlugin {

    private static final Logger LOG = LoggerFactory.getLogger(KaaRestPlugin.class);

    private KaaRestPluginConfig config;
    private RestTemplate restTemplate;

    @Override
    public void init(PluginInitContext context) throws PluginLifecycleException {
        AvroByteArrayConverter<KaaRestPluginConfig> converter = new AvroByteArrayConverter<KaaRestPluginConfig>(KaaRestPluginConfig.class);
        try {
            LOG.info("Initializing the plugin with {}", context.toString());
            this.config = converter.fromByteArray(context.getPluginConfigurationData().getBytes());
        } catch (IOException cause) {
            LOG.error("Failed to initialize the plugin!", cause);
            throw new RuntimeException();
        }
    }

    @Override
    public void onPluginMessage(KaaPluginMessage message, PluginExecutionContext ctx) {
        AvroByteArrayConverter<KaaRestPluginItemConfig> converter = new AvroByteArrayConverter<KaaRestPluginItemConfig>(KaaRestPluginItemConfig.class);
        try {
            KaaRestPluginItemConfig itemConfig = converter.fromByteArray(message.getItemInfo().getConfigurationData());
            switch (itemConfig.getRequestMethod()) {
                case GET:
                case POST:
                case PUT:
                case DELETE:
            }
        } catch (IOException cause) {
        }
    }

    public void doGet(String mapping, List<RequestParam> requestParams, String inputSchema, String outputSchema) {
        StringBuilder buffer = new StringBuilder(mapping);
    }

    @Override
    public void stop() throws PluginLifecycleException {
    }

    // TODO: Used for testing purposes, remove when unnecessary
    public static void main(String[] args) throws IOException {
        PluginHttpServer httpServer = new PluginHttpServer();
        KaaRestPluginConfig config = new KaaRestPluginConfig();
        config.setHost("localhost");
        config.setPort(9999);
        config.setHttpSchema(HttpSchemaType.HTTP);
        httpServer.initServer(config);
        httpServer.start();
        System.in.read();
        httpServer.stop();
    }
}
