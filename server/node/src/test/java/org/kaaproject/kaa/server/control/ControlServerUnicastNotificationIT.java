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


import org.apache.commons.codec.binary.Base64;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.kaaproject.kaa.common.dto.EndpointNotificationDto;
import org.kaaproject.kaa.common.dto.NotificationTypeDto;
import org.kaaproject.kaa.server.common.nosql.mongo.dao.MongoDBTestRunner;
import org.kaaproject.kaa.server.common.nosql.mongo.dao.MongoDataLoader;

/**
 * The Class ControlServerUnicastNotificationIT.
 */
@Ignore
public class ControlServerUnicastNotificationIT extends AbstractTestControlServer {

    /** The Constant KEY_HASH. */
    private static final byte[] KEY_HASH = Base64.decodeBase64("ZThNRW56Wm9GeU1tRDdXU0hkTnJGSnlFazhNPQ==");

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.control.AbstractTestControlServer#beforeTest()
     */
    @Before
    public void beforeTest() throws Exception {
//        MongoDataLoader.loadData();
        super.beforeTest();
    }
    
    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.control.AbstractTestControlServer#afterTest()
     */
    @After
    public void afterTest() throws Exception {
        MongoDBTestRunner.getDB().dropDatabase();
        super.afterTest();
    }

    /**
     * Test send unicast notification.
     *
     * @throws Exception the exception
     */
    @Test
    public void testSendUnicastNotification() throws Exception {
        EndpointNotificationDto unicast = sendUnicastNotification(KEY_HASH, null, null, NotificationTypeDto.USER);
        String id = unicast.getId();
        Assert.assertNotNull(id);
        Assert.assertNotNull(unicast.getNotificationDto());
    }

}
