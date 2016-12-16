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

package org.kaaproject.kaa.server.common.utils;

import org.apache.commons.codec.binary.Hex;

import java.nio.ByteBuffer;

public final class Utils {

  private Utils() {}

  /**
   * Encode byte array into string if it is not null.
   *
   * @param buf byte array to be encoded
   * @return    encoded string
   */
  public static String encodeHexString(byte[] buf) {
    return buf != null ? Hex.encodeHexString(buf) : "";
  }

  public static String encodeHexString(ByteBuffer buf) {
    return buf != null && buf.hasArray() ? Hex.encodeHexString(buf.array()) : "";
  }
}
