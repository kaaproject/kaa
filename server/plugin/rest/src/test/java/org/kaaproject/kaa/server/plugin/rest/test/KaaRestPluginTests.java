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

package org.kaaproject.kaa.server.plugin.rest.test;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withBadRequest;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.kaaproject.kaa.common.avro.AvroJsonConverter;
import org.kaaproject.kaa.common.dto.plugin.PluginContractDirection;
import org.kaaproject.kaa.server.common.core.plugin.base.BasePluginContractInstance;
import org.kaaproject.kaa.server.common.core.plugin.base.BasePluginContractItemDef;
import org.kaaproject.kaa.server.common.core.plugin.base.BasePluginContractItemInfo;
import org.kaaproject.kaa.server.common.core.plugin.def.PluginContractDef;
import org.kaaproject.kaa.server.common.core.plugin.def.PluginContractItemDef;
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
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.ResponseCreator;

/**
 * @author Bohdan Khablenko
 *
 * @see org.kaaproject.kaa.server.plugin.rest.KaaRestPlugin
 *
 * @since v1.0.0
 */
public class KaaRestPluginTests {

    private static final Logger LOG = LoggerFactory.getLogger(KaaRestPluginTests.class);

    private static final String PLUGIN_CONFIG = "rest_plugin_config.json";
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

    private final AvroJsonConverter<InputMessage> converter = new AvroJsonConverter<>(InputMessage.SCHEMA$, InputMessage.class);

    private PluginTestInitContext initContext = new PluginTestInitContext(this.read(PLUGIN_CONFIG), Collections.singleton(this.contract));
    private PluginTestExecutionContext<OutputMessage> executionContext = new PluginTestExecutionContext<>(OutputMessage.class);

    public KaaRestPluginTests() throws Exception {

        String itemSchema = this.read(PLUGIN_ITEM_SCHEMA);
        this.def = BasePluginContractItemDef.builder(null)
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

    /**
     * Sends a GET request and processes the response.
     *
     * @throws Exception
     */
    @Test
    public void getRequestTest1() throws Exception {

        InputMessage request = InputMessage.newBuilder().setA(100).setB(200).setC(300).build();
        KaaPluginMessage message = new KaaPluginTestMessage(this.def, this.items.get(HttpRequestMethod.GET), this.converter.encodeToJson(request));

        ResponseCreator response = withSuccess("{ \"response\": \"success\" }", MediaType.APPLICATION_JSON);
        this.mockServer.expect(requestTo("http://127.0.0.1:8080/test/get")).andExpect(method(HttpMethod.GET)).andRespond(response);

        this.restPlugin.onPluginMessage(message, this.executionContext);
        this.mockServer.verify();

        OutputMessage expected = OutputMessage.newBuilder().setX("success").build();
        Assert.assertEquals(expected, this.executionContext.getMessageContent());
    }

    /**
     * Sends a GET request that results in 400 BAD_REQUEST.
     *
     * @throws Exception
     */
    @Test
    public void getRequestTest2() throws Exception {

        InputMessage request = InputMessage.newBuilder().setA(100).setB(200).setC(300).build();
        KaaPluginMessage message = new KaaPluginTestMessage(this.def, this.items.get(HttpRequestMethod.GET), this.converter.encodeToJson(request));

        ResponseCreator response = withBadRequest();
        this.mockServer.expect(requestTo("http://127.0.0.1:8080/test/get")).andExpect(method(HttpMethod.GET)).andRespond(response);

        this.restPlugin.onPluginMessage(message, this.executionContext);
        this.mockServer.verify();
        Assert.assertEquals(400, this.executionContext.getEndpointMessage().getErrorCode());
    }

    /**
     * Sends a POST request with no response mappings.
     *
     * @throws Exception
     */
    @Test
    public void postRequestTest1() throws Exception {

        InputMessage request = InputMessage.newBuilder().setA(100).setB(200).setC(300).build();
        KaaPluginMessage message = new KaaPluginTestMessage(this.def, this.items.get(HttpRequestMethod.POST), this.converter.encodeToJson(request));

        ResponseCreator response = withSuccess();
        this.mockServer.expect(requestTo("http://127.0.0.1:8080/test/post")).andExpect(method(HttpMethod.POST)).andRespond(response);

        this.restPlugin.onPluginMessage(message, this.executionContext);
        this.mockServer.verify();
        Assert.assertArrayEquals(new byte[] {}, this.executionContext.getEndpointMessage().getMessageData());
    }

    /**
     * Sends a POST request that results in 400 BAD_REQUEST.
     *
     * @throws Exception
     */
    @Test
    public void postRequestTest2() throws Exception {

        InputMessage request = InputMessage.newBuilder().setA(100).setB(200).setC(300).build();
        KaaPluginMessage message = new KaaPluginTestMessage(this.def, this.items.get(HttpRequestMethod.POST), this.converter.encodeToJson(request));

        ResponseCreator response = withBadRequest();
        this.mockServer.expect(requestTo("http://127.0.0.1:8080/test/post")).andExpect(method(HttpMethod.POST)).andRespond(response);

        this.restPlugin.onPluginMessage(message, this.executionContext);
        this.mockServer.verify();
        Assert.assertEquals(400, this.executionContext.getEndpointMessage().getErrorCode());
    }

    /**
     * Sends a PUT request.
     *
     * @throws Exception
     */
    @Test
    public void putRequestTest1() throws Exception {

        InputMessage request = InputMessage.newBuilder().setA(100).setB(200).setC(300).build();
        KaaPluginMessage message = new KaaPluginTestMessage(this.def, this.items.get(HttpRequestMethod.PUT), this.converter.encodeToJson(request));

        ResponseCreator response = withSuccess();
        this.mockServer.expect(requestTo("http://127.0.0.1:8080/test/put")).andExpect(method(HttpMethod.PUT)).andRespond(response);

        this.restPlugin.onPluginMessage(message, this.executionContext);
        this.mockServer.verify();
        Assert.assertArrayEquals(new byte[] {}, this.executionContext.getEndpointMessage().getMessageData());
    }

    /**
     * Sends a PUT request that results in 400 BAD_REQUEST.
     *
     * @throws Exception
     */
    @Test
    public void putRequestTest2() throws Exception {

        InputMessage request = InputMessage.newBuilder().setA(100).setB(200).setC(300).build();
        KaaPluginMessage message = new KaaPluginTestMessage(this.def, this.items.get(HttpRequestMethod.PUT), this.converter.encodeToJson(request));

        ResponseCreator response = withBadRequest();
        this.mockServer.expect(requestTo("http://127.0.0.1:8080/test/put")).andExpect(method(HttpMethod.PUT)).andRespond(response);

        this.restPlugin.onPluginMessage(message, this.executionContext);
        this.mockServer.verify();
        Assert.assertEquals(400, this.executionContext.getEndpointMessage().getErrorCode());
    }

    /**
     * Sends a DELETE request.
     *
     * @throws Exception
     */
    @Test
    public void deleteRequestTest1() throws Exception {

        InputMessage request = InputMessage.newBuilder().setA(100).setB(200).setC(300).build();
        KaaPluginMessage message = new KaaPluginTestMessage(this.def, this.items.get(HttpRequestMethod.DELETE), this.converter.encodeToJson(request));

        ResponseCreator response = withSuccess();
        this.mockServer.expect(requestTo("http://127.0.0.1:8080/test/delete")).andExpect(method(HttpMethod.DELETE)).andRespond(response);

        this.restPlugin.onPluginMessage(message, this.executionContext);
        this.mockServer.verify();
        Assert.assertArrayEquals(new byte[] {}, this.executionContext.getEndpointMessage().getMessageData());
    }

    /**
     * Sends a DELETE request that results in 400 BAD_REQUEST.
     *
     * @throws Exception
     */
    @Test
    public void deleteRequestTest2() throws Exception {

        InputMessage request = InputMessage.newBuilder().setA(100).setB(200).setC(300).build();
        KaaPluginMessage message = new KaaPluginTestMessage(this.def, this.items.get(HttpRequestMethod.DELETE), this.converter.encodeToJson(request));

        ResponseCreator response = withBadRequest();
        this.mockServer.expect(requestTo("http://127.0.0.1:8080/test/delete")).andExpect(method(HttpMethod.DELETE)).andRespond(response);

        this.restPlugin.onPluginMessage(message, this.executionContext);
        this.mockServer.verify();
        Assert.assertEquals(400, this.executionContext.getEndpointMessage().getErrorCode());
    }

    private String read(String resource) throws Exception {
        try {
            return IOUtils.toString(this.getClass().getClassLoader().getResource(resource));
        } catch (Exception cause) {
            LOG.error("Failed to read from {}", resource);
            throw new RuntimeException(cause);
        }
    }
}
