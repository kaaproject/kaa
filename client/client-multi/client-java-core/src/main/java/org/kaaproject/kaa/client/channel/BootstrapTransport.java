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

import org.kaaproject.kaa.client.bootstrap.BootstrapManager;
import org.kaaproject.kaa.common.endpoint.gen.SyncRequest;
import org.kaaproject.kaa.common.endpoint.gen.SyncResponse;

/**
 * {@link KaaTransport} for the Bootstrap service.
 * Updates the Bootstrap manager state.
 *
 * @author Yaroslav Zeygerman
 */
public interface BootstrapTransport extends KaaTransport {

  /**
   * Creates new Resolve request.
   *
   * @return Resovle request.
   */
  SyncRequest createResolveRequest();

  /**
   * Updates the state of the Bootstrap manager according the given response.
   *
   * @param servers response from Bootstrap server.
   */
  void onResolveResponse(SyncResponse servers);

  /**
   * Sets the given Bootstrap manager.
   *
   * @param manager the Bootstrap manager to be set.
   */
  void setBootstrapManager(BootstrapManager manager);
}
