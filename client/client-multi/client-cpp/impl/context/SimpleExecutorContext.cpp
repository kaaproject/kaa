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

#include "kaa/context/SimpleExecutorContext.hpp"

#include "kaa/logging/Log.hpp"
#include "kaa/utils/IThreadPool.hpp"
#include "kaa/common/exception/KaaException.hpp"

namespace kaa {

SimpleExecutorContext::SimpleExecutorContext(std::size_t lifeCycleThreadCount, std::size_t apiThreadCount, std::size_t callbackThreadCount)
    : AbstractExecutorContext(), apiThreadCount_(apiThreadCount), callbackThreadCount_(callbackThreadCount)
    , lifeCycleThreadCount_(lifeCycleThreadCount)
{
    if (!lifeCycleThreadCount_ || !apiThreadCount_ || !callbackThreadCount_) {
        throw KaaException("Failed to crate executor context: bad input parameters");
    }
}

void SimpleExecutorContext::doInit()
{
    lifeCycleExecutor_ = createExecutor(lifeCycleThreadCount_);
    apiExecutor_ = createExecutor(apiThreadCount_);
    callbackExecutor_ = createExecutor(callbackThreadCount_);
}

void SimpleExecutorContext::doStop()
{
    shutdownExecutor(lifeCycleExecutor_);
    shutdownExecutor(apiExecutor_);
    shutdownExecutor(callbackExecutor_);
}

} /* namespace kaa */
