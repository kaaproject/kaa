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

#ifndef MOCKEXECUTORCONTEXT_HPP_
#define MOCKEXECUTORCONTEXT_HPP_

#include <memory>

#include "kaa/context/IExecutorContext.hpp"

#include "headers/utils/MockThreadPool.hpp"

namespace kaa {

class MockExecutorContext : public IExecutorContext {
public:
    MockExecutorContext() : threadPool_(std::make_shared<MockThreadPool>()) {}

    virtual void init() {}
    virtual void stop() {}

    virtual IThreadPool& getLifeCycleExecutor() { return *threadPool_; }
    virtual IThreadPool& getApiExecutor() { return *threadPool_; }
    virtual IThreadPool& getCallbackExecutor() { return *threadPool_; }

public:
    IThreadPoolPtr    threadPool_;
};

} /* namespace kaa */

#endif /* MOCKEXECUTORCONTEXT_HPP_ */
