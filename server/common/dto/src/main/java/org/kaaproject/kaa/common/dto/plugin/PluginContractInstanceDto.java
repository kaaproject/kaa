/*
 * Copyright 2014-2015 CyberVision, Inc.
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
import java.util.Set;

import org.kaaproject.kaa.common.dto.HasId;

public class PluginContractInstanceDto implements HasId, Serializable {

    private static final long serialVersionUID = -2398551245259052576L;

    private String id;
    private PluginContractDto contract;
    private Set<PluginContractInstanceItemDto> items;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public PluginContractDto getContract() {
        return contract;
    }

    public void setContract(PluginContractDto contract) {
        this.contract = contract;
    }

    public Set<PluginContractInstanceItemDto> getItems() {
        return items;
    }

    public void setItems(Set<PluginContractInstanceItemDto> items) {
        this.items = items;
    }

}
