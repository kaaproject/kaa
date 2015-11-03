package org.kaaproject.kaa.server.common.core.plugin.instance;

public interface PluginContractItemInfo {

    byte[] getConfigurationData();

    String getInMessageSchema();

    String getOutMessageSchema();

}
