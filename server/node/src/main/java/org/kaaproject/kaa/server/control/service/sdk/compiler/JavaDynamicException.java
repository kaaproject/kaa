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

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaFileObject;

/**
 * The Class JavaDynamicException.
 */
public class JavaDynamicException extends RuntimeException {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The collector. */
    private DiagnosticCollector<JavaFileObject> collector;

    /**
     * Instantiates a new java dynamic exception.
     *
     * @param message the message
     */
    public JavaDynamicException(String message) {
        super(message);
    }

    /**
     * Instantiates a new java dynamic exception.
     *
     * @param message the message
     * @param collector the collector
     */
    public JavaDynamicException(String message,
            DiagnosticCollector<JavaFileObject> collector) {
        super(message);
        this.collector = collector;
    }

    /**
     * Instantiates a new java dynamic exception.
     *
     * @param e the e
     * @param collector the collector
     */
    public JavaDynamicException(Throwable e,
            DiagnosticCollector<JavaFileObject> collector) {
        super(e);
        this.collector = collector;
    }

    /**
     * Gets the compilation error.
     *
     * @return the compilation error
     */
    public String getCompilationError() {
        if (collector != null) {
            StringBuilder sb = new StringBuilder();
            for (Diagnostic<? extends JavaFileObject> diagnostic : collector
                    .getDiagnostics()) {
                sb.append(diagnostic.getMessage(null));
            }
            return sb.toString();
        } else {
            return getMessage();
        }
    }

    /* (non-Javadoc)
     * @see java.lang.Throwable#toString()
     */
    @Override
    public String toString() {
        return getCompilationError();
    }
}
