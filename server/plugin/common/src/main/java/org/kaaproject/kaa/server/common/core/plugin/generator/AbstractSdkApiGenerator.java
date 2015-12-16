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

package org.kaaproject.kaa.server.common.core.plugin.generator;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.avro.specific.SpecificRecordBase;
import org.kaaproject.kaa.common.avro.AvroByteArrayConverter;
import org.kaaproject.kaa.server.common.core.plugin.def.PluginContractItemDef;
import org.kaaproject.kaa.server.common.core.plugin.def.SdkApiFile;
import org.kaaproject.kaa.server.common.core.plugin.instance.PluginContractItemInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractSdkApiGenerator<T extends SpecificRecordBase> implements PluginSdkApiGenerator {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractSdkApiGenerator.class);

    protected static final String PLUGIN_API_TEMPLATE_FILE = "templates/pluginAPI.template";
    protected static final String PLUGIN_IMPLEMENTATION_TEMPLATE_FILE = "templates/pluginImplementation.template";

    protected String prefix;
    protected String namespace;

    /**
     * Allows different method signature generation strategies based on contract
     * item definitions.
     */
    protected Map<PluginContractItemDef, MethodSignatureGenerator> signatureGenerators = new HashMap<>();

    protected List<MethodSignature> signatures = new ArrayList<>();

    protected Set<Entity> entities = new HashSet<>();

    @Override
    public List<SdkApiFile> generatePluginSdkApi(PluginSdkApiGenerationContext context) throws SdkApiGenerationException {
        AvroByteArrayConverter<T> converter = new AvroByteArrayConverter<>(this.getConfigurationClass());
        try {
            T config = converter.fromByteArray(context.getPluginConfigurationData());
            LOG.info("Initializing transport {} with {}", this.getClassName(), config);
            return this.generatePluginSdkApi(new SpecificPluginSdkApiGenerationContext<T>(context, config));
        } catch (IOException cause) {
            LOG.error(MessageFormat.format("Failed to initialize transport {0}", this.getClassName()), cause);
            throw new SdkApiGenerationException(cause);
        }
    }

    public abstract Class<T> getConfigurationClass();

    private String getClassName() {
        return this.getClass().getName();
    }

    protected abstract List<SdkApiFile> generatePluginSdkApi(SpecificPluginSdkApiGenerationContext<T> context);

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

    protected static final String CONSTANT = "${constant}";

    protected static final String METHOD_NAME = "${methodName}";
    protected static final String RETURN_TYPE = "${returnType}";
    protected static final String PARAM_TYPE = "${paramType}";

    protected static final String SOURCE_FILE_NAME_TEMPLATE = "{0}.java";

    protected static final String CLASS_NAME = "${className}";
    protected static final String PLUGIN_API_CLASS_NAME_TEMPLATE = "{0}PluginAPI";
    protected static final String PLUGIN_IMPLEMENTATION_CLASS_NAME_TEMPLATE = "{0}Plugin";

    protected static final String PACKAGE_NAME = "${packageName}";
    protected static final String PACKAGE_NAME_TEMPLATE = "{0}.ext{1}";

    protected static final String METHOD_SIGNATURES = "${methodSignatures}";

    protected static final String METHOD_CONSTANT_TEMPLATE_FILE = "templates/constant.template";
    protected static final String ENTITY_CONVERTER_TEMPLATE_FILE = "templates/converter.template";

    protected static final String CONSTANTS = "${constants}";
    protected static final String CONVERTERS = "${converters}";

    protected String generateMethodConstant(MethodSignature signature) {
        String template = this.readFileAsString(METHOD_CONSTANT_TEMPLATE_FILE);
        template = template.replace(PACKAGE_NAME, this.namespace);
        template = template.replace(CLASS_NAME, MessageFormat.format(PLUGIN_API_CLASS_NAME_TEMPLATE, this.prefix));
        template = template.replace(METHOD_NAME, signature.getMethodName());
        template = template.replace(PARAM_TYPE, signature.getParamType());
        template = template.replace(CONSTANT, Integer.toString(signature.getId()));
        return template;
    }

    protected String generateMethodConstants() {

        StringBuilder buffer = new StringBuilder();
        String template = this.readFileAsString(METHOD_CONSTANT_TEMPLATE_FILE);

        for (MethodSignature signature : signatures) {
            String source = template;
            source = source.replace(PACKAGE_NAME, this.namespace);
            source = source.replace(CLASS_NAME, MessageFormat.format(PLUGIN_API_CLASS_NAME_TEMPLATE, this.prefix));
            source = source.replace(METHOD_NAME, signature.getMethodName());
            source = source.replace(PARAM_TYPE, signature.getParamType());
            source = source.replace(CONSTANT, Integer.toString(signature.getId()));

            buffer.append(source).append("\n");
        }

        return buffer.toString();
    }

    protected String generateEntityConverters() {

        StringBuilder buffer = new StringBuilder();
        String template = this.readFileAsString(ENTITY_CONVERTER_TEMPLATE_FILE);

        for (Entity entity : entities) {
            String source = template;
            source = source.replace(PARAM_TYPE, entity.getEntityClass().getName());
            source = source.replace(CONSTANT, Integer.toString(entity.getId()));

            buffer.append(source).append("\n");
        }

        return buffer.toString();
    }

    protected void includeMethod(MethodSignature signature) {
        signature.setId(this.signatures.size() + 1);
        this.signatures.add(signature);
        try {
            Entity paramType = new Entity(this.entities.size() + 1, Class.forName(signature.getParamType()));
            this.entities.add(paramType);
            Entity returnType = new Entity(this.entities.size() + 1, Class.forName(signature.getReturnType()));
            this.entities.add(returnType);
        } catch (ClassNotFoundException cause) {
            throw new RuntimeException(cause);
        }
    }
    
    protected abstract String getMethodName(PluginContractItemInfo item, PluginContractItemDef def);
}
