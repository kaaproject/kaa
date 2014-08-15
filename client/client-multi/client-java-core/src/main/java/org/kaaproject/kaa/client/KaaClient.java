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

package org.kaaproject.kaa.client;

import java.security.PrivateKey;
import java.security.PublicKey;

import org.kaaproject.kaa.client.channel.KaaChannelManager;
import org.kaaproject.kaa.client.channel.KaaDataDemultiplexer;
import org.kaaproject.kaa.client.channel.KaaDataMultiplexer;
import org.kaaproject.kaa.client.configuration.delta.manager.DeltaManager;
import org.kaaproject.kaa.client.configuration.manager.ConfigurationManager;
import org.kaaproject.kaa.client.configuration.storage.ConfigurationPersistenceManager;
import org.kaaproject.kaa.client.event.EventFamilyFactory;
import org.kaaproject.kaa.client.event.EventListenersResolver;
import org.kaaproject.kaa.client.event.registration.EndpointRegistrationManager;
import org.kaaproject.kaa.client.logging.LogCollector;
import org.kaaproject.kaa.client.notification.NotificationManager;
import org.kaaproject.kaa.client.profile.ProfileManager;
import org.kaaproject.kaa.client.schema.storage.SchemaPersistenceManager;

/**
 * Interface for the Kaa client.
 *
 * Base interface to operate with {@link Kaa} library.
 *
 * @author Yaroslav Zeygerman
 *
 * @see ProfileManager
 * @see ConfigurationManager
 * @see DeltaManager
 * @see ConfigurationPersistenceManager
 * @see SchemaPersistenceManager
 * @see NotificationManager
 * @see EventFamilyFactory
 * @see EndpointRegistrationManager
 * @see EventListenersResolver
 * @see KaaChannelManager
 * @see KaaDataMultiplexer
 * @see KaaDataDemultiplexer
 * @see PublicKey
 * @see PrivateKey
 * @see LogCollector
 */
public interface KaaClient {

    /**
     * Retrieves Kaa profile manager.
     *
     * @return {@link ProfileManager} object.
     *
     */
    ProfileManager getProfileManager();

    /**
     * Retrieves Kaa configuration manager.
     *
     * @return {@link ConfigurationManager} object.
     *
     */
    ConfigurationManager getConfiguationManager();

    /**
     * Retrieves Kaa delta manager.
     *
     * @return {@link DeltaManager} object.
     *
     */
    DeltaManager getDeltaManager();

    /**
     * Retrieves Kaa configuration persistence manager.
     *
     * @return {@link ConfigurationPersistenceManager} object.
     *
     */
    ConfigurationPersistenceManager getConfigurationPersistenceManager();

    /**
     * Retrieves Kaa schema persistence manager.
     *
     * @return {@link SchemaPersistenceManager} object.
     *
     */
    SchemaPersistenceManager getSchemaPersistenceManager();

    /**
     * Retrieves Kaa notification manager.
     *
     * @return {@link NotificationManager} object.
     *
     */
    NotificationManager getNotificationManager();

    /**
     * Retrieves Kaa event family factory.
     *
     * @return {@link EventFamilyFactory} object.
     *
     */
    EventFamilyFactory getEventFamilyFactory();

    /**
     * Retrieves Kaa endpoint registration manager
     *
     * @return {@link EndpointRegistrationManager} object
     */
    EndpointRegistrationManager getEndpointRegistrationManager();

    /**
     * Retrieves Kaa event listeners resolver
     *
     * @return {@link EventListenersResolver} object
     */
    EventListenersResolver getEventListenerResolver();

    /**
     * Retrieves Kaa channel manager
     *
     * @return {@link KaaChannelManager} object
     */
    KaaChannelManager getChannelMananager();

    /**
     * Retrieves data multiplexer for communication with Operation server.
     *
     * Required in user implementation of an operation data channel.
     *
     * @return {@link KaaDataMultiplexer} object
     */
    KaaDataMultiplexer getOperationMultiplexer();

    /**
     * Retrieves data demultiplexer for communication with Operation server.
     *
     * Required in user implementation of an operation data channel.
     *
     * @return {@link KaaDataDemultiplexer} object
     */
    KaaDataDemultiplexer getOperationDemultiplexer();

    /**
     * Retrieves data multiplexer for communication with Bootstrap server.
     *
     * Required in user implementation of a bootstrap data channel.
     *
     * @return {@link KaaDataMultiplexer} object
     */
    KaaDataMultiplexer getBootstrapMultiplexer();

    /**
     * Retrieves data demultiplexer for communication with Bootstrap server.
     *
     * Required in user implementation of a bootstrap data channel.
     *
     * @return {@link KaaDataDemultiplexer} object
     */
    KaaDataDemultiplexer getBootstrapDemultiplexer();

    /**
     * Retrieves the client's public key.
     *
     * Required in user implementation of an operation data channel.
     * Public key hash (SHA-1) is used by servers as identification number to
     * uniquely identify each connected endpoint.
     *
     * @return client's public key
     */
    PublicKey getClientPublicKey();

    /**
     * Retrieves endpoint public key hash. <br>
     * <br>
     * Required in {@link EndpointRegistrationManager} implementation
     * to react on detach response from Operations server.
     *
     * @return String containing current endpoint's public key hash.
     */
    String getEndpointKeyHash();

    /**
     * Retrieves the client's private key.
     *
     * Required in user implementation of an operation data channel.
     * Private key is used by encryption schema between endpoint and servers.
     *
     * @return client's private key
     */
    PrivateKey getClientPrivateKey();

    /**
     * Retrieves Kaa log collector
     *
     * @return LogCollector object
     */
    LogCollector getLogCollector();
}
