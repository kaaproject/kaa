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

package org.kaaproject.kaa.server.common.core.plugin.generator.java;

import java.util.Map;

import org.kaaproject.kaa.server.common.core.plugin.def.SdkApiFile;
import org.kaaproject.kaa.server.common.core.plugin.generator.common.PluginBuilderCore;
import org.kaaproject.kaa.server.common.core.plugin.generator.common.PluginInterfaceBuilder;
import org.kaaproject.kaa.server.common.core.plugin.generator.common.entity.GenericEntity;
import org.kaaproject.kaa.server.common.core.plugin.generator.java.entity.JavaConstant;
import org.kaaproject.kaa.server.common.core.plugin.generator.java.entity.JavaImportStatement;
import org.kaaproject.kaa.server.common.core.plugin.generator.java.entity.JavaMethodSignature;

/**
 * @author Andrew Shvayka
 * @author Bohdan Khablenko
 *
 * @since v1.0.0
 */
public class JavaPluginInterfaceBuilder extends PluginBuilderCore implements PluginInterfaceBuilder {

    private static final String DEFAULT_TEMPLATE_FILE = "templates/java/interface.template";

    public JavaPluginInterfaceBuilder(String name, String namespace) {
        super(name, namespace, PluginBuilderCore.readFileAsString(DEFAULT_TEMPLATE_FILE));
    }

    public JavaPluginInterfaceBuilder(String name, String namespace, String template) {
        super(name, namespace, template);
    }

    @Override
    public PluginInterfaceBuilder withEntity(GenericEntity entity) {
        this.addEntity(entity);
        return this;
    }

    @Override
    public PluginInterfaceBuilder withImportStatement(String body) {
        this.addEntity(new JavaImportStatement(body));
        return this;
    }

    @Override
    public PluginInterfaceBuilder withConstant(String name, String type, String value) {
        this.addEntity(new JavaConstant(name, type, value, new String[] {}));
        return this;
    }

    @Override
    public PluginInterfaceBuilder withConstant(String name, String type, String value, String... modifiers) {
        this.addEntity(new JavaConstant(name, type, value, modifiers));
        return this;
    }

    @Override
    public PluginInterfaceBuilder withMethodSignature(String name, String returnType, String[] paramTypes, String[] modifiers) {
        this.addEntity(new JavaMethodSignature(name, returnType, paramTypes, modifiers));
        return this;
    }

    @Override
    public PluginInterfaceBuilder withMethodSignature(String name, String returnType, Map<String, String> params, String[] modifiers) {
        this.addEntity(new JavaMethodSignature(name, returnType, params, modifiers));
        return this;
    }

    @Override
    public SdkApiFile build() {
        return super.build();
    }

    // TODO: Used for testing purposes, remove when unnecessary
    public static void main(String[] args) {
        PluginInterfaceBuilder o = new JavaPluginInterfaceBuilder("MessagingPluginAPI", "org.kaaproject.kaa.plugin.messaging")
                .withImportStatement("java.util.Map").withImportStatement("java.lang.*").withConstant("ANOTHER_TEST", "String", "\"Hello, World\"")
                .withConstant("TEST", "String", "\"Hello, World\"").withConstant("TEST_x", "String", "\"Hello, World\"");
        System.out.println(new String(o.build().getFileData()));
    }
}
