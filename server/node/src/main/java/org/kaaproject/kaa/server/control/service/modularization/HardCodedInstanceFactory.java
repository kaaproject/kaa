package org.kaaproject.kaa.server.control.service.modularization;

import org.kaaproject.kaa.common.dto.plugin.PluginContractDto;
import org.kaaproject.kaa.common.dto.plugin.PluginContractInstanceDto;
import org.kaaproject.kaa.common.dto.plugin.PluginContractInstanceItemDto;
import org.kaaproject.kaa.common.dto.plugin.PluginContractItemDto;
import org.kaaproject.kaa.common.dto.plugin.PluginDto;
import org.kaaproject.kaa.common.dto.plugin.PluginInstanceDto;
import org.kaaproject.kaa.common.dto.plugin.PluginInstanceState;

import java.util.HashSet;
import java.util.Set;

public class HardCodedInstanceFactory {

    public static enum Type {
        MESSAGING, REST
    }

    public static PluginInstanceDto create(Type type, PluginDto pluginDto) {
        switch (type) {
            case MESSAGING:
                return createEndpointMessagingInstance(pluginDto);
            case REST:
            default:
                return null;
        }
    }

    private static PluginInstanceDto createEndpointMessagingInstance(PluginDto pluginDto) {
        PluginInstanceDto pluginInstanceDto = new PluginInstanceDto();
        pluginInstanceDto.setPluginDefinition(pluginDto);
        pluginInstanceDto.setName("Endpoint messaging plugin instance");
        pluginInstanceDto.setState(PluginInstanceState.ACTIVE);
        pluginInstanceDto.setConfigurationData("Configuration data for endpoint messaging plugin instance");

        Set<PluginContractInstanceDto> pluginContractInstances = new HashSet<>();
        PluginContractInstanceDto pluginContractInstanceDto = new PluginContractInstanceDto();

        pluginContractInstances.add(pluginContractInstanceDto);

        PluginContractDto endpointMessagingPluginContract = getPluginContractForContractName("Messaging SDK contract", pluginDto);
        addItemsToPluginContractInstance(pluginContractInstanceDto, endpointMessagingPluginContract);
        pluginContractInstanceDto.setContract(endpointMessagingPluginContract);
        pluginInstanceDto.setContracts(pluginContractInstances);

        return pluginInstanceDto;
    }

    private static PluginContractDto getPluginContractForContractName(String name, PluginDto pluginDto) {
        Set<PluginContractDto> pluginContractDtos = pluginDto.getPluginContracts();
        for (PluginContractDto pluginContractDto : pluginContractDtos) {
            if (name.equals(pluginContractDto.getContract().getName())) {
                return pluginContractDto;
            }
        }
        return null;
    }

    private static void addItemsToPluginContractInstance(PluginContractInstanceDto pluginContractInstanceDto,
                                                         PluginContractDto pluginContract) {
        Set<PluginContractInstanceItemDto> items = new HashSet<>();

        for (PluginContractItemDto pluginContractItem : pluginContract.getPluginContractItems()) {
            PluginContractInstanceItemDto instanceItemDto = new PluginContractInstanceItemDto();
        }

        pluginContractInstanceDto.setItems(items);
    }
}
