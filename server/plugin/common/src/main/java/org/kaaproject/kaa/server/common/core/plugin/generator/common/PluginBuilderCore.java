package org.kaaproject.kaa.server.common.core.plugin.generator.common;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.kaaproject.kaa.server.common.core.plugin.generator.common.PluginBuilder.TemplateVariable;
import org.kaaproject.kaa.server.common.core.plugin.generator.common.entity.Constant;
import org.kaaproject.kaa.server.common.core.plugin.generator.common.entity.ImportStatement;

public abstract class PluginBuilderCore {

    protected String name;
    protected String namespace;

    public PluginBuilderCore(String name, String namespace) {
        this.name = name;
        this.namespace = namespace;
    }

    protected String template;

    public void setTemplate(String template) {
        this.template = template;
    }

    protected Set<ImportStatement> importStatements = new HashSet<>();
    protected Set<Constant> constants = new HashSet<>();

    protected String insertValues(String template, Map<TemplateVariable, Object> values) {
        if (values != null) {
            for (TemplateVariable variable : values.keySet()) {
                Object value = values.get(variable);
                if (value != null) {
                    if (value instanceof Collection<?>) {
                        StringBuilder buffer = new StringBuilder();
                        for (Object element : (Collection<?>) value) {
                            if (element != null) {
                                buffer.append(element.toString()).append(";\n");
                            }
                        }
                        value = buffer;
                    }
                    template = template.replace(variable.toString(), value.toString());
                }
            }
        }
        return template;
    }

    public String readFileAsString(String fileName) {
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
