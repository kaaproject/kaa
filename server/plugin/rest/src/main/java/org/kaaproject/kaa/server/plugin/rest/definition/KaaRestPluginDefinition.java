package org.kaaproject.kaa.server.plugin.rest.definition;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

import org.kaaproject.kaa.server.common.core.plugin.def.PluginContractDef;
import org.kaaproject.kaa.server.common.core.plugin.def.PluginContractDirection;
import org.kaaproject.kaa.server.common.core.plugin.def.PluginDef;
import org.kaaproject.kaa.server.common.core.plugin.def.PluginScope;
import org.kaaproject.kaa.server.plugin.contracts.messaging.MessagingPluginContract;

public class KaaRestPluginDefinition implements PluginDef {

    private static final long serialVersionUID = 3242496999565136016L;
    
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
        contracts.add(MessagingPluginContract.buildMessagingContract(PluginContractDirection.OUT,
                readFileAsString("rest_plugin_read_item.avsc"), readFileAsString("rest_plugin_write_item.avsc")));

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
