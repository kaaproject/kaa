package org.kaaproject.kaa.server.common.core.plugin.generator.common.entity;

import java.util.Map;

public interface TemplatableGeneratorEntity extends GeneratorEntity {

    default String insertValues(String template, Map<String, String> values) {
        StringBuilder buffer = new StringBuilder(template);
        values.keySet().stream().forEach(key -> {
            buffer.replace(buffer.indexOf(key), buffer.indexOf(key) + key.length(), values.get(key));
        });
        return buffer.toString();
    }
}
