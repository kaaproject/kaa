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

#ifndef IDETACHENDPOINTCALLBACK_HPP_
#define IDETACHENDPOINTCALLBACK_HPP_

#include <memory>

namespace kaa {

/**
 * @brief Interface to the listener notifies of the result of whether the current endpoint
 * has successfully detached a target endpoint.
 *
 * The target endpoint will be detached to the user to which the current endpoint is attached.
 */
class IDetachEndpointCallback {
public:
    /**
     * @brief Callback is used when the current endpoint has successfully detached the target endpoint.
     */
    virtual void onDetachSuccess() = 0;

    /**
     * @brief Callback is used when the current endpoint has failed to detach the target endpoint.
     */
    virtual void onDetachFailed() = 0;

    virtual ~IDetachEndpointCallback() {}
};

typedef std::shared_ptr<IDetachEndpointCallback> IDetachEndpointCallbackPtr;

} /* namespace kaa */

#endif /* IDETACHENDPOINTCALLBACK_HPP_ */
