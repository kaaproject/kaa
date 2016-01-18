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
import java.util.stream.Collectors;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.generic.GenericRecordBuilder;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.ObjectMapper;
import org.kaaproject.kaa.common.avro.AvroJsonConverter;
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
    private RestTemplate restTemplate = new RestTemplate();

    public RestTemplate getRestTemplate() {
        return this.restTemplate;
    }

    @Override
    public void init(PluginInitContext context) throws PluginLifecycleException {
        AvroJsonConverter<KaaRestPluginConfig> converter = new AvroJsonConverter<>(KaaRestPluginConfig.SCHEMA$, KaaRestPluginConfig.class);
        try {
            String pluginConfig = context.getPluginConfigurationData();
            LOG.info("Initializing the plugin with {}", pluginConfig.replaceAll("\\s+", " "));
            this.pluginConfig = converter.decodeJson(pluginConfig);
        } catch (Exception cause) {
            LOG.error("Failed to initialize the plugin!", cause);
            throw new PluginLifecycleException(cause);
        }
    }

    @Override
    public void onPluginMessage(KaaPluginMessage message, PluginExecutionContext ctx) {

        LOG.info("Received message [{}]", message.getUid());

        try {
            HttpRequestDetails details = new HttpRequestDetails(message, this.pluginConfig);
            EndpointMessage response = details.getResponse();

            switch (details.getHttpRequestMethod()) {

                case GET:
                    LOG.info("Processing GET for [{}] with {}", details.getURL(), details.getHttpRequestBody());
                    response.setMessageData(this.getForObject(details));
                    LOG.info("Responding to message [{}] with {}", message.getUid(), new String(response.getMessageData()));
                    ctx.tellPlugin(message.getUid(), response);
                    break;

                case POST:
                    LOG.info("Processing POST for [{}] with {}", details.getURL(), details.getHttpRequestBody());
                    List<HttpResponseMapping> mappings = details.getHttpResponseMappings();
                    if (mappings != null & !mappings.isEmpty()) {
                        response.setMessageData(this.postForObject(details));
                        LOG.info("Responding to message [{}] with {}", message.getUid(), new String(response.getMessageData()));
                        ctx.tellPlugin(message.getUid(), response);
                    } else {
                        this.postForLocation(details);
                    }
                    break;

                case PUT:
                    LOG.info("Processing PUT for [{}] with {}", details.getURL(), details.getHttpRequestBody());
                    this.doPut(details);
                    break;

                case DELETE:
                    LOG.info("Processing DELETE for [{}] with {}", details.getURL(), details.getHttpRequestBody());
                    this.doDelete(details);
                    break;
            }
        } catch (Exception cause) {
            LOG.error("Failed to process the message!", cause);
            throw new IllegalArgumentException(cause);
        }
    }

    @Override
    public void stop() throws PluginLifecycleException {
        try {
            LOG.info("Stopping the plugin...");
        } catch (Exception cause) {
            LOG.error("Failed to stop the plugin!", cause);
            throw new PluginLifecycleException(cause);
        }
    }

    private byte[] getForObject(HttpRequestDetails request) throws Exception {

        String response = this.restTemplate.getForObject(request.getURL(), String.class, request.getHttpRequestParams());
        LOG.info("The host responded with {}", response.replaceAll("\\s+", " "));

        Schema typeExpected = request.getResponseSchema();
        if (KaaRestPlugin.validate(response, typeExpected)) {
            return response.getBytes();
        } else {
            LOG.debug("Converting the response to the expected type {}", typeExpected.toString().replaceAll("\\s+", " "));
            GenericRecord buffer = KaaRestPlugin.mapResponseFields(response, request.getResponseSchema(), request.getHttpResponseMappings());
            return buffer.toString().getBytes();
        }
    }

    private byte[] postForObject(HttpRequestDetails request) throws Exception {

        String response = this.restTemplate.postForObject(request.getURL(), request.getHttpRequestParams(), String.class);
        LOG.debug("The host responded with {}", response.replaceAll("\\s+", " "));

        Schema typeExpected = request.getResponseSchema();
        if (KaaRestPlugin.validate(response, typeExpected)) {
            return response.getBytes();
        } else {
            LOG.debug("Converting the response to the expected type {}", typeExpected.toString().replaceAll("\\s+", " "));
            GenericRecord buffer = KaaRestPlugin.mapResponseFields(response, request.getResponseSchema(), request.getHttpResponseMappings());
            return buffer.toString().getBytes();
        }
    }

    private void postForLocation(HttpRequestDetails request) throws Exception {
        this.restTemplate.postForLocation(request.getURL(), request.getHttpRequestParams());
    }

    private void doPut(HttpRequestDetails request) throws Exception {
        this.restTemplate.put(request.getURL(), request.getHttpRequestParams());
    }

    private void doDelete(HttpRequestDetails request) throws Exception {
        this.restTemplate.delete(request.getURL(), request.getHttpRequestParams());
    }

    private static boolean validate(String data, Schema schema) {
        boolean success = true;
        try {
            GenericAvroConverter<GenericRecord> converter = new GenericAvroConverter<GenericRecord>(schema);
            converter.decodeJson(data);
        } catch (Exception ignored) {
            success = false;
        }
        return success;
    }

    private static GenericRecord mapResponseFields(String responseBody, Schema responseType, List<HttpResponseMapping> responseMappings) throws Exception {

        // Used to track response type fields that are missing a value
        List<String> missingFields = responseType.getFields().stream().map(Field::name).collect(Collectors.toList());

        JsonNode response;
        try {
            response = new ObjectMapper().readTree(responseBody);
        } catch (JsonParseException cause) {
            LOG.error("{} is not a valid JSON!", responseBody);
            throw new IllegalArgumentException(cause);
        }

        GenericRecordBuilder buffer = new GenericRecordBuilder(responseType);

        Optional.ofNullable(responseMappings).ifPresent(collection -> collection.forEach(mapping -> {
            String field = mapping.getAvroSchemaField();
            JsonNode value = response.get(mapping.getResponseField());
            if (value != null) {
                LOG.debug("Setting the field [{}] to {}", field, value.toString());
                try {
                    buffer.set(field, value);
                    missingFields.remove(field);
                } catch (Exception cause) {
                    LOG.debug("Failed to set the field [{}]", field, cause);
                    throw new IllegalArgumentException(cause);
                }
            }
        }));

        missingFields.forEach(name -> {
            Field field = responseType.getField(name);
            JsonNode defaultValue = field.defaultValue();
            if (defaultValue != null) {
                LOG.debug("Using the default value {} for the field [{}]", defaultValue.toString(), name);
            } else {
                LOG.debug("The field [{}] is missing a value!", name);
            }
        });

        return buffer.build();
    }
}
