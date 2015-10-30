package org.kaaproject.kaa.server.common.dao.model.sql.plugin;

import org.kaaproject.kaa.server.common.dao.model.sql.GenericModel;

import java.io.Serializable;
import java.util.Set;

public class PluginInstance extends GenericModel implements Serializable {

    private String configData;
    private String state;
    private Plugin plugin;
    private Set<PluginContractInstance> pluginContractInstances;

    @Override
    protected Object createDto() {
        return null;
    }

    @Override
    public Object toDto() {
        return null;
    }
}
