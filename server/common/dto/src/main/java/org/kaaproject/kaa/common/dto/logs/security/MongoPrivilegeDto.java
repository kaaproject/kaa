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

package org.kaaproject.kaa.common.dto.logs.security;

import java.io.Serializable;
import java.util.List;

public class MongoPrivilegeDto implements Serializable {
    
    private static final long serialVersionUID = -9133629508883896396L;
    
    private MongoResourceDto resource;
    private List<String> actions;
    
    public MongoPrivilegeDto () {
        
    }
    
    public MongoPrivilegeDto(MongoResourceDto resource, List<String> actions) {
        this.resource = resource;
        this.actions = actions;
    }
    
    public void setResource(MongoResourceDto resource) {
        this.resource = resource;
    }
    
    public MongoResourceDto getResource() {
        return resource;
    }
    
    public void setActions(List<String> actions) {
        this.actions = actions;
    }
    
    public List<String> getActions() {
        return actions;
    }
}
