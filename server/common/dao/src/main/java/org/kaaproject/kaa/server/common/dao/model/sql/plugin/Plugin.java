package org.kaaproject.kaa.server.common.dao.model.sql.plugin;

import org.kaaproject.kaa.server.common.dao.model.sql.GenericModel;

import java.io.Serializable;
import java.util.Set;

public class Plugin extends GenericModel implements Serializable {

    private String type;
    private String scope;
    private String configSchema;
    private Set<PluginInstance> pluginInstances;
    private Set<PluginContract> pluginContracts;

    @Override
    protected Object createDto() {
        return null;
    }

    @Override
    public Object toDto() {
        return null;
    }
}
