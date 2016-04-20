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

#include "kaa/http/HttpResponse.hpp"
#include "kaa/common/exception/KaaException.hpp"

namespace kaa {

static const std::string response_wo_body = "HTTP/1.1\t200\r\nContent-Length: 0\r\nContent-Type: text/plain\r\n\r\n";
static const std::string response_with_body = "HTTP/1.1\t200\r\nContent-Length: 10\r\nContent-Type: text/plain\r\n\r\n0123456789";

BOOST_AUTO_TEST_SUITE(HttpResponseSuite)

BOOST_AUTO_TEST_CASE(checkCreateEmptyResponse)
{
    std::string empty("");
    BOOST_REQUIRE_THROW(HttpResponse hr(empty), KaaException);
    BOOST_REQUIRE_THROW(HttpResponse hr(nullptr, 100), KaaException);

    const char *    data = "abc";
    size_t          data_len = 0;
    BOOST_REQUIRE_THROW(HttpResponse hr(data, data_len), KaaException);
}

BOOST_AUTO_TEST_CASE(checkSimpleResponseWithoutBody)
{
    HttpResponse hr(response_wo_body);
    BOOST_CHECK_EQUAL(hr.getStatusCode(), 200);
    BOOST_CHECK_EQUAL(hr.getHeaderField("Content-Length"), "0");
    BOOST_CHECK_EQUAL(hr.getHeaderField("Content-Type"), "text/plain");

    SharedBody body = hr.getBody();
    BOOST_CHECK_EQUAL(body.second, 0);
}

BOOST_AUTO_TEST_CASE(checkSimpleResponseWithBody)
{
    HttpResponse hr(response_with_body);
    BOOST_CHECK_EQUAL(hr.getStatusCode(), 200);
    BOOST_CHECK_EQUAL(hr.getHeaderField("Content-Length"), "10");
    BOOST_CHECK_EQUAL(hr.getHeaderField("Content-Type"), "text/plain");

    const char * expected_body = "0123456789";
    size_t expected_body_length = 10;

    SharedBody body = hr.getBody();
    BOOST_CHECK_EQUAL(body.second, expected_body_length);
    auto data = body.first;

    BOOST_CHECK_EQUAL_COLLECTIONS(body.first.get(), body.first.get() + expected_body_length, expected_body, expected_body + expected_body_length);
}

BOOST_AUTO_TEST_SUITE_END()

}  // namespace kaa

