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

package org.kaaproject.kaa.client;

import java.security.PrivateKey;
import java.security.PublicKey;

import javax.annotation.Generated;

import org.kaaproject.kaa.client.channel.KaaChannelManager;
import org.kaaproject.kaa.client.channel.KaaDataChannel;
import org.kaaproject.kaa.client.event.EventFamilyFactory;
import org.kaaproject.kaa.client.event.EventListenersResolver;
import org.kaaproject.kaa.client.event.registration.EndpointRegistrationManager;
import org.kaaproject.kaa.client.logging.future.RecordFuture;
import org.kaaproject.kaa.schema.base.Configuration;
import org.kaaproject.kaa.schema.base.Log;

/**
 * <p>
 * Base interface to operate with {@link Kaa} library.
 *
 * </p>
 *
 * @author Yaroslav Zeygerman
 * @author Andrew Shvayka
 *
 * @see EventFamilyFactory
 * @see EndpointRegistrationManager
 * @see EventListenersResolver
 * @see KaaChannelManager
 * @see PublicKey
 * @see PrivateKey
 * @see KaaDataChannel
 */
@Generated("KaaClient.java.template")
public interface KaaClient extends GenericKaaClient {

    /**
     * Adds new log record to local storage.
     *
     * @param record A log record object.
     * @return The {@link RecordFuture} object which allows tracking a delivery status of a log record.
     */
    RecordFuture addLogRecord(Log record);

    /**
     * Returns latest configuration.
     *
     * @return configuration
     */
    Configuration getConfiguration();
}
