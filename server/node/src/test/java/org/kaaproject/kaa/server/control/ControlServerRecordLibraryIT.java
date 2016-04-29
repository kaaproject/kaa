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

import org.junit.Assert;
import org.junit.Test;
import org.kaaproject.kaa.common.dto.ApplicationDto;
import org.kaaproject.kaa.common.dto.admin.RecordKey;
import org.kaaproject.kaa.common.dto.file.FileData;
import org.kaaproject.kaa.common.dto.logs.LogSchemaDto;

/**
 * The Class ControlServerRecordLibraryIT.
 */
public class ControlServerRecordLibraryIT extends AbstractTestControlServer {

    /**
     * Test generate record library.
     *
     * @throws Exception the exception
     */
    @Test
    public void testGenerateRecordLibrary() throws Exception {
        ApplicationDto application = createApplication(tenantAdminDto);
        LogSchemaDto logSchema = createLogSchema(application.getId());
        FileData library = client.downloadLogRecordLibrary(new RecordKey(application.getId(), logSchema.getVersion()));
        Assert.assertNotNull(library);
        Assert.assertFalse(strIsEmpty(library.getFileName()));
        Assert.assertNotNull(library.getFileData());
    }

    /**
     * Test generate record library with empty app.
     *
     * @throws Exception the exception
     */
    @Test
    public void testGenerateRecordLibraryWithEmptyApp() throws Exception {
        loginTenantDeveloper(tenantDeveloperUser);
        checkNotFound(new TestRestCall() {
            @Override
            public void executeRestCall() throws Exception {
                client.downloadLogRecordLibrary(new RecordKey("0", 0));
            }
        });
    }

    /**
     * Test generate record library with empty log schema.
     *
     * @throws Exception the exception
     */
    @Test
    public void testGenerateRecordLibraryWithEmptyLogSchema() throws Exception {
        final ApplicationDto application = createApplication(tenantAdminDto);
        loginTenantDeveloper(tenantDeveloperUser);
        checkNotFound(new TestRestCall() {
            @Override
            public void executeRestCall() throws Exception {
                client.downloadLogRecordLibrary(new RecordKey(application.getId(), 0));
            }
        });
    }

    /**
     * Test get record structure schema.
     *
     * @throws Exception the exception
     */
    @Test
    public void testGetRecordStructureSchema() throws Exception {
        ApplicationDto application = createApplication(tenantAdminDto);
        LogSchemaDto logSchema = createLogSchema(application.getId());

        FileData library = client.downloadLogRecordSchema(new RecordKey(application.getId(), logSchema.getVersion()));
        Assert.assertNotNull(library);
        Assert.assertFalse(strIsEmpty(library.getFileName()));
        Assert.assertNotNull(library.getFileData());
    }

    /**
     * Test get record structure schema with empty app.
     *
     * @throws Exception the exception
     */
    @Test
    public void testGetRecordStructureSchemaWithEmptyApp() throws Exception {
        loginTenantDeveloper(tenantDeveloperUser);
        checkNotFound(new TestRestCall() {
            @Override
            public void executeRestCall() throws Exception {
                client.downloadLogRecordSchema(new RecordKey("0", 0));
            }
        });
    }

    /**
     * Test get record structure schema with empty log schema.
     *
     * @throws Exception the exception
     */
    @Test
    public void testGetRecordStructureSchemaWithEmptyLogSchema() throws Exception {
        final ApplicationDto application = createApplication(tenantAdminDto);
        loginTenantDeveloper(tenantDeveloperUser);
        checkNotFound(new TestRestCall() {
            @Override
            public void executeRestCall() throws Exception {
                client.downloadLogRecordSchema(new RecordKey(application.getId(), 0));
            }
        });
    }
}
