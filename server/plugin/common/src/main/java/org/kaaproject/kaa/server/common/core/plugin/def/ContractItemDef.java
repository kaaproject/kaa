package org.kaaproject.kaa.server.common.core.plugin.def;

import java.io.Serializable;

public interface ContractItemDef extends Serializable {

    String getName();

    ContractMessageDef getInMessage();

    ContractMessageDef getOutMessage();
}
