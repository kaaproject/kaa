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

#ifndef KAACLIENTCONTEXT_H
#define KAACLIENTCONTEXT_H

#include "IKaaClientContext.hpp"
#include <kaa/ClientStatus.hpp>
#include <kaa/KaaClientContext.hpp>

#include <kaa/KaaClientProperties.hpp>
#include <kaa/logging/ILogger.hpp>
#include <kaa/IKaaClientStateStorage.hpp>
#include <kaa/context/IExecutorContext.hpp>

namespace kaa {

class KaaClientContext : public IKaaClientContext
{
public:
    KaaClientContext(KaaClientProperties &properties,
        ILogger &logger,
        IExecutorContext &executorContext,
        IKaaClientStateStoragePtr state = nullptr,
        KaaClientStateListenerPtr stateListener = std::make_shared<KaaClientStateListener>())
        : properties_(properties),
          logger_(logger),
          executorContext_(executorContext),
          state_(state),
          stateListener_(stateListener) {}

    virtual KaaClientProperties         &getProperties() { return properties_; }
    virtual ILogger                     &getLogger() { return logger_; }
    virtual IKaaClientStateStorage      &getStatus() { return *state_; }
    virtual IExecutorContext            &getExecutorContext() { return executorContext_; }
    virtual KaaClientStateListener      &getClientStateListener() { return *stateListener_; }
    void  setStatus(IKaaClientStateStoragePtr status) { state_ = status; }

private:
    KaaClientProperties       &properties_;
    ILogger                   &logger_;
    IExecutorContext          &executorContext_;
    IKaaClientStateStoragePtr  state_;
    KaaClientStateListenerPtr stateListener_;
};

}


#endif // KAACLIENTCONTEXT_H
