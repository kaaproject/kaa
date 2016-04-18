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

#ifndef IATTACHSTATUSLISTENER_HPP_
#define IATTACHSTATUSLISTENER_HPP_

#include <string>
#include <memory>

namespace kaa {

/**
 * @brief Interface to the listener notifies of the current endpoint is attached/detached by the another endpoint.
 */
class IAttachStatusListener {
public:

    /**
     * @brief Callback is used when the current endpoint is attached to a user by the another endpoint.
     *
     * @param[in] userExternalId         The external id of the user to which the current endpoint is attached.
     * @param[in] endpointAccessToken    The access token of the endpoint that has attached the current one.
     *
     */
    virtual void onAttach(const std::string& userExternalId, const std::string& endpointAccessToken) = 0;

    /**
     * @brief Callback is used when the current endpoint is detached from the user by the another endpoint.
     *
     * @param[in] endpointAccessToken    The access token of the endpoint that has detached the current one.
     *
     */
    virtual void onDetach(const std::string& endpointAccessToken) = 0;

    virtual ~IAttachStatusListener() {}
};

typedef std::shared_ptr<IAttachStatusListener> IAttachStatusListenerPtr;

} /* namespace kaa */

#endif /* IATTACHSTATUSLISTENER_HPP_ */
