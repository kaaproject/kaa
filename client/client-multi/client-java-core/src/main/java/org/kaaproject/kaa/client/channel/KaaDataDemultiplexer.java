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

/**
 * Demultiplexer is responsible for deserializing of response data and notifying
 * appropriate services.
 * Required in user implementation of any kind of data channel.
 *
 * @author Yaroslav Zeygerman
 */
public interface KaaDataDemultiplexer {

  /**
   * Processes the given response bytes.
   *
   * @param response buffer which to be processed.
   * @throws Exception the exception
   */
  void processResponse(byte[] response) throws Exception;

  /**
   * Routines to be executed before response will be processed.
   */
  void preProcess();

  /**
   * Define routines to be executed after response is processed.
   */
  void postProcess();
}
