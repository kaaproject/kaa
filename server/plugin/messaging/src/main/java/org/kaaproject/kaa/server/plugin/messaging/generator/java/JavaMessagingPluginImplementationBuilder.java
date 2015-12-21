package org.kaaproject.kaa.server.plugin.messaging.generator.java;

import java.util.LinkedHashMap;
import java.util.Map;

import org.kaaproject.kaa.server.common.core.plugin.generator.common.PluginBuilderCore;
import org.kaaproject.kaa.server.common.core.plugin.generator.common.PluginImplementationBuilder;
import org.kaaproject.kaa.server.common.core.plugin.generator.common.entity.SimpleGeneratorEntity;
import org.kaaproject.kaa.server.common.core.plugin.generator.common.entity.TemplateVariable;
import org.kaaproject.kaa.server.common.core.plugin.generator.java.JavaPluginImplementationBuilder;
import org.kaaproject.kaa.server.common.core.plugin.generator.java.entity.JavaConstant;
import org.kaaproject.kaa.server.common.core.plugin.generator.java.entity.JavaField;
import org.kaaproject.kaa.server.common.core.plugin.generator.java.entity.JavaMethod;
import org.kaaproject.kaa.server.plugin.messaging.generator.common.MessagingPluginImplementationBuilder;

public class JavaMessagingPluginImplementationBuilder extends JavaPluginImplementationBuilder implements MessagingPluginImplementationBuilder {

    public JavaMessagingPluginImplementationBuilder(String name, String namespace) {
        super(name, namespace);
    }

    public JavaMessagingPluginImplementationBuilder(String name, String namespace, String template) {
        super(name, namespace, template);
    }

    @Override
    public MessagingPluginImplementationBuilder withMethodConstant(String method, String[] paramTypes, int id) {

        StringBuilder body = new StringBuilder();
        body.append("/** Auto-generated constant for method {@link #").append(method).append("(");
        for (int i = 0; i < paramTypes.length; i++) {
            if (paramTypes[i] != null && !paramTypes[i].isEmpty()) {
                body.append(i > 0 ? ", " : "").append(paramTypes[i]);
            }
        }
        body.append(")}\n");
        body.append(new JavaConstant("METHOD_" + Integer.toString(id) + "_ID", "short", Integer.toString(id), new String[] { "private", "final" }));

        this.addEntity(new SimpleGeneratorEntity(TemplateVariable.CONSTANTS, body.toString(), true, 1));
        return this;
    }

    @Override
    public MessagingPluginImplementationBuilder withMethodListener(String name, String type) {
        this.addEntity(new JavaField(name, type, new String[] { "private", "volatile" }));
        this.addEntity(JavaMethod.setter(name, type));
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
    public MessagingPluginImplementationBuilder withEntityMessageHandlersMapping(Map<String, Integer> handlersMapping) {

        Map<String, String> params = new LinkedHashMap<>();
        params.put("msg", "PayloadMessage");

        String template = "\nif (msg.getMethodId() == METHOD_%d_ID) { handleMethod%dMsg(msg); }";

        StringBuilder buffer = new StringBuilder();
        handlersMapping.forEach((method, id) -> buffer.append(String.format(template, id, id)));

        this.addEntity(new JavaMethod("handleEntityMsg", null, params, new String[] { "protected" }, buffer.toString(), null));
        return this;
    }

    @Override
    public MessagingPluginImplementationBuilder withVoidMessageHandlersMapping(Map<String, Integer> handlersMapping) {

        Map<String, String> params = new LinkedHashMap<>();
        params.put("msg", "PayloadMessage");

        String template = "\nif (msg.getMethodId() == METHOD_%d_ID) { handleMethod%dVoid(msg.getUid()); }";

        StringBuilder buffer = new StringBuilder();
        handlersMapping.forEach((method, id) -> buffer.append(String.format(template, id, id)));

        this.addEntity(new JavaMethod("handleVoidMsg", null, params, new String[] { "protected" }, buffer.toString(), null));
        return this;
    }

    @Override
    public MessagingPluginImplementationBuilder withMessageHandler(String body, Map<String, String> values) {
        this.addEntity(new SimpleGeneratorEntity(TemplateVariable.METHODS, body, values));
        return this;
    }

    public static void main(String[] args) {
        Object builder = new JavaMessagingPluginImplementationBuilder("MessagingPlugin", "org.kaaproject.kaa.plugin.messaging");
        builder = ((JavaMessagingPluginImplementationBuilder) builder).withMethodConstant("setMethodListener", new String[] {}, 1);
        builder = ((JavaMessagingPluginImplementationBuilder) builder).withMethodListener("listener", "MethodListener");
        builder = ((JavaMessagingPluginImplementationBuilder) builder).withEntityConverter("entity3Converter", "ClassB");

        Map<String, Integer> handlersMapping = new LinkedHashMap<>();
        handlersMapping.put("handleMethod1", 1);
        handlersMapping.put("handleMethod2", 2);

        builder = ((JavaMessagingPluginImplementationBuilder) builder).withEntityMessageHandlersMapping(handlersMapping);
        builder = ((JavaMessagingPluginImplementationBuilder) builder).withVoidMessageHandlersMapping(handlersMapping);

        System.out.println(new String(((PluginImplementationBuilder) builder).build().getFileData()));
    }
}
