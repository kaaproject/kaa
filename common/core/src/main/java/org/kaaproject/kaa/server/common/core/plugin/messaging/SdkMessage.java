package org.kaaproject.kaa.server.common.core.plugin.messaging;

import org.kaaproject.kaa.server.common.core.plugin.base.BaseContractMessageDef;
import org.kaaproject.kaa.server.common.core.plugin.def.ContractMessageDef;
import org.kaaproject.kaa.server.common.core.plugin.instance.KaaPluginMessage;

public class SdkMessage implements KaaPluginMessage {

    private static final long serialVersionUID = -8369085602140052037L;

    @Override
    public ContractMessageDef getMessageDef() {
        return new BaseContractMessageDef(SdkMessage.class.getName(), 1);
    }

    public SdkMessage(byte[] data) {

    }

}
