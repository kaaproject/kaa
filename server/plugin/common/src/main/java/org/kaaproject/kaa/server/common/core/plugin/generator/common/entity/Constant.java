package org.kaaproject.kaa.server.common.core.plugin.generator.common.entity;

public class Constant {

    private String name;
    private String type;
    private String value;

    public Constant(String name, String type, String value) {
        this.name = name;
        this.type = type;
        this.value = value;
    }

    private static final String TEMPLATE = "static final %s %s = %s";

    @Override
    public String toString() {
        return String.format(TEMPLATE, this.type, this.name, this.value);
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
        Constant other = (Constant) obj;
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
