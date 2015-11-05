package org.kaaproject.kaa.server.common.core.plugin.def;

import java.io.Serializable;
import java.util.Set;

public interface PluginContractDef extends Serializable {

    ContractDef getContract();

    PluginContractDirection getDirection();

    Set<PluginContractItemDef> getPluginContractItems();
}
