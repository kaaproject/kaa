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

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.generic.GenericRecordBuilder;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
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

public class HttpRequestDetails {

    private static final Logger LOG = LoggerFactory.getLogger(HttpRequestDetails.class);

    private final KaaPluginMessage message;

    private HttpRequestMethod httpRequestMethod = null;
    private MultiValueMap<String, String> httpRequestParams = null;
    private GenericRecord httpRequestBody = null;
    private List<HttpResponseMapping> httpResponseMappings = null;

    private final Schema requestSchema;
    private final Schema responseSchema;

    public Schema getRequestSchema() {
        return this.requestSchema;
    }

    public Schema getResponseSchema() {
        return this.responseSchema;
    }

    public HttpRequestDetails(KaaPluginMessage message, KaaRestPluginConfig pluginConfig) throws Exception {
        try {
            this.message = message;
            this.pluginConfig = pluginConfig;
            this.httpRequestMethod = this.getHttpRequestMethod();
            this.httpRequestBody = this.getHttpRequestBody();
            this.httpResponseMappings = this.getHttpResponseMappings();
            this.requestSchema = new Schema.Parser().parse(message.getItemInfo().getInMessageSchema());
            this.responseSchema = new Schema.Parser().parse(message.getItemInfo().getOutMessageSchema());
        } catch (Exception cause) {
            LOG.error("Failed to process the message!", cause);
            throw new ExceptionInInitializerError(cause);
        }
    }

    public HttpRequestMethod getHttpRequestMethod() {
        if (this.httpRequestMethod == null) {
            this.httpRequestMethod = this.getItemConfig().getHttpRequestMethod();
        }
        return this.httpRequestMethod;
    }

    /*
     * Parses the body to map request parameters as the input type schema
     * suggests.
     */
    public MultiValueMap<String, String> getHttpRequestParams() {
        if (this.httpRequestParams == null) {
            this.httpRequestParams = new LinkedMultiValueMap<>();
            Optional.ofNullable(itemConfig.getHttpRequestParams()).ifPresent(collection -> {
                collection.forEach(p -> {
                    String key = p.getName();
                    String value = this.getHttpRequestBody().get(p.getAvroSchemaMapping()).toString();
                    this.httpRequestParams.add(key, value);
                });
            });
            LOG.debug("Request parameters: {}", this.httpRequestParams.toString());
        }
        return this.httpRequestParams;
    }

    /*
     * The request body as is.
     */
    public GenericRecord getHttpRequestBody() {
        if (this.httpRequestBody == null) {
            try {
                GenericAvroConverter<GenericRecord> converter = new GenericAvroConverter<>(this.message.getItemInfo().getInMessageSchema());
                byte[] messageData = ((EndpointMessage) this.message.getMsg()).getMessageData();
                this.httpRequestBody = converter.decodeJson(messageData);
            } catch (Exception cause) {
                LOG.error("Failed to decode message data!", cause);
                throw new IllegalArgumentException(cause);
            }
        }
        return this.httpRequestBody;
    }

    public List<HttpResponseMapping> getHttpResponseMappings() {
        if (this.httpResponseMappings == null) {
            this.httpResponseMappings = this.getItemConfig().getHttpResponseMappings();
        }
        return this.httpResponseMappings;
    }

    private KaaRestPluginItemConfig itemConfig;
    private KaaRestPluginConfig pluginConfig;

    private KaaRestPluginItemConfig getItemConfig() {
        if (this.itemConfig == null && this.message != null) {
            try {
                AvroJsonConverter<KaaRestPluginItemConfig> converter = new AvroJsonConverter<>(KaaRestPluginItemConfig.SCHEMA$, KaaRestPluginItemConfig.class);
                String configData = this.message.getItemInfo().getConfigurationData();
                this.itemConfig = converter.decodeJson(configData);
            } catch (Exception cause) {
                throw new IllegalArgumentException(cause);
            }
        }
        return this.itemConfig;
    }

    private EndpointMessage response = null;

    public EndpointMessage getResponse() {
        if (this.response == null) {
            EndpointObjectHash key = ((EndpointMessage) this.message.getMsg()).getKey();
            this.response = new EndpointMessage(key);
        }
        return this.response;
    }

    private String url = null;

    public String getURL() {
        if (this.url == null && this.pluginConfig != null) {
            String protocol = this.pluginConfig.getProtocol().toString();
            String host = this.pluginConfig.getHost();
            int port = this.pluginConfig.getPort();
            this.url = protocol.toLowerCase() + "://" + host + ":" + port + (this.itemConfig != null ? this.itemConfig.getPath() : "");
        }
        return this.url;
    }
}
