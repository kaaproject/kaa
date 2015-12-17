package org.kaaproject.kaa.server.common.core.plugin.generator.java.entity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.kaaproject.kaa.server.common.core.plugin.generator.common.entity.MethodSignature;

public class JavaMethodSignature implements MethodSignature {

    private static final String DEFAULT_TEMPLATE = "public %s %s(%s)";

    private final String name;
    private final String returnType;
    private final List<String> paramTypes = new ArrayList<>();
    private final Set<String> modifiers = new LinkedHashSet<>();
    private final String template;

    public JavaMethodSignature(String name, String returnType, String[] paramTypes, String[] modifiers) {
        this(name, returnType, paramTypes, modifiers, DEFAULT_TEMPLATE);
    }

    public JavaMethodSignature(String name, String returnType, String[] paramTypes, String[] modifiers, String template) {
        this.name = name;
        this.returnType = (returnType == null || returnType.isEmpty()) ? "void" : returnType;
        if (paramTypes != null) {
            this.paramTypes.addAll(Arrays.asList(paramTypes));
        }
        if (modifiers != null) {
            this.modifiers.addAll(Arrays.asList(modifiers));
        }
        this.template = template;
    }

    @Override
    public String getBody() {
        StringBuilder buffer = new StringBuilder();
        String delim = "";
        for (int i = 0; i < this.paramTypes.size(); i++) {
            String paramType = this.paramTypes.get(i);
            if (paramType != null && !paramType.isEmpty()) {
                buffer.append(delim).append(paramType).append(" p").append(i + 1);
                delim = ", ";
            }
        }
        return String.format(this.template, this.returnType, this.name, buffer.toString()).trim();
    }

    @Override
    public String toString() {
        return this.getBody();
    }

    @Override
    public boolean requiresTermination() {
        return true;
    }

    @Override
    public boolean includeLineSeparator() {
        return true;
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
        result = prime * result + ((paramTypes == null) ? 0 : paramTypes.hashCode());
        result = prime * result + ((returnType == null) ? 0 : returnType.hashCode());
        result = prime * result + ((template == null) ? 0 : template.hashCode());
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
        if (template == null) {
            if (other.template != null) {
                return false;
            }
        } else if (!template.equals(other.template)) {
            return false;
        }
        return true;
    }
}
