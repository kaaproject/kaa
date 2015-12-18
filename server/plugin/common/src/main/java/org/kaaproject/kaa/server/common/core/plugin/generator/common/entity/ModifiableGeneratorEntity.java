package org.kaaproject.kaa.server.common.core.plugin.generator.common.entity;

import java.util.Set;

public interface ModifiableGeneratorEntity extends GeneratorEntity {

    Set<String> getModifiers();

    default String formatModifiers(Set<String> modifiers) {
        StringBuilder buffer = new StringBuilder();
        modifiers.forEach(modifier -> buffer.append(modifier).append(" "));
        return buffer.toString().trim();
    }
}
