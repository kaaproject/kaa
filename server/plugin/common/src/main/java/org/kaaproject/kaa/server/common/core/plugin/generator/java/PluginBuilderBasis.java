package org.kaaproject.kaa.server.common.core.plugin.generator.java;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.kaaproject.kaa.server.common.core.plugin.generator.common.Constant;
import org.kaaproject.kaa.server.common.core.plugin.generator.common.ImportStatement;
import org.kaaproject.kaa.server.common.core.plugin.generator.common.PluginBuilder.TemplateVariable;

public abstract class PluginBuilderBasis {

    protected String name;
    protected String namespace;

    public PluginBuilderBasis(String name, String namespace) {
        this.name = name;
        this.namespace = namespace;
    }

    protected String template;

    public void setTemplate(String template) {
        this.template = template;
    }

    protected Set<ImportStatement> importStatements = new HashSet<>();
    protected Set<Constant> constants = new HashSet<>();

    protected String insertValues(String template, Map<TemplateVariable, String> values) {
        if (values != null) {
            for (TemplateVariable parameter : values.keySet()) {
                template = template.replace(parameter.toString(), values.get(parameter));
            }
        }
        return template;
    }

    protected <T> StringBuilder accumulateStatements(StringBuilder buffer, Collection<T> collection) {
        return this.accumulateStatements(buffer, collection, 0);
    }

    protected <T> StringBuilder accumulateStatements(StringBuilder buffer, Collection<T> collection, int indents) {
        if (collection != null) {
            for (T element : collection) {
                for (int i = 0; i < indents; i++) {
                    buffer.append("    ");
                }
                buffer.append(element.toString()).append(";\n");
            }
        }
        return buffer;
    }

    protected String readFileAsString(String fileName) {
        String fileContent = null;
        URL url = this.getClass().getClassLoader().getResource(fileName);
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
