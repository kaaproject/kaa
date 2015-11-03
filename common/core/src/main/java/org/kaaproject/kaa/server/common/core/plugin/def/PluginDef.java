package org.kaaproject.kaa.server.common.core.plugin.def;

import java.io.Serializable;
import java.util.Set;

public interface PluginDef extends Serializable {

    String getName();
    
    int getVersion();
    
    String getType();

    PluginScope getScope();

    String getConfigurationSchema();

    Set<PluginContractDef> getPluginContracts();

}
