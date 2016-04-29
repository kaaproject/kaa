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

package org.kaaproject.kaa.server.admin.shared.properties;

import java.io.Serializable;

import org.kaaproject.kaa.common.dto.HasId;
import org.kaaproject.avro.ui.shared.RecordField;

public class PropertiesDto implements HasId, Serializable {

    private static final long serialVersionUID = 8961974348857075717L;
    
    private String id;
    private RecordField configuration;

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }
    
    public RecordField getConfiguration() {
        return configuration;
    }

    public void setConfiguration(RecordField configuration) {
        this.configuration = configuration;
    }

}
