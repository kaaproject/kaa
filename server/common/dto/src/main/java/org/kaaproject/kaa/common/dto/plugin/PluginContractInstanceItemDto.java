package org.kaaproject.kaa.common.dto.plugin;

import java.io.Serializable;

import org.kaaproject.kaa.common.dto.HasId;

public class PluginContractInstanceItemDto implements HasId, Serializable {

    private String id;
    private String parentId;
    private byte[] confData;
    private CTLSchemaDto inMessageSchema;
    private CTLSchemaDto outMessageSchema;

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

}
