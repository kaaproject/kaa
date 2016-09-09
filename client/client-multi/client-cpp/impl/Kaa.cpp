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

#include "kaa/Kaa.hpp"

#include <memory>

#include "kaa/KaaClient.hpp"
#include "kaa/common/exception/KaaException.hpp"
#include "kaa/KaaClientPlatformContext.hpp"

namespace kaa {

Botan::LibraryInitializer Kaa::botanInit_("thread_safe=true");

std::shared_ptr<IKaaClient> Kaa::newClient(IKaaClientPlatformContextPtr context
                                         , KaaClientStateListenerPtr listener)
{
    if (!context) {
        throw KaaException("Kaa client platform context is null");
    }
    return std::shared_ptr<IKaaClient>(new KaaClient(context, listener));
}

}
