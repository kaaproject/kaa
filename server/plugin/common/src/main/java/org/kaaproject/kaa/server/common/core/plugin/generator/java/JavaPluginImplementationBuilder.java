package org.kaaproject.kaa.server.common.core.plugin.generator.java;

import static org.kaaproject.kaa.server.common.core.plugin.generator.common.PluginBuilder.TemplateVariable.CONSTANTS;
import static org.kaaproject.kaa.server.common.core.plugin.generator.common.PluginBuilder.TemplateVariable.IMPORT_STATEMENTS;
import static org.kaaproject.kaa.server.common.core.plugin.generator.common.PluginBuilder.TemplateVariable.FIELDS;
import static org.kaaproject.kaa.server.common.core.plugin.generator.common.PluginBuilder.TemplateVariable.METHODS;
import static org.kaaproject.kaa.server.common.core.plugin.generator.common.PluginBuilder.TemplateVariable.NAME;
import static org.kaaproject.kaa.server.common.core.plugin.generator.common.PluginBuilder.TemplateVariable.NAMESPACE;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.kaaproject.kaa.server.common.core.plugin.def.SdkApiFile;
import org.kaaproject.kaa.server.common.core.plugin.generator.common.Constant;
import org.kaaproject.kaa.server.common.core.plugin.generator.common.Field;
import org.kaaproject.kaa.server.common.core.plugin.generator.common.ImportStatement;
import org.kaaproject.kaa.server.common.core.plugin.generator.common.Method;
import org.kaaproject.kaa.server.common.core.plugin.generator.common.PluginImplementationBuilder;
import org.kaaproject.kaa.server.common.core.plugin.generator.common.PluginInterfaceBuilder;
import org.kaaproject.kaa.server.common.core.plugin.generator.common.PluginBuilder.TemplateVariable;

public class JavaPluginImplementationBuilder extends PluginBuilderBasis implements PluginImplementationBuilder {

    private static final String DEFAULT_TEMPLATE_FILE = "templates/java/implementation.template";

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
        this.methods.add(new Method("set" + this.toCamelCase(name), null, Arrays.asList(new String[] { type }), null));
        this.methods.add(new Method("get" + this.toCamelCase(name), type, Arrays.asList(new String[] {}), null));
        return this;
    }

    @Override
    public SdkApiFile generateFile() {

        Map<TemplateVariable, String> values = new EnumMap<>(TemplateVariable.class);

        values.put(NAME, this.name);
        values.put(NAMESPACE, this.namespace);
        values.put(IMPORT_STATEMENTS, this.accumulateStatements(new StringBuilder(), this.importStatements).toString());
        values.put(CONSTANTS, this.accumulateStatements(new StringBuilder(), this.constants, 1).toString());
        values.put(FIELDS, this.accumulateStatements(new StringBuilder(), this.fields, 1).toString());
        values.put(METHODS, this.accumulateStatements(new StringBuilder(), this.methods).toString());

        if (this.template == null || this.template.isEmpty()) {
            this.template = this.readFileAsString(DEFAULT_TEMPLATE_FILE);
        }

        String fileName = this.name + "Plugin.java";
        byte[] fileData = this.insertValues(this.template, values).getBytes();

        return new SdkApiFile(fileName, fileData);
    }

    private String toCamelCase(String arg) {
        StringBuilder buffer = new StringBuilder();
        for (String s : arg.split("_")) {
            buffer.append(Character.toUpperCase(s.charAt(0)));
            if (s.length() > 0) {
                buffer.append(s.substring(1, s.length()).toLowerCase());
            }
        }
        return buffer.toString();
    }

    public static void main(String[] args) {
        PluginImplementationBuilder o = new JavaPluginBuilder().createImplementation("Messaging", "org.kaaproject.kaa.plugin.messaging");
        o = o.withProperty("temperatureOutside", "int");
        System.out.println(new String(o.generateFile().getFileData()));
    }
}
