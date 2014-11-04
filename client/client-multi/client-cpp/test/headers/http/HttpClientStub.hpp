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

#include "kaa/http/IHttpClient.hpp"

#include "kaa/common/AvroByteArrayConverter.hpp"
#include "kaa/common/EndpointObjectHash.hpp"
#include "kaa/http/IHttpRequest.hpp"
#include "kaa/http/HttpResponse.hpp"

namespace kaa {

const std::string HttpResponseStart = "HTTP/1.1\t200\r\nX-SIGNATURE: \r\n";

template <typename T>
class HttpClientStub : public IHttpClient {
public:
    HttpClientStub() : shift(0) {}

    std::shared_ptr<IHttpResponse> sendRequest(const IHttpRequest& request) {
        AvroByteArrayConverter<T> converter;
        SharedDataBuffer buffer = converter.toByteArray(element);

        std::stringstream ss;
        ss << HttpResponseStart;
        ss << "Content-Length: " << buffer.second + shift << "\r\n\r\n";
        for (size_t i = 0; i < buffer.second; ++i) {
            ss << (unsigned char) buffer.first[i];
        }
        ss << "\r\n\r\n";
        std::shared_ptr<IHttpResponse> response(new HttpResponse(ss.str()));

        return response;
    }

    void closeConnection() { }

    T element;
    int shift;
};


}  // namespace kaa
