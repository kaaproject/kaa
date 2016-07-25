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

package org.kaaproject.kaa.common.dto.logs;

import org.kaaproject.kaa.common.dto.BaseSchemaDto;

public class LogSchemaDto extends BaseSchemaDto {

    private static final long serialVersionUID = -7023640650614573350L;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof LogSchemaDto)) {
            return false;
        }
        
        return super.equals(o);
    }

    @Override
    public int hashCode() { //NOSONAR
        return super.hashCode();
    }

    @Override
    public String toString() {
        return "LogSchemaDto{" + super.toString() +
                '}';
    }
}
