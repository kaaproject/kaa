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

#ifndef IATTACHEDENDPOINTLISTLISTENER_HPP_
#define IATTACHEDENDPOINTLISTLISTENER_HPP_

#include "kaa/KaaDefaults.hpp"

#include <map>
#include <string>

namespace kaa {

/**
 * Callback interface for attached endpoint list change notifications
 */
class IAttachedEndpointListListener
{
public:
    /**
     * Callback on attached endpoints list changed
     *
     * @param list Info about each attached endpoint as "token/key hash" pair
     */
    virtual void onListUpdated(const std::map<std::string/*epToken*/, std::string/*epHash*/>& list) = 0;
    virtual ~IAttachedEndpointListListener() {}
};

}

#endif /* IATTACHEDENDPOINTLISTLISTENER_HPP_ */
