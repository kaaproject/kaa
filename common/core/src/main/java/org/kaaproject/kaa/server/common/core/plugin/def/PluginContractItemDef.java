package org.kaaproject.kaa.server.common.core.plugin.def;

import java.io.Serializable;


public interface PluginContractItemDef extends Serializable {

    ContractItemDef getContractItem();
    
    String getConfigurationSchema();
    
}
