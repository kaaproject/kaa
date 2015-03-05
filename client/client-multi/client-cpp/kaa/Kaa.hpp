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

#include "kaa/KaaClient.hpp"
#include <botan/botan.h>

namespace kaa {

/**
 * Entry point to the Kaa library.
 *
 * Responsible for the Kaa initialization and start/stop actions.
 */
class Kaa {
public:
    /**
     * Initialize Kaa library
     */
    static void init(int options = KaaClient::KAA_DEFAULT_OPTIONS);

    /**
     * Starts Kaa's workflow.
     */
    static void start();

    /**
     * Stops Kaa's workflow.
     */
    static void stop();

    /**
     * Retrieves the Kaa client.
     *
     * @return @link IKaaClient @endlink instance.
     *
     */
    static IKaaClient& getKaaClient();

    /**
     * Pauses Kaa's workflow.
     */
    static void pause();

    /**
     * Resumes Kaa's workflow.
     */
    static void resume();

private:
    Kaa();
    ~Kaa();
    Kaa(const Kaa&);
    Kaa& operator=(const Kaa&);

private:
    static Botan::LibraryInitializer botanInit_;
    static KaaClient client_;

};

}

#endif
