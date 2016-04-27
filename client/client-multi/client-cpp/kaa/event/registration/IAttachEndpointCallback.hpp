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

#ifndef IATTACHENDPOINTCALLBACK_HPP_
#define IATTACHENDPOINTCALLBACK_HPP_

#include <string>
#include <memory>

namespace kaa {

/**
 * @brief Interface to the listener notifies of the result of whether the current endpoint
 * has successfully attached a target endpoint.
 *
 * The target endpoint will be attached to the user to which the current endpoint is attached.
 */
class IAttachEndpointCallback {
public:
    /**
     * @brief Callback is used when the current endpoint has successfully attached the target endpoint.
     *
     * @param[in] endpointKeyHash    The key hash of the attached endpoint.
     */
    virtual void onAttachSuccess(const std::string& endpointKeyHash) = 0;

    /**
     * @brief Callback is used when the current endpoint has failed to attach the target endpoint.
     */
    virtual void onAttachFailed() = 0;

    virtual ~IAttachEndpointCallback() {}
};

typedef std::shared_ptr<IAttachEndpointCallback> IAttachEndpointCallbackPtr;

} /* namespace kaa */

#endif /* IATTACHENDPOINTCALLBACK_HPP_ */
