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

package org.kaaproject.kaa.common.dto.ctl;

import java.io.Serializable;

/**
 * Uniquely identifies a Common Type Library schema that other schemas may use
 * as a dependency for their own definitions.
 * 
 * @author Andrew Shvayka
 * @author Bohdan Khablenko
 * 
 * @see org.kaaproject.kaa.common.dto.ctl.CTLSchemaDto
 * 
 * @since v0.8.0
 */
public class CTLDependencyDto implements Serializable {

    private static final long serialVersionUID = 8463948575247107993L;

    private String fqn;
    private int version;

    public CTLDependencyDto(String fqn, int version) {
        this.fqn = fqn;
        this.version = version;
    }

    public String getFqn() {
        return fqn;
    }

    public void setFqn(String fqn) {
        this.fqn = fqn;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    @Override
    public int hashCode() {
        final int prime = 31;

        int result = 1;

        result = prime * result + ((fqn == null) ? 0 : fqn.hashCode());
        result = prime * result + version;

        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null) {
            return false;
        }

        if (this.getClass() != o.getClass()) {
            return false;
        }

        CTLDependencyDto other = (CTLDependencyDto) o;

        if (this.fqn == null) {
            if (other.fqn != null) {
                return false;
            }
        } else if (!this.fqn.equals(other.fqn)) {
            return false;
        }

        if (this.version != other.version) {
            return false;
        }

        return true;
    }

    @Override
    public String toString() {
        return "CTLDependencyDto [fqn=" + fqn + ", version=" + version + "]";
    }
}
