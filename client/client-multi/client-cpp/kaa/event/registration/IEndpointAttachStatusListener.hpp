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

#ifndef IENDPOINTATTACHSTATUSLISTENER_HPP_
#define IENDPOINTATTACHSTATUSLISTENER_HPP_

#include "kaa/KaaDefaults.hpp"

#ifdef KAA_USE_EVENTS

#include <string>

namespace kaa {

/**
 * Interface for listener to notify about attaching/detaching
 * current endpoint by another one
 */
class IEndpointAttachStatusListener {
public:

    /**
     * Callback on attaching either the current endpoint or another one
     *
     * @param userExternalId external id of the user to be attached
     * @param endpointAccessToken endpoint access token of the current endpoint
     *
     */
    virtual void onAttachSuccess(const std::string& userExternalId, const std::string& endpointAccessToken) = 0;

    /**
     * Callback on failure while attaching either current endpoint or another one
     */
    virtual void onAttachFailure() = 0;

    /**
     * Callback on detaching either the current endpoint or another one
     *
     * @param endpointAccessToken endpoint access token of the current endpoint
     *
     */
    virtual void onDetachSuccess(const std::string& endpointAccessToken) = 0;

    /**
     * Callback on failure while detaching either current endpoint or another one
     */
    virtual void onDetachFailure() = 0;

    virtual ~IEndpointAttachStatusListener() {}
};

} /* namespace kaa */

#endif

#endif /* IENDPOINTATTACHSTATUSLISTENER_HPP_ */
