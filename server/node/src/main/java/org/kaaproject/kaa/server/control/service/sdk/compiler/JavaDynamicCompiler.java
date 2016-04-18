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

package org.kaaproject.kaa.server.control.service.sdk.compiler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

/**
 * The Class JavaDynamicCompiler.
 */
public class JavaDynamicCompiler {

    /** The compiler. */
    private JavaCompiler compiler;
    
    /** The java dynamic manager. */
    private JavaDynamicManager javaDynamicManager;
    
    /** The diagnostics. */
    private DiagnosticCollector<JavaFileObject> diagnostics;
    
    private List<String> optionList;

    /**
     * Instantiates a new java dynamic compiler.
     *
     */
    public JavaDynamicCompiler() {
        optionList = new ArrayList<>();
        optionList.add("-g:source");
    }
    
    /**
     * Inits the.
     *
     */
    public void init() {
        compiler = getCompiler();
        if (compiler == null) {
            throw new JavaDynamicException("Compiler not found");
        }

        diagnostics = new DiagnosticCollector<JavaFileObject>();

        StandardJavaFileManager standardFileManager = compiler
                .getStandardFileManager(diagnostics, null, null);
        javaDynamicManager = new JavaDynamicManager(standardFileManager);
    }
    
    /**
     * Gets the compiler.
     *
     * @return the compiler
     */
    public JavaCompiler getCompiler() {
        return ToolProvider.getSystemJavaCompiler();
    }

    /**
     * Compile.
     *
     * @param sources the sources
     * @param additionalOptions the additional options
     * @return the collection of java dynamic beans
     */
    public synchronized Collection<JavaDynamicBean> compile(List<JavaDynamicBean> sources, String... additionalOptions) {
        try {
            List<String> options = optionList;
            if (additionalOptions.length > 0) {
                List<String> newOptions = new ArrayList<>();
                newOptions.addAll(optionList);
                newOptions.addAll(Arrays.asList(additionalOptions));
                options = newOptions;
            }
            CompilationTask task = compiler.getTask(null, javaDynamicManager,
                    diagnostics, options, null, sources);
            boolean result = task.call();
            if (!result) {
                throw new JavaDynamicException("The compilation failed",
                        diagnostics);
            }
            
            return javaDynamicManager.getCompiledObjects();
        } catch (Exception exception) {
            throw new JavaDynamicException(exception, diagnostics);
        }
    }
}
