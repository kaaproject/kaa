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

package org.kaaproject.kaa.server.common.core.configuration;

import org.kaaproject.kaa.server.common.core.schema.KaaSchema;

public abstract class AbstractKaaData<T extends KaaSchema> implements KaaData<T> {

    private static final long serialVersionUID = -2634202801757181444L;

    protected final T schema;
    protected final String data;

    public AbstractKaaData(T schema, String data){
        this.schema = schema;
        this.data = data;
    }

    @Override
    public T getSchema(){
        return schema;
    }

    @Override
    public String getRawData() {
        return data;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((data == null) ? 0 : data.hashCode());
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
        AbstractKaaData<T> other = (AbstractKaaData<T>) obj;
        if (data == null) {
            if (other.data != null) {
                return false;
            }
        } else if (!data.equals(other.data)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "AbstractKaaData [data=" + data + "]";
    }

}
