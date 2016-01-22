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

package org.kaaproject.kaa.server.plugin.rest;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.generic.GenericRecordBuilder;
import org.codehaus.jackson.JsonNode;
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
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

/**
 * A plugin to execute calls to external services.
 *
 * @author Andrew Shvayka
 * @author Bohdan Khablenko
 *
 * @since v1.0.0
 */
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
            HttpRequestDetails request = new HttpRequestDetails(message, this.pluginConfig);
            EndpointMessage response = request.createResponse();

            switch (request.getRequestMethod()) {

                case GET:
                    LOG.info("Processing GET for [{}] with {}", request.getURL(), request.getRequestBody());
                    this.processRequest(request, this::getForObject);
                    break;

                case POST:
                    LOG.info("Processing POST for [{}] with {}", request.getURL(), request.getRequestBody());
                    List<HttpResponseMapping> mappings = request.getResponseMappings();
                    if (mappings != null && !mappings.isEmpty()) {
                        this.processRequest(request, this::postForObject);
                    } else {
                        this.processRequest(request, this::postForLocation);
                    }
                    break;

                case PUT:
                    LOG.info("Processing PUT for [{}] with {}", request.getURL(), request.getRequestBody());
                    this.processRequest(request, this::put);
                    break;

                case DELETE:
                    LOG.info("Processing DELETE for [{}] with {}", request.getURL(), request.getRequestBody());
                    this.processRequest(request, this::delete);
                    break;
            }

            this.logResponse(message, response);
            ctx.tellPlugin(message.getUid(), response);

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

    private byte[] getForObject(HttpRequestDetails request) throws HttpClientErrorException {

        // Send the request
        String response = this.restTemplate.getForObject(request.getURL(), String.class, request.getRequestParams());
        LOG.info("The host responded with {}", response.replaceAll("\\s+", " "));

        // Process the response
        Schema typeExpected = request.getOutputMessageType();
        if (this.validate(response, typeExpected)) {
            return response.getBytes();
        } else {
            LOG.debug("Converting the response to the expected type {}", typeExpected.toString().replaceAll("\\s+", " "));
            GenericRecord buffer = this.convert(response, request.getOutputMessageType(), request.getResponseMappings());
            return buffer.toString().getBytes();
        }
    }

    private byte[] postForObject(HttpRequestDetails request) throws HttpClientErrorException {

        // Send the request
        String response = this.restTemplate.postForObject(request.getURL(), request.getRequestParams(), String.class);
        LOG.debug("The host responded with {}", response.replaceAll("\\s+", " "));

        // Process the response
        Schema typeExpected = request.getOutputMessageType();
        if (this.validate(response, typeExpected)) {
            return response.getBytes();
        } else {
            LOG.debug("Converting the response to the expected type {}", typeExpected.toString().replaceAll("\\s+", " "));
            GenericRecord buffer = this.convert(response, request.getOutputMessageType(), request.getResponseMappings());
            return buffer.toString().getBytes();
        }
    }

    private byte[] postForLocation(HttpRequestDetails request) throws HttpClientErrorException {
        this.restTemplate.postForLocation(request.getURL(), request.getRequestParams());
        return new byte[] {};
    }

    private byte[] put(HttpRequestDetails request) throws HttpClientErrorException {
        this.restTemplate.put(request.getURL(), request.getRequestParams());
        return new byte[] {};
    }

    private byte[] delete(HttpRequestDetails request) throws HttpClientErrorException {
        this.restTemplate.delete(request.getURL(), request.getRequestParams());
        return new byte[] {};
    }

    /**
     * Validates a string against the given schema.
     *
     * @param body A string to validate
     * @param type A schema to validate the string against
     *
     * @return <code>true</code> if the given string conforms to the schema,
     *         <code>false</code> otherwise
     */
    private boolean validate(String body, Schema type) {
        boolean success = true;
        try {
            GenericAvroConverter<GenericRecord> converter = new GenericAvroConverter<GenericRecord>(type);
            converter.decodeJson(body);
        } catch (Exception ignored) {
            success = false;
        }
        return success;
    }

    /**
     * Maps the fields of the given JSON object to the fields of an object
     * described by the schema provided.
     *
     * @param body A JSON object to map
     * @param type A schema of the resulting object
     * @param mappings Rules for mappings
     *
     * @return A mapped object
     */
    private GenericRecord convert(String body, Schema type, List<HttpResponseMapping> mappings) {

        // Used to track response type fields that are missing a value
        List<String> missingFields = type.getFields().stream().map(Field::name).collect(Collectors.toList());

        // Read the response tree
        JsonNode response;
        try {
            response = new ObjectMapper().readTree(body);
        } catch (Exception cause) {
            LOG.error("{} is not a valid JSON!", body);
            throw new IllegalArgumentException(cause);
        }

        GenericRecordBuilder builder = new GenericRecordBuilder(type);

        // Map response fields
        Optional.ofNullable(mappings).ifPresent(collection -> collection.forEach(mapping -> {
            String field = mapping.getOutputMessageField();
            JsonNode value = response.get(mapping.getResponseField());
            if (value != null) {
                LOG.debug("Setting the field [{}] to {}", field, value.toString());
                try {
                    builder.set(field, value);
                    missingFields.remove(field);
                } catch (Exception cause) {
                    LOG.debug("Failed to set the field [{}]", field, cause);
                    throw new IllegalArgumentException(cause);
                }
            }
        }));

        // Log unmapped fields
        missingFields.forEach(name -> {
            Field field = type.getField(name);
            JsonNode defaultValue = field.defaultValue();
            if (defaultValue != null) {
                LOG.debug("Using the default value {} for the field [{}]", defaultValue.toString(), name);
            } else {
                LOG.debug("The field [{}] is missing a value!", name);
            }
        });

        return builder.build();
    }

    /**
     * Processes an HTTP request with the given request processor.
     *
     * @param request An HTTP request to process
     * @param processor An HTTP request processor
     */
    private void processRequest(HttpRequestDetails request, RequestProcessor processor) {
        EndpointMessage response = request.getResponse();
        try {
            byte[] bytes = processor.send(request);
            response.setMessageData(bytes);
        } catch (HttpClientErrorException cause) {
            response.setErrorCode(cause.getStatusCode().value());
            response.setErrorMessage(cause.getStatusCode().name());
        }
    }

    /**
     * Logs the response to an incoming message.
     *
     * @param message An incoming message
     * @param response The response
     */
    private void logResponse(KaaPluginMessage message, EndpointMessage response) {
        if (response.successful()) {
            String body = new String(response.getMessageData());
            if (body.isEmpty()) {
                body = "empty response body.";
            }
            LOG.info("Responding to message [{}] with {}", message.getUid(), body);
        } else {
            LOG.info("The request resulted in {} {}", response.getErrorCode(), response.getErrorMessage());
        }
    }
}
