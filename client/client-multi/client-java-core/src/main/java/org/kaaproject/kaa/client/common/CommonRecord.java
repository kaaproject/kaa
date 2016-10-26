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

import java.util.UUID;

/**
 * CommonRecord interface. Represents avro record data structure.
 *
 * @author Yaroslav Zeygerman
 */
public interface CommonRecord extends SchemaDependent {

  /**
   * Retrieves the uuid of this record.
   *
   * @return uuid of the record.
   * @see java.util.UUID
   */
  UUID getUuid();

  /**
   * Sets the given uuid to the record.
   *
   * @param uuid uuid which is going to be set.
   * @see java.util.UUID
   */
  void setUuid(UUID uuid);

  /**
   * Checks if this record contains field with the given name.
   *
   * @param field the field name.
   * @return true if the record contains given field name, false otherwise.
   */
  boolean hasField(String field);

  /**
   * Sets new value to the field with the given name (existing value will be replaced).
   *
   * @param field the field name.
   * @param value value which is going to be set.
   * @see CommonValue
   */
  void setField(String field, CommonValue value);

  /**
   * Retrieves the field's value by the given field name.
   *
   * @param field the field name.
   * @return field's value.
   * @see CommonValue
   */
  CommonValue getField(String field);

}
