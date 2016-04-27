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

#ifndef MULTIPARTPOSTHTTPREQUEST_HPP_
#define MULTIPARTPOSTHTTPREQUEST_HPP_

#include "kaa/KaaDefaults.hpp"

#include "kaa/http/IHttpRequest.hpp"
#include "kaa/http/HttpUrl.hpp"
#include "kaa/IKaaClientContext.hpp"

#include <map>
#include <vector>

namespace kaa {

class MultipartPostHttpRequest : public IHttpRequest {
public:
    MultipartPostHttpRequest(const HttpUrl& url, IKaaClientContext &context);
    virtual ~MultipartPostHttpRequest();

    virtual std::string getHost() const;
    virtual std::uint16_t getPort() const;
    virtual std::string getRequestData() const;
    virtual void setHeaderField(const std::string& name, const std::string& value);
    virtual void removeHeaderField(const std::string& name);

    void setBodyField(const std::string& name, const std::vector<std::uint8_t>& value);
    void removeBodyField(const std::string& name);

private:
    static const std::string BOUNDARY;

private:
    HttpUrl url_;
    std::map<std::string, std::string> headerFields_;
    std::map<std::string, std::vector<std::uint8_t>> bodyFields_;

    IKaaClientContext &context_;
};

}

#endif /* MULTIPARTPOSTHTTPREQUEST_HPP_ */
