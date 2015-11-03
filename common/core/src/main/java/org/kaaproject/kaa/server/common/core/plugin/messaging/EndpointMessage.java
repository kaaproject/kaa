package org.kaaproject.kaa.server.common.core.plugin.messaging;

import org.kaaproject.kaa.common.hash.EndpointObjectHash;
import org.kaaproject.kaa.server.common.core.plugin.base.BaseContractMessageDef;
import org.kaaproject.kaa.server.common.core.plugin.def.ContractMessageDef;
import org.kaaproject.kaa.server.common.core.plugin.instance.KaaPluginMessage;

public class EndpointMessage implements KaaPluginMessage {

    private static final long serialVersionUID = -7358355594071995237L;

    @Override
    public ContractMessageDef getMessageDef() {
        return new BaseContractMessageDef(SdkMessage.class.getName(), 1);
    }

    public EndpointObjectHash getEndpointKey() {
        return null;
    }

    public byte[] getMessageData() {
        return null;
    }

}
