package org.kaaproject.kaa.common.dto.plugin;

public class PluginContractItemDto {

    private String id;
    private String configSchema;
    private ContractItemDto contractItem;

    public PluginContractItemDto() {
    }

    public PluginContractItemDto(String configSchema, ContractItemDto contractItem) {
        this.configSchema = configSchema;
        this.contractItem = contractItem;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getConfigSchema() {
        return configSchema;
    }

    public void setConfigSchema(String configSchema) {
        this.configSchema = configSchema;
    }

    public ContractItemDto getContractItem() {
        return contractItem;
    }

    public void setContractItem(ContractItemDto contractItem) {
        this.contractItem = contractItem;
    }
}
