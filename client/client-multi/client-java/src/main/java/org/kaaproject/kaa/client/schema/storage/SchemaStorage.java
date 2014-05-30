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

package org.kaaproject.kaa.client.schema.storage;

import java.nio.ByteBuffer;

/**
 * Interface for object which is going to save and load schema
 *
 * @author Yaroslav Zeygerman
 *
 */
public interface SchemaStorage {

    /**
     * Saves schema
     *
     * @param buffer buffer with schema
     *
     */
    void saveSchema(ByteBuffer buffer);

    /**
     * Loads schema
     *
     * @return buffer with loaded schema, or null if schema is empty
     *
     */
    ByteBuffer loadSchema();

}
