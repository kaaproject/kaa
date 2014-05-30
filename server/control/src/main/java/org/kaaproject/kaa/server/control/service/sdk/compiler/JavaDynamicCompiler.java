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

package org.kaaproject.kaa.server.control.service.sdk.compiler;

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

    /**
     * Instantiates a new java dynamic compiler.
     *
     * @throws JavaDynamicException the java dynamic exception
     */
    public JavaDynamicCompiler() throws JavaDynamicException {
    }
    
    /**
     * Inits the.
     *
     * @throws JavaDynamicException the java dynamic exception
     */
    public void init() throws JavaDynamicException {
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
     * @return the class
     * @throws JavaDynamicException the java dynamic exception
     */
    public synchronized Collection<JavaDynamicBean> compile(List<JavaDynamicBean> sources) throws JavaDynamicException {
        try {
            CompilationTask task = compiler.getTask(null, javaDynamicManager,
                    diagnostics, null, null, sources);
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
