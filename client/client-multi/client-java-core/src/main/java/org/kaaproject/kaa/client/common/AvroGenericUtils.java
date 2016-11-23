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

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericArray;
import org.apache.avro.generic.GenericEnumSymbol;
import org.apache.avro.generic.GenericFixed;
import org.apache.avro.generic.GenericRecord;

import java.nio.ByteBuffer;
import java.util.UUID;

/**
 * Common tools for avro generic objects.
 *
 * @author Yaroslav Zeygerman
 */
public class AvroGenericUtils {
  private static final String RESETT = "org.kaaproject.configuration.resetT";
  private static final String UNCHANGEDT = "org.kaaproject.configuration.unchangedT";
  private static final String UUIDT = "org.kaaproject.configuration.uuidT";

  private AvroGenericUtils() {

  }

  /**
   * Creates UUID from the given GenericFixed object.
   *
   * @param fixed the fixed
   * @return uuid object.
   */
  public static UUID createUuidFromFixed(GenericFixed fixed) {
    ByteBuffer bb = ByteBuffer.wrap(fixed.bytes());
    long first = bb.getLong();
    long second = bb.getLong();

    return new UUID(first, second);
  }

  /**
   * Checks if the given value is GenericFixed.
   *
   * @param field object which going to be verified.
   * @return true if the value is GenericFixed, false otherwise.
   */
  public static boolean isFixed(Object field) {
    return field instanceof GenericFixed;
  }

  /**
   * Checks if the given value is GenericEnum.
   *
   * @param field object which going to be verified.
   * @return true if the value is GenericEnum, false otherwise.
   */
  public static boolean isEnum(Object field) {
    return field instanceof GenericEnumSymbol;
  }

  /**
   * Checks if the given value is GenericArray.
   *
   * @param field object which going to be verified.
   * @return true if the value is GenericArray, false otherwise.
   */
  public static boolean isArray(Object field) {
    return field instanceof GenericArray;
  }

  /**
   * Checkss if the given value is GenericRecord.
   *
   * @param field object which going to be verified.
   * @return true if the value is GenericRecord, false otherwise.
   */
  public static boolean isRecord(Object field) {
    return field instanceof GenericRecord;
  }

  /**
   * Checks if the given value is UUID (value's schema is "org.kaaproject.configuration.uuidT").
   *
   * @param field object which going to be verified.
   * @return true if the value is UUID, false otherwise.
   */
  public static boolean isUuid(Object field) {
    if (!isFixed(field)) {
      return false;
    }
    GenericFixed checkFixed = (GenericFixed) field;
    return checkFixed.getSchema().getFullName().equals(UUIDT);
  }

  /**
   * Retrieves full schema name of the given enum.
   *
   * @param symbol enum symbol whose name will be returned.
   * @return schema full name.
   */
  public static String getEnumFullName(GenericEnumSymbol symbol) {
    Schema enumSchema = symbol.getSchema();
    return enumSchema.getFullName();
  }

  /**
   * checks if the given value is Reset (value's schema is "org.kaaproject.configuration.resetT").
   *
   * @param field object which going to be verified.
   * @return true if the value is Reset, false otherwise.
   */
  public static boolean isReset(Object field) {
    if (!isEnum(field)) {
      return false;
    }
    return getEnumFullName((GenericEnumSymbol) field).equals(RESETT);
  }

  /**
   * Checks if the given value is Unchanged (value's schema is
   * "org.kaaproject.configuration.unchangedT").
   *
   * @param field object which going to be verified.
   * @return true if the value is Unchanged, false otherwise.
   */
  public static boolean isUnchanged(Object field) {
    if (!isEnum(field)) {
      return false;
    }
    return getEnumFullName((GenericEnumSymbol) field).equals(UNCHANGEDT);
  }

}
