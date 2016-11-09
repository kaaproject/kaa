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
 * @author Yaroslav Zeygerman.
 */
public final class DefaultCommonValue implements CommonValue {
  private final Object value;

  DefaultCommonValue(Object value) {
    this.value = value;
  }

  @Override
  public boolean isNull() {
    return value == null;
  }

  @Override
  public boolean isInteger() {
    return value instanceof Integer;
  }

  @Override
  public boolean isBoolean() {
    return value instanceof Boolean;
  }

  @Override
  public boolean isDouble() {
    return value instanceof Double;
  }

  @Override
  public boolean isLong() {
    return value instanceof Long;
  }

  @Override
  public boolean isFloat() {
    return value instanceof Float;
  }

  @Override
  public boolean isString() {
    return value instanceof CharSequence;
  }

  @Override
  public boolean isRecord() {
    return value instanceof CommonRecord;
  }

  @Override
  public boolean isArray() {
    return value instanceof CommonArray;
  }

  @Override
  public boolean isFixed() {
    return value instanceof CommonFixed;
  }

  @Override
  public boolean isNumber() {
    return value instanceof Number;
  }

  @Override
  public boolean isBytes() {
    return value instanceof ByteBuffer;
  }

  @Override
  public boolean isEnum() {
    return value instanceof CommonEnum;
  }

  @Override
  public Integer getInteger() {
    if (isInteger()) {
      return (Integer) value;
    }
    return null;
  }

  @Override
  public Boolean getBoolean() {
    if (isBoolean()) {
      return (Boolean) value;
    }
    return null;
  }

  @Override
  public Double getDouble() {
    if (isDouble()) {
      return (Double) value;
    }
    return null;
  }

  @Override
  public Long getLong() {
    if (isLong()) {
      return (Long) value;
    }
    return null;
  }

  @Override
  public Float getFloat() {
    if (isFloat()) {
      return (Float) value;
    }
    return null;
  }

  @Override
  public CharSequence getString() {
    if (isString()) {
      return (CharSequence) value;
    }
    return null;
  }

  @Override
  public CommonRecord getRecord() {
    if (isRecord()) {
      return (CommonRecord) value;
    }
    return null;
  }

  @Override
  public CommonArray getArray() {
    if (isArray()) {
      return (CommonArray) value;
    }
    return null;
  }

  @Override
  public CommonFixed getFixed() {
    if (isFixed()) {
      return (CommonFixed) value;
    }
    return null;
  }

  @Override
  public Number getNumber() {
    if (isNumber()) {
      return (Number) value;
    }
    return null;
  }

  @Override
  public ByteBuffer getBytes() {
    if (isBytes()) {
      return (ByteBuffer) value;
    }
    return null;
  }

  @Override
  public CommonEnum getEnum() {
    if (isEnum()) {
      return (CommonEnum) value;
    }
    return null;
  }

  @Override
  public String toString() {
    if (value != null) {
      return value.toString();
    }
    return "null";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((value == null) ? 0 : value.hashCode());
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
    DefaultCommonValue other = (DefaultCommonValue) obj;
    if (value == null) {
      if (other.value != null) {
        return false;
      }
    } else if (!value.equals(other.value)) {
      return false;
    }
    return true;
  }

}
