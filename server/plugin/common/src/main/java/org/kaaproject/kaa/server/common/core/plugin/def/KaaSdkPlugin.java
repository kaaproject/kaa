package org.kaaproject.kaa.server.common.core.plugin.def;

import org.kaaproject.kaa.server.common.core.plugin.instance.KaaSdkMessage;

public interface KaaSdkPlugin extends KaaPlugin {

    void onSdkMessage(KaaSdkMessage message, PluginExecutionContext ctx);

    byte[] encodeSDKMessage(KaaSdkMessage msg);

    KaaSdkMessage decodeSDKMessage(KaaSdkMessage message, byte[] data);

}
