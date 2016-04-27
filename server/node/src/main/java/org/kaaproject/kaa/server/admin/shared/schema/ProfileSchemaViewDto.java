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

package org.kaaproject.kaa.server.admin.shared.schema;

import org.kaaproject.kaa.common.dto.EndpointProfileSchemaDto;

public class ProfileSchemaViewDto extends BaseSchemaViewDto<EndpointProfileSchemaDto> {

    private static final long serialVersionUID = -5289268279407697791L;
    
    public ProfileSchemaViewDto() {
        super();
    }

    public ProfileSchemaViewDto(EndpointProfileSchemaDto schema,
            CtlSchemaFormDto ctlSchemaForm) {
        super(schema, ctlSchemaForm);
    }

    @Override
    protected EndpointProfileSchemaDto createEmptySchema() {
        return new EndpointProfileSchemaDto();
    }

}
