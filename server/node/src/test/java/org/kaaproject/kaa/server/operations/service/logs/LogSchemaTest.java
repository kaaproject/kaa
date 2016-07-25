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

package org.kaaproject.kaa.server.operations.service.logs;

import org.junit.Assert;
import org.junit.Test;
import org.kaaproject.kaa.common.dto.logs.LogSchemaDto;
import org.kaaproject.kaa.server.common.log.shared.appender.LogSchema;

public class LogSchemaTest {
    
    private static final String ID = "id";
    private static final String APPLICATION_ID = "application id";  
    private static final String SCHEMA = "schema";
    private static final int VERSION = 3;
    
    
    @Test
    public void basicLogSchemaTest() {
        LogSchemaDto dto = new LogSchemaDto();
        dto.setId(ID);
        dto.setApplicationId(APPLICATION_ID);
        dto.setVersion(VERSION);
        LogSchema logSchema = new LogSchema(dto, SCHEMA);
        
        Assert.assertEquals(ID, logSchema.getId());
        Assert.assertEquals(APPLICATION_ID, logSchema.getApplicationId());
        Assert.assertEquals(SCHEMA, logSchema.getSchema());
        Assert.assertEquals(VERSION, logSchema.getVersion());
    }
}
