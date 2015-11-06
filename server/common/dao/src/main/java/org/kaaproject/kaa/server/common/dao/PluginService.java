package org.kaaproject.kaa.server.common.dao;

import org.kaaproject.kaa.common.dto.plugin.PluginInstanceDto;

public interface PluginService {

    PluginInstanceDto getInstanceById(String id);
    
    
    
}
