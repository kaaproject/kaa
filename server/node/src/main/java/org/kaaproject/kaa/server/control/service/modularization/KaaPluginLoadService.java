/*
 * Copyright 2015-2016 CyberVision, Inc.
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

package org.kaaproject.kaa.server.control.service.modularization;

import org.kaaproject.kaa.server.control.service.exception.KaaPluginLoadException;

/**
 * Enables Kaa plugins loading from the specified in the properties filesystem location
 */
public interface KaaPluginLoadService {

    /**
     * Loads plugin metadata to the database by the following rules:
     * 1. Only new plugin definitions are loaded, old definitions remain in database.
     * 2. If there is a definition in database, but not present in classpath/folder the exception is thrown.
     */
    void load() throws KaaPluginLoadException;
}
