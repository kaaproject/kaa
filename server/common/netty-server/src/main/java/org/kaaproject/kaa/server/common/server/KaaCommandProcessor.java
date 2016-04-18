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

package org.kaaproject.kaa.server.common.server;

public interface KaaCommandProcessor<T, U> {

    /**
     * @return the commandId
     */
    int getCommandId();

    /**
     * @param commandId the commandId to set
     */
    void setCommandId(int commandId);

    /**
     * @return the syncTime
     */
    long getSyncTime();

    /**
     * @param syncTime the syncTime to set
     */
    void setSyncTime(long syncTime);

    /**
     * Retrieves a response.
     * @return response instance.
     */
    U getResponse();

    /**
     * Sets a response.
     *
     */
    void setResponse(U response);

    /**
     * @return request instance.
     */
    T getRequest();

    /**
     * Sets a request.
     *
     */
    void setRequest(T request);

    /**
     * @return command's name.
     */
    String getName();
}
