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

import java.util.HashSet;
import java.util.Set;

//public enum TemplateVariable {
//
//    CONSTANTS("${constants}"),
//    FIELDS("${fields}"),
//    IMPORT_STATEMENTS("${importStatements}"),
//    METHODS("${methods}"),
//    METHOD_SIGNATURES("${methodSignatures}"),
//    NAME("${name}"),
//    NAMESPACE("${namespace}");
//
//    private String body;
//
//    private TemplateVariable(String body) {
//        this.body = body;
//    }
//
//    public String getBody() {
//        return this.body;
//    }
//
//    @Override
//    public String toString() {
//        return getBody();
//    }
//}

public class TemplateVariable {

    private static final Set<String> values = new HashSet<>();

    public static Set<String> values() {
        return values;
    }

    public static final String CONSTANTS = "${constants}";
    public static final String FIELDS = "${fields}";
    public static final String IMPORT_STATEMENTS = "${importStatements}";
    public static final String METHODS = "${methods}";
    public static final String METHOD_SIGNATURES = "${methodSignatures}";
    public static final String NAME = "${name}";
    public static final String NAMESPACE = "${namespace}";

    static {
        values.add(CONSTANTS);
        values.add(FIELDS);
        values.add(IMPORT_STATEMENTS);
        values.add(METHODS);
        values.add(METHOD_SIGNATURES);
        values.add(NAME);
        values.add(NAMESPACE);
    }
}
