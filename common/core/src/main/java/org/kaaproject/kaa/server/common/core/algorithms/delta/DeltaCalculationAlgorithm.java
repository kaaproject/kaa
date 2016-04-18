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

import org.kaaproject.kaa.server.common.core.configuration.BaseData;

/**
 * Performs total list of deltas from given old and new configurations.
 *
 * @author Yaroslav Zeygerman
 */
public interface DeltaCalculationAlgorithm {

    /**
     * Calculates delta merging new and old configurations.
     *
     * @param oldConfiguration old configuration data (binary)
     * @param newConfigurationBody the new configuration body (binary)
     * @return the raw binary delta
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws DeltaCalculatorException the delta calculator exception
     */
    RawBinaryDelta calculate(BaseData endpointConfiguration, BaseData newConfigurationBody)
            throws IOException, DeltaCalculatorException;

    /**
     * Calculates delta using only new configuration (no merging).
     *
     * @param newConfigurationBody the new configuration body (binary)
     * @return the raw binary delta
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws DeltaCalculatorException the delta calculator exception
     */
    RawBinaryDelta calculate(BaseData newConfigurationBody) throws IOException, DeltaCalculatorException;
}
