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
 * Class to hold transport id constants. Please note that this constants should
 * match same constants in appropriate transport configs on server side
 *
 * @author Andrew Shvayka
 */
public class TransportProtocolIdConstants {

  private static final int HTTP_TRANSPORT_PROTOCOL_ID = 0xfb9a3cf0;
  private static final int HTTP_TRANSPORT_PROTOCOL_VERSION = 1;
  public static final TransportProtocolId HTTP_TRANSPORT_ID = new TransportProtocolId(
          HTTP_TRANSPORT_PROTOCOL_ID, HTTP_TRANSPORT_PROTOCOL_VERSION);
  private static final int TCP_TRANSPORT_PROTOCOL_ID = 0x56c8ff92;
  private static final int TCP_TRANSPORT_PROTOCOL_VERSION = 1;
  public static final TransportProtocolId TCP_TRANSPORT_ID = new TransportProtocolId(
          TCP_TRANSPORT_PROTOCOL_ID, TCP_TRANSPORT_PROTOCOL_VERSION);

  private TransportProtocolIdConstants() {
  }
}
