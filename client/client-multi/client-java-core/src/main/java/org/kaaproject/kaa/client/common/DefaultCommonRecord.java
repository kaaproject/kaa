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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Default {@link CommonRecord} implementation.
 *
 * @author Yaroslav Zeygerman
 */
public final class DefaultCommonRecord implements CommonRecord {
  private final Schema schema;
  private final Map<String, CommonValue> record = new HashMap<String, CommonValue>();
  private UUID uuid = new UUID(0, 0);

  DefaultCommonRecord(CommonRecord other) {
    DefaultCommonRecord defaultOther = (DefaultCommonRecord) other;
    this.uuid = defaultOther.uuid;
    this.schema = defaultOther.schema;
    copyFromCommonRecord(defaultOther);
  }

  DefaultCommonRecord(UUID uuid, Schema schema) {
    this.uuid = uuid;
    this.schema = schema;
  }

  DefaultCommonRecord(Schema schema) {
    this.schema = schema;
  }

  private void copyFromCommonRecord(DefaultCommonRecord other) {
    for (Map.Entry<String, CommonValue> entry : other.record.entrySet()) {
      String key = entry.getKey();
      CommonValue value = entry.getValue();
      if (value.isRecord()) {
        this.record.put(key, new DefaultCommonValue(new DefaultCommonRecord(value.getRecord())));
      } else if (value.isArray()) {
        List<CommonValue> newArray = new LinkedList<CommonValue>();
        List<CommonValue> array = value.getArray().getList();
        for (CommonValue item : array) {
          if (item.isRecord()) {
            newArray.add(new DefaultCommonValue(new DefaultCommonRecord(item.getRecord())));
          } else {
            newArray.add(item);
          }
        }
        this.record.put(key, new DefaultCommonValue(new DefaultCommonArray(
                value.getArray().getSchema(), newArray)));
      } else {
        this.record.put(key, value);
      }
    }
  }

  @Override
  public UUID getUuid() {
    return uuid;
  }

  @Override
  public void setUuid(UUID uuid) {
    this.uuid = uuid;
  }

  @Override
  public Schema getSchema() {
    return schema;
  }

  @Override
  public void setField(String field, CommonValue value) {
    record.put(field, value);
  }

  @Override
  public boolean hasField(String field) {
    return record.get(field) != null;
  }

  @Override
  public CommonValue getField(String field) {
    return record.get(field);
  }

  @Override
  public String toString() {
    return record.toString();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((record == null) ? 0 : record.hashCode());
    result = prime * result + ((schema == null) ? 0 : schema.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    DefaultCommonRecord other = (DefaultCommonRecord) obj;
    if (record == null) {
      if (other.record != null) {
        return false;
      }
    } else if (!record.equals(other.record)) {
      return false;
    }
    if (schema == null) {
      if (other.schema != null) {
        return false;
      }
    } else if (!schema.equals(other.schema)) {
      return false;
    }
    return true;
  }

}
