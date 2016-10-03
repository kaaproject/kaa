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

/**
 * Default {@link CommonArray} implementation.
 *
 * @author Yaroslav Zeygerman
 */
public final class DefaultCommonArray implements CommonArray {
  private final List<CommonValue> list;
  private final Schema schema;

  DefaultCommonArray(Schema schema, List<CommonValue> list) {
    this.schema = schema;
    this.list = list;
  }

  @Override
  public Schema getSchema() {
    return schema;
  }

  @Override
  public List<CommonValue> getList() {
    return list;
  }

  @Override
  public String toString() {
    return list.toString();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((list == null) ? 0 : list.hashCode());
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
    DefaultCommonArray other = (DefaultCommonArray) obj;
    if (list == null) {
      if (other.list != null) {
        return false;
      }
    } else if (!list.equals(other.list)) {
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
