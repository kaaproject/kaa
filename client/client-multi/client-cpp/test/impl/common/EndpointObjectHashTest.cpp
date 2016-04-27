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

#include <cstdlib>

#include "kaa/common/EndpointObjectHash.hpp"
#include "kaa/common/exception/KaaException.hpp"

namespace kaa {

#define SHA1_SIZE   20

BOOST_AUTO_TEST_SUITE(EndpointObjectHashSuite)

BOOST_AUTO_TEST_CASE(NoData)
{
    /*
     * EndpointObjectHash with no data should return empty hash buffer.
     */
    EndpointObjectHash hashObject;
    HashDigest empty = hashObject.getHashDigest();

    BOOST_CHECK(empty.empty());
}

BOOST_AUTO_TEST_CASE(SameDataHash)
{
    /*
     * EndpointObjectHash derived from another one should have the same hash.
     */
    std::string str("Test EndpointObjectHash");

    EndpointObjectHash hash(str);
    EndpointObjectHash theSame(str);
    EndpointObjectHash copy(hash);
    EndpointObjectHash theSameCopy = copy;

    BOOST_CHECK(hash == theSame);
    BOOST_CHECK(copy == hash);
    BOOST_CHECK(theSameCopy == copy);
}

BOOST_AUTO_TEST_CASE(DifferentDataHash)
{
    /*
     * EndpointObjectHashs with different data are not equal.
     */
    std::string str("Test EndpointObjectHash");
    std::string another_str("Another data");

    EndpointObjectHash hash(str);
    EndpointObjectHash another(another_str);

    BOOST_CHECK(hash != another);
}

BOOST_AUTO_TEST_CASE(EmptyHash)
{
    /*
     * EndpointObjectHash with no data are never equal to another with some data.
     */
    std::string str("Test EndpointObjectHash");

    EndpointObjectHash hash(str);
    EndpointObjectHash empty;

    BOOST_CHECK(hash != empty);
}

void createInvalidHashObject() { EndpointObjectHash hash(nullptr, 1 + std::rand()); }

BOOST_AUTO_TEST_CASE(InvalidData)
{
    /*
     * EndpointObjectHashs with invalid (null) data should never be created.
     */
    BOOST_CHECK_THROW(createInvalidHashObject(), KaaException);
}

BOOST_AUTO_TEST_CASE(CompareCalculationWithSample)
{
    /*
     * Compare EndpointObjectHash stuff with pre-defined SHA-1 sample.
     */
    HashDigest sampleBuffer = { 0x2f, 0xd4, 0xe1, 0xc6,
            0x7a, 0x2d, 0x28, 0xfc,
            0xed, 0x84, 0x9e, 0xe1,
            0xbb, 0x76, 0xe7, 0x39,
            0x1b, 0x93, 0xeb, 0x12 };
    std::string str("The quick brown fox jumps over the lazy dog");

    HashDigest calculatedHash = EndpointObjectHash(
            reinterpret_cast<const std::uint8_t*>(str.data()), str.length()).getHashDigest();

    BOOST_CHECK(sampleBuffer == calculatedHash);
}

BOOST_AUTO_TEST_SUITE_END()

} /* namespace kaa */
