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
package org.kaaproject.kaa.server.common.core.plugin.generator.common;

import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.kaaproject.kaa.server.common.core.plugin.generator.common.entity.GeneratorEntity;
import org.kaaproject.kaa.server.common.core.plugin.generator.common.entity.SimpleGeneratorEntity;
import org.kaaproject.kaa.server.common.core.plugin.generator.common.entity.TemplateVariableType;

public abstract class PluginBuilderCore {

    public static final Charset UTF8 = Charset.forName("UTF-8"); 

    private final String name;
    private final String namespace;
    private final String template;
    private Map<TemplateVariableType, List<GeneratorEntity>> entities;

    public PluginBuilderCore(String template, String name, String namespace) {
        this.template = template;
        this.name = name;
        this.namespace = namespace;
        this.entities = new HashMap<>();
        addEntity(new SimpleGeneratorEntity(TemplateVariableType.NAME, name, false));
        addEntity(new SimpleGeneratorEntity(TemplateVariableType.NAMESPACE, namespace, false));
    }

    public String getName() {
        return name;
    }

    public String getNamespace() {
        return namespace;
    }

    public void addEntity(GeneratorEntity entity) {
        List<GeneratorEntity> entityList = entities.get(entity.getType());
        if (entityList == null) {
            entityList = new ArrayList<>();
            entities.put(entity.getType(), entityList);
        }
        entityList.add(entity);
    }

    protected String substituteAllEntities() {
        String template = this.template;
        for (Entry<TemplateVariableType, List<GeneratorEntity>> entry : entities.entrySet()) {
            TemplateVariableType type = entry.getKey();
            StringBuilder buffer = new StringBuilder();
            for (GeneratorEntity entity : entry.getValue()) {
                buffer = buffer.append(entity.getBody());
                if (entity.requireNewLineAtEnd()) {
                    buffer = buffer.append(System.lineSeparator());
                }
            }
            template = template.replace(type.getBody(), buffer.toString());
        }
        // cleanup unused templates;
        for (TemplateVariableType type : TemplateVariableType.values()) {
            template = template.replace(type.getBody(), "");
        }
        return template;
    }

    public static String readFileAsString(String fileName) {
        String fileContent = null;
        URL url = PluginBuilderCore.class.getClassLoader().getResource(fileName);
        if (url != null) {
            try {
                Path path = Paths.get(url.toURI());
                byte[] bytes = Files.readAllBytes(path);
                if (bytes != null) {
                    fileContent = new String(bytes);
                }
            } catch (Exception cause) {
                cause.printStackTrace();
            }
        }
        return fileContent;
    }
}
