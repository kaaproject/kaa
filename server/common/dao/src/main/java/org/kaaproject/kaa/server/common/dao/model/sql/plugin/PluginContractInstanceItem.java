package org.kaaproject.kaa.server.common.dao.model.sql.plugin;

import org.kaaproject.kaa.server.common.dao.model.sql.GenericModel;

import java.io.Serializable;
import java.util.Set;

public class PluginContractInstanceItem extends GenericModel implements Serializable {

    private String confData;
    private PluginContractInstance pluginContractInstance;
    private PluginContractItem pluginContractItem;
    private PluginContractItem parentPluginContractItem;
    private CLTSchema inMessageSchema;
    private CLTSchema outMessageSchema;
    private Set<ContractItemRoute> contractItemRoutes;

    @Override
    protected Object createDto() {
        return null;
    }

    @Override
    public Object toDto() {
        return null;
    }
}
