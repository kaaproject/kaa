package org.kaaproject.kaa.server.common.core.plugin.def;

import java.util.UUID;

import org.kaaproject.kaa.common.hash.EndpointObjectHash;
import org.kaaproject.kaa.server.common.core.plugin.messaging.SdkMessage;

public interface PluginExecutionContext {

    UUID getUid();

    void tellToEndpoint(EndpointObjectHash endpointKey, SdkMessage sdkMessage);

}
