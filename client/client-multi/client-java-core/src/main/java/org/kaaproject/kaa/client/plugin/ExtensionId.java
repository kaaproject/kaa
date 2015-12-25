/*
 * Copyright 2014-2015 CyberVision, Inc.
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
package org.kaaproject.kaa.client.plugin;

/**
 * Represents unique identifier of plugin instance within current SDK profile.
 * 
 * @author Andrew Shvayka
 *
 */
public class ExtensionId {

    private final int id;

    public ExtensionId(int id) {
        super();
        this.id = id;
    }

    /**
     * Integer extension id value that is used in transport layer
     * 
     * @return extension id
     */
    public int getValue() {
        return id;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj instanceof ExtensionId) {
            ExtensionId extensionId = (ExtensionId)obj;
            return this.id == extensionId.id;
        }

        return false;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + id;
        return result;
    }

    @Override
    public String toString() {
        return "ExtensionId [id=" + id + "]";
    }

}
