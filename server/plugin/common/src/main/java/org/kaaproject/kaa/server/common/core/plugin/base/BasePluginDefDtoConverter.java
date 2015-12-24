package org.kaaproject.kaa.server.common.core.plugin.base;

import org.kaaproject.kaa.common.dto.plugin.ContractDto;
import org.kaaproject.kaa.common.dto.plugin.ContractItemDto;
import org.kaaproject.kaa.common.dto.plugin.ContractMessageDto;
import org.kaaproject.kaa.common.dto.plugin.PluginContractDto;
import org.kaaproject.kaa.common.dto.plugin.PluginContractItemDto;
import org.kaaproject.kaa.common.dto.plugin.PluginDto;
import org.kaaproject.kaa.server.common.core.plugin.def.ContractDef;
import org.kaaproject.kaa.server.common.core.plugin.def.ContractItemDef;
import org.kaaproject.kaa.server.common.core.plugin.def.ContractMessageDef;
import org.kaaproject.kaa.server.common.core.plugin.def.PluginContractDef;
import org.kaaproject.kaa.server.common.core.plugin.def.PluginContractItemDef;
import org.kaaproject.kaa.server.common.core.plugin.def.PluginDef;

import java.util.HashSet;
import java.util.Set;

public abstract class BasePluginDefDtoConverter {

    private BasePluginDefDtoConverter() throws Exception {
        throw new Exception();
    }

    public static PluginDto convertBasePluginDef(PluginDef pluginDef, String className) {
        PluginDto pluginDto = new PluginDto();
        pluginDto.setName(pluginDef.getName());
        pluginDto.setVersion(pluginDef.getVersion());
        pluginDto.setClassName(className);
        pluginDto.setConfSchema(pluginDef.getConfigurationSchema());
        pluginDto.setScope(pluginDef.getScope());
        pluginDto.setPluginContracts(convertPluginContracts(pluginDef.getPluginContracts()));
        return pluginDto;
    }

    public static Set<PluginContractDto> convertPluginContracts(Set<PluginContractDef> pluginContracts) {
        Set<PluginContractDto> pluginContractSet = new HashSet<>();
        for (PluginContractDef pluginContractDef : pluginContracts) {
            pluginContractSet.add(convertPluginContract(pluginContractDef));
        }
        return pluginContractSet;
    }

    public static PluginContractDto convertPluginContract(PluginContractDef pcDef) {
        PluginContractDto pluginContract = new PluginContractDto();
        pluginContract.setDirection(pcDef.getDirection());
        ContractDto contractDto = convertContract(pcDef);
        pluginContract.setContract(contractDto);
        pluginContract.setPluginContractItems(convertPluginContractItems(pcDef.getPluginContractItems()));
        return pluginContract;
    }

    public static ContractDto convertContract(PluginContractDef contractDef) {
        ContractDef cDef = contractDef.getContract();
        ContractDto contractDto = new ContractDto();
        contractDto.setName(cDef.getName());
        contractDto.setType(cDef.getType());
        contractDto.setVersion(cDef.getVersion());
        contractDto.setContractItems(convertContractItems(contractDef.getPluginContractItems()));
        return contractDto;
    }

    public static Set<ContractItemDto> convertContractItems(Set<PluginContractItemDef> contractItems) {
        Set<ContractItemDto> contractItemSet = new HashSet<>(contractItems.size());
        for (PluginContractItemDef pluginContractItemDef : contractItems) {
            ContractItemDef current = pluginContractItemDef.getContractItem();
            ContractItemDto itemDto = new ContractItemDto();
            itemDto.setName(current.getName());
            itemDto.setInMessage(convertContractMessage(current.getInMessage()));
            itemDto.setOutMessage(convertContractMessage(current.getOutMessage()));
            contractItemSet.add(itemDto);
        }
        return contractItemSet;
    }

    public static Set<PluginContractItemDto> convertPluginContractItems(Set<PluginContractItemDef> contractItems) {
        Set<PluginContractItemDto> pluginContractItemSet = new HashSet<>(contractItems.size());
        for (PluginContractItemDef pluginContractItemDef : contractItems) {
            ContractItemDef current = pluginContractItemDef.getContractItem();
            ContractItemDto itemDto = new ContractItemDto();
            itemDto.setName(current.getName());
            itemDto.setInMessage(convertContractMessage(current.getInMessage()));
            itemDto.setOutMessage(convertContractMessage(current.getOutMessage()));
            pluginContractItemSet.add(new PluginContractItemDto(pluginContractItemDef.getConfigurationSchema(), itemDto));
        }
        return pluginContractItemSet;
    }

    public static ContractMessageDto convertContractMessage(ContractMessageDef messageDef) {
        ContractMessageDto contractMessage = new ContractMessageDto();
        contractMessage.setFqn(messageDef.getFqn());
        contractMessage.setVersion(messageDef.getVersion());
        return contractMessage;
    }
}
