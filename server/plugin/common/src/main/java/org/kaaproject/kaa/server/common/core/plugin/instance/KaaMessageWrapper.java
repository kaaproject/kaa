package org.kaaproject.kaa.server.common.core.plugin.instance;

import java.io.Serializable;

import org.kaaproject.kaa.server.common.core.plugin.def.PluginContractItemDef;

public interface KaaMessageWrapper extends Serializable{

    KaaMessage getMsg();
    void setMsg(KaaMessage msg);
    PluginContractItemDef getItemDef();
    PluginContractItemInfo getItemInfo();

}
