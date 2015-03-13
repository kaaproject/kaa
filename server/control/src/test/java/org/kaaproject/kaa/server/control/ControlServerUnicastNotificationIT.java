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


import static org.kaaproject.kaa.server.common.thrift.util.ThriftDtoConverter.toDto;
import static org.kaaproject.kaa.server.common.thrift.util.ThriftDtoConverter.toDtoList;

import java.nio.ByteBuffer;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.apache.thrift.TException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.kaaproject.kaa.common.dto.EndpointNotificationDto;
import org.kaaproject.kaa.common.dto.NotificationTypeDto;
import org.kaaproject.kaa.server.common.nosql.mongo.dao.MongoDBTestRunner;
import org.kaaproject.kaa.server.common.nosql.mongo.dao.MongoDataLoader;

/**
 * The Class ControlServerUnicastNotificationIT.
 */
public class ControlServerUnicastNotificationIT extends AbstractTestControlServer {

    private static final String TOPIC_ID = "530db773687f16fec3527354";
    /** The Constant KEY_HASH. */
    private static final byte[] KEY_HASH = Base64.decodeBase64("ZThNRW56Wm9GeU1tRDdXU0hkTnJGSnlFazhNPQ==");

    @Before
    public void beforeTest() throws Exception {
        MongoDataLoader.loadData();
        super.beforeTest();
    }
    
    @After
    public void afterTest() throws Exception {
        MongoDBTestRunner.getDB().dropDatabase();
        super.afterTest();
    }
    
    /**
     * Test get unicast notification.
     *
     * @throws TException the t exception
     */
    @Test
    public void testGetUnicastNotification() throws TException {
        EndpointNotificationDto unicast = createUnicastNotification(KEY_HASH, TOPIC_ID, null, null, NotificationTypeDto.USER);
        String id = unicast.getId();
        Assert.assertNotNull(id);
        Assert.assertEquals(unicast, toDto(client.getUnicastNotification(id)));
    }
    
    /**
     * Test get unicast notification by keyHash.
     *
     * @throws TException the t exception
     */
    @Test
    public void testGetUnicastNotificationByKeyHash() throws TException {
        EndpointNotificationDto unicast = createUnicastNotification(KEY_HASH, TOPIC_ID, null, null, NotificationTypeDto.USER);
        List<EndpointNotificationDto> notifications = toDtoList(client.getUnicastNotificationsByKeyHash(ByteBuffer.wrap(KEY_HASH)));
        Assert.assertNotNull(notifications);
        Assert.assertFalse(notifications.isEmpty());
        Assert.assertEquals(1, notifications.size());
        Assert.assertEquals(unicast, notifications.get(0));
    }

    /**
     * Test edit unicast notification.
     *
     * @throws TException the t exception
     */
    @Test
    public void testEditUnicastNotification() throws TException {
        EndpointNotificationDto unicast = createUnicastNotification(KEY_HASH, TOPIC_ID, null, null, NotificationTypeDto.USER);
        String id = unicast.getId();
        Assert.assertNotNull(id);
        Assert.assertNotNull(unicast.getNotificationDto());
    }

}
