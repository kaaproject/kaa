package org.kaaproject.kaa.server.common.core.plugin.generator.java.entity;

import org.kaaproject.kaa.server.common.core.plugin.generator.common.entity.Method;

public class JavaMethod implements Method {

    private JavaMethodSignature signature;
    private String body;

    public JavaMethod(String name, String returnType, String[] paramTypes, String body) {
        this.signature = new JavaMethodSignature(name, returnType, paramTypes) {
            @Override
            public boolean requiresTermination() {
                return false;
            }
        };
        this.body = body;
    }

    @Override
    public String getBody() {
        return this.signature.toString() + " {\n" + this.body + "\n}\n";
    }

    @Override
    public String toString() {
        return this.getBody();
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
        JavaMethod other = (JavaMethod) obj;
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
