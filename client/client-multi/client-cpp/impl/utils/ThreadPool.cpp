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
    KAA_LOG_TRACE(boost::format("Starting thread pool worker [0x%x]") % std::this_thread::get_id());

    KAA_MUTEX_LOCKING("tasksGuard_");
    KAA_MUTEX_UNIQUE_DECLARE(tasksLock, threadPool_.threadPoolGuard_);
    KAA_MUTEX_LOCKED("tasksGuard_");

    while (threadPool_.isRun_) {
        while (threadPool_.isRun_ && threadPool_.tasks_.empty() && !threadPool_.isPendingShutdown_) {
            KAA_CONDITION_WAIT(threadPool_.onNewTask_, tasksLock);
        }

        if (!threadPool_.isRun_ || (threadPool_.isPendingShutdown_ && threadPool_.tasks_.empty())) {
            return;
        }

        auto task = threadPool_.tasks_.front();
        threadPool_.tasks_.pop_front();

        KAA_MUTEX_UNLOCKING("tasksGuard_");
        KAA_UNLOCK(tasksLock);
        KAA_MUTEX_UNLOCKED("tasksGuard_");

        try {
            task();
        } catch (...) {
            KAA_LOG_WARN("Caught exception while executing thread pool task");
        }

        KAA_MUTEX_LOCKING("tasksGuard_");
        KAA_LOCK(tasksLock);
        KAA_MUTEX_LOCKED("tasksGuard_");
    }
}

ThreadPool::ThreadPool(std::size_t workerCount): workerCount_(workerCount)
{
    if (!workerCount_) {
        KAA_LOG_ERROR(boost::format("Failed to create thread pool with %u workers ") % workerCount_);
        throw KaaException(boost::format("Failed to create thread pool with %u workers ") % workerCount_);
    }
    KAA_LOG_INFO(boost::format("Creating thread pool with %u workers ") % workerCount_);
}

ThreadPool::~ThreadPool()
{
    stop(!shutdownTimer_);
}

void ThreadPool::add(const ThreadPoolTask& task)
{
    if (!task) {
        KAA_LOG_WARN("Failed to add task to thread pool: empty callback");
        throw KaaException("Failed to add task to thread pool: empty callback");
    }

    KAA_MUTEX_LOCKING("tasksGuard_");
    KAA_MUTEX_UNIQUE_DECLARE(tasksLock, threadPoolGuard_);
    KAA_MUTEX_LOCKED("tasksGuard_");

    if (isPendingShutdown_) {
        KAA_LOG_WARN("Failed to add task to thread pool: pending shutdown");
        throw KaaException("Failed to add task to thread pool: pending shutdown");
    }

    if (workers_.empty()) {
        start();
    }

    tasks_.push_back(task);

    KAA_MUTEX_UNLOCKING("tasksGuard_");
    KAA_UNLOCK(tasksLock);
    KAA_MUTEX_UNLOCKED("tasksGuard_");

    onNewTask_.notify_one();
}

void ThreadPool::awaitTermination(std::size_t seconds)
{
    KAA_MUTEX_LOCKING("tasksGuard_");
    KAA_MUTEX_UNIQUE_DECLARE(tasksLock, threadPoolGuard_);
    KAA_MUTEX_LOCKED("tasksGuard_");

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
    KAA_LOG_INFO(boost::format("Going to launch %u thread pool workers") % workerCount_);

    for (std::size_t i = 0; i < workerCount_; ++i) {
        workers_.push_back(std::thread(Worker(*this)));
    }
}

void ThreadPool::stop(bool force)
{
    KAA_LOG_INFO(boost::format("Going to stop %u thread pool workers (isRun=%s)")
                            % workerCount_ % (workers_.empty() ? "false" : "true"));

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
