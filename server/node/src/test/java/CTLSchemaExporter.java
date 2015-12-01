/*
 * Copyright 2014-2015 CyberVision, Inc.
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

import java.nio.file.Files;
import java.nio.file.Paths;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.JsonNodeFactory;
import org.codehaus.jackson.node.ObjectNode;

public class CTLSchemaExporter {

    public static void main(String[] args) throws Exception {
        String content = new String(Files.readAllBytes(Paths.get("C:\\beta.json")));
        JsonNode beta = asJsonNode(content);
        content = new String(Files.readAllBytes(Paths.get("C:\\alpha.json")));
        JsonNode alpha = asJsonNode(content);

        ArrayNode fields = JsonNodeFactory.instance.arrayNode();
        for (JsonNode field : alpha.get("fields")) {
            if (field.get("type").asText().equals(beta.get("namespace").asText() + "." + beta.get("name").asText())) {
                change(field, "type", beta);
            }
        }

        System.out.println(alpha);
    }

    public static JsonNode asJsonNode(String content) throws Exception {
        return new ObjectMapper().readValue(content, JsonNode.class);
    }

    public static void change(JsonNode parent, String name, JsonNode value) {
        if (parent.has(name)) {
            ((ObjectNode) parent).put(name, value);
        }
    }
}
