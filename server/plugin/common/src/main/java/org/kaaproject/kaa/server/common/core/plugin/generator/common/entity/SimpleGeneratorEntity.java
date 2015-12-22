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

package org.kaaproject.kaa.server.common.core.plugin.generator.common.entity;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A custom source code entity.
 *
 * @author Andrew Shvayka
 * @author Bohdan Khablenko
 *
 * @since v1.0.0
 */
public class SimpleGeneratorEntity implements TemplatableGeneratorEntity {

    /**
     * The template variable this entity is a part of.
     */
    private final TemplateVariable templateVariable;

    /**
     * The body of this entity.
     */
    private final String body;

    /**
     * The values to replace placeholders in the entitity body.
     */
    private Map<String, String> values = new LinkedHashMap<>();

    private boolean requiresTermination;
    private int emptyLines;

    public SimpleGeneratorEntity(TemplateVariable templateVariable, String body) {
        this(templateVariable, body, new LinkedHashMap<>(), false, 0);
    }

    public SimpleGeneratorEntity(TemplateVariable templateVariable, String body, boolean requiresTermination, int emptyLines) {
        this(templateVariable, body, new LinkedHashMap<>(), requiresTermination, emptyLines);
    }

    public SimpleGeneratorEntity(TemplateVariable templateVariable, String body, Map<String, String> values) {
        this(templateVariable, body, values, false, 0);
    }

    public SimpleGeneratorEntity(TemplateVariable templateVariable, String body, Map<String, String> values, boolean requiresTermination, int emptyLines) {
        this.templateVariable = templateVariable;
        this.body = body;
        if (values != null) {
            values.forEach((key, value) -> this.values.put(key, value));
        }
        this.requiresTermination = requiresTermination;
        this.emptyLines = emptyLines;
    }

    @Override
    public TemplateVariable getTemplateVariable() {
        return this.templateVariable;
    }

    @Override
    public String getBody() {
        return this.insertValues(this.body, this.values).trim();
    }

    @Override
    public String toString() {
        return this.getBody();
    }

    @Override
    public boolean requiresTermination() {
        return this.requiresTermination;
    }

    @Override
    public int emptyLines() {
        return this.emptyLines;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((body == null) ? 0 : body.hashCode());
        result = prime * result + emptyLines;
        result = prime * result + (requiresTermination ? 1231 : 1237);
        result = prime * result + ((templateVariable == null) ? 0 : templateVariable.hashCode());
        result = prime * result + ((values == null) ? 0 : values.hashCode());
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
        SimpleGeneratorEntity other = (SimpleGeneratorEntity) obj;
        if (body == null) {
            if (other.body != null) {
                return false;
            }
        } else if (!body.equals(other.body)) {
            return false;
        }
        if (emptyLines != other.emptyLines) {
            return false;
        }
        if (requiresTermination != other.requiresTermination) {
            return false;
        }
        if (templateVariable != other.templateVariable) {
            return false;
        }
        if (values == null) {
            if (other.values != null) {
                return false;
            }
        } else if (!values.equals(other.values)) {
            return false;
        }
        return true;
    }
}
