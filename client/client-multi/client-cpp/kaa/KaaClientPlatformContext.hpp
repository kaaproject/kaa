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

#ifndef KAACLIENTPLATFORMCONTEXT_HPP_
#define KAACLIENTPLATFORMCONTEXT_HPP_

#include <memory>

#include "kaa/KaaClientProperties.hpp"
#include "kaa/IKaaClientPlatformContext.hpp"
#include "kaa/context/IExecutorContext.hpp"
#include "kaa/context/SimpleExecutorContext.hpp"
#include "kaa/channel/connectivity/PingConnectivityChecker.hpp"
#include "kaa/common/exception/KaaException.hpp"

namespace kaa {

class KaaClientPlatformContext : public IKaaClientPlatformContext {
public:

    KaaClientPlatformContext()
        : properties_(), executorContext_(std::make_shared<SimpleExecutorContext>())
    {}

    KaaClientPlatformContext(const KaaClientProperties& properties)
        : properties_(properties), executorContext_(std::make_shared<SimpleExecutorContext>())
    {}

    KaaClientPlatformContext(const KaaClientProperties& properties, IExecutorContextPtr executorContext)
        : properties_(properties), executorContext_(executorContext)
    {
        if (!executorContext_) {
            throw KaaException("Executor context is null");
        }
    }

    virtual KaaClientProperties& getProperties()
    {
        return properties_;
    }

    virtual std::unique_ptr<IConnectivityChecker> createConnectivityChecker()
    {
        return std::unique_ptr<IConnectivityChecker>(new PingConnectivityChecker);
    }

    virtual IExecutorContext& getExecutorContext()
    {
        return *executorContext_;
    }

    KaaClientPlatformContext(const KaaClientPlatformContext& properties) = delete;
    KaaClientPlatformContext& operator=(const KaaClientPlatformContext& properties) = delete;

    KaaClientPlatformContext(KaaClientPlatformContext&& properties) = delete;
    KaaClientPlatformContext& operator=(KaaClientPlatformContext&& properties) = delete;

    ~KaaClientPlatformContext() noexcept = default;

private:
    KaaClientProperties    properties_;
    IExecutorContextPtr    executorContext_;
};

} /* namespace kaa */

#endif /* KAACLIENTPLATFORMCONTEXT_HPP_ */
