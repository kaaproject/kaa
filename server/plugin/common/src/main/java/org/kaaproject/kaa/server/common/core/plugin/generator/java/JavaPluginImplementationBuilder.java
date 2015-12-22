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
import org.kaaproject.kaa.server.common.core.plugin.generator.common.PluginImplementationBuilder;
import org.kaaproject.kaa.server.common.core.plugin.generator.common.entity.GenericEntity;
import org.kaaproject.kaa.server.common.core.plugin.generator.java.entity.JavaConstant;
import org.kaaproject.kaa.server.common.core.plugin.generator.java.entity.JavaField;
import org.kaaproject.kaa.server.common.core.plugin.generator.java.entity.JavaImportStatement;
import org.kaaproject.kaa.server.common.core.plugin.generator.java.entity.JavaMethod;

/**
 * @author Andrew Shvayka
 * @author Bohdan Khablenko
 *
 * @since v1.0.0
 */
public class JavaPluginImplementationBuilder extends PluginBuilderCore implements PluginImplementationBuilder {

    protected static final String DEFAULT_TEMPLATE_FILE = "templates/java/implementation.template";

    public JavaPluginImplementationBuilder(String name, String namespace) {
        this(name, namespace, PluginBuilderCore.readFileAsString(DEFAULT_TEMPLATE_FILE));
    }

    public JavaPluginImplementationBuilder(String name, String namespace, String template) {
        super(name, namespace, template);
    }

    @Override
    public JavaPluginImplementationBuilder withEntity(GenericEntity entity) {
        this.addEntity(entity);
        return this;
    }

    @Override
    public JavaPluginImplementationBuilder withImportStatement(String body) {
        this.addEntity(new JavaImportStatement(body));
        return this;
    }

    @Override
    public JavaPluginImplementationBuilder withConstant(String name, String type, String value) {
        return this.withConstant(name, type, value, new String[] {});
    }

    @Override
    public JavaPluginImplementationBuilder withConstant(String name, String type, String value, String... modifiers) {
        this.addEntity(new JavaConstant(name, type, value, modifiers));
        return this;
    }

    @Override
    public JavaPluginImplementationBuilder withProperty(String name, String type, String... modifiers) {
        this.addEntity(new JavaField(name, type, modifiers));
        this.addEntity(JavaMethod.getter(name, type));
        this.addEntity(JavaMethod.setter(name, type));
        return this;
    }

    @Override
    public JavaPluginImplementationBuilder withMethod(String name, String returnType, Map<String, String> params, String[] modifiers, String body,
            Map<String, String> values) {

        this.addEntity(new JavaMethod(name, returnType, params, modifiers, body, values));
        return this;
    }

    @Override
    public SdkApiFile build() {
        return super.build();
    }

    // TODO: Used for testing purposes, remove when unnecessary
    public static void main(String[] args) {
        PluginImplementationBuilder o = new JavaPluginImplementationBuilder("MessagingPlugin", "org.kaaproject.kaa.plugin.messaging").withConstant("CONSTANT",
                "Integer", null, new String[] { "volatile", "private" }).withImportStatement("java.util.Map");
        System.out.println(new String(o.withProperty("t", "int").build().getFileData()));
    }
}
