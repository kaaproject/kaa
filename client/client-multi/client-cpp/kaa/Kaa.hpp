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

#ifndef KAA_HPP_
#define KAA_HPP_

#include <memory>

#include <botan/botan.h>
#include <botan/init.h>

#include "kaa/IKaaClient.hpp"
#include "kaa/KaaClientStateListener.hpp"
#include "kaa/IKaaClientPlatformContext.hpp"
#include "kaa/KaaClientPlatformContext.hpp"

namespace kaa {

/**
 * @brief Creates a new Kaa client based on @link IKaaClientPlatformContext @endlink and optional
 * @link KaaClientStateListener @endlink.
 *
 */
class Kaa
{
public:
    /**
     * @brief Use @link newClient() @endlink to create a Kaa client instance.
     */
    Kaa() = delete;

    /**
     * @brief Use @link newClient() @endlink to create a Kaa client instance.
     */
    Kaa(const Kaa&) = delete;

    /**
     * @brief Use @link newClient() @endlink to create a Kaa client instance.
     */
    Kaa& operator=(const Kaa&) = delete;

    /**
     * @brief Creates a new instance of a Kaa client.
     *
     * @param context     Platform context.
     * @param listener    Kaa client state listener.
     *
     * @return New instance of Kaa client.
     */
    static std::shared_ptr<IKaaClient> newClient(
        IKaaClientPlatformContextPtr context = std::make_shared<KaaClientPlatformContext>(),
        KaaClientStateListenerPtr listener = std::make_shared<KaaClientStateListener>());

private:
    static Botan::LibraryInitializer     botanInit_;
};

}

#endif
