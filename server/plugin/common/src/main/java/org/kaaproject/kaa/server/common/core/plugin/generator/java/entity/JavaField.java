package org.kaaproject.kaa.server.common.core.plugin.generator.java.entity;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import org.kaaproject.kaa.server.common.core.plugin.generator.common.entity.Field;

public class JavaField implements Field {

    private static final String DEFAULT_TEMPLATE = "%s %s %s";

    private final String name;
    private final String type;
    private final Set<String> modifiers = new LinkedHashSet<>();

    public JavaField(String name, String type) {
        this(name, type, DEFAULT_TEMPLATE);
    }

    public JavaField(String name, String type, String... modifiers) {
        this.name = name;
        this.type = type;
        if (modifiers != null) {
            this.modifiers.addAll(Arrays.asList(modifiers));
        }
    }

    @Override
    public String getBody() {
        return String.format(DEFAULT_TEMPLATE, this.formatModifiers(this.modifiers), this.type, this.name).trim();
    }

    @Override
    public String toString() {
        return this.getBody();
    }

    @Override
    public Set<String> getModifiers() {
        return this.modifiers;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((modifiers == null) ? 0 : modifiers.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        JavaField other = (JavaField) obj;
        if (modifiers == null) {
            if (other.modifiers != null) {
                return false;
            }
        } else if (!modifiers.equals(other.modifiers)) {
            return false;
        }
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (type == null) {
            if (other.type != null) {
                return false;
            }
        } else if (!type.equals(other.type)) {
            return false;
        }
        return true;
    }
}
