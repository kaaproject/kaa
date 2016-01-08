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

import java.util.List;
import java.util.Optional;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.generic.GenericRecordBuilder;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.kaaproject.kaa.common.avro.AvroByteArrayConverter;
import org.kaaproject.kaa.common.avro.GenericAvroConverter;
import org.kaaproject.kaa.server.common.core.plugin.def.Plugin;
import org.kaaproject.kaa.server.common.core.plugin.def.PluginExecutionContext;
import org.kaaproject.kaa.server.common.core.plugin.def.PluginInitContext;
import org.kaaproject.kaa.server.common.core.plugin.instance.KaaPlugin;
import org.kaaproject.kaa.server.common.core.plugin.instance.KaaPluginMessage;
import org.kaaproject.kaa.server.common.core.plugin.instance.PluginLifecycleException;
import org.kaaproject.kaa.server.plugin.contracts.messaging.EndpointMessage;
import org.kaaproject.kaa.server.plugin.rest.gen.HttpResponseMapping;
import org.kaaproject.kaa.server.plugin.rest.gen.KaaRestPluginConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestTemplate;

@Plugin(KaaRestPluginDef.class)
public class KaaRestPlugin implements KaaPlugin {

    private static final Logger LOG = LoggerFactory.getLogger(KaaPlugin.class);

    private KaaRestPluginConfig pluginConfig;

    @Override
    public void init(PluginInitContext context) throws PluginLifecycleException {
        AvroByteArrayConverter<KaaRestPluginConfig> converter = new AvroByteArrayConverter<KaaRestPluginConfig>(KaaRestPluginConfig.class);
        try {
            LOG.info("Initializing the plugin with {}", context);
            this.pluginConfig = converter.fromByteArray(context.getPluginConfigurationData().getBytes());
        } catch (Exception cause) {
            LOG.error("Failed to initialize the plugin!");
            throw new PluginLifecycleException(cause);
        }
    }

    @Override
    public void onPluginMessage(KaaPluginMessage message, PluginExecutionContext ctx) {
        try {
            HttpRequestDetails httpRequestDetails = new HttpRequestDetails(message);
            EndpointMessage response = httpRequestDetails.getResponse();

            switch (httpRequestDetails.getHttpRequestMethod()) {

                case GET:
                    response.setMessageData(this.get(httpRequestDetails));
                    ctx.tellPlugin(message.getUid(), response);
                    break;

                case POST:

                case PUT:

                case DELETE:

            }
        } catch (Exception cause) {
            throw new IllegalArgumentException(cause);
        }
    }

    @Override
    public void stop() throws PluginLifecycleException {
        LOG.info("Stopping the plugin...");
    }

    private byte[] get(HttpRequestDetails o) throws Exception {

        String response = new RestTemplate().getForObject(o.formatPath(this.pluginConfig), String.class, o.getHttpRequestParams());

        byte[] bytes = null;
        if (KaaRestPlugin.validateContent(response, o.getResponseSchema())) {
            bytes = response.getBytes();
        } else {
            GenericRecord buffer = KaaRestPlugin.mapResponseFields(response, o.getResponseSchema(), o.getHttpResponseMappings());
            bytes = buffer.toString().getBytes();
        }

        return bytes;
    }

    private static boolean validateContent(String content, Schema schema) {
        boolean success = true;
        try {
            new GenericAvroConverter<GenericRecord>(schema).decodeJson(content);
        } catch (Exception ignored) {
            success = false;
        }
        return success;
    }

    private static GenericRecord mapResponseFields(String content, Schema responseSchema, List<HttpResponseMapping> httpResponseMappings) throws Exception {

        JsonNode response = new ObjectMapper().readTree(content);
        GenericRecordBuilder record = new GenericRecordBuilder(responseSchema);

        Optional.ofNullable(httpResponseMappings).ifPresent(collection -> collection.forEach(mapping -> {
            String field = mapping.getAvroSchemaField();
            JsonNode value = response.get(mapping.getResponseField());
            record.set(field, value);
        }));

        return record.build();
    }

    public static void main(String[] args) throws Exception {
    }
}
