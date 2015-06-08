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

#ifndef KAATIMER_HPP_
#define KAATIMER_HPP_

#include <chrono>
#include <mutex>
#include <thread>
#include <functional>
#include <condition_variable>

#include "kaa/KaaThread.hpp"
#include "kaa/logging/Log.hpp"

namespace kaa {

template<class Signature, class Function = std::function<Signature>>
class KaaTimer {
    typedef std::chrono::system_clock TimerClock;
public:
    KaaTimer(const std::string& timerName) :
        timerName_(timerName), isThreadRun_(true), isTimerRun_(false), timerThread_([&] { run(); })
    {

    }
    ~KaaTimer()
    {
        //KAA_LOG_TRACE(boost::format("Timer[%1%] destroying ...") % timerName_);
        if (timerThread_.joinable()) {
            //KAA_MUTEX_LOCKING("timerGuard_");
            KAA_MUTEX_UNIQUE_DECLARE(timerLock, timerGuard_);
            //KAA_MUTEX_LOCKED("timerGuard_");

            isThreadRun_ = false;
            condition_.notify_one();

            //KAA_MUTEX_UNLOCKING("timerGuard_");
            KAA_UNLOCK(timerLock);
            //KAA_MUTEX_UNLOCKED("timerGuard_");

            timerThread_.join();
        }
    }

    void start(std::size_t seconds, const Function& callback)
    {

        KAA_LOG_TRACE(boost::format("Timer[%1%] scheduling for %2% sec ...") % timerName_ % seconds );

        KAA_MUTEX_LOCKING("timerGuard_");
        KAA_MUTEX_UNIQUE_DECLARE(timerLock, timerGuard_);
        KAA_MUTEX_LOCKED("timerGuard_");

        if (!isTimerRun_) {
            endTS_ = TimerClock::now() + std::chrono::seconds(seconds);
            isTimerRun_ = true;
            callback_ = callback;
            condition_.notify_one();
        }
    }

    void stop()
    {
        KAA_LOG_TRACE(boost::format("Timer[%1%] stopping ...") % timerName_);

        KAA_MUTEX_LOCKING("timerGuard_");
        KAA_MUTEX_UNIQUE_DECLARE(timerLock, timerGuard_);
        KAA_MUTEX_LOCKED("timerGuard_");

        if (isTimerRun_) {
            isTimerRun_ = false;
            condition_.notify_one();
        }
    }

private:
    void run()
    {
        KAA_LOG_TRACE(boost::format("Timer[%1%] starting thread ...") % timerName_);

        while (isThreadRun_) {

            KAA_MUTEX_LOCKING("timerGuard_");
            KAA_MUTEX_UNIQUE_DECLARE(timerLock, timerGuard_);
            KAA_MUTEX_LOCKED("timerGuard_");

            if (isTimerRun_) {
                auto now = TimerClock::now();
                if (now >= endTS_) {
                    isTimerRun_ = false;
                    KAA_LOG_TRACE(boost::format("Timer[%1%] executing callback ...") % timerName_);

                    KAA_MUTEX_UNLOCKING("timerGuard_");
                    KAA_UNLOCK(timerLock);
                    KAA_MUTEX_UNLOCKED("timerGuard_");

                    callback_();

                    KAA_MUTEX_LOCKING("timer_mutex_");
                    KAA_LOCK(timerLock);
                    KAA_MUTEX_LOCKED("timer_mutex_");
                } else {
                    KAA_MUTEX_UNLOCKING("timerGuard_");
                    condition_.wait_for(timerLock, (endTS_ - now));
                    KAA_MUTEX_UNLOCKED("timerGuard_");
                }
            } else {
                KAA_MUTEX_UNLOCKING("timerGuard_");
                condition_.wait(timerLock);
                KAA_MUTEX_UNLOCKED("timerGuard_");
            }
        }
    }

private:

    const std::string timerName_;

    bool isThreadRun_;
    bool isTimerRun_;

    std::thread timerThread_;
    std::condition_variable condition_;

    KAA_MUTEX_DECLARE(timerGuard_);

    std::chrono::time_point<TimerClock> endTS_;

    Function callback_;
};

} /* namespace kaa */

#endif /* KAATIMER_HPP_ */
