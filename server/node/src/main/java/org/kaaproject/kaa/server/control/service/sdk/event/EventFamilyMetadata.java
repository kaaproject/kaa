/*
 * Copyright 2014-2016 CyberVision, Inc.
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

package org.kaaproject.kaa.server.control.service.sdk.event;

import java.util.List;

import org.kaaproject.kaa.common.dto.event.ApplicationEventMapDto;

public class EventFamilyMetadata {

    private String ecfName;
    private String ecfNamespace;
    private String ecfClassName;
    private int version;
    private String ecfSchema;
    private List<ApplicationEventMapDto> eventMaps;
    
    public String getEcfName() {
        return ecfName;
    }
    
    public void setEcfName(String ecfName) {
        this.ecfName = ecfName;
    }
    
    public String getEcfNamespace() {
        return ecfNamespace;
    }
    
    public void setEcfNamespace(String ecfNamespace) {
        this.ecfNamespace = ecfNamespace;
    }
    
    public String getEcfClassName() {
        return ecfClassName;
    }
    
    public void setEcfClassName(String ecfClassName) {
        this.ecfClassName = ecfClassName;
    }
    
    public int getVersion() {
        return version;
    }
    
    public void setVersion(int version) {
        this.version = version;
    }
    
    public String getEcfSchema() {
        return ecfSchema;
    }
    
    public void setEcfSchema(String ecfSchema) {
        this.ecfSchema = ecfSchema;
    }
    
    public List<ApplicationEventMapDto> getEventMaps() {
        return eventMaps;
    }
    
    public void setEventMaps(List<ApplicationEventMapDto> eventMaps) {
        this.eventMaps = eventMaps;
    }
    
}
