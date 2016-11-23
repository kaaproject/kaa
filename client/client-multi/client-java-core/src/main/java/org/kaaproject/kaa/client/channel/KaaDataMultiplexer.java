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

import org.kaaproject.kaa.common.TransportType;

import java.util.Map;

/**
 * Multiplexer collects the info about states from different
 * services and compiles it in one request.
 * Required in user implementation of any kind of data channel.
 *
 * @author Yaroslav Zeygerman
 */
public interface KaaDataMultiplexer {

  /**
   * Compiles request for given transport types.
   *
   * @param types the map of types to be polled.
   * @return the serialized request data.
   * @throws Exception the exception
   * @see TransportType
   * @see ChannelDirection
   */
  byte[] compileRequest(Map<TransportType, ChannelDirection> types) throws Exception;

}
