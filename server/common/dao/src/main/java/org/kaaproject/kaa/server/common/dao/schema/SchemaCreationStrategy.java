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

package org.kaaproject.kaa.server.common.dao.schema;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The Interface SchemaCreationStrategy.
 */
public interface SchemaCreationStrategy {

    /**
     * Checks if is array editable.
     *
     * @return true, if is array editable
     */
    boolean isArrayEditable();

    /**
     * Checks if is uuid optional.
     *
     * @return true, if is uuid optional
     */
    boolean isUuidOptional();

    /**
     * Checks if is unchanged supported.
     *
     * @return true, if is unchanged supported
     */
    boolean isUnchangedSupported();

    /**
     * On optional field listener.
     *
     * @param union the list of union type objects
     */
    void onOptionalField(List<Object> union);

    /**
     * On mandatory field listener.
     *
     * @param union the list of union type objects
     */
    void onMandatoryField(List<Object> union);

    /**
     * On schema processed.
     *
     * @param rootSchema the root schema
     * @param addressableRecords the addressable records
     * @return the map
     */
    Map<String, Object> onSchemaProcessed(Map<String, Object> rootSchema, Set<String> addressableRecords);
}
