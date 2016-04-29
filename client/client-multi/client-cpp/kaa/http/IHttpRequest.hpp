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

#ifndef IHTTPREQUEST_HPP_
#define IHTTPREQUEST_HPP_

#include "kaa/KaaDefaults.hpp"

#include <string>
#include <cstdint>

namespace kaa {

class IHttpRequest {
public:
    virtual std::string getHost() const = 0;
    virtual std::uint16_t getPort() const = 0;
    virtual std::string getRequestData() const = 0;
    virtual void setHeaderField(const std::string& name, const std::string& value) = 0;
    virtual void removeHeaderField(const std::string& name) = 0;
    virtual ~IHttpRequest() { }
};

}

#endif /* IHTTPREQUEST_HPP_ */
