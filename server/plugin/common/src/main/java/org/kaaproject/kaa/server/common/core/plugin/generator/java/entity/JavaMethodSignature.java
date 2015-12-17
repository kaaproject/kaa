package org.kaaproject.kaa.server.common.core.plugin.generator.java.entity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.kaaproject.kaa.server.common.core.plugin.generator.common.entity.MethodSignature;

public class JavaMethodSignature implements MethodSignature {

    private static final String DEFAULT = "    %s %s(%s);";

    private final String template;
    private String name;
    private String returnType;
    private List<String> paramTypes = new ArrayList<>();

    public JavaMethodSignature(String name, String returnType, String... paramTypes) {
        this(DEFAULT, name, returnType, paramTypes);
    }

    public JavaMethodSignature(String template, String name, String returnType, String... paramTypes) {
        this.template = template;
        this.name = name;
        this.returnType = (returnType == null || returnType.isEmpty()) ? "void" : returnType;
        if (paramTypes != null) {
            this.paramTypes.addAll(Arrays.asList(paramTypes));
        }
    }

    @Override
    public String getBody() {
        StringBuilder buffer = new StringBuilder();
        String delim = "";
        for (int i = 0; i < paramTypes.size(); i++) {
            String paramType = paramTypes.get(i);
            if (paramType != null && !paramType.isEmpty()) {
                buffer.append(delim).append(paramType).append(" p").append(i + 1);
                delim = ", ";
            }
        }
        return String.format(template, returnType, name, buffer.toString());
    }

    @Override
    public String toString() {
        return getBody();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((paramTypes == null) ? 0 : paramTypes.hashCode());
        result = prime * result + ((returnType == null) ? 0 : returnType.hashCode());
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
        JavaMethodSignature other = (JavaMethodSignature) obj;
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (paramTypes == null) {
            if (other.paramTypes != null) {
                return false;
            }
        } else if (!paramTypes.equals(other.paramTypes)) {
            return false;
        }
        if (returnType == null) {
            if (other.returnType != null) {
                return false;
            }
        } else if (!returnType.equals(other.returnType)) {
            return false;
        }
        return true;
    }
}
