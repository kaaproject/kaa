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

#include "kaa/http/MultipartPostHttpRequest.hpp"
#include "kaa/KaaClientContext.hpp"
#include "kaa/KaaClientProperties.hpp"
#include "kaa/logging/DefaultLogger.hpp"

#include "headers/context/MockExecutorContext.hpp"
#include "headers/MockKaaClientStateStorage.hpp"


#include <vector>

namespace kaa {

static std::string test_url0 = "http://test.com:1234/path?par1=val1&par2=val2";

static std::string body_name = "SimpleBody";
static const std::vector<std::uint8_t> body_data = {'0','1','2','3','4','5','6','7','8','9'};

static std::string header_name  = "MyHttpHeader";
static std::string header_value = "MyHttpHeaderValue";

static std::string request_body =
    "POST /path?par1=val1&par2=val2 HTTP/1.1\r\n"
    "Accept: */*\r\n"
    "Content-Type: multipart/form-data; boundary=----Sanj56fD843koI0\r\n"
    "Host: test.com\r\n"
    "MyHttpHeader: MyHttpHeaderValue\r\n"
    "Connection: Close\r\n"
    "Content-Length: 115\r\n\r\n"
    "------Sanj56fD843koI0\r\n"
    "Content-Disposition: form-data; name=\"SimpleBody\"\r\n\r\n"
    "0123456789\r\n"
    "------Sanj56fD843koI0--\r\n\r\n"
    "\r\n\r\n";

static std::string request_body_wo_header =
    "POST /path?par1=val1&par2=val2 HTTP/1.1\r\n"
    "Accept: */*\r\n"
    "Content-Type: multipart/form-data; boundary=----Sanj56fD843koI0\r\n"
    "Host: test.com\r\n"
    "Connection: Close\r\n"
    "Content-Length: 115\r\n\r\n"
    "------Sanj56fD843koI0\r\n"
    "Content-Disposition: form-data; name=\"SimpleBody\"\r\n\r\n"
    "0123456789\r\n"
    "------Sanj56fD843koI0--\r\n\r\n"
    "\r\n\r\n";

static std::string request_body_wo_body =
    "POST /path?par1=val1&par2=val2 HTTP/1.1\r\n"
    "Accept: */*\r\n"
    "Content-Type: multipart/form-data; boundary=----Sanj56fD843koI0\r\n"
    "Host: test.com\r\n"
    "Connection: Close\r\n"
    "Content-Length: 27\r\n\r\n"
    "------Sanj56fD843koI0--\r\n\r\n"
    "\r\n\r\n";

BOOST_AUTO_TEST_SUITE(HttpRequestSuite)

BOOST_AUTO_TEST_CASE(httpMultipartRequestTest)
{
    IKaaClientStateStoragePtr stateMock(new MockKaaClientStateStorage);
    MockExecutorContext context;
    KaaClientProperties properties;
    DefaultLogger tmp_logger(properties.getClientId());
    KaaClientContext clientContext(properties, tmp_logger, context, stateMock);

    HttpUrl url(test_url0);
    MultipartPostHttpRequest req(url, clientContext);

    BOOST_CHECK_EQUAL(req.getHost(), "test.com");
    BOOST_CHECK_EQUAL(req.getPort(), 1234);

    req.setHeaderField(header_name, header_value);
    req.setBodyField(body_name, body_data);

    BOOST_CHECK_EQUAL(req.getRequestData(), request_body);

    req.removeHeaderField(header_name);
    BOOST_CHECK_EQUAL(req.getRequestData(), request_body_wo_header);

    req.removeBodyField(body_name);
    BOOST_CHECK_EQUAL(req.getRequestData(), request_body_wo_body);
}

BOOST_AUTO_TEST_SUITE_END()

}  // namespace kaa
