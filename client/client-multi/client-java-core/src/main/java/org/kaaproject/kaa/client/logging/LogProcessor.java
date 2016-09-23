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

package org.kaaproject.kaa.client.logging;

import org.kaaproject.kaa.common.endpoint.gen.LogSyncRequest;
import org.kaaproject.kaa.common.endpoint.gen.LogSyncResponse;

import java.io.IOException;

/**
 * Processes the Logging requests and responses.
 *
 * @author Yaroslav Zeygerman
 */
public interface LogProcessor {

  /**
   * Fills the given request with the latest Logging state.
   *
   * @param request the Log request which is going to be filled.
   */
  void fillSyncRequest(LogSyncRequest request);

  /**
   * Updates the state using response from the server.
   *
   * @param response the response from the remote server.
   * @throws IOException the io exception
   */
  void onLogResponse(LogSyncResponse response) throws IOException;

}
