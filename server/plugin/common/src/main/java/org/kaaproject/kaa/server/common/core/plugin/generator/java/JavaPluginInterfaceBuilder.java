package org.kaaproject.kaa.server.common.core.plugin.generator.java;

import static org.kaaproject.kaa.server.common.core.plugin.generator.common.PluginBuilder.TemplateVariable.CONSTANTS;
import static org.kaaproject.kaa.server.common.core.plugin.generator.common.PluginBuilder.TemplateVariable.IMPORT_STATEMENTS;
import static org.kaaproject.kaa.server.common.core.plugin.generator.common.PluginBuilder.TemplateVariable.METHOD_SIGNATURES;
import static org.kaaproject.kaa.server.common.core.plugin.generator.common.PluginBuilder.TemplateVariable.NAME;
import static org.kaaproject.kaa.server.common.core.plugin.generator.common.PluginBuilder.TemplateVariable.NAMESPACE;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.kaaproject.kaa.server.common.core.plugin.def.SdkApiFile;
import org.kaaproject.kaa.server.common.core.plugin.generator.common.Constant;
import org.kaaproject.kaa.server.common.core.plugin.generator.common.ImportStatement;
import org.kaaproject.kaa.server.common.core.plugin.generator.common.MethodSignature;
import org.kaaproject.kaa.server.common.core.plugin.generator.common.PluginBuilder.TemplateVariable;
import org.kaaproject.kaa.server.common.core.plugin.generator.common.PluginInterfaceBuilder;

public class JavaPluginInterfaceBuilder extends PluginBuilderBasis implements PluginInterfaceBuilder {

    private static final String DEFAULT_TEMPLATE_FILE = "templates/java/interface.template";

    private Set<MethodSignature> methodSignatures = new HashSet<>();

    public JavaPluginInterfaceBuilder(String name, String namespace) {
        super(name, namespace);
    }

    @Override
    public PluginInterfaceBuilder withImportStatement(String body) {
        this.importStatements.add(new ImportStatement(body));
        return this;
    }

    @Override
    public PluginInterfaceBuilder withConstant(String name, String type, String value) {
        this.constants.add(new Constant(name, type, value));
        return this;
    }

    @Override
    public PluginInterfaceBuilder withMethodSignature(String name, String returnType, String... paramTypes) {
        methodSignatures.add(new MethodSignature(name, returnType, paramTypes));
        return this;
    }

    @Override
    public SdkApiFile generateFile() {

        Map<TemplateVariable, String> values = new EnumMap<>(TemplateVariable.class);

        values.put(NAME, this.name);
        values.put(NAMESPACE, this.namespace);
        values.put(IMPORT_STATEMENTS, this.accumulateStatements(new StringBuilder(), this.importStatements).toString());
        values.put(CONSTANTS, this.accumulateStatements(new StringBuilder(), this.constants, 1).toString());
        values.put(METHOD_SIGNATURES, this.accumulateStatements(new StringBuilder(), this.methodSignatures, 1).toString());

        if (this.template == null || this.template.isEmpty()) {
            this.template = this.readFileAsString(DEFAULT_TEMPLATE_FILE);
        }

        String fileName = this.name + "PluginAPI.java";
        byte[] fileData = this.insertValues(this.template, values).getBytes();

        return new SdkApiFile(fileName, fileData);
    }

    public static void main(String[] args) {
        PluginInterfaceBuilder o = new JavaPluginBuilder().createInterface("Messaging", "org.kaaproject.kaa.plugin.messaging");
        o = o.withImportStatement("java.util.Map");
        o = o.withConstant("ANOTHER_TEST", "String", "\"Hello, World\"");
        o = o.withConstant("TEST", "String", "\"Hello, World\"");
        o = o.withConstant("TEST_x", "String", "\"Hello, World\"");
        o = o.withMethodSignature("foo", "void", new String[] {});
        o = o.withMethodSignature("bar", "Double", "String", "Future<Void>");
        System.out.println(new String(o.generateFile().getFileData()));
    }
}
