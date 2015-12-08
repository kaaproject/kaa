package org.kaaproject.kaa.common.dto.plugin;

import java.util.Set;

public class ContractItemDto {

    private String id;
    private String name;
    private ContractDto contract;
    private ContractMessageDto inMessage;
    private ContractMessageDto outMessage;
    private Set<PluginContractItemDto> pluginContractItems;
}
