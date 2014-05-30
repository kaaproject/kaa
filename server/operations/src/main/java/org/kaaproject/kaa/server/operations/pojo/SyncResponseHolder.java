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

package org.kaaproject.kaa.server.operations.pojo;

import java.util.HashMap;
import java.util.Map;

import org.kaaproject.kaa.common.endpoint.gen.SyncResponse;


/**
 * The Class SyncResponseHolder.
 */
public class SyncResponseHolder {

    /** The response. */
    private final SyncResponse response;
    
    /** The subscription states. */
    private final Map<String, Integer> subscriptionStates;
    
    /** The system nf version. */
    private final int systemNfVersion;
    
    /** The user nf version. */
    private final int userNfVersion;

    /**
     * Instantiates a new sync response holder.
     *
     * @param response the response
     */
    public SyncResponseHolder(SyncResponse response){
        this(response, new HashMap<String, Integer>(), 0, 0);
    }
    
    /**
     * Instantiates a new sync response holder.
     *
     * @param response the response
     * @param subscriptionStates the subscription states
     */
    public SyncResponseHolder(SyncResponse response, Map<String, Integer> subscriptionStates){
        this(response, subscriptionStates, 0, 0);
    }    
    
    /**
     * Instantiates a new sync response holder.
     *
     * @param response the response
     * @param subscriptionStates the subscription states
     * @param systemNfVersion the system nf version
     * @param userNfVersion the user nf version
     */
    public SyncResponseHolder(SyncResponse response, Map<String, Integer> subscriptionStates, int systemNfVersion, int userNfVersion) {
        super();
        this.response = response;
        this.subscriptionStates = subscriptionStates;
        this.systemNfVersion = systemNfVersion;
        this.userNfVersion = userNfVersion;
    }

    /**
     * Gets the response.
     *
     * @return the response
     */
    public SyncResponse getResponse() {
        return response;
    }

    /**
     * Gets the subscription states.
     *
     * @return the subscription states
     */
    public Map<String, Integer> getSubscriptionStates() {
        return subscriptionStates;
    }

    /**
     * Gets the system nf version.
     *
     * @return the system nf version
     */
    public int getSystemNfVersion() {
        return systemNfVersion;
    }

    /**
     * Gets the user nf version.
     *
     * @return the user nf version
     */
    public int getUserNfVersion() {
        return userNfVersion;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("SyncResponseHolder [response=");
        builder.append(response);
        builder.append(", subscriptionStates=");
        builder.append(subscriptionStates);
        builder.append("]");
        return builder.toString();
    }
}
