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

package org.kaaproject.kaa.server.operations.service.delta.merge;


/**
 * The Class MergeConstants.
 */
public abstract class MergeConstants {

    /** The Constant FIELD_UUID. */
    public static final String FIELD_UUID = "__uuid";

    /** The Constant FIELD_UNCHANGED. */
    public static final String FIELD_UNCHANGED = "unchanged";

    /** The Constant FIELD_NAME. */
    public static final String FIELD_NAME = "name";
    
    /** The Constant FIELD_NAMESPACE. */
    public static final String FIELD_NAMESPACE = "namespace";
    
    /** The Constant FIELD_FIELDS. */
    public static final String FIELD_FIELDS = "fields";
    
    /** The Constant FIELD_TYPE. */
    public static final String FIELD_TYPE = "type";
    
    /** The Constant FIELD_ARRAY. */
    public static final String FIELD_ARRAY = "array";
    
    /** The Constant FIELD_MERGE_STRATEGY. */
    public static final String FIELD_OVERRIDE_STRATEGY = "overrideStrategy";
}
