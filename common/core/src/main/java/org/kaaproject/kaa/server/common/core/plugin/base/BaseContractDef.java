package org.kaaproject.kaa.server.common.core.plugin.base;

import org.kaaproject.kaa.server.common.core.plugin.def.ContractDef;
import org.kaaproject.kaa.server.common.core.plugin.def.ContractType;

public class BaseContractDef implements ContractDef {

    private static final long serialVersionUID = 5898867461695874611L;

    private String name;
    private int version;
    private ContractType type;

    public BaseContractDef(String name, int version, ContractType type) {
        super();
        this.name = name;
        this.version = version;
        this.type = type;
    }

    @Override 
    public String getName() {
        return name;
    }

    @Override
    public int getVersion() {
        return version;
    }

    @Override
    public ContractType getType() {
        return type;
    }

}
