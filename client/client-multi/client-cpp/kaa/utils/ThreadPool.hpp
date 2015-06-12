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

#ifndef THREADPOOL_HPP_
#define THREADPOOL_HPP_

#include <cstdlib>
#include <vector>
#include <thread>

#include "kaa/KaaThread.hpp"
#include "kaa/utils/IThreadPool.hpp"

namespace kaa {

class ThreadPool : public IThreadPool {
    friend class Worker;

public:
    ThreadPool() : ThreadPool(DEFAULT_WORKER_NUMBER) {}
    ThreadPool(std::size_t workerCount);
    ~ThreadPool();

    virtual void add(const ThreadPoolTask& task);

public:
    static const std::size_t DEFAULT_WORKER_NUMBER = 1;

private:
    void start();
    void stop();

private:
    bool isRun_ = true;

    std::list<std::thread>    workers_;
    std::size_t               workerCount_ = 0;

    std::list<ThreadPoolTask>   tasks_;
    KAA_MUTEX_DECLARE(tasksGuard_);

    KAA_CONDITION_VARIABLE      onNewTask_;
};

} /* namespace kaa */

#endif /* THREADPOOL_HPP_ */
