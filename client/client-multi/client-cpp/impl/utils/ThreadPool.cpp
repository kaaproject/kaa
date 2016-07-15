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

#include "kaa/common/exception/KaaException.hpp"

namespace kaa {

class Worker {
public:
    Worker(ThreadPool& threadPool)
        : threadPool_(threadPool)
    { }

    void operator() ()
    {
        while (true) {
            ThreadPoolTask task;

            {
                KAA_MUTEX_UNIQUE_DECLARE(tasksLock, threadPool_.threadPoolGuard_);

                threadPool_.onNewTask_.wait(tasksLock,
                        [this]{ return threadPool_.state_ == ThreadPool::State::STOPPED || !threadPool_.tasks_.empty(); });

                if (threadPool_.state_ == ThreadPool::State::STOPPED && threadPool_.tasks_.empty()) {
                    return;
                }

                task = std::move(threadPool_.tasks_.front());
                threadPool_.tasks_.pop_front();
            }

            try {
                task();
            } catch (...) {}
        }
    }

private:
    ThreadPool& threadPool_;
};

ThreadPool::ThreadPool(std::size_t workerCount)
    : workerCount_(workerCount)
{
    if (!workerCount_) {
        throw std::invalid_argument((boost::format("Wrong thread pool worker count %u") % workerCount_).str());
    }
}

ThreadPool::~ThreadPool()
{
    stop(true);

    for (auto &worker : workers_) {
        if (worker.joinable()) {
            worker.join();
        }
    }
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
    stop(true);
}

void ThreadPool::shutdown()
{
    stop(false);
}

void ThreadPool::shutdownNow()
{
    stop(true);
}

void ThreadPool::start()
{
    for (std::size_t i = 0; i < workerCount_; ++i) {
        workers_.emplace_back(Worker(*this));
    }

    state_ = State::RUNNING;
}

void ThreadPool::stop(bool force)
{
    {
        KAA_MUTEX_UNIQUE_DECLARE(tasksLock, threadPoolGuard_);

        if (force) {
            tasks_.clear();
            state_ = State::STOPPED;
        } else {
            state_ = State::PENDING_SHUTDOWN;
        }
    }

    onNewTask_.notify_all();
}

} /* namespace kaa */
