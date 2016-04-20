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

#ifndef IEXECUTORCONTEXT_HPP_
#define IEXECUTORCONTEXT_HPP_

#include <memory>

namespace kaa {

class IThreadPool;

class IExecutorContext {
public:
    /**
     * Initialize executors
     */
    virtual void init() = 0;

    /**
     * Stops executors
     */
    virtual void stop() = 0;

    /**
     * Executes lifecycle events/commands of Kaa client
     *
     * @return
     */
    virtual IThreadPool& getLifeCycleExecutor() = 0;

    /**
     * Executes user API calls to SDK client. For example, serializing of log
     * records before submit to transport
     *
     * @return
     */
    virtual IThreadPool& getApiExecutor() = 0;

    /**
     * Executes callback methods provided by SDK client user.
     *
     * @return
     */
    virtual IThreadPool& getCallbackExecutor() = 0;

    virtual ~IExecutorContext() = default;
};

typedef std::shared_ptr<IExecutorContext> IExecutorContextPtr;

} /* namespace kaa */

#endif /* IEXECUTORCONTEXT_HPP_ */
