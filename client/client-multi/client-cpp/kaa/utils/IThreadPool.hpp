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

#ifndef ITHREADPOOL_HPP_
#define ITHREADPOOL_HPP_

#include <chrono>
#include <functional>
#include <memory>

namespace kaa {

typedef std::function<void()> ThreadPoolTask;

class IThreadPool {
public:

    /**
     * @brief Add a task for execution.
     *
     * @param task The task to add.
     *
     * @throw std::invalid_argument The task object is invalid, i.e. empty.
     * @throw std::logic_error The thread pool is shut down.
     */
    virtual void add(const ThreadPoolTask& task) = 0;

    /**
     * @brief Blocks until all tasks have completed execution after
     * a shutdown request, or the timeout occurs.
     *
     * @note Call only after shutdown().
     *
     * @param seconds The maximum time in seconds to wait.
     *
     * @throw std::logic_error The method was called without shutdown().
     */
    virtual void awaitTermination(std::size_t seconds) = 0;

    /**
     * @brief Initiates a shutdown in which previously added tasks
     * are executed, but no new tasks will be accepted.
     */
    virtual void shutdown() = 0;

    /**
     * @brief Initiates a shutdown in which all executing tasks
     * will complete. Pending tasks will be declined.
     */
    virtual void shutdownNow() = 0;

    virtual ~IThreadPool() {}
};

typedef std::shared_ptr<IThreadPool>    IThreadPoolPtr;

} /* namespace kaa */

#endif /* ITHREADPOOL_HPP_ */
