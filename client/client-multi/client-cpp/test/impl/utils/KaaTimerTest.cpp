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

#include <thread>

#include "kaa/utils/KaaTimer.hpp"

namespace kaa {

std::int32_t counter = 0;

void increment(void) { counter++; }

void resetCounter() { counter = 0; }

BOOST_AUTO_TEST_SUITE(KaaTimerTestSuite)

BOOST_AUTO_TEST_CASE(SuccessTimerTriggerTest)
{
    std::size_t timeToWait = 4;
    KaaTimer<void (void)> timer { "Kaa Timer" };
    timer.start(timeToWait, [] { increment(); });

    std::this_thread::sleep_for(std::chrono::seconds(timeToWait / 2));

    BOOST_CHECK_EQUAL(counter, 0);

    std::this_thread::sleep_for(std::chrono::seconds(timeToWait / 2 + 1));

    BOOST_CHECK_EQUAL(counter, 1);
    resetCounter();
}

BOOST_AUTO_TEST_CASE(StopTimerTest)
{
    std::size_t timeToWait = 4;
    KaaTimer<void (void)> timer { "Kaa Timer" };
    timer.start(timeToWait, [] { increment(); });

    std::this_thread::sleep_for(std::chrono::seconds(timeToWait / 2));

    BOOST_CHECK_EQUAL(counter, 0);
    timer.stop();

    std::this_thread::sleep_for(std::chrono::seconds(timeToWait / 2 + 1));

    BOOST_CHECK_EQUAL(counter, 0);
    resetCounter();
}

BOOST_AUTO_TEST_SUITE_END()

}
