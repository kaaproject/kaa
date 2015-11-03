package org.kaaproject.kaa.server.common.core.plugin.def;

import java.util.Set;

import org.kaaproject.kaa.server.common.core.plugin.instance.PluginContractInstance;

public interface PluginInitContext {

    byte[] getPluginConfigurationData();

    Set<PluginContractInstance> getPluginContracts();

}
