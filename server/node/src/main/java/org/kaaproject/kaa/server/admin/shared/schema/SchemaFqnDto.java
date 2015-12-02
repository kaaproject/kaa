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

package org.kaaproject.kaa.server.admin.shared.schema;

import org.kaaproject.avro.ui.shared.Fqn;
import org.kaaproject.kaa.common.dto.HasId;

public class SchemaFqnDto extends Fqn implements HasId {

    private static final long serialVersionUID = 3962779315398811005L;
    
    private String schemaName;

    public SchemaFqnDto() {
        super();
    }
    
    public SchemaFqnDto(Fqn fqn) {
        super(fqn);
    }
    
    public SchemaFqnDto(String fqnString) {
        super(fqnString);
    }
    
    public SchemaFqnDto(String fqnString, String schemaName) {
        super(fqnString);
        this.schemaName = schemaName;
    }
    
    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    @Override
    public String getId() {
        return getFqnString();
    }

    @Override
    public void setId(String id) {}

}
