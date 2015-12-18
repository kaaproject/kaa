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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kaaproject.kaa.server.common.core.plugin.def.SdkApiFile;
import org.kaaproject.kaa.server.common.core.plugin.generator.common.entity.GeneratorEntity;
import org.kaaproject.kaa.server.common.core.plugin.generator.common.entity.SimpleGeneratorEntity;
import org.kaaproject.kaa.server.common.core.plugin.generator.common.entity.TemplateVariable;

public abstract class PluginBuilderCore {

    private final String name;
    private final String namespace;
    private final String template;
    private final Map<TemplateVariable, List<GeneratorEntity>> values = new HashMap<>();

    public PluginBuilderCore(String name, String namespace, String template) {
        this.name = name;
        this.namespace = namespace;
        this.template = template;
        this.addEntity(new SimpleGeneratorEntity(TemplateVariable.NAME, name));
        this.addEntity(new SimpleGeneratorEntity(TemplateVariable.NAMESPACE, namespace));
    }

    public String getName() {
        return this.name;
    }

    public String getNamespace() {
        return this.namespace;
    }

    public void addEntity(GeneratorEntity entity) {
        List<GeneratorEntity> entityList = this.values.get(entity.getTemplateVariable());
        if (entityList == null) {
            entityList = new ArrayList<>();
            this.values.put(entity.getTemplateVariable(), entityList);
        }
        entityList.add(entity);
    }

    protected String insertValues() {

        String template = this.template;
        for (TemplateVariable templateVariable : this.values.keySet()) {
            StringBuilder buffer = new StringBuilder();
            for (GeneratorEntity entity : this.values.get(templateVariable)) {
                buffer.append(entity.getBody());
                if (entity.requiresTermination()) {
                    buffer.append(";");
                }
                if (entity.insertLineSeparator()) {
                    buffer.append(System.lineSeparator());
                }
            }
            template = template.replace(templateVariable.getBody(), buffer.toString());
        }

        // Remove unused template variable placeholders
        for (TemplateVariable templateVariable : TemplateVariable.values()) {
            template = template.replace(templateVariable.getBody(), "");
        }

        return template;
    }

    protected SdkApiFile build() {
        String fileName = this.getName() + ".java";
        byte[] fileData = this.insertValues().getBytes();
        return new SdkApiFile(fileName, fileData);
    }

    protected static String readFileAsString(String fileName) {
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
