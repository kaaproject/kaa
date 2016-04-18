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

#define BOOST_TEST_MODULE "MAIN_KAA_TEST_MODULE"
#include <boost/test/unit_test.hpp>

BOOST_AUTO_TEST_SUITE(SimpleSuite)

BOOST_AUTO_TEST_CASE(SimpleCase)
{
    BOOST_CHECK(true);
}

BOOST_AUTO_TEST_SUITE_END()
