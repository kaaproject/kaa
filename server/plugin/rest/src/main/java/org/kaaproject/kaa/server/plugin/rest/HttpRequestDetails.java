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
import org.kaaproject.kaa.common.avro.AvroJsonConverter;
import org.kaaproject.kaa.common.avro.GenericAvroConverter;
import org.kaaproject.kaa.common.hash.EndpointObjectHash;
import org.kaaproject.kaa.server.common.core.plugin.instance.KaaPluginMessage;
import org.kaaproject.kaa.server.plugin.contracts.messaging.EndpointMessage;
import org.kaaproject.kaa.server.plugin.rest.gen.HttpRequestMethod;
import org.kaaproject.kaa.server.plugin.rest.gen.HttpResponseMapping;
import org.kaaproject.kaa.server.plugin.rest.gen.KaaRestPluginConfig;
import org.kaaproject.kaa.server.plugin.rest.gen.KaaRestPluginItemConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/**
 * @author Bohdan Khablenko
 */
public class HttpRequestDetails {

    private static final Logger LOG = LoggerFactory.getLogger(HttpRequestDetails.class);

    private KaaPluginMessage message;
    private KaaRestPluginConfig pluginConfig;
    private KaaRestPluginItemConfig itemConfig;

    private String url;
    private HttpRequestMethod requestMethod;
    private MultiValueMap<String, String> requestParams = new LinkedMultiValueMap<>();
    private GenericRecord requestBody = null;

    private Schema inputMessageType;
    private Schema outputMessageType;

    private List<HttpResponseMapping> responseMappings = null;

    public HttpRequestDetails(KaaPluginMessage message, KaaRestPluginConfig pluginConfig) {

        this.message = message;
        this.pluginConfig = pluginConfig;

        this.execute(() -> {
            AvroJsonConverter<KaaRestPluginItemConfig> converter = new AvroJsonConverter<>(KaaRestPluginItemConfig.SCHEMA$, KaaRestPluginItemConfig.class);
            String configData = this.message.getItemInfo().getConfigurationData();
            this.itemConfig = converter.decodeJson(configData);
        }, "Failed to decode the plugin item configuration!");

        this.requestMethod = this.itemConfig.getRequestMethod();

        this.execute(() -> {
            GenericAvroConverter<GenericRecord> converter = new GenericAvroConverter<>(message.getItemInfo().getInMessageSchema());
            byte[] messageData = ((EndpointMessage) message.getMsg()).getMessageData();
            this.requestBody = converter.decodeJson(messageData);
        }, "Failed to decode message data!");

        Optional.ofNullable(this.itemConfig.getRequestParams()).ifPresent(collection -> {
            collection.forEach(element -> {
                String key = element.getParamName();
                String value = this.requestBody.get(element.getInputMessageField()).toString();
                this.requestParams.add(key, value);
            });
        });
        LOG.debug("Request Parameters: {}", this.requestParams);

        this.execute(() -> {
            this.inputMessageType = new Schema.Parser().parse(message.getItemInfo().getInMessageSchema());
        }, "Failed to parse the request type schema!");

        this.execute(() -> {
            this.outputMessageType = new Schema.Parser().parse(message.getItemInfo().getOutMessageSchema());
        }, "Failed to parse the response type schema!");

        this.responseMappings = this.itemConfig.getResponseMappings();

        String protocol = this.pluginConfig.getProtocol().toString().toLowerCase();
        String host = this.pluginConfig.getHost();
        int port = this.pluginConfig.getPort();
        this.url = protocol + "://" + host + ":" + port + this.itemConfig.getPath();
    }

    public String getURL() {
        return this.url;
    }

    public HttpRequestMethod getRequestMethod() {
        return this.requestMethod;
    }

    public MultiValueMap<String, String> getRequestParams() {
        return this.requestParams;
    }

    public GenericRecord getRequestBody() {
        return this.requestBody;
    }

    public Schema getInputMessageType() {
        return this.inputMessageType;
    }

    public Schema getOutputMessageType() {
        return this.outputMessageType;
    }

    public List<HttpResponseMapping> getResponseMappings() {
        return this.responseMappings;
    }

    private EndpointMessage response = null;

    public EndpointMessage getResponse() {
        if (this.response == null) {
            EndpointObjectHash key = ((EndpointMessage) this.message.getMsg()).getKey();
            this.response = new EndpointMessage(key);
        }
        return this.response;
    }

    private void execute(Task task, String message) {
        try {
            task.complete();
        } catch (Exception cause) {
            LOG.error(message, cause);
            throw new IllegalArgumentException(cause);
        }
    }
}
