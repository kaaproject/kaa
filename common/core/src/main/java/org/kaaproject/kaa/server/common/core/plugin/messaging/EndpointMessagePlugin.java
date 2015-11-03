package org.kaaproject.kaa.server.common.core.plugin.messaging;

import java.util.UUID;

import org.kaaproject.kaa.common.hash.EndpointObjectHash;
import org.kaaproject.kaa.server.common.core.plugin.def.KaaPlugin;
import org.kaaproject.kaa.server.common.core.plugin.def.Plugin;
import org.kaaproject.kaa.server.common.core.plugin.def.PluginContractItemDef;
import org.kaaproject.kaa.server.common.core.plugin.def.PluginExecutionContext;
import org.kaaproject.kaa.server.common.core.plugin.def.PluginInitContext;
import org.kaaproject.kaa.server.common.core.plugin.instance.KaaPluginMessage;
import org.kaaproject.kaa.server.common.core.plugin.instance.PluginContractItemInfo;

@Plugin(EndpointMessagingPluginDefinition.class)
public class EndpointMessagePlugin implements KaaPlugin {

    @Override
    public void init(PluginInitContext context) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onMessage(KaaPluginMessage msg, PluginExecutionContext ctx) {
        if(msg instanceof EndpointMessage){
            //message from Plugin to Endpoint SDK
            processEndpointMessage((EndpointMessage) msg, ctx);
        } else if (msg instanceof SdkMessage){
            //message from SDK to other Plugin.
            
        }
    }

    private void processEndpointMessage(EndpointMessage msg, PluginExecutionContext ctx) {
        EndpointObjectHash endpointKey = msg.getEndpointKey();
        UUID requestId = ctx.getUid();
        ctx.tellToEndpoint(endpointKey, new SdkMessage(msg.getMessageData()));
    }

    public KaaPluginMessage decodeSDKMessage(byte[] data) {
        return null;
    }
    
    public byte[] encodeSDKMessage(SdkMessage sdkMessage, PluginContractItemDef itemDef, PluginContractItemInfo itemInfo) {
        return null;
    }

}
