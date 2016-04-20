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

package org.kaaproject.kaa.server.sync.platform;

import org.kaaproject.kaa.server.sync.ClientSync;
import org.kaaproject.kaa.server.sync.ServerSync;

/**
 * The PlatformEncDec is used to decode platform level data to
 * {@link ClientSync} and encode {@link ServerSync}.
 */
public interface PlatformEncDec {

    /**
     * Returns id of the platform level protocol
     *
     */
    int getId();

    /**
     * Decodes platform level data to {@link ClientSync}.
     *
     * @param data
     *            the data to decode
     * @return the client sync
     * @throws PlatformEncDecException
     *             signals that decode exception has occurred.
     */
    ClientSync decode(byte[] data) throws PlatformEncDecException;

    /**
     * Encodes {@link ServerSync} to platform data.
     *
     * @param sync
     *            the sync to encode
     * @return the encoded platform data
     * @throws PlatformEncDecException
     *             signals that encode exception has occurred.
     */
    byte[] encode(ServerSync sync) throws PlatformEncDecException;

}
