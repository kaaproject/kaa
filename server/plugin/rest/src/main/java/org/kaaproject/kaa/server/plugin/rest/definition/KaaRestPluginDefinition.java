package org.kaaproject.kaa.server.plugin.rest.definition;

import org.kaaproject.kaa.server.common.core.plugin.base.BasePluginContractDef;
import org.kaaproject.kaa.server.common.core.plugin.base.BasePluginContractItemDef;
import org.kaaproject.kaa.server.common.core.plugin.def.ContractType;
import org.kaaproject.kaa.server.common.core.plugin.def.PluginContractDef;
import org.kaaproject.kaa.server.common.core.plugin.def.PluginContractDirection;
import org.kaaproject.kaa.server.common.core.plugin.def.PluginDef;
import org.kaaproject.kaa.server.common.core.plugin.def.PluginScope;
import org.kaaproject.kaa.server.plugin.rest.messages.EndpointRequestMessage;
import org.kaaproject.kaa.server.plugin.rest.messages.EndpointResponseMessage;

import java.io.IOException;
import java.io.Serializable;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

public class KaaRestPluginDefinition implements PluginDef {

    private static final String REST_PLUGIN_TYPE = "REST";

    public KaaRestPluginDefinition() {
    }

    @Override
    public String getName() {
        return "Rest";
    }

    @Override
    public int getVersion() {
        return 1;
    }

    @Override
    public String getType() {
        return REST_PLUGIN_TYPE;
    }

    @Override
    public PluginScope getScope() {
        return PluginScope.LOCAL_APPLICATION;
    }

    @Override
    public String getConfigurationSchema() throws URISyntaxException {
        return readFileAsString("rest_plugin.avsc");
    }

    @Override
    public Set<PluginContractDef> getPluginContracts() {
        Set<PluginContractDef> contracts = new HashSet<>();
        contracts.add(BasePluginContractDef.builder("rest_plugin", 1).withDirection(PluginContractDirection.IN).withType(ContractType.ROUTE)
                .withItem(
                        BasePluginContractItemDef.builder("getFromEndpoint")
                                .withInMessage(EndpointRequestMessage.class).withOutMessage(EndpointResponseMessage.class)
                                .withSchema(readFileAsString("rest_plugin_read_item.avsc")).build())
                .withItem(
                        BasePluginContractItemDef.builder("postToEndpoint")
                                .withInMessage(EndpointRequestMessage.class).withOutMessage(EndpointResponseMessage.class)
                                .withSchema(readFileAsString("rest_plugin_write_item.avsc")).build())
                .build());

        return contracts;
    }


    private String readFileAsString(String fileName) {
        String fileBody = null;
        URL url = getClass().getResource(fileName);
        if (url != null) {
            try {
                Path path = Paths.get(url.toURI());
                byte[] bytes = Files.readAllBytes(path);
                if (bytes != null) {
                    fileBody = new String(bytes);
                }
            } catch (IOException | URISyntaxException e) {
                e.printStackTrace();
            }
        }
        return fileBody;
    }
}
