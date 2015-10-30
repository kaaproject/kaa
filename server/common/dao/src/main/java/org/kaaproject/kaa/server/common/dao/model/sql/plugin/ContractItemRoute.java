package org.kaaproject.kaa.server.common.dao.model.sql.plugin;

import org.kaaproject.kaa.server.common.dao.model.sql.GenericModel;

import java.io.Serializable;

public class ContractItemRoute extends GenericModel implements Serializable {

    private PluginContractInstanceItem inPluginContractInstanceItem;
    private PluginContractInstanceItem outPluginContractInstanceItem;

    @Override
    protected Object createDto() {
        return null;
    }

    @Override
    public Object toDto() {
        return null;
    }
}
