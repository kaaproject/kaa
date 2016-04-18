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

#include "kaa/http/HttpUrl.hpp"

namespace kaa {

BOOST_AUTO_TEST_SUITE(HttpUrlSuite)

const char * const test_url0 = "http://test.com:1234/path?par1=val1&par2=val2";
const char * const test_url1 = "https://test.com:1234/path?par1=val1&par2=val2";

const char * const test_url2 = "http://test.com:1234";
const char * const test_url3 = "http://test.com";

const char * const test_url4 = "https://test.com:443";
const char * const test_url5 = "https://test.com";

BOOST_AUTO_TEST_CASE(checkParseHttpUrl)
{
    HttpUrl url(test_url0);
    BOOST_CHECK_EQUAL(url.getHost(), "test.com");
    BOOST_CHECK_EQUAL(url.getPort(), 1234);
    BOOST_CHECK_EQUAL(url.getUri(), "/path?par1=val1&par2=val2");
}

BOOST_AUTO_TEST_CASE(checkParseHttpsUrl)
{
    HttpUrl url(test_url1);
    BOOST_CHECK_EQUAL(url.getHost(), "test.com");
    BOOST_CHECK_EQUAL(url.getPort(), 1234);
    BOOST_CHECK_EQUAL(url.getUri(), "/path?par1=val1&par2=val2");
}

BOOST_AUTO_TEST_CASE(checkParseHttpUrlFromString)
{
    std::string url_str(test_url0);
    HttpUrl *url = new HttpUrl(url_str);
    BOOST_CHECK_EQUAL(url->getHost(), "test.com");
    BOOST_CHECK_EQUAL(url->getPort(), 1234);
    BOOST_CHECK_EQUAL(url->getUri(), "/path?par1=val1&par2=val2");
    delete url;
}

BOOST_AUTO_TEST_CASE(checkParseHttpUrlWithoutUri)
{
    HttpUrl url(test_url2);
    BOOST_CHECK_EQUAL(url.getHost(), "test.com");
    BOOST_CHECK_EQUAL(url.getPort(), 1234);
    BOOST_CHECK_EQUAL(url.getUri(), "/");
}

BOOST_AUTO_TEST_CASE(checkParseHttpUrlWithoutUriAndPort)
{
    HttpUrl url(test_url3);
    BOOST_CHECK_EQUAL(url.getHost(), "test.com");
    BOOST_CHECK_EQUAL(url.getPort(), 80);
    BOOST_CHECK_EQUAL(url.getUri(), "/");
}

BOOST_AUTO_TEST_CASE(checkParseHttpsUrlWithoutUri)
{
    HttpUrl url(test_url4);
    BOOST_CHECK_EQUAL(url.getHost(), "test.com");
    BOOST_CHECK_EQUAL(url.getPort(), 443);
    BOOST_CHECK_EQUAL(url.getUri(), "/");
}

BOOST_AUTO_TEST_CASE(checkParseHttpsUrlWithoutUriAndPort)
{
    HttpUrl url(test_url5);
    BOOST_CHECK_EQUAL(url.getHost(), "test.com");
    BOOST_CHECK_EQUAL(url.getPort(), 443);
    BOOST_CHECK_EQUAL(url.getUri(), "/");
}

BOOST_AUTO_TEST_SUITE_END()

}  // namespace kaa
