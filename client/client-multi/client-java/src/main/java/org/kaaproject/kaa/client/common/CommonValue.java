/*
 * Copyright 2014 CyberVision, Inc.
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
 * Common value interface
 *
 * @author Yaroslav Zeygerman
 *
 */
public interface CommonValue {

    /**
     *
     * @return true if it is null value, false otherwise
     *
     */
    boolean isNull();

    /**
     *
     * @return true if it is Integer value, false otherwise
     *
     */
    boolean isInteger();

    /**
     *
     * @return true if it is Boolean value, false otherwise
     *
     */
    boolean isBoolean();

    /**
     *
     * @return true if it is Double value, false otherwise
     *
     */
    boolean isDouble();

    /**
     *
     * @return true if it is Long value, false otherwise
     *
     */
    boolean isLong();

    /**
     *
     * @return true if it is Float value, false otherwise
     *
     */
    boolean isFloat();

    /**
     *
     * @return true if it is String value, false otherwise
     *
     */
    boolean isString();

    /**
     *
     * @return true if it is Record value, false otherwise
     *
     */
    boolean isRecord();

    /**
     *
     * @return true if it is array value, false otherwise
     *
     */
    boolean isArray();

    /**
     *
     * @return true if it is Fixed value, false otherwise
     *
     */
    boolean isFixed();

    /**
     *
     * @return true if it is Number value, false otherwise
     *
     */
    boolean isNumber();

    /**
     *
     * @return true if it is bytes value, false otherwise
     *
     */
    boolean isBytes();

    /**
     *
     * @return true if it is enum value, false otherwise
     *
     */
    boolean isEnum();

    /**
     *
     * @return Integer value or null if value is not Integer
     *
     */
    Integer getInteger();

    /**
     *
     * @return Boolean value or null if value is not Boolean
     *
     */
    Boolean getBoolean();

    /**
     *
     * @return Double value or null if value is not Double
     *
     */
    Double getDouble();

    /**
     *
     * @return Long value or null if value is not Long
     *
     */
     Long getLong();

    /**
     *
     * @return Float value or null if value is not Float
     *
     */
    Float getFloat();

    /**
     *
     * @return CharSequence value or null if value is not CharSequence
     *
     */
    CharSequence getString();

    /**
     *
     * @return CommonRecord value or null if value is not CommonRecord
     *
     */
    CommonRecord getRecord();

    /**
     *
     * @return Array value or null if value is not array
     *
     */
    CommonArray getArray();

    /**
     *
     * @return Fixed value or null if value is not fixed
     *
     */
    CommonFixed getFixed();

    /**
     *
     * @return Number value or null if value is not Number
     *
     */
    Number getNumber();

    /**
     *
     * @return Bytes value or null if value is not ByteBuffer
     *
     */
    ByteBuffer getBytes();

    /**
     *
     * @return CommonEnum value or null if value is not CommonEnum
     *
     */
    CommonEnum getEnum();

}
