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

#ifndef SIMPLEEXECUTORCONTEXT_HPP_
#define SIMPLEEXECUTORCONTEXT_HPP_

#include <memory>

#include "kaa/context/AbstractExecutorContext.hpp"

namespace kaa {

class SimpleExecutorContext : public AbstractExecutorContext {
public:
    SimpleExecutorContext(std::size_t lifeCycleThreadCount = DEFAULT_THREAD_COUNT
                        , std::size_t apiThreadCount = DEFAULT_THREAD_COUNT
                        , std::size_t callbackThreadCount = DEFAULT_THREAD_COUNT);

    virtual IThreadPool& getLifeCycleExecutor() { return *lifeCycleExecutor_; }
    virtual IThreadPool& getApiExecutor() { return *apiExecutor_; }
    virtual IThreadPool& getCallbackExecutor() { return *callbackExecutor_; }

public:
    static const std::size_t DEFAULT_THREAD_COUNT = 1;

protected:
    virtual void doInit();
    virtual void doStop();

private:
    const std::size_t    apiThreadCount_;
    const std::size_t    callbackThreadCount_;
    const std::size_t    lifeCycleThreadCount_;

    IThreadPoolPtr    apiExecutor_;
    IThreadPoolPtr    callbackExecutor_;
    IThreadPoolPtr    lifeCycleExecutor_;
};

} /* namespace kaa */

#endif /* SIMPLEEXECUTORCONTEXT_HPP_ */
