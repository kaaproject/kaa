package org.kaaproject.kaa.server.common.core.plugin.base;

import org.kaaproject.kaa.server.common.core.plugin.def.ContractMessageDef;

public class BaseContractMessageDef implements ContractMessageDef {

    private static final long serialVersionUID = -7309289183547651496L;

    private final String fqn;
    private final int version;

    public BaseContractMessageDef(String fqn, int version) {
        super();
        this.fqn = fqn;
        this.version = version;
    }

    @Override
    public String getFqn() {
        return fqn;
    }

    @Override
    public int getVersion() {
        return version;
    }

}