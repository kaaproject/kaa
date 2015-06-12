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

#include <boost/test/unit_test.hpp>

#include <atomic>
#include <chrono>
#include <thread>
#include <functional>

#include "kaa/utils/ThreadPool.hpp"
#include "kaa/common/exception/KaaException.hpp"

namespace kaa {


BOOST_AUTO_TEST_SUITE(ThreadPoolTestSuite)

BOOST_AUTO_TEST_CASE(ThreadPoolCreationtTest)
{
    BOOST_CHECK_NO_THROW({ ThreadPool pool; });
    BOOST_CHECK_NO_THROW({ ThreadPool pool(10); });

    BOOST_CHECK_THROW({ ThreadPool pool(0); }, KaaException);
}

BOOST_AUTO_TEST_CASE(BadTaskTest)
{
    ThreadPool pool;
    ThreadPoolTask task;

    BOOST_CHECK_THROW(pool.add(task), KaaException);
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
//        std::cout << "Source #" << i << ": " << taskRepeat << " task(s)" << std::endl;
        taskSources.push_back(std::thread(TestTaskSource(pool, task, taskRepeat)));
    }

    for (std::size_t i = 0; i < taskSourceCount; ++i) {
        if (taskSources[i].joinable()) {
            taskSources[i].join();
        }
    }

    {
//        std::cout << "Waiting for task(s) will be executed..." << std::endl;
        std::unique_lock<std::mutex> lock(m);
        onTestComplete.wait(lock, [&expectedTaskCount, &actualTaskCount] () { return actualTaskCount == expectedTaskCount; });
    }

//    std::cout << "All tasks are executed" << std::endl;
}

BOOST_AUTO_TEST_SUITE_END()

}
