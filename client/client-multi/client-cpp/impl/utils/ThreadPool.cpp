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

#include "kaa/utils/ThreadPool.hpp"

#include "kaa/logging/Log.hpp"
#include "kaa/common/exception/KaaException.hpp"


namespace kaa {

class Worker {
public:
    Worker(ThreadPool& threadPool) : threadPool_(threadPool) {}
    void operator() ();

private:
    ThreadPool& threadPool_;
};

void Worker::operator ()()
{
    KAA_MUTEX_UNIQUE_DECLARE(tasksLock, threadPool_.threadPoolGuard_);

    while (threadPool_.isRun_) {
        while (threadPool_.isRun_ && threadPool_.tasks_.empty() && !threadPool_.isPendingShutdown_) {
            KAA_CONDITION_WAIT(threadPool_.onNewTask_, tasksLock);
        }

        if (!threadPool_.isRun_ || (threadPool_.isPendingShutdown_ && threadPool_.tasks_.empty())) {
            return;
        }

        auto task = threadPool_.tasks_.front();
        threadPool_.tasks_.pop_front();

        KAA_UNLOCK(tasksLock);

        try {
            task();
        } catch (...) {}

        KAA_LOCK(tasksLock);
    }
}

ThreadPool::ThreadPool(std::size_t workerCount): workerCount_(workerCount)
{
    if (!workerCount_) {
        throw KaaException(boost::format("Failed to create thread pool with %u workers ") % workerCount_);
    }
}

ThreadPool::~ThreadPool()
{
    stop(!shutdownTimer_);
}

void ThreadPool::add(const ThreadPoolTask& task)
{
    if (!task) {
        throw KaaException("Failed to add task to thread pool: empty callback");
    }
    KAA_MUTEX_UNIQUE_DECLARE(tasksLock, threadPoolGuard_);

    if (isPendingShutdown_) {
        throw KaaException("Failed to add task to thread pool: pending shutdown");
    }

    if (workers_.empty()) {
        start();
    }

    tasks_.push_back(task);

    KAA_UNLOCK(tasksLock);

    onNewTask_.notify_one();
}

void ThreadPool::awaitTermination(std::size_t seconds)
{
    KAA_MUTEX_UNIQUE_DECLARE(tasksLock, threadPoolGuard_);

    if (isRun_) {
        shutdownTimer_.reset(new KaaTimer<void()>("Thread pool shutdown timer"));
        shutdownTimer_->start(seconds, [this] () { stop(true); } );
    }
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
        workers_.push_back(std::thread(Worker(*this)));
    }
}

void ThreadPool::stop(bool force)
{
    KAA_MUTEX_UNIQUE_DECLARE(tasksLock, threadPoolGuard_);

    if (!workers_.empty()) {
        if (force) {
            tasks_.clear();
            isRun_ = false;
        } else {
            isPendingShutdown_ = true;
        }

        onNewTask_.notify_all();

        KAA_UNLOCK(tasksLock);

        for (auto &worker : workers_) {
            if (worker.joinable()) {
                worker.join();
            }
        }
    }
}

} /* namespace kaa */
