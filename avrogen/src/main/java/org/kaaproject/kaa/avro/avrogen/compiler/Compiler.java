/*
 * Copyright 2014-2016 CyberVision, Inc.
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

package org.kaaproject.kaa.avro.avrogen.compiler;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.Schema.Type;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.kaaproject.kaa.avro.avrogen.GenerationContext;
import org.kaaproject.kaa.avro.avrogen.KaaGeneratorException;
import org.kaaproject.kaa.avro.avrogen.StyleUtils;
import org.kaaproject.kaa.avro.avrogen.TypeConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.commons.lang.StringUtils.join;

public abstract class Compiler {
    private static final String DIRECTION_PROP = "direction";


    private static final Logger LOG = LoggerFactory.getLogger(Compiler.class);

    private final String generatedSourceName;

    private List<Schema> schemas = new ArrayList<>();

    protected VelocityEngine engine;

    protected PrintWriter headerWriter;
    protected PrintWriter sourceWriter;

    protected String namespacePrefix;

    protected final Map<Schema, GenerationContext> schemaGenerationQueue;
    // list of schemas that should be skipped during generation
    protected Set<Schema> generatedSchemas = new HashSet<>();


    private void initVelocityEngine() {
        engine = new VelocityEngine();

        engine.addProperty("resource.loader", "class, file");
        engine.addProperty("class.resource.loader.class",
                "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        engine.addProperty("file.resource.loader.class",
                "org.apache.velocity.runtime.resource.loader.FileResourceLoader");
        engine.addProperty("file.resource.loader.path", "/, .");
        engine.setProperty("runtime.references.strict", true);
        engine.setProperty("runtime.log.logsystem.class", "org.apache.velocity.runtime.log.NullLogSystem");
    }

    private Compiler(String sourceName) throws KaaGeneratorException {
        this.namespacePrefix = "kaa";
        this.generatedSourceName = sourceName;
        this.schemaGenerationQueue = new LinkedHashMap<>();
        initVelocityEngine();
    }

    public Compiler(Schema schema, String sourceName, OutputStream hdrS, OutputStream srcS) throws KaaGeneratorException {
        this(sourceName);
        this.schemas.add(schema);
        this.headerWriter = new PrintWriter(hdrS);
        this.sourceWriter = new PrintWriter(srcS);
        prepareTemplates(false);
    }

    public Compiler(List<Schema> schemas, String sourceName, OutputStream hdrS, OutputStream srcS) throws KaaGeneratorException {
        this(sourceName);
        this.schemas.addAll(schemas);
        this.headerWriter = new PrintWriter(hdrS);
        this.sourceWriter = new PrintWriter(srcS);
        prepareTemplates(false);
    }


    public Compiler(List<Schema> schemas, String sourceName, OutputStream hdrS, OutputStream srcS, Set<Schema> generatedSchemas) throws KaaGeneratorException {
        this(schemas, sourceName, hdrS, srcS);
        this.generatedSchemas = new HashSet<>(generatedSchemas);
    }

    public Compiler(String schemaPath, String outputPath, String sourceName) throws KaaGeneratorException {
        this(sourceName);
        try {
            this.schemas.add(new Schema.Parser().parse(new File(schemaPath)));

            prepareTemplates(true);

            File outputDir = new File(outputPath);
            outputDir.mkdirs();

            String headerPath = outputPath + File.separator + generatedSourceName + ".h";
            String sourcePath = outputPath + File.separator + generatedSourceName + getSourceExtension();

            Files.move(new File(headerTemplateGen()).toPath()
                    , new File(headerPath).toPath(), StandardCopyOption.REPLACE_EXISTING);
            Files.move(new File(sourceTemplateGen()).toPath()
                    , new File(sourcePath).toPath(), StandardCopyOption.REPLACE_EXISTING);

            this.headerWriter = new PrintWriter(new BufferedWriter(new FileWriter(headerPath, true)));
            this.sourceWriter = new PrintWriter(new BufferedWriter(new FileWriter(sourcePath, true)));
        } catch (Exception e) {
            LOG.error("Failed to create ouput path: ", e);
            throw new KaaGeneratorException("Failed to create output path: " + e.toString());
        }
    }

    protected abstract String headerTemplateGen();

    protected abstract String sourceTemplateGen();

    protected abstract String headerTemplate();

    protected abstract String sourceTemplate();

    protected abstract String getSourceExtension();

    private void prepareTemplates(boolean toFile) throws KaaGeneratorException {
        try {
            VelocityContext context = new VelocityContext();
            context.put("headerName", generatedSourceName);

            StringWriter hdrWriter = new StringWriter();
            engine.getTemplate(headerTemplate()).merge(context, hdrWriter);

            StringWriter srcWriter = new StringWriter();
            engine.getTemplate(sourceTemplate()).merge(context, srcWriter);

            if (toFile) {
                writeToFile(hdrWriter, srcWriter);
            } else {
                writeToStream(hdrWriter, srcWriter);
            }
        } catch (Exception e) {
            LOG.error("Failed to prepare source templates: ", e);
            throw new KaaGeneratorException("Failed to prepare source templates: " + e.toString());
        }
    }

    private void writeToStream(StringWriter hdrWriter, StringWriter srcWriter) {
        headerWriter.write(hdrWriter.toString());
        sourceWriter.write(srcWriter.toString());
    }

    private void writeToFile(StringWriter hdrWriter, StringWriter srcWriter) throws Exception {
        FileOutputStream hdrOs = new FileOutputStream(headerTemplateGen());
        hdrOs.write(hdrWriter.toString().getBytes());
        hdrOs.close();

        FileOutputStream srcOs = new FileOutputStream(sourceTemplateGen());
        srcOs.write(srcWriter.toString().getBytes());
        srcOs.close();
    }


    public Set<Schema> generate() throws KaaGeneratorException {
        try {
            LOG.debug("Processing schemas: [" + join(schemas, ", ") + "]");
            for (Schema schema : schemas) {

                if (schema.getType() == Type.UNION) {
                    for (Schema s : schema.getTypes()) {
                        addAllSchemasToQueue(s, null);
                    }
                } else {
                    addAllSchemasToQueue(schema, null);
                }
            }


            doGenerate();

            LOG.debug("Sources were successfully generated");
            return schemaGenerationQueue.keySet();
        } catch (Exception e) {
            LOG.error("Failed to generate C sources: ", e);
            throw new KaaGeneratorException("Failed to generate sources: " + e.toString());
        } finally {
            headerWriter.close();
            sourceWriter.close();
        }
    }

    /**
     * Recursively add all unique dependencies of a passed schema and the one to generation queue,
     * that used to generate sources.
     */
    private void addAllSchemasToQueue(Schema schema, GenerationContext context) {
        GenerationContext existingContext = schemaGenerationQueue.get(schema);

        if (existingContext != null) {
            existingContext.updateDirection(context);
            return;
        }

        switch (schema.getType()) {
            case RECORD:
                for (Field f : schema.getFields()) {
                    addAllSchemasToQueue(f.schema(), new GenerationContext(
                            schema.getName(), f.name(), schema.getProp(DIRECTION_PROP)));
                }
                schemaGenerationQueue.put(schema, null);
                break;
            case UNION:
                for (Schema branchSchema : schema.getTypes()) {
                    addAllSchemasToQueue(branchSchema, context);
                }
                schemaGenerationQueue.put(schema, context);
                break;
            case ARRAY:
                addAllSchemasToQueue(schema.getElementType(), context);
                break;
            case ENUM:
                schemaGenerationQueue.put(schema, null);
                break;
            default:
                break;
        }
    }

    protected abstract void doGenerate();

    protected void processRecord(Schema schema, String headerTemplate, String sourceTemplate) {
        VelocityContext context = new VelocityContext();

        context.put("schema", schema);
        context.put("StyleUtils", StyleUtils.class);
        context.put("TypeConverter", TypeConverter.class);
        context.put("namespacePrefix", namespacePrefix);

        StringWriter hdrWriter = new StringWriter();
        engine.getTemplate(headerTemplate).merge(context, hdrWriter);
        appendResult(hdrWriter.toString(), true);

        StringWriter srcWriter = new StringWriter();
        engine.getTemplate(sourceTemplate).merge(context, srcWriter);
        appendResult(srcWriter.toString(), false);
    }

    protected void processEnum(Schema schema, String template) {
        VelocityContext context = new VelocityContext();

        List<String> symbols = schema.getEnumSymbols();

        context.put("schema", schema);
        context.put("symbols", symbols);
        context.put("StyleUtils", StyleUtils.class);
        context.put("namespacePrefix", namespacePrefix);

        StringWriter writer = new StringWriter();
        engine.getTemplate(template).merge(context, writer);
        appendResult(writer.toString(), true);
    }

    protected void appendResult(String str, boolean toHeader) {
        if (toHeader) {
            headerWriter.write(str);
        } else {
            sourceWriter.write(str);
        }
    }

    public void setNamespacePrefix(String namespacePrefix) {
        this.namespacePrefix = namespacePrefix;
    }

}