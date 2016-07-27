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

#include "kaa/utils/ThreadPool.hpp"

#include <chrono>
#include <stdexcept>

namespace kaa {

ThreadPool::ThreadPool(std::size_t workerCount)
    : workerCount_(workerCount)
{
    if (!workerCount_) {
        throw std::invalid_argument((boost::format("Wrong thread pool worker count %u") % workerCount_).str());
    }
}

ThreadPool::~ThreadPool()
{
    shutdownNow();
}

void ThreadPool::add(const ThreadPoolTask& task)
{
    if (!task) {
        throw std::invalid_argument("Null thread pool task");
    }

    {
        KAA_MUTEX_UNIQUE_DECLARE(tasksLock, threadPoolGuard_);

        if (state_ == State::CREATED) {
            start();
        }

        if (state_ != State::RUNNING) {
            throw std::runtime_error("Thread pool pending shutdown");
        }

        tasks_.push_back(task);
    }

    onNewTask_.notify_one();
}

void ThreadPool::awaitTermination(std::size_t seconds)
{
    std::this_thread::sleep_for(std::chrono::seconds(seconds));
    shutdownNow();
}

void ThreadPool::shutdown()
{
    stop();
}

void ThreadPool::shutdownNow()
{
    forceStop();
    waitForWorkersShutdown();
}

void ThreadPool::start()
{
    for (std::size_t i = 0; i < workerCount_; ++i) {
        workers_.emplace_back([this]()
            {
                while (true) {
                    ThreadPoolTask task;

                    {
                        KAA_MUTEX_UNIQUE_DECLARE(tasksLock, threadPoolGuard_);

                        onNewTask_.wait(tasksLock,
                                [this]{ return state_ == ThreadPool::State::STOPPED || !tasks_.empty(); });

                        if (state_ == ThreadPool::State::STOPPED && tasks_.empty()) {
                            return;
                        }

                        task = std::move(tasks_.front());
                        tasks_.pop_front();
                    }

                    try {
                        task();
                    } catch (...) {
                        // Just suppress an exception as
                        // it is unknown where to log this.
                    }
                }
            }
        );
    }

    state_ = State::RUNNING;
}

void ThreadPool::stop()
{
    {
        KAA_MUTEX_UNIQUE_DECLARE(tasksLock, threadPoolGuard_);
        state_ = State::PENDING_SHUTDOWN;
    }

    onNewTask_.notify_all();
}

void ThreadPool::forceStop()
{
    {
        KAA_MUTEX_UNIQUE_DECLARE(tasksLock, threadPoolGuard_);

        tasks_.clear();
        state_ = State::STOPPED;
    }

    onNewTask_.notify_all();
}

void ThreadPool::waitForWorkersShutdown()
{
    for (auto &worker : workers_) {
        if (worker.joinable()) {
            worker.join();
        }
    }
}

} /* namespace kaa */
