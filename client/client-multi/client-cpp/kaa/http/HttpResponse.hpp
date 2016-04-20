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

#ifndef HTTPRESPONSE_HPP_
#define HTTPRESPONSE_HPP_

#include "kaa/KaaDefaults.hpp"

#include "kaa/http/IHttpResponse.hpp"

#include <map>

namespace kaa {

class HttpResponse : public IHttpResponse {
public:
    HttpResponse(const char *data, std::size_t len);
    HttpResponse(const std::string& data);
    ~HttpResponse() { }

    virtual std::string getHeaderField(const std::string& name) const;
    virtual SharedBody getBody() const;
    virtual int getStatusCode() const;

private:
    static const std::uint8_t  HTTP_VERSION_OFFSET = 9;

    void parseResponse(const char *data, size_t len);

private:
    SharedBody body_;
    std::map<std::string, std::string> header_;
    int statusCode_;
};

}

#endif /* HTTPRESPONSE_HPP_ */
