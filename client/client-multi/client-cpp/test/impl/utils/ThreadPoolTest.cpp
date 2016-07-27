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

#include <boost/test/unit_test.hpp>

#include <atomic>
#include <chrono>
#include <thread>
#include <functional>
#include <exception>

#include "kaa/utils/ThreadPool.hpp"

namespace kaa {


BOOST_AUTO_TEST_SUITE(ThreadPoolTestSuite)

BOOST_AUTO_TEST_CASE(ThreadPoolCreationtTest)
{
    BOOST_CHECK_NO_THROW({ ThreadPool pool; });
    BOOST_CHECK_NO_THROW({ ThreadPool pool(10); });

    BOOST_CHECK_THROW({ ThreadPool pool(0); }, std::exception);
}

BOOST_AUTO_TEST_CASE(BadTaskTest)
{
    ThreadPool pool;
    ThreadPoolTask task;

    BOOST_CHECK_THROW(pool.add(task), std::exception);
}

BOOST_AUTO_TEST_CASE(TaskWithExceptionTest)
{
    try {
        std::atomic_int executedTaskCounter(0);
        int expectedTaskCounter = 0;

        ThreadPool pool;
        ThreadPoolTask taskWithStrangeException = [&executedTaskCounter] ()
                                                                    {
                                                                        executedTaskCounter++;
                                                                        throw 1;
                                                                    };
        ThreadPoolTask taskWithNormalException = [&executedTaskCounter] ()
                                                                    {
                                                                        executedTaskCounter++;
                                                                        throw std::runtime_error("this is a test exception");
                                                                    };

        pool.add(taskWithStrangeException);
        ++expectedTaskCounter;
        pool.add(taskWithNormalException);
        ++expectedTaskCounter;

        while (executedTaskCounter.load() != expectedTaskCounter) {
            std::this_thread::sleep_for(std::chrono::seconds(2));
        }
    } catch (...) {
        BOOST_CHECK(false);
    }
}

class TestTaskSource {
public:
    TestTaskSource(IThreadPool& threadpool, std::function<void()> task, std::size_t taskCount)
        : threapool_(threadpool), task_(task), taskCount_(taskCount) {}

    void operator() ()
    {
        while (taskCount_--) {
            threapool_.add(task_);
        }
    }

private:
    IThreadPool& threapool_;

    std::function<void()> task_;
    std::size_t taskCount_;
};

BOOST_AUTO_TEST_CASE(AddThreadPoolTaskTest)
{
    std::mutex m;
    std::condition_variable onTestComplete;

    std::uint16_t expectedTaskCount = 0;
    std::atomic_uint actualTaskCount(0);

    const std::size_t workerCount = 3;
    const std::size_t taskSourceCount = 2 * workerCount;

    ThreadPool pool(workerCount);

    ThreadPoolTask task = [&actualTaskCount, &onTestComplete] ()
                              {
                                    std::this_thread::sleep_for(std::chrono::milliseconds((rand() % 1000) + 1));
                                    actualTaskCount++;
                                    onTestComplete.notify_one();
                              };

    std::vector<std::thread> taskSources;
    taskSources.reserve(taskSourceCount);
    for (std::size_t i = 1; i <= taskSourceCount; ++i) {
        std::size_t taskRepeat = rand() % 10 + 1;
        expectedTaskCount += taskRepeat;
        taskSources.push_back(std::thread(TestTaskSource(pool, task, taskRepeat)));
    }

    for (std::size_t i = 0; i < taskSourceCount; ++i) {
        if (taskSources[i].joinable()) {
            taskSources[i].join();
        }
    }

    {
        std::unique_lock<std::mutex> lock(m);
        onTestComplete.wait(lock, [&expectedTaskCount, &actualTaskCount] () { return actualTaskCount == expectedTaskCount; });
    }
}

BOOST_AUTO_TEST_CASE(ShutdownNowTest)
{
    ThreadPool threadPool;
    std::atomic_uint actualTaskCount(0);
    std::size_t timeToWait = 4;

    ThreadPoolTask task = [&actualTaskCount, &timeToWait] ()
                              {
                                    std::this_thread::sleep_for(std::chrono::seconds(timeToWait));
                                    actualTaskCount++;
                              };
    threadPool.add(task);
    threadPool.add(task);

    std::this_thread::sleep_for(std::chrono::seconds(timeToWait / 2));

    threadPool.shutdownNow();

    std::this_thread::sleep_for(std::chrono::seconds(timeToWait / 2 + 1));

    BOOST_CHECK_EQUAL(actualTaskCount.load(), 1);
}

BOOST_AUTO_TEST_CASE(PendingAllTaskShutdownTest)
{
    std::mutex m;
    std::condition_variable onTestComplete;

    ThreadPool threadPool;

    std::uint16_t expectedTaskCount = 0;
    std::atomic_uint actualTaskCount(0);
    std::size_t timeToWait = 4;

    ThreadPoolTask task = [&actualTaskCount, &timeToWait, &onTestComplete] ()
                              {
                                    std::this_thread::sleep_for(std::chrono::seconds(timeToWait));
                                    actualTaskCount++;
                                    onTestComplete.notify_one();
                              };
    threadPool.add(task);
    ++expectedTaskCount;

    threadPool.add(task);
    ++expectedTaskCount;

    std::this_thread::sleep_for(std::chrono::seconds(timeToWait / 2));

    threadPool.shutdown();

    {
        std::unique_lock<std::mutex> lock(m);
        onTestComplete.wait(lock, [&expectedTaskCount, &actualTaskCount] () { return actualTaskCount == expectedTaskCount; });
    }
}

BOOST_AUTO_TEST_CASE(AwaitTerminationWithoutShutdownTest)
{
    ThreadPool threadPool;

    // Just to init thread pool
    threadPool.add([] { });

    BOOST_CHECK_THROW(threadPool.awaitTermination(5), std::logic_error);
}

BOOST_AUTO_TEST_CASE(AwaitTerminationAllTaskCompletedTest)
{
    std::mutex mutex;
    std::condition_variable onStartCondition;

    ThreadPool threadPool;

    bool isStart = false;
    std::size_t taskExecutionTime = 1;
    std::uint16_t expectedTaskCount = rand() % 5 + 1;
    std::atomic_uint actualTaskCount(0);
    std::size_t ONE_YEAR_TO_WAIT = 365 * 24 * 3600;

    ThreadPoolTask task = [&mutex, &onStartCondition, &actualTaskCount, &isStart, &taskExecutionTime] ()
                              {
                                    {
                                        std::unique_lock<std::mutex> lock(mutex);
                                        onStartCondition.wait(lock, [&isStart] () { return isStart; });
                                    }

                                    std::this_thread::sleep_for(std::chrono::seconds(taskExecutionTime));
                                    actualTaskCount++;
                              };

    for (auto i = expectedTaskCount; i > 0; --i) {
        threadPool.add(task);
    }

    auto startTime = std::time(nullptr);

    {
        std::unique_lock<std::mutex> lock(mutex);
        isStart = true;
        onStartCondition.notify_all();
    }

    threadPool.shutdown();
    threadPool.awaitTermination(ONE_YEAR_TO_WAIT);

    auto finishTime = std::time(nullptr);

    BOOST_CHECK_EQUAL(actualTaskCount.load(), expectedTaskCount);
    BOOST_CHECK_LT((finishTime - startTime), ONE_YEAR_TO_WAIT);
}

BOOST_AUTO_TEST_CASE(AwaitTerminationTimeoutTest)
{
    std::mutex mutex;
    std::condition_variable onStartCondition;

    ThreadPool threadPool;

    bool isStart = false;
    std::size_t taskExecutionTime = 1;
    std::uint16_t expectedTaskCount = 0;
    std::atomic_uint actualTaskCount(0);
    std::size_t timeToWaitAllTasks = 0;

    ThreadPoolTask task = [&mutex, &onStartCondition, &actualTaskCount, &isStart, &taskExecutionTime] ()
                              {
                                    {
                                        std::unique_lock<std::mutex> lock(mutex);
                                        onStartCondition.wait(lock, [&isStart] () { return isStart; });
                                    }
                                    std::this_thread::sleep_for(std::chrono::seconds(taskExecutionTime));
                                    actualTaskCount++;
                              };

    // Executed.
    threadPool.add(task);
    ++expectedTaskCount;
    timeToWaitAllTasks += taskExecutionTime;

    // Executed.
    threadPool.add(task);
    ++expectedTaskCount;
    timeToWaitAllTasks += taskExecutionTime;

    // Declined.
    threadPool.add(task);
    timeToWaitAllTasks += taskExecutionTime;

    {
        std::unique_lock<std::mutex> lock(mutex);
        isStart = true;
        onStartCondition.notify_all();
    }

    threadPool.shutdown();
    threadPool.awaitTermination(expectedTaskCount * taskExecutionTime);

    // Assume no tasks have not been declined so wait for all of them
    std::this_thread::sleep_for(std::chrono::seconds(timeToWaitAllTasks));

    BOOST_CHECK_EQUAL(actualTaskCount.load(), expectedTaskCount);
}

BOOST_AUTO_TEST_SUITE_END()

}
