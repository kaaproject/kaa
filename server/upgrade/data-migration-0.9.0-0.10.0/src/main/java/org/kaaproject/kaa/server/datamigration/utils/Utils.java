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

package org.kaaproject.kaa.server.datamigration.utils;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ObjectNode;

import java.io.IOException;
import java.util.Base64;

public final class Utils {

  private static final String UUID_FIELD = "__uuid";
  private static final String UUID_VALUE = "org.kaaproject.configuration.uuidT";


  /**
   * Change encoding of uuids  from <b>latin1 (ISO-8859-1)</b> to <b>base64</b>.
   *
   * @param json the json that should be processed
   * @return the json with changed uuids
   * @throws IOException the io exception
   */
  public static JsonNode encodeUuids(JsonNode json) throws IOException {
    if (json.has(UUID_FIELD)) {
      JsonNode jsonNode = json.get(UUID_FIELD);
      if (jsonNode.has(UUID_VALUE)) {
        String value = jsonNode.get(UUID_VALUE).asText();
        String encodedValue = Base64.getEncoder().encodeToString(value.getBytes("ISO-8859-1"));
        ((ObjectNode) jsonNode).put(UUID_VALUE, encodedValue);
      }
    }

    for (JsonNode node : json) {
      if (node.isContainerNode()) {
        encodeUuids(node);
      }
    }

    return json;
  }

}
