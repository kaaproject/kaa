package org.kaaproject.kaa.server.common.core.plugin.instance;

import org.kaaproject.kaa.server.common.core.plugin.def.PluginContractDef;
import org.kaaproject.kaa.server.common.core.plugin.def.PluginContractItemDef;

public interface PluginContractInstance {

    PluginContractDef getDef();

    PluginContractItemInfo getContractItemInfo(PluginContractItemDef contractItem);

}
