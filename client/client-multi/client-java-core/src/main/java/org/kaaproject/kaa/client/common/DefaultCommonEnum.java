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

/**
 * Default {@link CommonEnum} implementation.
 *
 * @author Yaroslav Zeygerman
 */
public final class DefaultCommonEnum implements CommonEnum {
  private final String symbol;
  private final Schema schema;

  DefaultCommonEnum(Schema schema, String symbol) {
    this.symbol = symbol;
    this.schema = schema;
  }

  @Override
  public String getSymbol() {
    return symbol;
  }

  @Override
  public Schema getSchema() {
    return schema;
  }

  @Override
  public String toString() {
    return symbol;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((schema == null) ? 0 : schema.hashCode());
    result = prime * result + ((symbol == null) ? 0 : symbol.hashCode());
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
    DefaultCommonEnum other = (DefaultCommonEnum) obj;
    if (schema == null) {
      if (other.schema != null) {
        return false;
      }
    } else if (!schema.equals(other.schema)) {
      return false;
    }
    if (symbol == null) {
      if (other.symbol != null) {
        return false;
      }
    } else if (!symbol.equals(other.symbol)) {
      return false;
    }
    return true;
  }

}
