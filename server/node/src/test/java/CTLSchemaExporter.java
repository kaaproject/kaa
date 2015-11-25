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
