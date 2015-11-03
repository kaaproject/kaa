package org.kaaproject.kaa.server.common.core.plugin.instance;

import java.io.Serializable;

import org.kaaproject.kaa.server.common.core.plugin.def.ContractMessageDef;

public interface KaaPluginMessage extends Serializable {

    ContractMessageDef getMessageDef();

}
