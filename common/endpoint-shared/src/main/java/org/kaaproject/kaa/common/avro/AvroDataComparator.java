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

package org.kaaproject.kaa.common.avro;

import java.util.Comparator;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;

public class AvroDataComparator implements Comparator<Object> {
    private Schema schema;

    public AvroDataComparator() {
        super();
    }

    @Override
    public int compare(Object o1, Object o2) {
        return GenericData.get().compare(o1, o2, schema);
    }

    public void setSchema(Schema schema) {
        this.schema = schema;
    }
}
