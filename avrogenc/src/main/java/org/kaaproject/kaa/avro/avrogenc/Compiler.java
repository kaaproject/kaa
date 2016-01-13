/*
 * Copyright 2014 CyberVision, Inc.
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

package org.kaaproject.kaa.avro.avrogenc;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.Schema.Type;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Compiler {
    private static final String DIRECTION_PROP = "direction";

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(Compiler.class);

    private final String generatedSourceName;

    private Schema schema;

    private VelocityEngine engine;

    private PrintWriter headerWriter;
    private PrintWriter sourceWriter;

    private String namespacePrefix;

    private final Map<Schema, GenerationContext> schemaQueue;

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

    private Compiler(String sourceName) throws KaaCGeneratorException {
        this.namespacePrefix = "kaa";
        this.generatedSourceName = sourceName;
        this.schemaQueue = new LinkedHashMap<>();
        initVelocityEngine();
    }

    public Compiler(Schema schema, String sourceName, OutputStream hdrS, OutputStream srcS)
            throws KaaCGeneratorException {
        this(sourceName);

        this.schema = schema;
        this.headerWriter = new PrintWriter(hdrS);
        this.sourceWriter = new PrintWriter(srcS);

        prepareTemplates(false);
    }

    public Compiler(String schemaPath, String outputPath, String sourceName) throws KaaCGeneratorException {
        this(sourceName);
        try {
            this.schema = new Schema.Parser().parse(new File(schemaPath));

            prepareTemplates(true);

            File outputDir = new File(outputPath);
            outputDir.mkdirs();

            String headerPath = outputPath + File.separator + generatedSourceName + ".h";
            String sourcePath = outputPath + File.separator + generatedSourceName + ".c";

            Files.move(new File("src/main/resources/header.tmpl.gen").toPath()
                    , new File(headerPath).toPath(), StandardCopyOption.REPLACE_EXISTING);
            Files.move(new File("src/main/resources/source.tmpl.gen").toPath()
                    , new File(sourcePath).toPath(), StandardCopyOption.REPLACE_EXISTING);

            this.headerWriter = new PrintWriter(new BufferedWriter(new FileWriter(headerPath, true)));
            this.sourceWriter = new PrintWriter(new BufferedWriter(new FileWriter(sourcePath, true)));
        } catch (Exception e) {
            LOG.error("Failed to create ouput path: ", e);
            throw new KaaCGeneratorException("Failed to create ouput path: " + e.toString());
        }
    }

    private void prepareTemplates(boolean toFile) throws KaaCGeneratorException  {
        try {
            VelocityContext context = new VelocityContext();
            context.put("headerName", generatedSourceName);

            StringWriter hdrWriter = new StringWriter();
            engine.getTemplate("header.tmpl").merge(context, hdrWriter);

            StringWriter srcWriter = new StringWriter();
            engine.getTemplate("source.tmpl").merge(context, srcWriter);

            if (toFile) {
                writeToFile(hdrWriter, srcWriter);
            } else {
                writeToStream(hdrWriter, srcWriter);
            }
        } catch (Exception e) {
            LOG.error("Failed to prepare source templates: ", e);
            throw new KaaCGeneratorException("Failed to prepare source templates: " + e.toString());
        }
    }

    private void writeToStream(StringWriter hdrWriter, StringWriter srcWriter) {
        headerWriter.write(hdrWriter.toString());
        sourceWriter.write(srcWriter.toString());
    }

    private void writeToFile(StringWriter hdrWriter, StringWriter srcWriter) throws Exception {
        FileOutputStream hdrOs = new FileOutputStream("src/main/resources/header.tmpl.gen");
        hdrOs.write(hdrWriter.toString().getBytes());
        hdrOs.close();

        FileOutputStream srcOs = new FileOutputStream("src/main/resources/source.tmpl.gen");
        srcOs.write(srcWriter.toString().getBytes());
        srcOs.close();
    }

    public void generate() throws KaaCGeneratorException {
        try {
            System.out.println("Processing schema: " + schema);

            if (schema.getType() == Type.UNION) {
                for (Schema s : schema.getTypes()) {
                    filterSchemas(s, null);
                }
            } else {
                filterSchemas(schema, null);
            }

            doGenerate();
            compeleteGeneration();

            System.out.println("C sources were successfully generated");
        } catch (Exception e) {
            LOG.error("Failed to generate C sources: ", e);
            throw new KaaCGeneratorException("Failed to generate C sources: " + e.toString());
        } finally {
            headerWriter.close();
            sourceWriter.close();
        }
    }

    private void filterSchemas(Schema schema, GenerationContext context) {
        GenerationContext existingContext = schemaQueue.get(schema);

        if (existingContext != null) {
            existingContext.updateDirection(context);
            return;
        }

        switch (schema.getType()) {
        case RECORD:
            for (Field f : schema.getFields()) {
                filterSchemas(f.schema(), new GenerationContext(
                        schema.getName(), f.name(), schema.getProp(DIRECTION_PROP)));
            }
            schemaQueue.put(schema, null);
            break;
        case UNION:
            for (Schema branchSchema : schema.getTypes()) {
                filterSchemas(branchSchema, context);
            }
            schemaQueue.put(schema, context);
            break;
        case ARRAY:
            filterSchemas(schema.getElementType(), context);
            break;
        case ENUM:
            schemaQueue.put(schema, null);
            break;
        default:
            break;
        }
    }

    private void doGenerate() {
        for (Map.Entry<Schema, GenerationContext> cursor : schemaQueue.entrySet()) {
            switch (cursor.getKey().getType()) {
            case RECORD:
                processRecord(cursor.getKey());
                break;
            case UNION:
                processUnion(cursor.getKey(), cursor.getValue());
                break;
            case ENUM:
                processEnum(cursor.getKey());
                break;
            default:
                break;
            }
        }
    }

    private void processUnion(Schema schema, GenerationContext genContext) {
        VelocityContext context = new VelocityContext();

        context.put("schema", schema);
        context.put("generationContext", genContext);
        context.put("StyleUtils", StyleUtils.class);
        context.put("TypeConverter", TypeConverter.class);
        context.put("namespacePrefix", namespacePrefix);

        StringWriter hdrWriter = new StringWriter();
        engine.getTemplate("union.h.vm").merge(context, hdrWriter);
        appendResult(hdrWriter.toString(), true);

        StringWriter srcWriter = new StringWriter();
        engine.getTemplate("union.c.vm").merge(context, srcWriter);
        appendResult(srcWriter.toString(), false);
    }

    private void processRecord(Schema schema) {
        VelocityContext context = new VelocityContext();

        context.put("schema", schema);
        context.put("StyleUtils", StyleUtils.class);
        context.put("TypeConverter", TypeConverter.class);
        context.put("namespacePrefix", namespacePrefix);

        StringWriter hdrWriter = new StringWriter();
        engine.getTemplate("record.h.vm").merge(context, hdrWriter);
        appendResult(hdrWriter.toString(), true);

        StringWriter srcWriter = new StringWriter();
        engine.getTemplate("record.c.vm").merge(context, srcWriter);
        appendResult(srcWriter.toString(), false);
    }

    private void processEnum(Schema schema) {
        VelocityContext context = new VelocityContext();

        List<String> symbols = schema.getEnumSymbols();

        context.put("schema", schema);
        context.put("symbols", symbols);
        context.put("StyleUtils", StyleUtils.class);
        context.put("namespacePrefix", namespacePrefix);

        StringWriter writer = new StringWriter();
        engine.getTemplate("enum.h.vm").merge(context, writer);
        appendResult(writer.toString(), true);
    }

    private void appendResult(String str, boolean toHeader) {
        if (toHeader) {
            headerWriter.write(str);
        } else {
            sourceWriter.write(str);
        }
    }

    private void compeleteGeneration() {
        headerWriter.write("#ifdef __cplusplus\n}      /* extern \"C\" */\n#endif\n#endif");
    }

    public void setNamespacePrefix(String namespacePrefix) {
        this.namespacePrefix = namespacePrefix;
    }


}