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

import java.util.List;
import java.util.UUID;

/**
 * Factory for Common* objects.
 *
 * @author Yaroslav Zeygerman
 */
public interface CommonFactory {

  /**
   * Creates CommonValue from the given value.
   *
   * @param value value which will be stored in CommonValue.
   * @return new CommonValue object.
   */
  CommonValue createCommonValue(Object value);

  /**
   * Creates CommonRecord with the given schema.
   *
   * @param schema avro schema object for the new CommonRecord object.
   * @return new CommonRecord object.
   */
  CommonRecord createCommonRecord(Schema schema);

  /**
   * Creates CommonRecord from the existing record.
   *
   * @param record record object which is going to be copied.
   * @return new CommonRecord object.
   */
  CommonRecord createCommonRecord(CommonRecord record);

  /**
   * Creates CommonRecord with the given avro schema and uuid .
   *
   * @param uuid   uuid for the new record.
   * @param schema avro schema object for the new record.
   * @return new CommonRecord object.
   */
  CommonRecord createCommonRecord(UUID uuid, Schema schema);

  /**
   * Creates CommonArray with the given schema and list of values.
   *
   * @param schema avro schema for the new array.
   * @param list   list of values.
   * @return new CommonArray object.
   */
  CommonArray createCommonArray(Schema schema, List<CommonValue> list);

  /**
   * Creates CommonFixed with the given schema and byte array.
   *
   * @param schema avro schema for the new fixed object.
   * @param bytes  byte array for the new CommonFixed.
   * @return new CommonFixed object.
   */
  CommonFixed createCommonFixed(Schema schema, byte[] bytes);

  /**
   * Creates CommonEnum with the given schema and symbol.
   *
   * @param schema avro schema for the new enum object.
   * @param symbol enum symbol.
   * @return new CommonEnum object.
   */
  CommonEnum createCommonEnum(Schema schema, String symbol);

}
