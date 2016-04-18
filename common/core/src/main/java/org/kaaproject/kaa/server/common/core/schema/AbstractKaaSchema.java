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

package org.kaaproject.kaa.server.common.core.schema;

public abstract class AbstractKaaSchema implements KaaSchema {

    private static final long serialVersionUID = 3174607197960720521L;

    private final String rawSchema;

    public AbstractKaaSchema(String schema) {
        this.rawSchema = schema;
    }

    @Override
    public String getRawSchema() {
        return rawSchema;
    }

    @Override
    public boolean isEmpty() {
        return rawSchema.isEmpty();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((rawSchema == null) ? 0 : rawSchema.hashCode());
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
        AbstractKaaSchema other = (AbstractKaaSchema) obj;
        if (rawSchema == null) {
            if (other.rawSchema != null) {
                return false;
            }
        } else if (!rawSchema.equals(other.rawSchema)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "AbstractKaaSchema [rawSchema=" + rawSchema + "]";
    }

}
