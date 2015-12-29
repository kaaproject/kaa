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

package org.kaaproject.kaa.server.plugin.messaging.generator.java;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.kaaproject.kaa.server.common.core.plugin.generator.common.PluginBuilderCore;
import org.kaaproject.kaa.server.common.core.plugin.generator.common.entity.SimpleGeneratorEntity;
import org.kaaproject.kaa.server.common.core.plugin.generator.common.entity.TemplateVariable;
import org.kaaproject.kaa.server.common.core.plugin.generator.java.JavaPluginImplementationBuilder;
import org.kaaproject.kaa.server.common.core.plugin.generator.java.entity.JavaConstant;
import org.kaaproject.kaa.server.common.core.plugin.generator.java.entity.JavaField;
import org.kaaproject.kaa.server.common.core.plugin.generator.java.entity.JavaMethod;
import org.kaaproject.kaa.server.plugin.messaging.generator.common.MessagingPluginImplementationBuilder;

public class JavaMessagingPluginImplementationBuilder extends JavaPluginImplementationBuilder implements MessagingPluginImplementationBuilder {

    public JavaMessagingPluginImplementationBuilder(String name, String namespace, String parentClass, String interfaceClass) {
        super(name, namespace);
        this.addEntity(new SimpleGeneratorEntity(TemplateVariable.PARENT_CLASS, parentClass));
        this.addEntity(new SimpleGeneratorEntity(TemplateVariable.INTERFACE_CLASS, interfaceClass));
    }

    public JavaMessagingPluginImplementationBuilder(String name, String namespace, String template, String parentClass, String interfaceClass) {
        super(name, namespace, template);
        this.addEntity(new SimpleGeneratorEntity(TemplateVariable.PARENT_CLASS, parentClass));
        this.addEntity(new SimpleGeneratorEntity(TemplateVariable.INTERFACE_CLASS, interfaceClass));
    }

    @Override
    public MessagingPluginImplementationBuilder withMethodConstant(String method, String[] paramTypes, int id) {

        StringBuilder body = new StringBuilder();

        // A comment that points to the method this constant references
        body.append("/** Auto-generated constant for method {@link #").append(method).append("(");
        for (int i = 0; i < paramTypes.length; i++) {
            if (paramTypes[i] != null && !paramTypes[i].isEmpty()) {
                body.append(i > 0 ? ", " : "").append(paramTypes[i]);
            }
        }
        body.append(")} */\n");

        // The constant itself
        body.append(new JavaConstant("METHOD_" + Integer.toString(id) + "_ID", "short", Integer.toString(id), new String[] { "private", "final" }));

        this.addEntity(new SimpleGeneratorEntity(TemplateVariable.CONSTANTS, body.toString(), true, 1));
        return this;
    }

    @Override
    public MessagingPluginImplementationBuilder withMethodListener(String methodName, String fieldName, String fieldType) {

        String template = "public void %s(%s %s) { this.%s = %s; }";
        String body = String.format(template, methodName, fieldType, fieldName, fieldName, fieldName);

        this.addEntity(new JavaField(fieldName, fieldType, new String[] { "private", "volatile" }));
        this.addEntity(new SimpleGeneratorEntity(TemplateVariable.METHODS, body, false, 1));
        return this;
    }

    @Override
    public MessagingPluginImplementationBuilder withEntityConverter(String name, String type) {

        String body = PluginBuilderCore.readFileAsString("templates/java/entityConverter.template");
        body = body.replace("${name}", name);
        body = body.replace("${type}", type);

        this.addEntity(new SimpleGeneratorEntity(TemplateVariable.FIELDS, body, false, 1));
        return this;
    }

    @Override
    public MessagingPluginImplementationBuilder withEntityMessageHandlersMapping(Set<Integer> handlersMapping) {

        // Method parameters
        Map<String, String> params = new LinkedHashMap<>();
        params.put("msg", "PayloadMessage");

        String controlStatement = "\nif (msg.getMethodId() == METHOD_%d_ID) { handleMethod%dMsg(msg); }";

        // The method body
        StringBuilder buffer = new StringBuilder();
        handlersMapping.forEach((id) -> buffer.append(String.format(controlStatement, id, id)));

        this.addEntity(new JavaMethod("handleEntityMsg", null, params, new String[] { "protected" }, buffer.toString(), null));
        return this;
    }

    @Override
    public MessagingPluginImplementationBuilder withVoidMessageHandlersMapping(Set<Integer> handlersMapping) {

        // Method parameters
        Map<String, String> params = new LinkedHashMap<>();
        params.put("msg", "Message");

        String controlStatement = "\nif (msg.getMethodId() == METHOD_%d_ID) { handleMethod%dVoid(msg.getUid()); }";

        // The method body
        StringBuilder buffer = new StringBuilder();
        handlersMapping.forEach((id) -> buffer.append(String.format(controlStatement, id, id)));

        this.addEntity(new JavaMethod("handleVoidMsg", null, params, new String[] { "protected" }, buffer.toString(), null));
        return this;
    }
}
