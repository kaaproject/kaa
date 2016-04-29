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

package org.kaaproject.kaa.server.operations.service.akka.messages.core.notification;

import org.kaaproject.kaa.server.common.thrift.gen.operations.Notification;


/**
 * The Class ThriftNotificationMessage.
 */
public class ThriftNotificationMessage {

    /** The notification. */
    private final Notification notification;

    /** The app token. */
    private final String appToken;

    /**
     * Instantiates a new thrift notification message.
     *
     * @param appToken the app token
     * @param notification the notification
     */
    public ThriftNotificationMessage(String appToken, Notification notification) {
        super();
        this.appToken = appToken;
        this.notification = notification;
    }

    /**
     * Gets the app token.
     *
     * @return the app token
     */
    public String getAppToken() {
        return appToken;
    }

    /**
     * Gets the notification.
     *
     * @return the notification
     */
    public Notification getNotification() {
        return notification;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ThriftNotificationMessage [notification=");
        builder.append(notification);
        builder.append(", appToken=");
        builder.append(appToken);
        builder.append("]");
        return builder.toString();
    }
}
