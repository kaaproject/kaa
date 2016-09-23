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
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericEnumSymbol;
import org.apache.avro.generic.GenericFixed;
import org.apache.avro.generic.GenericRecord;
import org.kaaproject.kaa.common.avro.AvroDataCanonizationUtils;

import java.util.List;

/**
 * Tools for converting from Common* objects to avro Generic* objects.
 *
 * @author Yaroslav Zeygerman
 */
public class CommonToGeneric {

  private CommonToGeneric() {

  }

  /**
   * Creates new GenericEnumSymbol from the given CommonEnum object.
   *
   * @param commonEnum CommonEnum object.
   * @return new GenericEnumSymbol object.
   */
  public static GenericEnumSymbol createEnum(CommonEnum commonEnum) {
    return new GenericData.EnumSymbol(commonEnum.getSchema(), commonEnum.getSymbol());
  }

  /**
   * Creates new GenericFixed from the given CommonFixed object.
   *
   * @param commonFixed CommonFixed object.
   * @return new GenericFixed object.
   */
  public static GenericFixed createFixed(CommonFixed commonFixed) {
    return new GenericData.Fixed(commonFixed.getSchema(), commonFixed.getBytes());
  }

  /**
   * Creates new GenericArray from the given CommonArray object.
   *
   * @param commonArray CommonArray object.
   * @return new GenericArray object.
   */
  public static GenericArray createArray(CommonArray commonArray) {
    final Schema arraySchema = commonArray.getSchema();
    List<CommonValue> array = commonArray.getList();
    GenericArray avroArray = new GenericData.Array(array.size(), arraySchema);
    for (CommonValue value : array) {
      if (value.isRecord()) {
        avroArray.add(createRecord(value.getRecord()));
      } else if (value.isFixed()) {
        avroArray.add(createFixed(value.getFixed()));
      } else if (value.isBoolean()) {
        avroArray.add(value.getBoolean());
      } else if (value.isInteger()) {
        avroArray.add(value.getInteger());
      } else if (value.isLong()) {
        avroArray.add(value.getLong());
      } else if (value.isDouble()) {
        avroArray.add(value.getDouble());
      } else if (value.isFloat()) {
        avroArray.add(value.getFloat());
      } else if (value.isString()) {
        avroArray.add(value.getString());
      } else if (value.isBytes()) {
        avroArray.add(value.getBytes());
      }
    }
    return avroArray;
  }

  /**
   * Creates new GenericRecord from the given CommonRecord object.
   *
   * @param record CommonRecord object.
   * @return new GenericRecord object.
   */
  public static GenericRecord createRecord(CommonRecord record) {
    Schema recordSchema = record.getSchema();
    GenericRecord avroRecord = new GenericData.Record(recordSchema);
    for (Schema.Field field : recordSchema.getFields()) {
      String fieldName = field.name();
      CommonValue value = record.getField(fieldName);
      if (value.isRecord()) {
        avroRecord.put(fieldName, createRecord(value.getRecord()));
      } else if (value.isArray()) {
        avroRecord.put(fieldName, createArray(value.getArray()));
      } else if (value.isEnum()) {
        avroRecord.put(fieldName, createEnum(value.getEnum()));
      } else if (value.isFixed()) {
        avroRecord.put(fieldName, createFixed(value.getFixed()));
      } else if (value.isBoolean()) {
        avroRecord.put(fieldName, value.getBoolean());
      } else if (value.isInteger()) {
        avroRecord.put(fieldName, value.getInteger());
      } else if (value.isLong()) {
        avroRecord.put(fieldName, value.getLong());
      } else if (value.isDouble()) {
        avroRecord.put(fieldName, value.getDouble());
      } else if (value.isFloat()) {
        avroRecord.put(fieldName, value.getFloat());
      } else if (value.isString()) {
        avroRecord.put(fieldName, value.getString());
      } else if (value.isBytes()) {
        avroRecord.put(fieldName, value.getBytes());
      }
    }

    AvroDataCanonizationUtils.canonizeRecord(avroRecord);
    return avroRecord;
  }

}
