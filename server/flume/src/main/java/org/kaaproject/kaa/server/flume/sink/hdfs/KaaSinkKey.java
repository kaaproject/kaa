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
package org.kaaproject.kaa.server.flume.sink.hdfs;

import java.util.Map;

import org.apache.hadoop.fs.Path;

public class KaaSinkKey implements EventConstants {

    private final String applicationToken;
    private final int schemaVersion;
    
    public KaaSinkKey(String applicationToken,
            int schemaVersion) {
        super();
        this.applicationToken = applicationToken;
        this.schemaVersion = schemaVersion;
    }
    
    public KaaSinkKey(Map<String,String> headers) {
        this.applicationToken = headers.get(APPLICATION_TOKEN_HEADER);
        this.schemaVersion = Integer.valueOf(headers.get(SCHEMA_VERSION_HEADER));
    }

    public String getApplicationToken() {
        return applicationToken;
    }

    public int getSchemaVersion() {
        return schemaVersion;
    }
    
    public void updateHeaders(Map<String,String> headers) {
        headers.put(APPLICATION_TOKEN_HEADER, applicationToken);
        headers.put(SCHEMA_VERSION_HEADER, ""+schemaVersion);
    }
    
    public String getPath() {
        return applicationToken + Path.SEPARATOR + schemaVersion;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime
                * result
                + ((applicationToken == null) ? 0 : applicationToken.hashCode());
        result = prime * result + schemaVersion;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        KaaSinkKey other = (KaaSinkKey) obj;
        if (applicationToken == null) {
            if (other.applicationToken != null) {
                return false;
            }
        } else if (!applicationToken.equals(other.applicationToken)) {
            return false;
        }
        if (schemaVersion != other.schemaVersion) {
            return false;
        }
        return true;
    }


    @Override
    public String toString() {
        return "KaaSinkKey [applicationToken=" + applicationToken
                + ", schemaVersion=" + schemaVersion + "]";
    }

}
