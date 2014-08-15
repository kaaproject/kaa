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

package org.kaaproject.kaa.server.control;

import static org.kaaproject.kaa.server.common.thrift.util.ThriftDtoConverter.*;

import java.io.IOException;
import java.util.List;

import org.apache.thrift.TException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.kaaproject.kaa.common.dto.SchemaDto;
import org.kaaproject.kaa.common.dto.logs.LogSchemaDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ControlServerLogSchemaIT extends AbstractTestControlServer {

    /** The Constant logger. */
    private static final Logger LOG = LoggerFactory.getLogger(ControlServerLogSchemaIT.class);

    @After
    public void afterTest() {
        clearDBData();
    }

    @Test
    public void getLogSchemasByApplicationIdTest() throws TException, IOException {
        LogSchemaDto logSchemaDto = createLogSchema();
        List<LogSchemaDto> found =toDtoList(client.getLogSchemasByApplicationId(logSchemaDto.getApplicationId()));
        Assert.assertEquals(2, found.size());
    }

    @Test
    public void getLogSchemasByIdTest() throws TException, IOException {
        LogSchemaDto logSchemaDto = createLogSchema();
        LogSchemaDto found =toDto(client.getLogSchema(logSchemaDto.getId()));
        Assert.assertEquals(logSchemaDto, found);
    }

    @Test
    public void getLogSchemaByApplicationIdAndVersionTest() throws TException, IOException {
        LogSchemaDto logSchemaDto = createLogSchema();
        LogSchemaDto found = toDto(client.getLogSchemaByApplicationIdAndVersion(logSchemaDto.getApplicationId(), logSchemaDto.getMajorVersion()));
        Assert.assertEquals(logSchemaDto, found);
    }

    @Test
    public void getLogSchemaVersionsByApplicationIdTest() throws TException, IOException {
        LogSchemaDto logSchemaDto = createLogSchema();
        List<SchemaDto> found = toDtoList(client.getLogSchemaVersionsByApplicationId(logSchemaDto.getApplicationId()));
        Assert.assertEquals(2, found.size());
    }
}
