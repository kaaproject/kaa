/*
 * Copyright 2014-2015 CyberVision, Inc.
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
package org.kaaproject.kaa.server.operations.service.akka.actors.core.user;

import org.kaaproject.kaa.server.operations.service.akka.AkkaContext;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.user.GlobalRouteInfoMessage;

public class GlobalUserActorMessageProcessor {
    
    private final String userId;
    private final String tenantId;

    public GlobalUserActorMessageProcessor(AkkaContext context, String userId, String tenantId) {
        this.userId = userId;
        this.tenantId = tenantId;
    }

    public void process(GlobalRouteInfoMessage message) {
        // TODO Auto-generated method stub
        
    }

}
