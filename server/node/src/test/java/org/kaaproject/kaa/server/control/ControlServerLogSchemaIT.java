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

package org.kaaproject.kaa.server.control;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.kaaproject.kaa.common.dto.ApplicationDto;
import org.kaaproject.kaa.common.dto.VersionDto;
import org.kaaproject.kaa.common.dto.admin.SchemaVersions;
import org.kaaproject.kaa.common.dto.logs.LogSchemaDto;

/**
 * The Class ControlServerLogSchemaIT.
 */
public class ControlServerLogSchemaIT extends AbstractTestControlServer {

    /**
     * Gets the log schemas by application token test.
     *
     * @return the log schemas by application token test
     * @throws Exception the exception
     */
    @Test
    public void getLogSchemasByApplicationTokenTest() throws Exception {
        ApplicationDto application = createApplication(tenantAdminDto);
        LogSchemaDto logSchemaDto = createLogSchema(application.getId());
        List<LogSchemaDto> found = client.getLogSchemasByAppToken(application.getApplicationToken());
        Assert.assertEquals(2, found.size());
    }

    /**
     * Gets the log schemas by id test.
     *
     * @return the log schemas by id test
     * @throws Exception the exception
     */
    @Test
    public void getLogSchemasByIdTest() throws Exception {
        LogSchemaDto logSchemaDto = createLogSchema();
        LogSchemaDto found = client.getLogSchema(logSchemaDto.getId());
        Assert.assertEquals(logSchemaDto, found);
    }

    /**
     * Gets the log schema by application token and version test.
     *
     * @return the log schema by application token and version test
     * @throws Exception the exception
     */
    @Test
    public void getLogSchemaByApplicationTokenAndVersionTest() throws Exception {
        ApplicationDto app = createApplication(tenantAdminDto);
        LogSchemaDto logSchemaDto = createLogSchema(app.getId());
        LogSchemaDto found = client.getLogSchemaByApplicationTokenAndSchemaVersion(app.getApplicationToken(), logSchemaDto.getVersion());
        Assert.assertEquals(logSchemaDto, found);
    }

    /**
     * Gets the log schema versions by application token test.
     *
     * @return the log schema versions by application token test
     * @throws Exception the exception
     */
    @Test
    public void getLogSchemaVersionsByApplicationTokenTest() throws Exception {
        ApplicationDto application = createApplication(tenantAdminDto);
        LogSchemaDto logSchemaDto = createLogSchema(application.getId());
        SchemaVersions schemaVersions = client.getSchemaVersionsByApplicationToken(application.getApplicationToken());
        List<VersionDto> found = schemaVersions.getLogSchemaVersions();
        Assert.assertEquals(2, found.size());
    }
}
