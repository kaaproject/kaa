package org.kaaproject.kaa.server.common.core.plugin.def;

import java.io.Serializable;
import java.net.URISyntaxException;
import java.util.Set;

public interface PluginDef extends Serializable {

    String getName();
    
    int getVersion();
    
    String getType();

    PluginScope getScope();

    String getConfigurationSchema() throws URISyntaxException;

    Set<PluginContractDef> getPluginContracts();

}
