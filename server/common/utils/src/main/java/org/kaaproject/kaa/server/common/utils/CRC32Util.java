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

package org.kaaproject.kaa.server.common.utils;

import java.nio.charset.Charset;
import java.util.zip.CRC32;

/**
 * 
 * An util class that provides convenient methods to get crc32 checksum from {@link String}
 * 
 * @author Andrew Shvayka
 *
 */
public class CRC32Util {

    private static final Charset UTF8 = Charset.forName("UTF-8");

    private CRC32Util() {
    }

    /**
     * Calculates the crc32 hash based on the name parameter.
     * 
     * @param name
     *            the name parameter
     * @return crc32 hash
     */
    public static int crc32(String name) {
        CRC32 crc32 = new CRC32();
        crc32.update(name.getBytes(UTF8));
        return (int) crc32.getValue();
    }
}
