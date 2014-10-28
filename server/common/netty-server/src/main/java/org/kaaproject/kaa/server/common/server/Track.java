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

package org.kaaproject.kaa.server.common.server;

/**
 * Interface Track is used to collect statistics of requests.
 *
 * @author Andrey Panasenko
 */
public interface Track {
    /**
     * Set new request was received
     * @return int - request ID
     */
    public int newRequest();

    /**
     * Set time of processed request
     * @param requestId - used to identify request
     * @param time long
     */
    public void setProcessTime(int requestId, long time);

    /**
     * Set request was complete
     * @param requestId - used to identify request
     */
    public void closeRequest(int requestId);
}
