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

package org.kaaproject.kaa.server.operations.service.profile;

import java.util.function.BiFunction;
import java.util.function.Function;

import org.kaaproject.kaa.common.dto.EndpointProfileDto;
import org.kaaproject.kaa.common.hash.EndpointObjectHash;
import org.kaaproject.kaa.server.operations.pojo.RegisterProfileRequest;
import org.kaaproject.kaa.server.operations.pojo.UpdateProfileRequest;
import org.kaaproject.kaa.server.sync.ClientSyncMetaData;

/**
 * The interface ProfileService is used to model profile service. Profile
 * service is responsible for various profile actions: get, register, update
 * 
 * @author ashvayka
 */
public interface ProfileService {

    /**
     * Gets the profile.
     *
     * @param endpointKey
     *            the endpoint key
     * @return the profile
     */
    EndpointProfileDto getProfile(EndpointObjectHash endpointKey);

    /**
     * Update profile.
     *
     * @param profile
     *            the profile
     * @param mergeFunction
     *            the merge function
     * @return the updated endpoint profile dto
     */
    EndpointProfileDto updateProfile(EndpointProfileDto profile, BiFunction<EndpointProfileDto, EndpointProfileDto, EndpointProfileDto> mergeFunction);

    /**
     * Register profile.
     *
     * @param request
     *            the request
     * @return the endpoint profile dto
     */
    EndpointProfileDto registerProfile(RegisterProfileRequest request);


    EndpointProfileDto updateProfile(UpdateProfileRequest request);


    EndpointProfileDto updateProfile(ClientSyncMetaData metaData, EndpointObjectHash keyHash, boolean useConfigurationRawSchema);

}
