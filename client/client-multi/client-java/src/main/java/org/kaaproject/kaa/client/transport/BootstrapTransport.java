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

package org.kaaproject.kaa.client.transport;

import org.kaaproject.kaa.common.bootstrap.gen.OperationsServerList;
import org.kaaproject.kaa.common.bootstrap.gen.Resolve;

/**
 * Interface for communication with bootstrap server.
 *
 * @author Yaroslav Zeygerman
 *
 */
public interface BootstrapTransport {

    /**
     * Sends Resolve request to the server.
     *
     * @param request the resolve request.
     * @return response from the server which contains the list of operations servers.
     *
     */
    OperationsServerList sendResolveRequest(Resolve request) throws TransportException;

}
