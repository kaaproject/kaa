package org.kaaproject.kaa.server.common.core.plugin.generator.common.entity;

import java.util.Map;

public interface TemplatableGeneratorEntity extends GeneratorEntity {

    default String insertValues(String template, Map<String, String> values) {
        if (values != null) {
            for (String key : values.keySet()) {
                String replacement = (values.get(key) != null) ? values.get(key) : "<UNDEFINED>";
                template = template.replace(key, replacement);
            }
        }
        return template;
    }
}
