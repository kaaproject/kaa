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

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;

/**
 * The Class JavaDynamicManager.
 */
public class JavaDynamicManager extends
        ForwardingJavaFileManager<JavaFileManager> {
    
    /** The compiled objects. */
    private Map<String, JavaDynamicBean> compiledObjects = new HashMap<String, JavaDynamicBean>();

    /**
     * Instantiates a new java dynamic manager.
     *
     * @param fileManager the file manager
     */
    public JavaDynamicManager(JavaFileManager fileManager) {
        super(fileManager);
    }

    /* (non-Javadoc)
     * @see javax.tools.ForwardingJavaFileManager#getJavaFileForOutput(javax.tools.JavaFileManager.Location, java.lang.String, javax.tools.JavaFileObject.Kind, javax.tools.FileObject)
     */
    @Override
    public JavaFileObject getJavaFileForOutput(Location location,
            String qualifiedName, Kind kind, FileObject outputFile)
            throws IOException {
        JavaDynamicBean object =  compiledObjects.get(qualifiedName);
        if (object == null) {
            object = new JavaDynamicBean(qualifiedName);
            compiledObjects.put(qualifiedName, object);
        }
        return object;
    }

    /**
     * Gets the compiled objects.
     *
     * @return the compiled objects
     */
    public Collection<JavaDynamicBean> getCompiledObjects() {
        return compiledObjects.values();
    }

}