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
import org.kaaproject.kaa.common.dto.NotificationSchemaDto;
import org.kaaproject.kaa.common.dto.NotificationTypeDto;
import org.kaaproject.kaa.common.dto.VersionDto;
import org.kaaproject.kaa.common.dto.admin.SchemaVersions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class ControlServerNotificationSchemaIT.
 */
public class ControlServerNotificationSchemaIT extends AbstractTestControlServer {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(ControlServerNotificationSchemaIT.class);

    /**
     * Test create notification schema.
     *
     * @throws Exception the exception
     */
    @Test
    public void testCreateNotificationSchema() throws Exception {
        NotificationSchemaDto schemaDto = createNotificationSchema(null, NotificationTypeDto.SYSTEM);
        Assert.assertNotNull(schemaDto.getId());
        LOG.debug("Create notification schema with id {}", schemaDto.getId());
    }

    /**
     * Test get notification schema.
     *
     * @throws Exception the exception
     */
    @Test
    public void testGetNotificationSchema() throws Exception {
        NotificationSchemaDto schemaDto = createNotificationSchema(null, NotificationTypeDto.SYSTEM);
        Assert.assertNotNull(schemaDto.getId());
        LOG.debug("Create notification schema with id {}", schemaDto.getId());
        NotificationSchemaDto foundSchema = client.getNotificationSchema(schemaDto.getId());
        Assert.assertNotNull(foundSchema);
        Assert.assertEquals(schemaDto, foundSchema);

    }

    /**
     * Test get notification schemas by app token.
     *
     * @throws Exception the exception
     */
    @Test
    public void testGetNotificationSchemasByAppToken() throws Exception {
        ApplicationDto application = createApplication(tenantAdminDto);
        NotificationSchemaDto schemaDto = createNotificationSchema(application.getId(), NotificationTypeDto.SYSTEM);
        Assert.assertNotNull(schemaDto.getId());
        LOG.debug("Create notification schema with id {}", schemaDto.getId());
        List<NotificationSchemaDto> foundSchema = client.getNotificationSchemasByAppToken(application.getApplicationToken());
        Assert.assertFalse(foundSchema.isEmpty());
        Assert.assertEquals(2, foundSchema.size());
        Assert.assertEquals(schemaDto, foundSchema.get(1));
    }

    /**
     * Test get user notification schemas by app id.
     *
     * @throws Exception the exception
     */
    @Test
    public void testGetUserNotificationSchemasByAppToken() throws Exception {
        ApplicationDto application = createApplication(tenantAdminDto);
        NotificationSchemaDto schemaDto = createNotificationSchema(application.getId(), NotificationTypeDto.USER);
        Assert.assertNotNull(schemaDto.getId());
        LOG.debug("Create notification schema with id {}", schemaDto.getId());
        List<VersionDto> foundSchema = client.getUserNotificationSchemasByAppToken(application.getApplicationToken());
        Assert.assertFalse(foundSchema.isEmpty());
        Assert.assertEquals(2, foundSchema.size());
        assertSchemasEquals(schemaDto, foundSchema.get(1));
    }

    /**
     * Test get notification schema versions by app token.
     *
     * @throws Exception the exception
     */
    @Test
    public void testGetNotificationSchemaVersionsByAppToken() throws Exception {
        ApplicationDto application = createApplication(tenantAdminDto);
        NotificationSchemaDto schemaDto = createNotificationSchema(application.getId(), NotificationTypeDto.USER);
        Assert.assertNotNull(schemaDto.getId());
        LOG.debug("Create notification schema with id {}", schemaDto.getId());
        SchemaVersions schemaVersions = client.getSchemaVersionsByApplicationToken(application.getApplicationToken());
        List<VersionDto> foundSchema = schemaVersions.getNotificationSchemaVersions();
        Assert.assertFalse(foundSchema.isEmpty());
        Assert.assertEquals(2, foundSchema.size());
        assertSchemasEquals(schemaDto, foundSchema.get(1));
    }
}
