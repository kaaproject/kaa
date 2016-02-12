/**
 *  Copyright 2014-2016 CyberVision, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

#ifndef ITHREADPOOL_HPP_
#define ITHREADPOOL_HPP_

#include <chrono>
#include <functional>
#include <memory>

namespace kaa {

typedef std::function<void()> ThreadPoolTask;

class IThreadPool {
public:
    virtual void add(const ThreadPoolTask& task) = 0;

    virtual void awaitTermination(std::size_t seconds) = 0;

    virtual void shutdown() = 0;
    virtual void shutdownNow() = 0;

    virtual ~IThreadPool() {}
};

typedef std::shared_ptr<IThreadPool>    IThreadPoolPtr;

} /* namespace kaa */

#endif /* ITHREADPOOL_HPP_ */
