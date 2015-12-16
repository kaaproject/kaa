package org.kaaproject.kaa.server.common.core.plugin.generator.java;

import static org.kaaproject.kaa.server.common.core.plugin.generator.common.PluginBuilder.TemplateVariable.CONSTANTS;
import static org.kaaproject.kaa.server.common.core.plugin.generator.common.PluginBuilder.TemplateVariable.FIELDS;
import static org.kaaproject.kaa.server.common.core.plugin.generator.common.PluginBuilder.TemplateVariable.IMPORT_STATEMENTS;
import static org.kaaproject.kaa.server.common.core.plugin.generator.common.PluginBuilder.TemplateVariable.METHODS;
import static org.kaaproject.kaa.server.common.core.plugin.generator.common.PluginBuilder.TemplateVariable.NAME;
import static org.kaaproject.kaa.server.common.core.plugin.generator.common.PluginBuilder.TemplateVariable.NAMESPACE;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.kaaproject.kaa.server.common.core.plugin.def.SdkApiFile;
import org.kaaproject.kaa.server.common.core.plugin.generator.common.PluginBuilder.TemplateVariable;
import org.kaaproject.kaa.server.common.core.plugin.generator.common.entity.Constant;
import org.kaaproject.kaa.server.common.core.plugin.generator.common.entity.Field;
import org.kaaproject.kaa.server.common.core.plugin.generator.common.entity.ImportStatement;
import org.kaaproject.kaa.server.common.core.plugin.generator.common.entity.Method;
import org.kaaproject.kaa.server.common.core.plugin.generator.common.PluginBuilderCore;
import org.kaaproject.kaa.server.common.core.plugin.generator.common.PluginImplementationBuilder;

public class JavaPluginImplementationBuilder extends PluginBuilderCore implements PluginImplementationBuilder {

    private Set<Field> fields = new HashSet<>();
    private Set<Method> methods = new HashSet<>();

    public JavaPluginImplementationBuilder(String name, String namespace) {
        super(name, namespace);
    }

    @Override
    public PluginImplementationBuilder withImportStatement(String body) {
        this.importStatements.add(new ImportStatement(body));
        return this;
    }

    @Override
    public PluginImplementationBuilder withConstant(String name, String type, String value) {
        this.constants.add(new Constant(name, type, value));
        return this;
    }

    @Override
    public PluginImplementationBuilder withProperty(String name, String type) {
        this.fields.add(new Field(name, type));
        this.methods.add(new Method(this.asGetterName(name), type, Arrays.asList(new String[] {}), null));
        this.methods.add(new Method(this.asSetterName(name), null, Arrays.asList(new String[] { type }), null));
        return this;
    }

    @Override
    public SdkApiFile generateFile() {

        Map<TemplateVariable, Object> values = new EnumMap<>(TemplateVariable.class);

        values.put(NAME, this.name);
        values.put(NAMESPACE, this.namespace);
        values.put(IMPORT_STATEMENTS, this.importStatements);
        values.put(CONSTANTS, this.constants);
        values.put(FIELDS, this.fields);
        values.put(METHODS, this.methods);

        String fileName = this.name + "Plugin.java";
        byte[] fileData = this.insertValues(this.template, values).getBytes();

        return new SdkApiFile(fileName, fileData);
    }

    private String asGetterName(String s) {
        return "get" + s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    private String asSetterName(String s) {
        return "set" + s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    public static void main(String[] args) {
        PluginImplementationBuilder o = new JavaPluginBuilder().fromTemplate(read()).createImplementation("Messaging", "org.kaaproject.kaa.plugin.messaging");
        o = o.withProperty("t", "int");
        System.out.println(new String(o.generateFile().getFileData()));
    }

    public static String read() {
        return new JavaPluginImplementationBuilder("test", "test").readFileAsString("templates/java/implementation.template");
    }
}
