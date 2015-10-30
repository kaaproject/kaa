package org.kaaproject.kaa.server.common.dao.model.sql.plugin;

import org.kaaproject.kaa.server.common.dao.model.sql.GenericModel;

import java.io.Serializable;

public class PluginContractItem extends GenericModel implements Serializable{

    private String configSchema;
    private PluginContract pluginContract;
    private ContractItem contractItem;

    @Override
    protected Object createDto() {
        return null;
    }

    @Override
    public Object toDto() {
        return null;
    }
}
