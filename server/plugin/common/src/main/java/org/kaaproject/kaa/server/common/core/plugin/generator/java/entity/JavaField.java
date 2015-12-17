package org.kaaproject.kaa.server.common.core.plugin.generator.java.entity;

import org.kaaproject.kaa.server.common.core.plugin.generator.common.entity.Field;

public class JavaField implements Field {

    private static final String DEFAULT_TEMPLATE = "    private %s %s";

    private final String name;
    private final String type;
    private final String template;

    public JavaField(String name, String type) {
        this(name, type, DEFAULT_TEMPLATE);
    }

    public JavaField(String name, String type, String template) {
        this.name = name;
        this.type = type;
        this.template = template;
    }

    @Override
    public String getBody() {
        return String.format(this.template, this.type, this.name);
    }

    @Override
    public String toString() {
        return this.getBody();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
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
