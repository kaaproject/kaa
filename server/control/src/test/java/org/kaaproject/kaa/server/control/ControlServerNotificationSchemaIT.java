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


import org.apache.thrift.TException;
import org.junit.Assert;
import org.junit.Test;
import org.kaaproject.kaa.common.dto.DtoByteMarshaller;
import org.kaaproject.kaa.common.dto.NotificationSchemaDto;
import org.kaaproject.kaa.common.dto.NotificationTypeDto;
import org.kaaproject.kaa.common.dto.SchemaDto;
import org.kaaproject.kaa.server.common.thrift.gen.shared.DataStruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.kaaproject.kaa.server.common.thrift.util.ThriftDtoConverter.toDto;
import static org.kaaproject.kaa.server.common.thrift.util.ThriftDtoConverter.toDtoList;

/**
 * The Class ControlServerNotificationSchemaIT.
 */
public class ControlServerNotificationSchemaIT extends AbstractTestControlServer {

    /** The Constant LOGGER. */
    private static final Logger LOGGER = LoggerFactory.getLogger(ControlServerNotificationSchemaIT.class);

    /**
     * Test edit notification schema.
     *
     * @throws TException the t exception
     */
    @Test
    public void testEditNotificationSchema() throws TException {
        NotificationSchemaDto schemaDto = createNotificationSchema(null, NotificationTypeDto.SYSTEM);
        Assert.assertNotNull(schemaDto.getId());
        LOGGER.debug("Create notification schema with id {}", schemaDto.getId());
    }

    /**
     * Test get notification schema.
     *
     * @throws TException the t exception
     */
    @Test
    public void testGetNotificationSchema() throws TException {
        NotificationSchemaDto schemaDto = createNotificationSchema(null, NotificationTypeDto.SYSTEM);
        Assert.assertNotNull(schemaDto.getId());
        LOGGER.debug("Create notification schema with id {}", schemaDto.getId());
        NotificationSchemaDto foundSchema = toDto(client.getNotificationSchema(schemaDto.getId()));
        Assert.assertNotNull(foundSchema);
        Assert.assertEquals(schemaDto, foundSchema);

    }

    /**
     * Test get notification schemas by app id.
     *
     * @throws TException the t exception
     */
    @Test
    public void testGetNotificationSchemasByAppId() throws TException {
        NotificationSchemaDto schemaDto = createNotificationSchema(null, NotificationTypeDto.SYSTEM);
        Assert.assertNotNull(schemaDto.getId());
        LOGGER.debug("Create notification schema with id {}", schemaDto.getId());
        List<NotificationSchemaDto> foundSchema = toDtoList(client.getNotificationSchemasByAppId(schemaDto.getApplicationId()));
        Assert.assertFalse(foundSchema.isEmpty());
        Assert.assertEquals(2, foundSchema.size());
        Assert.assertEquals(schemaDto, foundSchema.get(1));
    }
    
    /**
     * Test get user notification schemas by app id.
     *
     * @throws TException the t exception
     */
    @Test
    public void testGetUserNotificationSchemasByAppId() throws TException {
        NotificationSchemaDto schemaDto = createNotificationSchema(null, NotificationTypeDto.USER);
        Assert.assertNotNull(schemaDto.getId());
        LOGGER.debug("Create notification schema with id {}", schemaDto.getId());
        List<SchemaDto> foundSchema = toDtoList(client.getUserNotificationSchemasByAppId(schemaDto.getApplicationId()));
        Assert.assertFalse(foundSchema.isEmpty());
        Assert.assertEquals(2, foundSchema.size());
        assertSchemasEquals(schemaDto, foundSchema.get(1));
    }
    
    /**
     * Test get notification schema versions by app id.
     *
     * @throws TException the t exception
     */
    @Test
    public void testGetNotificationSchemaVersionsByAppId() throws TException {
        NotificationSchemaDto schemaDto = createNotificationSchema(null, NotificationTypeDto.USER);
        Assert.assertNotNull(schemaDto.getId());
        LOGGER.debug("Create notification schema with id {}", schemaDto.getId());
        List<SchemaDto> foundSchema = toDtoList(client.getNotificationSchemaVersionsByApplicationId(schemaDto.getApplicationId()));
        Assert.assertFalse(foundSchema.isEmpty());
        Assert.assertEquals(2, foundSchema.size());
        assertSchemasEquals(schemaDto, foundSchema.get(1));
    }

    /**
     * Test find notification schemas by app id and type.
     *
     * @throws TException the t exception
     */
    @Test
    public void testFindNotificationSchemasByAppIdAndType() throws TException {
        NotificationTypeDto typeDto = NotificationTypeDto.USER;
        NotificationSchemaDto schemaDto = createNotificationSchema(null, typeDto);
        Assert.assertNotNull(schemaDto.getId());
        LOGGER.debug("Create notification schema with id {}", schemaDto.getId());
        DataStruct dataStruct = new DataStruct();
        dataStruct.setData(DtoByteMarshaller.toBytes(typeDto));
        List<NotificationSchemaDto> foundSchema = toDtoList(client.findNotificationSchemasByAppIdAndType(schemaDto.getApplicationId(), dataStruct));
        Assert.assertFalse(foundSchema.isEmpty());
        Assert.assertEquals(2, foundSchema.size());
        Assert.assertEquals(schemaDto, foundSchema.get(1));
    }
}
