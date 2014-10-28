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

package org.kaaproject.kaa.client.notification;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.apache.avro.specific.SpecificRecordBase;
import org.kaaproject.kaa.common.avro.AvroByteArrayConverter;

/**
 * <p>Abstract listener for received notifications.</p>
 *
 * <p>Responsible for processing notifications either on a specific topic or all at once.</p>
 * {@code
 * // Assume, BasicNotification is a notification class auto-generated according to predefined Avro schema
 * public class UserNotificationListener extends AbstractNotificationListener<BasicNotification> {
 *     \@Override
 *     protected Class<BasicNotification> getNotificationClass() {
 *         return BasicNotification.class;
 *     }
 *
 *     \@Override
 *     public void onNotification(String topicId, BasicNotification notification) {
 *         System.out.println("Got notification: " + notification.toString());
 *     }
 * }
 * }
 * </pre>
 *
 * @author Yaroslav Zeygerman
 *
 */
public abstract class AbstractNotificationListener<T extends SpecificRecordBase> implements NotificationListener {
    private final AvroByteArrayConverter<T> converter;

    public AbstractNotificationListener() {
        converter = new AvroByteArrayConverter<T>(getNotificationClass());
    }

    /**
     * Convert raw Avro-encoded data to specific notification class according
     * to predefined Avro schema.
     *
     * @param topicId Unique topic identifier
     * @param notification Raw Avro-encoded data
     *
     */
    @Override
    public final void onNotificationRaw(String topicId, ByteBuffer notification) throws IOException {
        onNotification(topicId, converter.fromByteArray(notification.array()));
    }

    /**
     * Retrieves the notification's class.
     *
     * @return The notification's class object.
     *
     */
    protected abstract Class<T> getNotificationClass();

    /**
     * Called on each received notification.
     *
     * @param topicId Unique topic identifier
     * @param notification Received notification.
     *
     */
    public abstract void onNotification(String topicId, T notification);

}
