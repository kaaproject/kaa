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

#ifndef IKAACLIENTPLATFORMCONTEXT_HPP_
#define IKAACLIENTPLATFORMCONTEXT_HPP_

#include <memory>

#include "kaa/context/IExecutorContext.hpp"

namespace kaa {

class IExecutorContext;
class KaaClientProperties;
class IConnectivityChecker;

/**
 * Represents platform specific context for Kaa client initialization
 *
 * @author Denis Kimcherenko
 *
 */
class IKaaClientPlatformContext {
public:
    /**
     * @brief Returns platform SDK properties
     *
     * @return Reference to @c KaaClientProperties instance.
     */
    virtual KaaClientProperties& getProperties() = 0;

    /**
     * @brief Returns platform dependent implementation of @link ConnectivityChecker @endlink.
     *
     * @return Implementation of @link ConnectivityChecker @endlink.
     */
    virtual std::unique_ptr<IConnectivityChecker> createConnectivityChecker() = 0;

    /**
     * @brief Returns SDK thread execution context
     *
     * @return SDK thread execution context
     */
    virtual IExecutorContext& getExecutorContext() = 0;

    virtual ~IKaaClientPlatformContext() = default;
};

typedef std::shared_ptr<IKaaClientPlatformContext> IKaaClientPlatformContextPtr;

} /* namespace kaa */

#endif /* IKAACLIENTPLATFORMCONTEXT_HPP_ */
