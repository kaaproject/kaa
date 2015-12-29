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
 * A statement like <code>import</code> in Java or <code>include</code>
 * preprocessor directive in C/C++ to indicates that a source code file depends
 * on something external.
 *
 * @author Andrew Shvayka
 * @author Bohdan Khablenko
 *
 * @since v1.0.0
 */
public interface ImportStatement extends GenericEntity {

    @Override
    default TemplateVariable getTemplateVariable() {
        return TemplateVariable.IMPORT_STATEMENTS;
    }
}
