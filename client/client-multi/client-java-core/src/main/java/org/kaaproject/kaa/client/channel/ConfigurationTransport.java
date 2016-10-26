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

package org.kaaproject.kaa.client.channel;

import org.kaaproject.kaa.client.configuration.ConfigurationHashContainer;
import org.kaaproject.kaa.client.configuration.ConfigurationProcessor;
import org.kaaproject.kaa.client.schema.SchemaProcessor;
import org.kaaproject.kaa.common.endpoint.gen.ConfigurationSyncRequest;
import org.kaaproject.kaa.common.endpoint.gen.ConfigurationSyncResponse;

/**
 * {@link KaaTransport} for the Configuration service.
 * Updates the Configuration manager state.
 *
 * @author Yaroslav Zeygerman
 */
public interface ConfigurationTransport extends KaaTransport {

  /**
   * Creates the configuration request.
   *
   * @return the configuration request object.
   * @see ConfigurationSyncRequest
   */
  ConfigurationSyncRequest createConfigurationRequest();

  /**
   * Updates the state of the Configuration manager according to the given response.
   *
   * @param response the configuration response.
   * @throws Exception the exception
   * @see ConfigurationSyncResponse
   */
  void onConfigurationResponse(ConfigurationSyncResponse response) throws Exception;

  /**
   * Sets the configuration hash container.
   *
   * @param container the container to be set.
   * @see ConfigurationHashContainer
   */
  void setConfigurationHashContainer(ConfigurationHashContainer container);

  /**
   * Sets the configuration processor.
   *
   * @param processor the processor to be set.
   * @see ConfigurationProcessor
   */
  void setConfigurationProcessor(ConfigurationProcessor processor);

  /**
   * Sets the schema processor.
   *
   * @param processor the processor which is going to be set.
   * @see SchemaProcessor
   */
  void setSchemaProcessor(SchemaProcessor processor);


  void setResyncOnly(boolean resyncOnly);

}
