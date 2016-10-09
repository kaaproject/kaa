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

package org.kaaproject.kaa.client.configuration.manager;

import org.kaaproject.kaa.client.common.CommonRecord;
import org.kaaproject.kaa.client.configuration.storage.ConfigurationPersistenceManager;

/**
 * Interface for the configuration manager.<br>
 * <br>
 * Responsible for configuration updates subscriptions and configuration obtaining.<br>
 * <br>
 * Configuration manager can be used to fetch current configuration at any time.
 * If there were no configuration updates, default configuration will be returned.
 * Default configuration is built-in to the sdk. <br>
 * <br>
 * <b>NOTE:</b> Use {@link ConfigurationPersistenceManager} to set configuration storage.
 * Until configuration storage will not be specified each start of Kaa client will cause
 * full configuration resync from Operation server.
 * <br>
 * <pre>
 * {@code
 * // Assuming Kaa inited and started
 * ConfigurationManager configurationManager = kaaClient.getConfigurationManager();
 * CommonRecord fullCurrentConfiguration = configurationManager.getConfiguration();
 * }
 * </pre>
 * Configuration manager will notify subscribed configuration update observers
 * added using {@link ConfigurationManager#subscribeForConfigurationUpdates(ConfigurationReceiver)}
 * on each configuration update received from Operation server.<br>
 * Use {@link ConfigurationManager#unsubscribeFromConfigurationUpdates(ConfigurationReceiver)}
 * when configuration updates aren't needed anymore.
 * <br>
 * <pre>
 * {@code
 * class ConfigurationReceiverImpl implements ConfigurationReceiver {
 *     public void onConfigurationUpdated(CommonRecord configuration) {
 *         System.out.println("Configuration received: " + configuration.toString());
 *     }
 * }
 * // Assuming Kaa inited
 * ConfigurationReceiverImpl receiver = new ConfigurationReceiverImpl();
 * ConfigurationManager configurationManager = kaaClient.getConfigurationManager();
 * configurationManager.subscribeForConfigurationUpdates(receiver);
 * configurationManager.unsubscribeFromConfigurationUpdates(receiver);
 * }
 * </pre>
 *
 * @author Yaroslav Zeygerman
 * @see CommonRecord
 * @see ConfigurationReceiver
 * @see ConfigurationPersistenceManager
 */
public interface ConfigurationManager {

  /**
   * Subscribes for configuration updates.<br>
   * <br>
   * <b>NOTE:</b> Attempting to subscribe two instances of
   * {@link ConfigurationReceiver} such as
   * {@code receiver1.equals(receiver2) == true}
   * will result the fact that only first instance will be subscribed.
   *
   * @param receiver object to receive updates
   * @see ConfigurationReceiver
   */
  void subscribeForConfigurationUpdates(ConfigurationReceiver receiver);

  /**
   * Unsubscribes from configuration updates.<br>
   * <br>
   * <b>NOTE:</b> Attempting to unsubscribe object which was not subscribed previously will
   * not make any effect.
   *
   * @param receiver object which is no longer needs configuration updates
   * @see ConfigurationReceiver
   */
  void unsubscribeFromConfigurationUpdates(ConfigurationReceiver receiver);

  /**
   * Retrieves full configuration.
   *
   * @return common object with full configuration
   * @see CommonRecord
   */
  CommonRecord getConfiguration();
}
