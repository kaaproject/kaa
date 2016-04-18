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

#ifndef SINGLETHREADEXECUTORCONTEXT_HPP_
#define SINGLETHREADEXECUTORCONTEXT_HPP_

#include "kaa/context/AbstractExecutorContext.hpp"

namespace kaa {

class SingleThreadExecutorContext: public AbstractExecutorContext {
public:
    SingleThreadExecutorContext()
        : AbstractExecutorContext() {}

    virtual IThreadPool& getLifeCycleExecutor() { return *executor_; }
    virtual IThreadPool& getApiExecutor() { return *executor_; }
    virtual IThreadPool& getCallbackExecutor() { return *executor_; }

protected:
    virtual void doInit() { executor_ = createExecutor(1); }
    virtual void doStop() { shutdownExecutor(executor_); }

private:
    IThreadPoolPtr    executor_;
};

} /* namespace kaa */

#endif /* SINGLETHREADEXECUTORCONTEXT_HPP_ */
