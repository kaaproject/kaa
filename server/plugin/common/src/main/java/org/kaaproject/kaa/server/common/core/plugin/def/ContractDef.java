package org.kaaproject.kaa.server.common.core.plugin.def;

import java.io.Serializable;


public interface ContractDef extends Serializable {

    String getName();

    int getVersion();

    ContractType getType();
}
