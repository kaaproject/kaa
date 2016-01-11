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
import org.kaaproject.kaa.common.avro.AvroByteArrayConverter;
import org.kaaproject.kaa.common.avro.GenericAvroConverter;
import org.kaaproject.kaa.common.hash.EndpointObjectHash;
import org.kaaproject.kaa.server.common.core.plugin.instance.KaaPluginMessage;
import org.kaaproject.kaa.server.plugin.contracts.messaging.EndpointMessage;
import org.kaaproject.kaa.server.plugin.rest.gen.HttpRequestMethod;
import org.kaaproject.kaa.server.plugin.rest.gen.HttpResponseMapping;
import org.kaaproject.kaa.server.plugin.rest.gen.KaaRestPluginConfig;
import org.kaaproject.kaa.server.plugin.rest.gen.KaaRestPluginItemConfig;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

public class HttpRequestDetails {

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
                    String value = (String) this.getHttpRequestBody().get(p.getAvroSchemaMapping());
                    this.httpRequestParams.add(key, value);
                });
            });
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
                this.httpRequestBody = converter.decodeBinary(messageData);
            } catch (Exception cause) {
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
                AvroByteArrayConverter<KaaRestPluginItemConfig> converter = new AvroByteArrayConverter<>(KaaRestPluginItemConfig.class);
                byte[] configData = message.getItemInfo().getConfigurationData();
                this.itemConfig = converter.fromByteArray(configData);
            } catch (Exception cause) {
                throw new IllegalArgumentException(cause);
            }
        }
        return this.itemConfig;
    }

    private EndpointMessage response = null;

    public EndpointMessage getResponse() {
        if (this.response == null) {
            EndpointObjectHash key = ((EndpointMessage) this.message).getKey();
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
            this.url = protocol + "://" + host + ":" + port + (url != null ? url : "");
        }
        return this.url;
    }
}
