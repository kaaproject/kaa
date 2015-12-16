package org.kaaproject.kaa.server.common.core.plugin.generator.common;

public interface PluginBuilder {

    enum TemplateVariable {

        CONSTANTS("${constants}"),
        FIELDS("${fields}"),
        IMPORT_STATEMENTS("${importStatements}"),
        METHODS("${methods}"),
        METHOD_SIGNATURES("${methodSignatures}"),
        NAME("${name}"),
        NAMESPACE("${namespace}");

        private String body;

        private TemplateVariable(String body) {
            this.body = body;
        }

        @Override
        public String toString() {
            return this.body;
        }
    }

    PluginBuilder fromTemplate(String template);

    PluginInterfaceBuilder createInterface(String name, String namespace);

    PluginImplementationBuilder createImplementation(String name, String namespace);
}
