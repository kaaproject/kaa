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

package org.kaaproject.kaa.server.plugin.rest.test;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.kaaproject.kaa.common.dto.plugin.PluginContractDirection;
import org.kaaproject.kaa.common.hash.EndpointObjectHash;
import org.kaaproject.kaa.server.common.core.plugin.base.BasePluginContractInstance;
import org.kaaproject.kaa.server.common.core.plugin.base.BasePluginContractItemDef;
import org.kaaproject.kaa.server.common.core.plugin.base.BasePluginContractItemInfo;
import org.kaaproject.kaa.server.common.core.plugin.def.PluginContractDef;
import org.kaaproject.kaa.server.common.core.plugin.def.PluginContractItemDef;
import org.kaaproject.kaa.server.common.core.plugin.def.PluginExecutionContext;
import org.kaaproject.kaa.server.common.core.plugin.def.PluginInitContext;
import org.kaaproject.kaa.server.common.core.plugin.instance.KaaMessage;
import org.kaaproject.kaa.server.common.core.plugin.instance.KaaPluginMessage;
import org.kaaproject.kaa.server.common.core.plugin.instance.PluginContractInstance;
import org.kaaproject.kaa.server.common.core.plugin.instance.PluginContractItemInfo;
import org.kaaproject.kaa.server.plugin.contracts.messaging.EndpointMessage;
import org.kaaproject.kaa.server.plugin.contracts.messaging.MessagingPluginContract;
import org.kaaproject.kaa.server.plugin.rest.KaaRestPlugin;
import org.kaaproject.kaa.server.plugin.rest.gen.HttpRequestMethod;
import org.kaaproject.kaa.server.plugin.rest.test.gen.InputMessage;
import org.kaaproject.kaa.server.plugin.rest.test.gen.OutputMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.ResponseCreator;

/**
 * @author Bohdan Khablenko
 */
public class KaaRestPluginTests {

    private static final Logger LOG = LoggerFactory.getLogger(KaaRestPluginTests.class);

    private static final String PLUGIN_CONFIG_DATA = "rest_plugin_config.json";
    private static final String PLUGIN_ITEM_SCHEMA = "rest_plugin_item.avsc";

    private enum Items {

        GET("get_item.json"),
        POST("post_item.json"),
        PUT("put_item.json"),
        DELETE("delete_item.json");

        private String resource;

        private Items(String resource) {
            this.resource = resource;
        }

        public String getResource() {
            return this.resource;
        }

        @Override
        public String toString() {
            return this.getResource();
        }
    }

    private KaaRestPlugin restPlugin;
    private MockRestServiceServer mockServer;

    private final PluginContractItemDef def;
    private final Map<HttpRequestMethod, PluginContractItemInfo> items = new EnumMap<>(HttpRequestMethod.class);
    private final PluginContractInstance contract;

    private PluginInitContext initContext = new PluginInitContext() {

        private String pluginConfig = KaaRestPluginTests.this.read(PLUGIN_CONFIG_DATA);

        @Override
        public String getPluginConfigurationData() {
            return this.pluginConfig;
        }

        @Override
        public Set<PluginContractInstance> getPluginContracts() {
            return Collections.singleton(KaaRestPluginTests.this.contract);
        }
    };

    public KaaRestPluginTests() throws Exception {

        String itemSchema = this.read(PLUGIN_ITEM_SCHEMA);
        this.def = BasePluginContractItemDef.builder("Plugin Contract Item Definition")
                .withSchema(itemSchema)
                .withInMessage(EndpointMessage.class)
                .withOutMessage(EndpointMessage.class)
                .build();

        PluginContractDef contract = MessagingPluginContract.buildMessagingContract(PluginContractDirection.OUT);
        this.contract = new BasePluginContractInstance(contract);
        this.items.values().forEach(item -> ((BasePluginContractInstance) this.contract).addContractItemInfo(this.def, item));

        String inputSchema = InputMessage.SCHEMA$.toString();
        String outputSchema = OutputMessage.SCHEMA$.toString();

        for (HttpRequestMethod method : HttpRequestMethod.values()) {

            String source = Items.valueOf(method.name()).getResource();
            String data = this.read(source);

            PluginContractItemInfo item = BasePluginContractItemInfo.builder()
                    .withInMsgSchema(inputSchema)
                    .withOutMsgSchema(outputSchema)
                    .withData(data)
                    .build();
            this.items.put(method, item);
        }
    }

    @Before
    public void init() throws Exception {

        this.restPlugin = new KaaRestPlugin();
        this.restPlugin.init(this.initContext);
        this.mockServer = MockRestServiceServer.createServer(this.restPlugin.getRestTemplate());
    }

    @Test
    @Ignore
    public void testGetRequest() throws Exception {

        ResponseCreator response = withSuccess("{ \"response\": \"success\" }", MediaType.APPLICATION_JSON);
        this.mockServer.expect(requestTo("http://127.0.0.1:8080/test/get")).andExpect(method(HttpMethod.GET)).andRespond(response);

        KaaPluginMessage message = new KaaPluginTestMessage(this.def, this.items.get(HttpRequestMethod.GET), "{ \"a\": 1, \"b\": 2, \"c\": 3 }");
        this.restPlugin.onPluginMessage(message, new PluginTestExecutionContext());

        this.mockServer.verify();
    }

    @Test
    public void testPostRequest() throws Exception {

        ResponseCreator response = withStatus(HttpStatus.OK);
//        ResponseCreator response = withSuccess("{ \"response\": \"success\" }", MediaType.APPLICATION_JSON);
        this.mockServer.expect(requestTo("http://127.0.0.1:8080/test/post")).andExpect(method(HttpMethod.POST)).andRespond(response);

        KaaPluginMessage message = new KaaPluginTestMessage(this.def, this.items.get(HttpRequestMethod.POST), "{ \"a\": 1, \"b\": 2, \"c\": 3 }");
        this.restPlugin.onPluginMessage(message, new PluginTestExecutionContext());

        this.mockServer.verify();
    }

    private String read(String resource) throws Exception {
        try {
            return IOUtils.toString(this.getClass().getClassLoader().getResource(resource));
        } catch (Exception cause) {
            LOG.error("Failed to read from {}", resource);
            throw new RuntimeException(cause);
        }
    }

    protected class KaaPluginTestMessage implements KaaPluginMessage {

        private static final long serialVersionUID = 100L;

        private UUID id;
        private KaaMessage message;
        private PluginContractItemDef itemDef;
        private PluginContractItemInfo itemInfo;

        public KaaPluginTestMessage(PluginContractItemDef itemDef, PluginContractItemInfo itemInfo, String body) {

            this.id = UUID.randomUUID();

            try {
                // AvroJsonConverter<InputMessage> converter = new
                // AvroJsonConverter<InputMessage>(InputMessage.SCHEMA$,
                // InputMessage.class);
                // converter.decodeJson(body);
                this.message = new EndpointMessage(EndpointObjectHash.fromString("remote_endpoint"), body.getBytes());
            } catch (Exception cause) {
                LOG.error("Failed to create the message!", cause);
                throw new IllegalArgumentException(cause);
            }

            this.itemDef = itemDef;
            this.itemInfo = itemInfo;
        }

        @Override
        public UUID getUid() {
            return this.id;
        }

        @Override
        public KaaMessage getMsg() {
            return this.message;
        }

        @Override
        public void setMsg(KaaMessage message) {
            this.message = message;
        }

        @Override
        public PluginContractItemDef getItemDef() {
            return this.itemDef;
        }

        @Override
        public PluginContractItemInfo getItemInfo() {
            return this.itemInfo;
        }
    }

    protected class PluginTestExecutionContext implements PluginExecutionContext {

        @Override
        public void tellEndpoint(EndpointObjectHash endpointKey, KaaMessage message) {
        }

        @Override
        public void tellPlugin(UUID id, KaaMessage message) {
        }
    }
}
