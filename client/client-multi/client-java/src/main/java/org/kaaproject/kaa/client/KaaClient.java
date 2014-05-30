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

import org.kaaproject.kaa.client.configuration.delta.manager.DeltaManager;
import org.kaaproject.kaa.client.configuration.manager.ConfigurationManager;
import org.kaaproject.kaa.client.configuration.storage.ConfigurationPersistenceManager;
import org.kaaproject.kaa.client.notification.NotificationManager;
import org.kaaproject.kaa.client.profile.ProfileManager;
import org.kaaproject.kaa.client.schema.storage.SchemaPersistenceManager;

/**
 * Interface for the Kaa client.
 *
 * Basic interface to operate with Kaa library.
 *
 * @author Yaroslav Zeygerman
 *
 */
public interface KaaClient {

    /**
     * Retrieves Kaa profile manager.
     *
     * @return ConfigurationManager object.
     *
     */
    ProfileManager getProfileManager();

    /**
     * Retrieves Kaa configuration manager.
     *
     * @return ConfigurationManager object.
     *
     */
    ConfigurationManager getConfiguationManager();

    /**
     * Retrieves Kaa delta manager.
     *
     * @return DeltaManager object.
     *
     */
    DeltaManager getDeltaManager();

    /**
     * Retrieves Kaa configuration persistence manager.
     *
     * @return ConfigurationPersistenceManager object.
     *
     */
    ConfigurationPersistenceManager getConfigurationPersistenceManager();

    /**
     * Retrieves Kaa schema persistence manager.
     *
     * @return SchemaPersistenceManager object.
     *
     */
    SchemaPersistenceManager getSchemaPersistenceManager();

    /**
     * Retrieves Kaa notification manager.
     *
     * @return NotificationManager object.
     *
     */
    NotificationManager getNotificationManager();

}
