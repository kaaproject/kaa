package org.kaaproject.kaa.server.common.core.plugin.generator.java.entity;

import org.kaaproject.kaa.server.common.core.plugin.generator.common.entity.Constant;

public class JavaConstant implements Constant {

    private static final String DEFAULT_TEMPLATE = "    static final %s %s = %s";

    private final String name;
    private final String type;
    private final String value;
    private final String template;

    public JavaConstant(String name, String type, String value) {
        this(name, type, value, DEFAULT_TEMPLATE);
    }

    public JavaConstant(String name, String type, String value, String template) {
        this.name = name;
        this.type = type;
        this.value = value;
        this.template = template;
    }

    @Override
    public String getBody() {
        return String.format(this.template, this.type, this.name, this.value);
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
        result = prime * result + ((value == null) ? 0 : value.hashCode());
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
        JavaConstant other = (JavaConstant) obj;
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
        if (value == null) {
            if (other.value != null) {
                return false;
            }
        } else if (!value.equals(other.value)) {
            return false;
        }
        return true;
    }
}
