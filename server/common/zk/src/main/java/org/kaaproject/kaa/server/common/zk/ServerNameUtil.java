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

package org.kaaproject.kaa.server.common.zk;

import java.nio.charset.Charset;
import java.util.zip.CRC32;

import org.kaaproject.kaa.server.common.zk.gen.ConnectionInfo;

/**
 * 
 * An util class that provides convenient methods to get server name and hash of {@link ConnectionInfo}
 * 
 * @author Andrey Shvayka
 *
 */
public class ServerNameUtil {
    
    /** A delimiter in the DNS name host:port. */
    private static final String HOST_PORT_DELIMITER = ":";
    private static final Charset UTF8 = Charset.forName("UTF-8");

    private ServerNameUtil() {
    }

    /**
     * Builds the server name based on the connection info properties.
     * 
     * @param connectionInfo the connection info
     * @return name of the server
     */
    public static String getNameFromConnectionInfo(ConnectionInfo connectionInfo) {
        StringBuilder name = new StringBuilder();
        name.append(connectionInfo.getThriftHost());
        name.append(HOST_PORT_DELIMITER);
        name.append(connectionInfo.getThriftPort());
        return name.toString();
    }

    /**
     * Calculates the crc32 hash based on the connection info properties.
     * 
     * @param connectionInfo the connection info
     * @return crc32 hash
     */
    public static int crc32(ConnectionInfo connectionInfo) {
        CRC32 crc32 = new CRC32();
        crc32.update(getNameFromConnectionInfo(connectionInfo).getBytes(UTF8));
        return (int) crc32.getValue();
    }
}
