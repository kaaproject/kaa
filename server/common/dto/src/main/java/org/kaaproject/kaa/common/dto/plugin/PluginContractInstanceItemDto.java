/*
 * Copyright 2014 CyberVision, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kaaproject.kaa.common.dto.plugin;

import java.io.Serializable;

import org.kaaproject.kaa.common.dto.HasId;
import org.kaaproject.kaa.common.dto.ctl.CTLSchemaDto;

public class PluginContractInstanceItemDto implements HasId, Serializable {

    private static final long serialVersionUID = -6023668365684883106L;

    private String id;
    private String parentId;
    private String pluginInstanceId;
    private String pluginContractInstanceId;
    private String confSchema;
    private byte[] confData;
    private PluginContractMessageDto inMessage;
    private PluginContractMessageDto outMessage;
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

    public String getConfSchema() {
        return confSchema;
    }

    public void setConfSchema(String confSchema) {
        this.confSchema = confSchema;
    }

    public String getPluginInstanceId() {
        return pluginInstanceId;
    }

    public void setPluginInstanceId(String pluginInstanceId) {
        this.pluginInstanceId = pluginInstanceId;
    }

    public String getPluginContractInstanceId() {
        return pluginContractInstanceId;
    }

    public void setPluginContractInstanceId(String pluginContractInstanceId) {
        this.pluginContractInstanceId = pluginContractInstanceId;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public byte[] getConfData() {
        return confData;
    }

    public void setConfData(byte[] confData) {
        this.confData = confData;
    }

    public PluginContractMessageDto getInMessage() {
        return inMessage;
    }

    public void setInMessage(PluginContractMessageDto inMessage) {
        this.inMessage = inMessage;
    }

    public PluginContractMessageDto getOutMessage() {
        return outMessage;
    }

    public void setOutMessage(PluginContractMessageDto outMessage) {
        this.outMessage = outMessage;
    }

    public CTLSchemaDto getInMessageSchema() {
        return inMessageSchema;
    }

    public void setInMessageSchema(CTLSchemaDto inMessageSchema) {
        this.inMessageSchema = inMessageSchema;
    }

    public CTLSchemaDto getOutMessageSchema() {
        return outMessageSchema;
    }

    public void setOutMessageSchema(CTLSchemaDto outMessageSchema) {
        this.outMessageSchema = outMessageSchema;
    }

}
