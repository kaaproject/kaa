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

import org.apache.hadoop.fs.Path;

public class HdfsSinkKey {

    private String rootPath;
    private KaaSinkKey kaaSinkKey;
    
    public HdfsSinkKey(String rootPath, KaaSinkKey kaaSinkKey) {
        super();
        this.rootPath = rootPath;
        this.kaaSinkKey = kaaSinkKey;
    }

    public String getRootPath() {
        return rootPath;
    }

    public void setRootPath(String rootPath) {
        this.rootPath = rootPath;
    }
    
    public KaaSinkKey getKaaSinkKey() {
        return kaaSinkKey;
    }

    public void setKaaSinkKey(KaaSinkKey kaaSinkKey) {
        this.kaaSinkKey = kaaSinkKey;
    }

    public String getPath() {
        return rootPath + Path.SEPARATOR + kaaSinkKey.getPath();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((kaaSinkKey == null) ? 0 : kaaSinkKey.hashCode());
        result = prime * result
                + ((rootPath == null) ? 0 : rootPath.hashCode());
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
        HdfsSinkKey other = (HdfsSinkKey) obj;
        if (kaaSinkKey == null) {
            if (other.kaaSinkKey != null) {
                return false;
            }
        } else if (!kaaSinkKey.equals(other.kaaSinkKey)) {
            return false;
        }
        if (rootPath == null) {
            if (other.rootPath != null) {
                return false;
            }
        } else if (!rootPath.equals(other.rootPath)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "HdfsSinkKey [rootPath=" + rootPath + ", kaaSinkKey="
                + kaaSinkKey + "]";
    }
    
}
