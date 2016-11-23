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

import java.util.Arrays;

/**
 * Default {@link CommonFixed} implementation.
 *
 * @author Yaroslav Zeygerman
 */
public final class DefaultCommonFixed implements CommonFixed {
  private final byte[] bytes;
  private final Schema schema;

  DefaultCommonFixed(Schema schema, byte[] bytes) {
    this.schema = schema;
    this.bytes = Arrays.copyOf(bytes, bytes.length);
  }

  @Override
  public Schema getSchema() {
    return schema;
  }

  @Override
  public byte[] getBytes() {
    return Arrays.copyOf(bytes, bytes.length);
  }

  @Override
  public String toString() {
    return Arrays.toString(bytes);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + Arrays.hashCode(bytes);
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
    DefaultCommonFixed other = (DefaultCommonFixed) obj;
    if (!Arrays.equals(bytes, other.bytes)) {
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
