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

package org.kaaproject.kaa.server.operations.service.user;

import java.util.List;

import org.kaaproject.kaa.common.dto.EndpointProfileDto;
import org.kaaproject.kaa.common.dto.user.UserVerifierDto;
import org.kaaproject.kaa.server.operations.service.cache.AppSeqNumber;
import org.kaaproject.kaa.server.sync.EndpointAttachRequest;
import org.kaaproject.kaa.server.sync.EndpointAttachResponse;
import org.kaaproject.kaa.server.sync.EndpointDetachRequest;
import org.kaaproject.kaa.server.sync.EndpointDetachResponse;
import org.kaaproject.kaa.server.sync.EventListenersRequest;
import org.kaaproject.kaa.server.sync.EventListenersResponse;
import org.kaaproject.kaa.server.sync.UserAttachRequest;
import org.kaaproject.kaa.server.sync.UserAttachResponse;

public interface EndpointUserService {

    UserVerifierDto findUserVerifier(String appId, int verifierId);
    
    List<UserVerifierDto> findUserVerifiers(String appId);

    UserAttachResponse attachUser(EndpointProfileDto profile, UserAttachRequest userAttachRequest);

    EndpointAttachResponse attachEndpoint(EndpointProfileDto profile, EndpointAttachRequest endpointAttachRequest);

    EndpointDetachResponse detachEndpoint(EndpointProfileDto profile, EndpointDetachRequest endpointDetachRequest);

    EventListenersResponse findListeners(EndpointProfileDto profile, AppSeqNumber appSeqNumber, EventListenersRequest request);

}
