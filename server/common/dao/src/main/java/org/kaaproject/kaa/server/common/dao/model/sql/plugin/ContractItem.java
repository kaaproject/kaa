package org.kaaproject.kaa.server.common.dao.model.sql.plugin;

import org.kaaproject.kaa.server.common.dao.model.sql.GenericModel;

import java.io.Serializable;
import java.util.Set;

public class ContractItem extends GenericModel implements Serializable {

    private String name;
    private Contract contract;
    private ContractMessage inMessage;
    private ContractMessage outMessage;
    private Set<PluginContractItem> pluginContractItems;

    @Override
    protected Object createDto() {
        return null;
    }

    @Override
    public Object toDto() {
        return null;
    }
}
