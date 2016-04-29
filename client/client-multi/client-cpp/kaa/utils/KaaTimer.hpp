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

#ifndef KAATIMER_HPP_
#define KAATIMER_HPP_

#include <chrono>
#include <mutex>
#include <thread>
#include <functional>
#include <condition_variable>

#include "kaa/KaaThread.hpp"
#include "kaa/logging/Log.hpp"
#include "kaa/common/exception/KaaException.hpp"

namespace kaa {

template<class Signature, class Function = std::function<Signature>>
class KaaTimer {
    typedef std::chrono::system_clock TimerClock;
public:
    KaaTimer(const std::string& timerName) :
        timerName_(timerName), isThreadRun_(false), isTimerRun_(false), callback_([]{})
    {
    }

    ~KaaTimer()
    {
        /*
         * Do not add the mutex logging it may cause crashes.
         */
        if (isThreadRun_ && timerThread_.joinable()) {
            std::unique_lock<std::mutex> timerLock(timerGuard_);

            isThreadRun_ = false;
            condition_.notify_one();

            timerLock.unlock();

            timerThread_.join();
        }
    }

    void start(std::size_t seconds, const Function& callback)
    {
        if (!callback) {
            throw KaaException("Bad timer callback");
        }
        std::unique_lock<std::mutex> timerLock(timerGuard_);

        if (!isThreadRun_) {
            isThreadRun_ = true;
            timerThread_ = std::thread([&] { run(); });
        }

        if (!isTimerRun_) {
            endTS_ = TimerClock::now() + std::chrono::seconds(seconds);
            isTimerRun_ = true;
            callback_ = callback;
            condition_.notify_one();
        }
    }

    void stop()
    {
        std::unique_lock<std::mutex> timerLock(timerGuard_);

        if (isTimerRun_) {
            isTimerRun_ = false;
            condition_.notify_one();
        }
    }

private:
    void run()
    {
        std::unique_lock<std::mutex> timerLock(timerGuard_);

        while (isThreadRun_) {
            if (isTimerRun_) {
                auto now = TimerClock::now();
                if (now >= endTS_) {
                    isTimerRun_ = false;

                    auto currentCallback = callback_;

                    timerLock.unlock();

                    currentCallback();

                    timerLock.lock();
                } else {
                    condition_.wait_for(timerLock, (endTS_ - now));
                }
            } else {
                condition_.wait(timerLock);
            }
        }
    }

private:
    const std::string timerName_;

    bool isThreadRun_;
    bool isTimerRun_;

    std::thread timerThread_;
    std::condition_variable condition_;

    std::mutex timerGuard_;

    std::chrono::time_point<TimerClock> endTS_;

    Function callback_;
};

} /* namespace kaa */

#endif /* KAATIMER_HPP_ */
