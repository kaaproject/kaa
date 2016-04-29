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

package org.kaaproject.kaa.server.transports.http.transport;

import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Represent Multipart mixed objects of http request
 * @author Andrey Panasenko <apanasenko@cybervisiontech.com>
 *
 */
public class MultipartObjects {
    
    /**
    * The pool of ASCII chars to be used for generating a multipart boundary.
    */
    private final static char[] MULTIPART_CHARS =
            "-_1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
                .toCharArray();
    /** boundary size */
    public static final int BOUNDARY_LENGTH = 35;
    
    /** ContentType constant string */
    public static final String CONTENT_TYPE_CONST = "multipart/form-data; boundary=";
    
    /** ContentDisposition constant string */
    public static final String CONTENT_DISPOSITION = "Content-Disposition: form-data; ";
    
    /** Content name filed */ 
    public static final String CONTENT_NAME = "name=";
    
    /** CRLF */
    public static final String crlf = "\r\n";
    
    /** Generated boundary */
    private String boundary;
    
    /** Random number generator */
    private static Random rnd = new Random();
    
    /** Multipart objects */
    private Map<String, ByteBuffer> objects;
    
    /**
     * Constructor.
     */
    public MultipartObjects() {
        boundary = getRandomString(BOUNDARY_LENGTH);
        objects = new HashMap<String, ByteBuffer>();
    }
    
    /**
     * Add multipart/mixed object
     * @param name - String name of object
     * @param bytes - byte[] array of object
     */
    public void addObject(String name, byte[] bytes) {
        objects.put(name, ByteBuffer.wrap(bytes));
    }
    
    /**
     * Generate Multipart/mixed POST request objects to DataOutputStream
     * @param out - DataOutputStream of HTTP request
     * @throws IOException - if write failed
     */
    public void dumbObjects(DataOutputStream out) throws IOException {
        for(String name : objects.keySet()) {
            out.writeBytes("--"+boundary+crlf);
            out.writeBytes(CONTENT_DISPOSITION+CONTENT_NAME+"\""+name+"\""+crlf);
            out.writeBytes("Content-Type: application/octet-stream"+crlf);
            out.writeBytes(crlf);
            out.write(objects.get(name).array());
            out.writeBytes(crlf);
        }
        out.writeBytes("--"+boundary+"--"+crlf);
    }
    
    /**
     * Generate String with random ascii symbols from 48 till 122 with length size.
     * 
     * @param size of String
     * @return String with random ascii symbols
     */
    public static String getRandomString(int size) {
        StringBuffer sb = new StringBuffer();
        for(int i=0; i < size; i++) {
            int j = rnd.nextInt(MULTIPART_CHARS.length);
            sb.append(MULTIPART_CHARS[j]);
        }
        return sb.toString();
    }

    /**
     * Multipart mixed boundary getter.
     * @return the boundary
     */
    public String getBoundary() {
        return boundary;
    }
    
    /**
     * Multipart mixed Content-Length with boundary getter.
     * @return the boundary
     */
    public String getContentType() {
        return CONTENT_TYPE_CONST+boundary;
    }
}
