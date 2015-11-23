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

#ifndef KAA_HPP_
#define KAA_HPP_

#include <memory>

#include <botan/botan.h>

#include "kaa/IKaaClient.hpp"
#include "kaa/IKaaClientStateListener.hpp"
#include "kaa/IKaaClientPlatformContext.hpp"
#include "kaa/KaaClientPlatformContext.hpp"

#ifdef __GNUC__
#define KAA_DEPRECATED __attribute__ ((deprecated))
#elif defined(_MSC_VER)
#define KAA_DEPRECATED __declspec(deprecated)
#else
#pragma message("WARNING: You need to implement KAA_DEPRECATED for this compiler")
#endif

namespace kaa {

/**
 * @brief Creates a new Kaa client based on @link IKaaClientPlatformContext @endlink and optional
 * @link IKaaClientStateListener @endlink.
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
     * @note In case of sharing a single instance of a platform context among several Kaa clients, use
     * @link SharedExecutorContext @endlink as an executor.
     *
     * @param context     Platform context.
     * @param listener    Kaa client state listener.
     *
     * @return New instance of Kaa client.
     */
    static std::shared_ptr<IKaaClient> newClient(IKaaClientPlatformContextPtr context = std::make_shared<KaaClientPlatformContext>()
                                               , IKaaClientStateListenerPtr listener = IKaaClientStateListenerPtr());

    /**
     * @brief Initialize Kaa library.
     *
     * @deprecated Use @link newClient() @endlink to create a Kaa client instance.
     */
    KAA_DEPRECATED
    static void init(int options = 0/* unused */);

    /**
     * @brief Starts Kaa's workflow.
     *
     * @deprecated Use @link kaa::IKaaClient::start().
     */
    KAA_DEPRECATED
    static void start();

    /**
     * @brief Stops Kaa's workflow.
     *
     * @deprecated Use @link kaa::IKaaClient::stop().
     */
    KAA_DEPRECATED
    static void stop();

    /**
     * @brief Pauses Kaa's workflow.
     *
     * @deprecated Use @link kaa::IKaaClient::pause().
     */
    KAA_DEPRECATED
    static void pause();

    /**
     * @brief Resumes Kaa's workflow.
     *
     * @deprecated Use @link kaa::IKaaClient::resume().
     */
    KAA_DEPRECATED
    static void resume();

    /**
     * @brief Retrieves the Kaa client.
     *
     * @deprecated Use @link newClient() @endlink to create a Kaa client instance.
     *
     * @return @link IKaaClient @endlink instance.
     */
    KAA_DEPRECATED
    static IKaaClient& getKaaClient();

private:
    static Botan::LibraryInitializer     botanInit_;
    static std::shared_ptr<IKaaClient>   client_;
};

}

#endif
