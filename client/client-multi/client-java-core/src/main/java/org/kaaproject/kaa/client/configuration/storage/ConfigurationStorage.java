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

package org.kaaproject.kaa.client.configuration.storage;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Interface for object to save and load configuration data<br>
 * <br>
 * Provide implementation instance of this interface to save and load serialized
 * configuration. Configuration data is serialized according to the configuration
 * schema.<br>
 * <br>
 * Configuration storage can be added using {@link ConfigurationPersistenceManager}
 * accessed through {@link org.kaaproject.kaa.client.KaaClient} interface.<br>
 * <pre>
 * {@code
 * class FileConfigurationStorage implements ConfigurationStorage {
 *     public void saveConfiguration(ByteBuffer buffer) {
 *         ...
 *     }
 *     public ByteBuffer loadConfiguration() {
 *         ...
 *     }
 * }
 * ...
 * // Assuming Kaa instance is created
 * KaaClient kaaClient = kaa.getClient();
 *
 * ConfigurationStorage configurationStorage = new FileConfigurationStorage();
 * kaaClient.getConfigurationPersistenceManager().setConfigurationStorage(configurationStorage);
 * }
 * </pre>
 *
 * @author Yaroslav Zeygerman
 * @see ConfigurationPersistenceManager
 */
public interface ConfigurationStorage {

  /**
   * Saves configuration data.
   *
   * @param buffer buffer with configuration data
   * @throws IOException the io exception
   */
  void saveConfiguration(ByteBuffer buffer) throws IOException;

  /**
   * Loads configuration data.
   *
   * @return buffer with loaded configuration data, or null if configuration is empty
   * @throws IOException the io exception
   */
  ByteBuffer loadConfiguration() throws IOException;

  /**
   * Clear configuration data (file).
   *
   * @throws IOException the io exception
   */
  void clearConfiguration() throws IOException;


}
