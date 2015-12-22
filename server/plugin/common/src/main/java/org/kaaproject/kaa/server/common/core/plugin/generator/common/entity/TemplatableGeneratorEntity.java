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

import java.util.Map;

/**
 * A source code entity the body of which is derived from a template.
 *
 * @author Bohdan Khablenko
 *
 * @since v1.0.0
 */
public interface TemplatableGeneratorEntity extends GenericEntity {

    /**
     * Replaces the keys in the given template with appropriate values.
     *
     * @param template A template that contains placeholders for replacement
     * @param values Values to insert into the given template
     *
     * @return The template with the given values inserted
     */
    default String insertValues(String template, Map<String, String> values) {
        if (values != null) {
            for (String key : values.keySet()) {
                String replacement = (values.get(key) != null) ? values.get(key) : "";
                template = template.replace(key, replacement);
            }
        }
        return template;
    }
}
