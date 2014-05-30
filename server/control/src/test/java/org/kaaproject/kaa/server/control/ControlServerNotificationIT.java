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

import static org.kaaproject.kaa.server.common.thrift.util.ThriftDtoConverter.toDtoList;

import java.util.List;

import org.apache.thrift.TException;
import org.junit.Assert;
import org.junit.Test;
import org.kaaproject.kaa.common.dto.NotificationDto;
import org.kaaproject.kaa.common.dto.NotificationTypeDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class ControlServerNotificationIT.
 */
public class ControlServerNotificationIT extends AbstractTestControlServer {

    /** The Constant LOGGER. */
    private static final Logger LOGGER = LoggerFactory.getLogger(ControlServerNotificationIT.class);

    /**
     * Edits the notification.
     *
     * @throws TException the t exception
     */
    @Test
    public void editNotification() throws TException {
        NotificationDto notification = createNotification(null, null, NotificationTypeDto.SYSTEM);
        Assert.assertNotNull(notification.getId());
    }

    /**
     * Gets the notification.
     *
     * @throws TException the t exception
     */
    @Test
    public void getNotification() throws TException {
        NotificationDto notification = createNotification(null, null, NotificationTypeDto.SYSTEM);
        String id = notification.getId();
        Assert.assertNotNull(id);
    }

    /**
     * Gets the notifications by topic id.
     *
     * @throws TException the t exception
     */
    @Test
    public void getNotificationsByTopicId() throws TException {
        NotificationDto notification = createNotification(null, null, NotificationTypeDto.SYSTEM);
        String topicId = notification.getTopicId();
        Assert.assertNotNull(topicId);
        List<NotificationDto> notificationList = toDtoList(client.getNotificationsByTopicId(topicId));
        Assert.assertFalse(notificationList.isEmpty());
        Assert.assertEquals(notification, notificationList.get(0));
    }

}
