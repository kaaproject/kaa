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
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.kaaproject.kaa.common.avro.AvroByteArrayConverter;
import org.kaaproject.kaa.common.avro.GenericAvroConverter;
import org.kaaproject.kaa.server.common.core.plugin.def.Plugin;
import org.kaaproject.kaa.server.common.core.plugin.def.PluginExecutionContext;
import org.kaaproject.kaa.server.common.core.plugin.def.PluginInitContext;
import org.kaaproject.kaa.server.common.core.plugin.instance.KaaMessage;
import org.kaaproject.kaa.server.common.core.plugin.instance.KaaPlugin;
import org.kaaproject.kaa.server.common.core.plugin.instance.KaaPluginMessage;
import org.kaaproject.kaa.server.common.core.plugin.instance.PluginLifecycleException;
import org.kaaproject.kaa.server.plugin.contracts.messaging.EndpointMessage;
import org.kaaproject.kaa.server.plugin.rest.gen.HttpRequestParam;
import org.kaaproject.kaa.server.plugin.rest.gen.KaaRestPluginConfig;
import org.kaaproject.kaa.server.plugin.rest.gen.KaaRestPluginItemConfig;
import org.kaaproject.kaa.server.plugin.rest.gen.Protocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Plugin(KaaRestPluginDef.class)
public class KaaRestPlugin implements KaaPlugin {

    private static final Logger LOG = LoggerFactory.getLogger(KaaRestPlugin.class);

    private KaaRestPluginConfig pluginConfig;

    @Override
    public void init(PluginInitContext context) throws PluginLifecycleException {
        AvroByteArrayConverter<KaaRestPluginConfig> converter = new AvroByteArrayConverter<KaaRestPluginConfig>(KaaRestPluginConfig.class);
        try {
            LOG.info("Initializing the plugin with {}", context.toString());
            this.pluginConfig = converter.fromByteArray(context.getPluginConfigurationData().getBytes());
        } catch (Exception cause) {
            String errorMessage = "Failed to initialize the plugin!";
            LOG.error(errorMessage, cause);
            throw new PluginLifecycleException(errorMessage, cause);
        }
    }

    @Override
    public void onPluginMessage(KaaPluginMessage message, PluginExecutionContext ctx) {
        // do when ready : ctx.tellPlugin(uid, sdkMessage);

        try {
            KaaRestPluginItemConfig itemConfig = this.getItemConfig(message);
            switch (itemConfig.getHttpRequestMethod()) {
                case GET:
                    GenericRecord messageData = this.getMessageData(message);
                    String responseSchema = message.getItemInfo().getOutMessageSchema();

                    this.doGet(itemConfig, messageData, responseSchema);
            }
        } catch (Exception cause) {
            throw new IllegalArgumentException(cause);
        }
    }

    private String doGet(KaaRestPluginItemConfig itemConfig, GenericRecord messageData, String responseSchema) {

        MultiValueMap<String, String> httpRequestParams = new LinkedMultiValueMap<>();
        Optional.ofNullable(itemConfig.getHttpRequestParams()).ifPresent(collection -> collection.forEach(httpRequestParam -> {
            String key = httpRequestParam.getName();
            String value = (String) messageData.get(httpRequestParam.getAvroSchemaField());
            httpRequestParams.add(key, value);
        }));

        String response = new RestTemplate().getForObject(this.getURL() + itemConfig.getRequestMapping(), String.class, httpRequestParams);
        try {
            new GenericAvroConverter<GenericRecord>
        }
        
        
        
        
        
        
        try {
            JsonNode response = new ObjectMapper().readTree(responseBody);
        }
        
        
        
        JsonNode response = new ObjectMapper().readTree(responseBody);
        try {
            new GenericAvroConverter<GenericRecord>(responseSchema).decodeJson(responseBody);

        } catch (Exception ignored) {
            try {
                GenericRecord r = new GenericData.Record(new Schema.Parser().parse(responseSchema));
                Optional.ofNullable(itemConfig.getResponseMappings()).ifPresent(collection -> collection.forEach(responseMapping -> {
                }));

            } catch (Exception cause) {

            }
        }

        return null;
    }

    private KaaRestPluginItemConfig getItemConfig(KaaPluginMessage message) {
        try {
            AvroByteArrayConverter<KaaRestPluginItemConfig> converter = new AvroByteArrayConverter<>(KaaRestPluginItemConfig.class);
            byte[] configData = message.getItemInfo().getConfigurationData();
            return converter.fromByteArray(configData);
        } catch (Exception cause) {
            throw new IllegalArgumentException(cause);
        }
    }

    private GenericRecord getMessageData(KaaPluginMessage message) {
        try {
            GenericAvroConverter<GenericRecord> converter = new GenericAvroConverter<>(message.getItemInfo().getInMessageSchema());
            byte[] messageData = ((EndpointMessage) message.getMsg()).getMessageData();
            return converter.decodeBinary(messageData);
        } catch (Exception cause) {
            throw new IllegalArgumentException(cause);
        }
    }

    @Override
    public void stop() throws PluginLifecycleException {
    }

    private String url = null;

    private String getURL() {
        if (this.url == null) {
            String protocol = this.pluginConfig.getProtocol().toString();
            String host = this.pluginConfig.getHost();
            int port = this.pluginConfig.getPort();
            this.url = protocol + "://" + host + ":" + port + "/";
        }
        return this.url;
    }

    // TODO: Used for testing purposes, remove when unnecessary
    public static void main(String[] args) throws IOException {
        PluginHttpServer httpServer = new PluginHttpServer();
        KaaRestPluginConfig config = new KaaRestPluginConfig();
        config.setHost("localhost");
        config.setPort(9999);
        config.setProtocol(Protocol.HTTP);
        httpServer.initServer(config);
        httpServer.start();
        System.in.read();
        httpServer.stop();
    }
}
