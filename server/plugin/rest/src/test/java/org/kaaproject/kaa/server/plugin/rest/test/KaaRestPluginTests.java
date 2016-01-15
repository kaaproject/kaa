package org.kaaproject.kaa.server.plugin.rest.test;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.kaaproject.kaa.common.avro.AvroJsonConverter;
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
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.client.RestTemplate;

public class KaaRestPluginTests {

    private static final Logger LOG = LoggerFactory.getLogger(KaaRestPluginTests.class);

    private static final String PLUGIN_CONFIG_DATA = "rest_plugin_config.json";

    private static final String GET_ITEM_INFO = "get_item.json";
    private static final String POST_ITEM_INFO = "post_item.json";
    private static final String PUT_ITEM_INFO = "put_item.json";
    private static final String DELETE_ITEM_INFO = "delete_item.json";

    private KaaRestPlugin restPlugin;

    private final Map<HttpRequestMethod, PluginContractItemInfo> items = new HashMap<>();

    public KaaRestPluginTests() throws Exception {

        PluginContractItemInfo item;

        item = BasePluginContractItemInfo.builder()
                .withInMsgSchema(InputMessage.SCHEMA$.toString())
                .withOutMsgSchema(OutputMessage.SCHEMA$.toString())
                .withData(this.read(GET_ITEM_INFO))
                .build();
        items.put(HttpRequestMethod.GET, item);
    }

    @Before
    public void init() throws Exception {

        PluginContractDef contractDef = MessagingPluginContract.buildMessagingContract(PluginContractDirection.OUT);

        PluginContractItemDef itemDef = BasePluginContractItemDef.builder(null)
                .withSchema(this.read("rest_plugin_item.avsc"))
                .withInMessage(EndpointMessage.class)
                .withOutMessage(EndpointMessage.class)
                .build();

        BasePluginContractInstance contractInstance = new BasePluginContractInstance(contractDef);
        contractInstance.addContractItemInfo(itemDef, items.get(HttpRequestMethod.GET));

        PluginInitContext initContext = new PluginInitContext() {

            private String pluginConfig = KaaRestPluginTests.this.read(PLUGIN_CONFIG_DATA);

            @Override
            public String getPluginConfigurationData() {
                return this.pluginConfig;
            }

            @Override
            public Set<PluginContractInstance> getPluginContracts() {
                return Collections.singleton(contractInstance);
            }
        };

        this.restPlugin = new KaaRestPlugin();
        this.restPlugin.init(initContext);
    }

    @Test
    public void test() throws Exception {

        KaaPluginMessage message = new KaaPluginMessage() {

            private UUID id = UUID.randomUUID();

            @Override
            public KaaMessage getMsg() {
                try {
                    InputMessage message = new InputMessage("test");
                    AvroJsonConverter<InputMessage> converter = new AvroJsonConverter<InputMessage>(InputMessage.SCHEMA$, InputMessage.class);
                    return new EndpointMessage(EndpointObjectHash.fromString("remote_endpoint"), converter.encodeToJson(message).getBytes());
                } catch (Exception cause) {
                    cause.printStackTrace();
                    return null;
                }
            }

            @Override
            public void setMsg(KaaMessage msg) {
            }

            @Override
            public PluginContractItemDef getItemDef() {
                try {
                    return BasePluginContractItemDef.builder(null)
                            .withSchema(KaaRestPluginTests.this.read("rest_plugin_item.avsc"))
                            .withInMessage(EndpointMessage.class)
                            .withOutMessage(EndpointMessage.class)
                            .build();
                } catch (Exception cause) {
                    cause.printStackTrace();
                    return null;
                }
            }

            @Override
            public PluginContractItemInfo getItemInfo() {
                return KaaRestPluginTests.this.items.get(HttpRequestMethod.GET);
            }

            @Override
            public UUID getUid() {
                return this.id;
            }
        };

        PluginExecutionContext context = new PluginExecutionContext() {

            @Override
            public void tellEndpoint(EndpointObjectHash endpointKey, KaaMessage sdkMessage) {
            }

            @Override
            public void tellPlugin(UUID uid, KaaMessage sdkMessage) {
                LOG.info("Got response from {}", uid);
            }
        };

        RestTemplate restTemplate = this.restPlugin.getRestTemplate();
        MockRestServiceServer mockServer = MockRestServiceServer.createServer(restTemplate);
        mockServer.expect(requestTo("HTTP://127.0.0.1:8080/test/get")).andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("{ \"body\": \"success\" }", MediaType.APPLICATION_JSON));
        this.restPlugin.onPluginMessage(message, context);
        mockServer.verify();
    }

    public static void main(String[] args) {
        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer mockServer = MockRestServiceServer.createServer(restTemplate);

        mockServer.expect(requestTo("/hotels/42")).andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("{ \"id\": 42, \"name\": \"Lion's Pride Inn\" }", MediaType.APPLICATION_JSON));
        mockServer.verify();
    }

    private String read(String resource) throws Exception {
        try {
            return IOUtils.toString(this.getClass().getClassLoader().getResource(resource));
        } catch (Exception cause) {
            LOG.error("Failed to read from {}", resource);
            throw cause;
        }
    }
}
