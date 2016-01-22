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

package org.kaaproject.kaa.server.plugin.rest;

import org.springframework.web.client.HttpClientErrorException;

/**
 * An HTTP request processor.
 *
 * @author Bohdan Khablenko
 *
 * @see KaaRestPlugin
 *
 * @since v1.0.0
 */
@FunctionalInterface
public interface RequestProcessor {

    /**
     * Sends an HTTP request and retrieves the response.
     *
     * @param request Detailed information about an HTTP request to send
     *
     * @return The server response
     *
     * @throws HttpClientErrorException - if the server responds with an error
     *             code.
     */
    byte[] send(HttpRequestDetails request) throws HttpClientErrorException;
}
