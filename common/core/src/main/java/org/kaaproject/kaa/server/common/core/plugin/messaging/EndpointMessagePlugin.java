package org.kaaproject.kaa.server.common.core.plugin.messaging;

import java.nio.ByteBuffer;
import java.util.UUID;

import org.kaaproject.kaa.server.common.core.plugin.def.KaaSdkPlugin;
import org.kaaproject.kaa.server.common.core.plugin.def.Plugin;
import org.kaaproject.kaa.server.common.core.plugin.def.PluginExecutionContext;
import org.kaaproject.kaa.server.common.core.plugin.def.PluginInitContext;
import org.kaaproject.kaa.server.common.core.plugin.instance.KaaMessage;
import org.kaaproject.kaa.server.common.core.plugin.instance.KaaPluginMessage;
import org.kaaproject.kaa.server.common.core.plugin.instance.KaaSdkMessage;
import org.kaaproject.kaa.server.common.core.plugin.instance.SDKPlatform;

@Plugin(EndpointMessagingPluginDefinition.class)
public class EndpointMessagePlugin implements KaaSdkPlugin {

    @Override
    public void init(PluginInitContext context) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onPluginMessage(KaaPluginMessage message, PluginExecutionContext ctx) {
        KaaMessage msg = message.getMsg();
        if (msg instanceof EndpointMessage) {
            processEndpointMessage(message, (EndpointMessage) msg, ctx);
        }
    }

    private void processEndpointMessage(KaaPluginMessage meta, EndpointMessage msg, PluginExecutionContext ctx) {
        ctx.tellToEndpoint(msg.getKey(), new SdkMessage(meta.getUid(), msg.getMessageData()));
    }

    @Override
    public void onSdkMessage(KaaSdkMessage message, PluginExecutionContext ctx) {
        KaaMessage msg = message.getMsg();
        if (msg instanceof SdkMessage) {
            processSDKMessage(message, (SdkMessage) msg, ctx);
        }
    }

    private void processSDKMessage(KaaSdkMessage meta, SdkMessage msg, PluginExecutionContext ctx) {
        ctx.tellToPlugin(msg.getUid(), new EndpointMessage(meta.getEndpointKey()));
    }

    @Override
    public KaaSdkMessage decodeSDKMessage(KaaSdkMessage message, byte[] data) {
        ByteBuffer bb = ByteBuffer.wrap(data);
        UUID uid = new UUID(bb.getLong(), bb.getLong());
        byte[] payload = new byte[data.length - 16];
        bb.get(payload);
        SdkMessage msg = new SdkMessage(uid, payload);
        message.setMsg(msg);
        return message;
    }

    @Override
    public byte[] encodeSDKMessage(KaaSdkMessage sdkMessage) {
        // May be important for serialization;
        SDKPlatform platform = sdkMessage.getPlatform();
        KaaMessage msg = sdkMessage.getMsg();
        if(msg instanceof SdkMessage){
            SdkMessage sdkMsg = (SdkMessage) msg;
            //UUID size + payload size;
            ByteBuffer bb = ByteBuffer.wrap(new byte[16 + sdkMsg.getData().length]);
            bb.putLong(sdkMsg.getUid().getMostSignificantBits());
            bb.putLong(sdkMsg.getUid().getLeastSignificantBits());
            bb.put(sdkMsg.getData());
        }
        return null;
    }

}
