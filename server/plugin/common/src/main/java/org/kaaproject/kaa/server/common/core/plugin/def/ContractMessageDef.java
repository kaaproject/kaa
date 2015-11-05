package org.kaaproject.kaa.server.common.core.plugin.def;

import java.io.Serializable;


public interface ContractMessageDef extends Serializable {

    String getFqn();

    int getVersion();

}
