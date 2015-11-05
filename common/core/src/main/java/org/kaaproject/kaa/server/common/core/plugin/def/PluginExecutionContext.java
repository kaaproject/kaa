package org.kaaproject.kaa.server.common.core.plugin.def;

import java.util.UUID;

import org.kaaproject.kaa.common.hash.EndpointObjectHash;
import org.kaaproject.kaa.server.common.core.plugin.instance.KaaMessage;

public interface PluginExecutionContext {

    void tellToEndpoint(EndpointObjectHash endpointKey, KaaMessage sdkMessage);
    
    void tellToPlugin(UUID uid, KaaMessage sdkMessage);

}
