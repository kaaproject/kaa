package org.kaaproject.data_migration.utils;


import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.io.IOUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ObjectNode;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Base64;

final public class Utils {

    private static final String UUID_FIELD = "__uuid";
    private static final String UUID_VALUE = "org.kaaproject.configuration.uuidT";





    public static JsonNode encodeUuids(JsonNode json) throws IOException {
        if (json.has(UUID_FIELD)) {
            JsonNode j = json.get(UUID_FIELD);
            if (j.has(UUID_VALUE)) {
                String value = j.get(UUID_VALUE).asText();
                String encodedValue = Base64.getEncoder().encodeToString(value.getBytes("ISO-8859-1"));
                ((ObjectNode)j).put(UUID_VALUE, encodedValue);
            }
        }

        for (JsonNode node : json) {
            if (node.isContainerNode()) encodeUuids(node);
        }

        return json;
    }






}
