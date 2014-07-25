/*
 * Copyright 2014 CyberVision, Inc.
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

#include <boost/uuid/uuid.hpp>
#include <boost/uuid/string_generator.hpp>

#include "kaa/configuration/delta/DeltaHandlerId.hpp"

namespace kaa {

BOOST_AUTO_TEST_SUITE(DeltaHandlerSuite)

BOOST_AUTO_TEST_CASE(SimpleDeltaHandlerIdTest)
{
    boost::uuids::string_generator gen;
    boost::uuids::uuid u1 = gen("{01234567-89ab-cdef-0123-456789abcdef}");

    DeltaHandlerId deltaId(u1);
    DeltaHandlerId sameId(u1);

    BOOST_CHECK_MESSAGE(deltaId == sameId, "Delta ids constructed from the same uuid are not equal");
    BOOST_CHECK_MESSAGE(deltaId >= sameId, "Delta ids constructed from the same uuid are not equal");
    BOOST_CHECK_MESSAGE(deltaId <= sameId, "Delta ids constructed from the same uuid are not equal");
    BOOST_CHECK_MESSAGE(deltaId.getHandlerId() <= sameId.getHandlerId()
                    , "Delta ids constructed from the same uuid are not equal");

    boost::uuids::uuid u2 = gen("{76543210-19ab-cdef-9123-356789abcdef}");
    DeltaHandlerId anotherDeltaId(u2);

    BOOST_CHECK_MESSAGE(deltaId != anotherDeltaId, "Delta ids constructed from different uuids are equal");

    /*
     * Time-based uuids
     */
    boost::uuids::uuid later = gen("{97658be0-d081-11e3-9c1a-0800200c9a66}");
    boost::uuids::uuid earlier = gen("{b56ac9c0-d081-11e3-9c1a-0800200c9a66}");

    DeltaHandlerId laterDeltaId(later);
    DeltaHandlerId earlierDeltaId(earlier);

    BOOST_CHECK_MESSAGE(earlierDeltaId > laterDeltaId, "Delta id constructed from "
                        "earlier time-based uuid is not greater than later one ");
}

BOOST_AUTO_TEST_SUITE_END()

}
