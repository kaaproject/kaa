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

package org.kaaproject.kaa.client.common;

import java.nio.ByteBuffer;

/**
 * Common value interface.
 *
 * @author Yaroslav Zeygerman
 */
public interface CommonValue {

  /**
   * @return true if it is null value, false otherwise.
   */
  boolean isNull();

  /**
   * @return true if it is Integer value, false otherwise.
   */
  boolean isInteger();

  /**
   * @return true if it is Boolean value, false otherwise.
   */
  boolean isBoolean();

  /**
   * @return true if it is Double value, false otherwise.
   */
  boolean isDouble();

  /**
   * @return true if it is Long value, false otherwise.
   */
  boolean isLong();

  /**
   * @return true if it is Float value, false otherwise.
   */
  boolean isFloat();

  /**
   * @return true if it is String value, false otherwise.
   */
  boolean isString();

  /**
   * @return true if it is Record value, false otherwise.
   */
  boolean isRecord();

  /**
   * @return true if it is array value, false otherwise.
   */
  boolean isArray();

  /**
   * @return true if it is Fixed value, false otherwise.
   */
  boolean isFixed();

  /**
   * @return true if it is Number value, false otherwise.
   */
  boolean isNumber();

  /**
   * @return true if it is bytes value, false otherwise.
   */
  boolean isBytes();

  /**
   * @return true if it is enum value, false otherwise.
   */
  boolean isEnum();

  /**
   * @return {@link Integer} value or null if other type.
   */
  Integer getInteger();

  /**
   * @return {@link Boolean} value or null if other type.
   */
  Boolean getBoolean();

  /**
   * @return {@link Double} value or null if other type.
   */
  Double getDouble();

  /**
   * @return {@link Long} value or null if other type.
   */
  Long getLong();

  /**
   * @return {@link Float} value or null if other type.
   */
  Float getFloat();

  /**
   * @return {@link CharSequence} value or null if other type.
   */
  CharSequence getString();

  /**
   * @return {@link CommonRecord} value or null if other type.
   */
  CommonRecord getRecord();

  /**
   * @return {@link CommonArray} value or null if other type.
   */
  CommonArray getArray();

  /**
   * @return {@link CommonFixed} value or null if other type.
   */
  CommonFixed getFixed();

  /**
   * @return {@link Number} value or null if other type.
   */
  Number getNumber();

  /**
   * @return {@link ByteBuffer} value or null if other type.
   */
  ByteBuffer getBytes();

  /**
   * @return {@link CommonEnum} value or null if other type.
   */
  CommonEnum getEnum();

}
