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

package org.kaaproject.kaa.server.common.core.algorithms;

public interface CommonConstants { //NOSONAR

    /** The Constant NAME_FIELD. */
    static final String NAME_FIELD = "name";

    /** The Constant NAMESPACE_FIELD. */
    static final String NAMESPACE_FIELD = "namespace";
    
    /** The Constant DISPLAY_NAME_FIELD. */
    static final String DISPLAY_NAME_FIELD = "displayName";
    
    /** The Constant FIELD_ACCESS_FIELD. */
    static final String FIELD_ACCESS_FIELD = "fieldAccess";
    
    /** The Constant FIELD_ACCESS_READ_ONLY. */
    static final String FIELD_ACCESS_READ_ONLY = "read_only";

    /** The Constant UUID_FIELD. */
    static final String UUID_FIELD = "__uuid";
    
    /** The Constant UUID_FIELD_DISPLAY_NAME. */
    static final String UUID_FIELD_DISPLAY_NAME = "Record Id";

    /** The Constant UUID_FIELD. */
    static final String UUID_TYPE = "uuidT";

    /** The Constant UUID_SIZE. */
    static final int UUID_SIZE = 16;

    /** The Constant TYPE_FIELD. */
    static final String TYPE_FIELD = "type";

    /** The Constant UNCHANGED. */
    static final String UNCHANGED = "unchanged";

    /** The Constant RESET. */
    static final String RESET = "reset";

    /** The Constant FIELDS_FIELD. */
    static final String FIELDS_FIELD = "fields";

    /** The Constant ITEMS_FIELD. */
    static final String ITEMS_FIELD = "items";

    /** The Constant ARRAY_FIELD. */
    static final String ARRAY_FIELD_VALUE = "array";

    /** The Constant UUID_SCHEMA_SPACE. */
    static final String KAA_NAMESPACE = "org.kaaproject.configuration";

    /** The Constant ENUM_FIELD_VALUE. */
    static final String ENUM_FIELD_VALUE = "enum";

    /** The Constant SYMBOLS_FIELD. */
    static final String SYMBOLS_FIELD = "symbols";

    /** The Constant RECORD_FIELD_VALUE. */
    static final String RECORD_FIELD_VALUE = "record";

    /** The Constant MAP_FIELD_VALUE. */
    static final String MAP_FIELD_VALUE = "map";

    /** The Constant FIXED_FIELD_VALUE. */
    static final String FIXED_FIELD_VALUE = "fixed";

    /** The Constant NULL_FIELD_VALUE. */
    static final String NULL_FIELD_VALUE = "null";

    /** The Constant BYTES_FIELD_VALUE. */
    static final String BYTES_FIELD_VALUE = "bytes";

    /** The Constant BY_DEFAULT_FIELD. */
    static final String BY_DEFAULT_FIELD = "by_default";

    /** The Constant SIZE_FIELD. */
    static final String SIZE_FIELD = "size";

    /** The Constant DELTA. */
    static final String DELTA = "delta";
}
