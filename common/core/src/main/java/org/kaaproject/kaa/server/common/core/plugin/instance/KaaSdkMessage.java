package org.kaaproject.kaa.server.common.core.plugin.instance;

import org.kaaproject.kaa.common.hash.EndpointObjectHash;


public interface KaaSdkMessage extends KaaMessageWrapper {

    EndpointObjectHash getEndpointKey();
    
    SDKPlatform getPlatform();

}
