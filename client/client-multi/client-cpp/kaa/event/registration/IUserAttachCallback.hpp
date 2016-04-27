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

#ifndef IUSERATTACHCALLBACK_HPP_
#define IUSERATTACHCALLBACK_HPP_

#include <string>
#include <memory>

#include "kaa/gen/EndpointGen.hpp"

namespace kaa {

/**
 * @brief Interface to the listener notifies of the result of whether the current endpoint
 * has successfully attached itself to the user.
 */
class IUserAttachCallback {
public:

    /**
     * @brief Callback is used when the current endpoint has successfully attached itself to the user.
     */
    virtual void onAttachSuccess() = 0;

    /**
     * @brief Callback is used when the current endpoint has failed to attach itself to the user.
     *
     * @param errorCode[in]    The error code.
     * @param reason[in]       The human readable description.
     *
     * @see UserAttachErrorCode
     */
    virtual void onAttachFailed(UserAttachErrorCode errorCode, const std::string& reason) = 0;

    virtual ~IUserAttachCallback() {}
};

typedef std::shared_ptr<IUserAttachCallback> IUserAttachCallbackPtr;

} /* namespace kaa */

#endif /* IUSERATTACHCALLBACK_HPP_ */
