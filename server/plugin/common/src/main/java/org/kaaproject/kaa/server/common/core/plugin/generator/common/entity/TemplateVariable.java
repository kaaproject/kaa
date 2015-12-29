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

/**
 * A structural part of a source code file.
 *
 * @author Bohdan Khablenko
 *
 * @since v1.0.0
 */
public enum TemplateVariable {

    CONSTANTS("${constants}"),
    FIELDS("${fields}"),
    IMPORT_STATEMENTS("${importStatements}"),
    METHODS("${methods}"),
    METHOD_SIGNATURES("${methodSignatures}"),
    NAME("${name}"),
    NAMESPACE("${namespace}"),

    INTERFACE_CLASS("${interfaceClass}"),
    PARENT_CLASS("${parentClass}");

    private String body;

    private TemplateVariable(String body) {
        this.body = body;
    }

    public String getBody() {
        return this.body;
    }

    @Override
    public String toString() {
        return getBody();
    }
}
