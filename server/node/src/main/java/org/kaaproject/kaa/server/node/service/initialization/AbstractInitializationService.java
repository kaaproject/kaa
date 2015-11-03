package org.kaaproject.kaa.server.node.service.initialization;

import org.kaaproject.kaa.server.node.service.config.KaaNodeServerConfig;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractInitializationService implements InitializationService {

    @Autowired
    private KaaNodeServerConfig kaaNodeServerConfig;
    
    
    /**
     * KaaNodeServerConfig getter
     *
     * @return KaaNodeServerConfig
     */
    protected KaaNodeServerConfig getNodeConfig() {
        return kaaNodeServerConfig;
    }
}
