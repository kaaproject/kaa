package org.kaaproject.kaa.server.common.core.plugin.generator.common.entity;

import java.util.Arrays;
import java.util.Set;

public interface ModifiableGeneratorEntity extends GeneratorEntity {

    Set<String> getModifiers();

    default String formatModifiers(Set<String> modifiers) {
        System.out.println("form");
        StringBuilder buffer = new StringBuilder();
        Arrays.asList(modifiers).stream().forEach(modifier -> {
            buffer.append(buffer.length() == 0 ? "" : " ").append(modifier);
        });
        return buffer.toString();
    }
}
