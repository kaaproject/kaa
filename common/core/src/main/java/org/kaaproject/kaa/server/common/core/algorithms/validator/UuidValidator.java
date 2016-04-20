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

package org.kaaproject.kaa.server.common.core.algorithms.validator;

import java.io.IOException;

import org.apache.avro.generic.GenericRecord;
import org.kaaproject.kaa.server.common.core.configuration.KaaData;

public interface UuidValidator<T extends KaaData> {

    /**
     * Validates uuid fields.
     *
     * @param configurationToValidate configuration which fields are going to be validated
     * @param previousConfiguration configuration which will be used for comparison
     * @return the configuration with validated uuid fields
     * @throws IOException Signals that an I/O exception has occurred.
     */
    T validateUuidFields(T configurationToValidate, T previousConfiguration) throws IOException;

    /**
     * Validates uuid fields.
     *
     * @param configurationToValidate configuration which fields are going to be validated
     * @param previousConfiguration configuration which will be used for comparison
     * @return the configuration with validated uuid fields
     * @throws IOException Signals that an I/O exception has occurred.
     */
    T validateUuidFields(GenericRecord configurationToValidate, GenericRecord previousConfiguration) throws IOException;

}
