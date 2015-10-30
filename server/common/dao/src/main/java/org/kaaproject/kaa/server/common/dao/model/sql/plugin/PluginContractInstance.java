package org.kaaproject.kaa.server.common.dao.model.sql.plugin;

import org.kaaproject.kaa.server.common.dao.model.sql.GenericModel;

import java.io.Serializable;
import java.util.Set;

public class PluginContractInstance extends GenericModel implements Serializable {

    private PluginContract pluginContract;
    private PluginInstance pluginInstance;
    private Set<PluginContractInstanceItem> pluginContractInstanceItems;

    @Override
    protected Object createDto() {
        return null;
    }

    @Override
    public Object toDto() {
        return null;
    }
}
