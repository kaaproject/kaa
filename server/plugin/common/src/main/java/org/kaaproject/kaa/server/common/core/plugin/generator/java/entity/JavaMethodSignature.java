/*
 * Copyright 2014-2015 CyberVision, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kaaproject.kaa.server.common.core.plugin.generator.java.entity;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import org.kaaproject.kaa.server.common.core.plugin.generator.common.entity.MethodSignature;

/**
 * @author Bohdan Khablenko
 *
 * @since v1.0.0
 */
public class JavaMethodSignature implements MethodSignature {

    // <modifiers> <type> <name>(<parameters>)
    protected static final String DEFAULT_TEMPLATE = "%s %s %s(%s)";

    protected final String name;
    protected final String returnType;
    protected final Map<String, String> params = new LinkedHashMap<>();
    protected final Set<String> modifiers = new LinkedHashSet<>();

    /**
     * This object is used to generate parameter names.
     */
    private final Supplier<String> generator = new Supplier<String>() {

        private int index = 0;

        @Override
        public String get() {
            return "p" + Integer.toString(++index);
        }
    };

    public JavaMethodSignature(String name, String returnType, Map<String, String> params, String[] modifiers) {
        this.name = name;
        this.returnType = (returnType == null || returnType.isEmpty()) ? "void" : returnType;
        if (params != null) {
            params.forEach((paramName, paramType) -> {
                if (paramType != null && !paramType.isEmpty()) {
                    this.params.put(paramName, paramType);
                }
            });
        }
        if (modifiers != null) {
            this.modifiers.addAll(Arrays.asList(modifiers));
        }
    }

    public JavaMethodSignature(String name, String returnType, String[] paramTypes, String[] modifiers) {
        this(name, returnType, new LinkedHashMap<>(), modifiers);
        if (paramTypes != null) {
            Arrays.asList(paramTypes).forEach(paramType -> {
                if (paramType != null && !paramType.isEmpty()) {
                    this.params.put(this.generator.get(), paramType);
                }
            });
        }
    }

    @Override
    public String getBody() {

        StringBuilder buffer = new StringBuilder();
        this.params.forEach((paramName, paramType) -> {
            buffer.append(paramType).append(" ").append(paramName);
        });

        return String.format(DEFAULT_TEMPLATE, this.formatModifiers(modifiers), this.returnType, this.name, buffer.toString()).trim();
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
    public int emptyLines() {
        return 1;
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
        result = prime * result + ((params == null) ? 0 : params.hashCode());
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
        if (params == null) {
            if (other.params != null) {
                return false;
            }
        } else if (!params.equals(other.params)) {
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
