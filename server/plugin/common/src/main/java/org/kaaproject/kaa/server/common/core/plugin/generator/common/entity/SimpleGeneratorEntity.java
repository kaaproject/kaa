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

public class SimpleGeneratorEntity implements GeneratorEntity {

    private final TemplateVariable templateVariable;
    private final String body;

    public SimpleGeneratorEntity(TemplateVariable templateVariable, String body) {
        super();
        this.templateVariable = templateVariable;
        this.body = body;
    }

    @Override
    public TemplateVariable getTemplateVariable() {
        return templateVariable;
    }

    @Override
    public String getBody() {
        return this.body;
    }

    @Override
    public boolean requiresTermination() {
        return false;
    }

    @Override
    public boolean includeLineSeparator() {
        return false;
    }
}
