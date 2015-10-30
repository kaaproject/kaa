package org.kaaproject.kaa.server.common.dao.model.sql.plugin;

import org.kaaproject.kaa.server.common.dao.model.sql.GenericModel;

import java.io.Serializable;
import java.util.Set;

public class Contract extends GenericModel implements Serializable {

    private String name;
    private Integer version;
    private String type;
    private Set<ContractItem> contractItems;
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
