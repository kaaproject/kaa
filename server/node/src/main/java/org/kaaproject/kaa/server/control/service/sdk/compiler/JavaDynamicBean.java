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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.tools.SimpleJavaFileObject;

/**
 * The Class JavaDynamicBean.
 */
public class JavaDynamicBean extends SimpleJavaFileObject {
    
    /** The source. */
    private String source; 
    
    /** The byte code. */
    private ByteArrayOutputStream byteCode = new ByteArrayOutputStream();
    
    /**
     * Instantiates a new java dynamic bean.
     *
     * @param baseName the base name
     * @param source the source
     */
    public JavaDynamicBean(String baseName, String source) { 
        super(JavaDynamicUtils.INSTANCE.createURI(JavaDynamicUtils.INSTANCE.getClassNameWithExt(baseName)),
                Kind.SOURCE);
            this.source = source;
    } 
    
    /**
     * Instantiates a new java dynamic bean.
     *
     * @param name the name
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public JavaDynamicBean(String name) throws IOException { 
        super(JavaDynamicUtils.INSTANCE.createURI(name), Kind.CLASS);
    }
    
    /* (non-Javadoc)
     * @see javax.tools.SimpleJavaFileObject#getCharContent(boolean)
     */
    @Override 
    public String getCharContent(boolean ignoreEncodingErrors) { 
      return source; 
    } 

    /* (non-Javadoc)
     * @see javax.tools.SimpleJavaFileObject#openOutputStream()
     */
    @Override 
    public OutputStream openOutputStream() { 
      return byteCode; 
    } 

    /**
     * Gets the bytes.
     *
     * @return the bytes
     */
    public byte[] getBytes() { 
      return byteCode.toByteArray(); 
    } 
    
}
