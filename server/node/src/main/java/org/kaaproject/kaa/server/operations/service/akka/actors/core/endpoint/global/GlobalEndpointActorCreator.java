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

package org.kaaproject.kaa.server.operations.service.akka.actors.core.endpoint.global;

import org.kaaproject.kaa.common.hash.EndpointObjectHash;
import org.kaaproject.kaa.server.operations.service.akka.AkkaContext;
import org.kaaproject.kaa.server.operations.service.akka.actors.core.endpoint.EndpointActorCreator;

public class GlobalEndpointActorCreator extends EndpointActorCreator<GlobalEndpointActor> {

    private static final long serialVersionUID = 9080174513879065821L;

    public GlobalEndpointActorCreator(AkkaContext context, String endpointActorKey, String appToken, EndpointObjectHash key) {
        super(context, endpointActorKey, appToken, key);
    }

    @Override
    public GlobalEndpointActor create() throws Exception {
        return new GlobalEndpointActor(context, actorKey, appToken, endpointKey);
    }

}
