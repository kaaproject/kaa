package org.kaaproject.kaa.avro.avrogen.compiler;

import org.apache.avro.Schema;
import org.apache.velocity.VelocityContext;
import org.kaaproject.kaa.avro.avrogen.GenerationContext;
import org.kaaproject.kaa.avro.avrogen.KaaGeneratorException;
import org.kaaproject.kaa.avro.avrogen.StyleUtils;
import org.kaaproject.kaa.avro.avrogen.TypeConverter;

import java.io.OutputStream;
import java.io.StringWriter;
import java.lang.*;
import java.util.Map;

public class CCompiler extends Compiler {

    public CCompiler(String schemaPath, String outputPath, String sourceName) throws KaaGeneratorException {
        super(schemaPath, outputPath, sourceName);
    }

    public CCompiler(Schema schema, String sourceName, OutputStream hdrS, OutputStream srcS) throws KaaGeneratorException {
        super(schema, sourceName, hdrS, srcS);
    }

    @Override
    protected String headerTemplateGen() {
        return "src/main/resources/header.tmpl.gen";
    }

    @Override
    protected String sourceTemplateGen() {
        return "src/main/resources/source.tmpl.gen";
    }

    @Override
    protected String headerTemplate() {
        return "header.tmpl";
    }

    @Override
    protected String sourceTemplate() {
        return "source.tmpl";
    }

    @Override
    protected String getSourceExtension() {
        return ".c";
    }

    @Override
    protected void doGenerate() {
        for (Map.Entry<Schema, GenerationContext> cursor : schemaQueue.entrySet()) {
            switch (cursor.getKey().getType()) {
                case RECORD:
                    processRecord(cursor.getKey(), "record.h.vm", "record.c.vm");
                    break;
                case UNION:
                    processUnion(cursor.getKey(), cursor.getValue());
                    break;
                case ENUM:
                    processEnum(cursor.getKey(), "enum.h.vm");
                    break;
                default:
                    break;
            }
        }
        completeGeneration();
    }

    private void processUnion(Schema schema, GenerationContext genContext) {
        VelocityContext context = new VelocityContext();

        context.put("schema", schema);
        context.put("generationContext", genContext);
        context.put("StyleUtils", StyleUtils.class);
        context.put("TypeConverter", TypeConverter.class);
        context.put("namespacePrefix", namespacePrefix);

        StringWriter headerWriter = new StringWriter();
        engine.getTemplate("union.h.vm").merge(context, headerWriter);
        appendResult(headerWriter.toString(), true);

        StringWriter sourceWriter = new StringWriter();
        engine.getTemplate("union.c.vm").merge(context, sourceWriter);
        appendResult(sourceWriter.toString(), false);
    }

    private void completeGeneration() {
        headerWriter.write("#ifdef __cplusplus\n}      /* extern \"C\" */\n#endif\n#endif");
    }
}
