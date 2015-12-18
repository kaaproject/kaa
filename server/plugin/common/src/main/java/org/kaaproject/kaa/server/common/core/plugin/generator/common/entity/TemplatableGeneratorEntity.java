package org.kaaproject.kaa.server.common.core.plugin.generator.common.entity;

import java.util.Map;

public interface TemplatableGeneratorEntity extends GeneratorEntity {

    default String insertValues(String template, Map<String, String> values) {
        for (String key : values.keySet()) {
            template = template.replace(key, values.get(key));
        }
        return template;
    }
}
