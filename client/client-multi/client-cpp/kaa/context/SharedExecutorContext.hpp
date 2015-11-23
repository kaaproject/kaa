/*
 * Copyright 2014-2015 CyberVision, Inc.
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

#ifndef SHAREDEXECUTORCONTEXT_HPP_
#define SHAREDEXECUTORCONTEXT_HPP_

#include "kaa/KaaThread.hpp"
#include "kaa/context/SimpleExecutorContext.hpp"

namespace kaa {

class SharedExecutorContext: public SimpleExecutorContext {
public:
    SharedExecutorContext(std::size_t lifeCycleThreadCount = DEFAULT_THREAD_COUNT
                        , std::size_t apiThreadCount = DEFAULT_THREAD_COUNT
                        , std::size_t callbackThreadCount = DEFAULT_THREAD_COUNT)
     : SimpleExecutorContext(lifeCycleThreadCount, apiThreadCount, callbackThreadCount)
       , useCount_(0)
    {}

    virtual void init();
    virtual void stop();

private:
    std::size_t useCount_;
    KAA_MUTEX_DECLARE(useCountGuard_);
};

} /* namespace kaa */

#endif /* SHAREDEXECUTORCONTEXT_HPP_ */
