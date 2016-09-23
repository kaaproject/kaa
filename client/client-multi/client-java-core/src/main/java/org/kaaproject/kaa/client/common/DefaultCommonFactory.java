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
 * Default {@link CommonFactory} implementation.
 *
 * @author Yaroslav Zeygerman
 */
public class DefaultCommonFactory implements CommonFactory {

  @Override
  public CommonValue createCommonValue(Object value) {
    return new DefaultCommonValue(value);
  }

  @Override
  public CommonRecord createCommonRecord(Schema schema) {
    return new DefaultCommonRecord(schema);
  }

  @Override
  public CommonRecord createCommonRecord(CommonRecord record) {
    return new DefaultCommonRecord(record);
  }

  @Override
  public CommonRecord createCommonRecord(UUID uuid, Schema schema) {
    return new DefaultCommonRecord(uuid, schema);
  }

  @Override
  public CommonArray createCommonArray(Schema schema, List<CommonValue> list) {
    return new DefaultCommonArray(schema, list);
  }

  @Override
  public CommonFixed createCommonFixed(Schema schema, byte[] bytes) {
    return new DefaultCommonFixed(schema, bytes);
  }

  @Override
  public CommonEnum createCommonEnum(Schema schema, String symbol) {
    return new DefaultCommonEnum(schema, symbol);
  }

}
