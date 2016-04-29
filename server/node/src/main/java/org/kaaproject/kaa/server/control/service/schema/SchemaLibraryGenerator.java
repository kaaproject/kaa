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

package org.kaaproject.kaa.server.control.service.schema;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.tools.JavaFileObject.Kind;

import org.apache.avro.Schema;
import org.kaaproject.kaa.common.dto.admin.SdkPlatform;
import org.kaaproject.kaa.common.dto.file.FileData;
import org.kaaproject.kaa.server.control.service.sdk.JavaSdkGenerator;
import org.kaaproject.kaa.server.control.service.sdk.SchemaUtil;
import org.kaaproject.kaa.server.control.service.sdk.compiler.JavaDynamicBean;
import org.kaaproject.kaa.server.control.service.sdk.compiler.JavaDynamicCompiler;
import org.slf4j.helpers.MessageFormatter;

/**
 * The Class SchemaLibraryGenerator.
 */
public class SchemaLibraryGenerator {
    
    private static final String LIBRARY_NAME_PATTERN = "{}.jar";

    private SchemaLibraryGenerator() {
    }

    /**
     * Generate schema java library.
     *
     * @param schema the avro schema
     * @param fileName the resulting jar file name
     * @return the schema structure library
     * @throws Exception the exception
     */
    public static FileData generateSchemaLibrary(Schema schema, String fileName) throws Exception {
        
        Map<String, Schema> uniqueSchemas = SchemaUtil.getUniqueSchemasMap(Arrays.asList(schema));
        List<JavaDynamicBean> javaSources = JavaSdkGenerator.generateSchemaSources(schema, uniqueSchemas);

        ByteArrayOutputStream libraryOutput = new ByteArrayOutputStream();
        ZipOutputStream libraryFile = new ZipOutputStream(libraryOutput);

        JavaDynamicCompiler dynamicCompiler = new JavaDynamicCompiler();
        dynamicCompiler.init();
        Collection<JavaDynamicBean> compiledObjects = dynamicCompiler.compile(javaSources);
        for (JavaDynamicBean compiledObject : compiledObjects) {
            String className = compiledObject.getName();
            String classFileName = className.replace('.', '/') + Kind.CLASS.extension;
            ZipEntry classFile = new ZipEntry(classFileName);
            libraryFile.putNextEntry(classFile);
            libraryFile.write(compiledObject.getBytes());
            libraryFile.closeEntry();
        }
        libraryFile.close();

        String libraryFileName = MessageFormatter.arrayFormat(LIBRARY_NAME_PATTERN,
                new Object[]{fileName}).getMessage();
        byte[] libraryData = libraryOutput.toByteArray();

        FileData library = new FileData();
        library.setFileName(libraryFileName);
        library.setFileData(libraryData);
        library.setContentType(SdkPlatform.JAVA.getContentType());
        return library;
    }
}
