package org.kaaproject.kaa.avro.avrogen.compiler;

import org.apache.avro.Schema;
import org.kaaproject.kaa.avro.avrogen.GenerationContext;
import org.kaaproject.kaa.avro.avrogen.KaaGeneratorException;

import java.io.OutputStream;
import java.lang.*;
import java.util.Map;

public class ObjectiveCCompiler extends Compiler {

    public ObjectiveCCompiler(Schema schema, String sourceName, OutputStream hdrS, OutputStream srcS) throws KaaGeneratorException {
        super(schema, sourceName, hdrS, srcS);
        setNamespacePrefix("");
    }

    public ObjectiveCCompiler(String schemaPath, String outputPath, String sourceName) throws KaaGeneratorException {
        super(schemaPath, outputPath, sourceName);
        setNamespacePrefix("");
    }

    @Override
    protected String headerTemplateGen() {
        return "src/main/resources/ObjC/headerObjC.tmpl.gen";
    }

    @Override
    protected String sourceTemplateGen() {
        return "src/main/resources/ObjC/sourceObjC.tmpl.gen";
    }

    @Override
    protected String headerTemplate() {
        return "ObjC/headerObjC.tmpl";
    }

    @Override
    protected String sourceTemplate() {
        return "ObjC/sourceObjC.tmpl";
    }

    @Override
    protected String getSourceExtension() {
        return ".m";
    }

    @Override
    protected void doGenerate() {
        for (Map.Entry<Schema, GenerationContext> cursor : schemaQueue.entrySet()) {
            switch (cursor.getKey().getType()) {
                case RECORD:
                    processRecord(cursor.getKey(), "ObjC/recordObjC.h.vm", "ObjC/recordObjC.m.vm");
                    break;
                case ENUM:
                    processEnum(cursor.getKey(), "ObjC/enumObjC.h.vm");
                    break;
            }
        }
    }
}
