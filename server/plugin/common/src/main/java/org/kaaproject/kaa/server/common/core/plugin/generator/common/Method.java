package org.kaaproject.kaa.server.common.core.plugin.generator.common;

import java.util.List;

public class Method {

    private MethodSignature signature;
    private String body;

    public Method(String name, String returnType, List<String> paramTypes, String body) {
        this.signature = new MethodSignature(name, returnType, paramTypes.toArray(new String[paramTypes.size()]));
        this.body = body;
    }

    private static final String TEMPLATE = "%s {\n%s\n}";

    @Override
    public String toString() {
        return String.format(TEMPLATE, this.signature.toString(), this.body);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((body == null) ? 0 : body.hashCode());
        result = prime * result + ((signature == null) ? 0 : signature.hashCode());
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
        Method other = (Method) obj;
        if (body == null) {
            if (other.body != null) {
                return false;
            }
        } else if (!body.equals(other.body)) {
            return false;
        }
        if (signature == null) {
            if (other.signature != null) {
                return false;
            }
        } else if (!signature.equals(other.signature)) {
            return false;
        }
        return true;
    }
}
