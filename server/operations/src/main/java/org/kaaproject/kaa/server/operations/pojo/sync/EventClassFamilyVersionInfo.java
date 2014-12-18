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
package org.kaaproject.kaa.server.operations.pojo.sync;

public class EventClassFamilyVersionInfo {
    private String name;
    private int version;

    public EventClassFamilyVersionInfo() {
    }

    /**
     * All-args constructor.
     */
    public EventClassFamilyVersionInfo(String name, Integer version) {
        this.name = name;
        this.version = version;
    }

    /**
     * Gets the value of the 'name' field.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the 'name' field.
     * 
     * @param value
     *            the value to set.
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Gets the value of the 'version' field.
     */
    public Integer getVersion() {
        return version;
    }

    /**
     * Sets the value of the 'version' field.
     * 
     * @param value
     *            the value to set.
     */
    public void setVersion(Integer value) {
        this.version = value;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + version;
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
        EventClassFamilyVersionInfo other = (EventClassFamilyVersionInfo) obj;
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (version != other.version) {
            return false;
        }
        return true;
    }

}
