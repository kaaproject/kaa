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

package org.kaaproject.kaa.server.operations.service.cluster;

import org.kaaproject.kaa.server.operations.service.akka.messages.core.route.EndpointActorMsg;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.route.EndpointRouteMessage;

public interface ClusterServiceListener {

    /**
     * Process remote EndpointRouteMessage.
     * 
     * @param msg
     *            the endpoint route message
     */ 
    void onRouteMsg(EndpointRouteMessage msg);

    /**
     * Process endpoint actor message
     * 
     * @param msg 
     *            the endpoint actor message
     */
    void onEndpointActorMsg(EndpointActorMsg msg);

    
    /**
     * Reports update of cluster topology;
     */
    void onClusterUpdated();
    
}
