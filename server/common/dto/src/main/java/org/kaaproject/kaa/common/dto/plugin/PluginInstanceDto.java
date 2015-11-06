package org.kaaproject.kaa.common.dto.plugin;

import java.io.Serializable;
import java.util.Set;

import org.kaaproject.kaa.common.dto.HasId;

public class PluginInstanceDto implements HasId, Serializable {

    private static final long serialVersionUID = -4079418894836720242L;
    
    private String id;
    private String name;
    private PluginInstanceState state;
    private byte[] configurationData;
    private PluginDto pluginDefinition;
    private Set<PluginContractInstanceDto> contracts;
    
    @Override
    public String getId() {
        return id;
    }
    @Override
    public void setId(String id) {
        this.id = id;
    }
    
    
    
    
    

}
