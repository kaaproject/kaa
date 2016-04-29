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

package org.kaaproject.kaa.server.common.core.algorithms.delta;

import java.io.IOException;
import java.io.Serializable;


/**
 * The Interface RawBinaryDelta.
 *
 * @author Yaroslav Zeygerman
 */
public interface RawBinaryDelta extends Serializable {

    /**
     * Gets the data.
     *
     * @return the data or null if there is no changes
     * @throws IOException Signals that an I/O exception has occurred.
     */
    byte[] getData() throws IOException;

    /**
     * Tells if the delta has any changes.
     *
     * @return true if there is any changes, false otherwise.
     */
    boolean hasChanges();
}
