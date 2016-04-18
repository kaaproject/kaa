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

#ifndef IHTTPRESPONSE_HPP_
#define IHTTPRESPONSE_HPP_

#include  "kaa/KaaDefaults.hpp"

#include <cstdint>
#include <boost/shared_array.hpp>
#include <string>
#include <vector>

namespace kaa {

typedef std::pair<boost::shared_array<std::uint8_t>, size_t> SharedBody;

class IHttpResponse {
public:
    virtual std::string getHeaderField(const std::string& name) const = 0;
    virtual SharedBody getBody() const = 0;
    virtual int getStatusCode() const = 0;
    virtual ~IHttpResponse() { };
};

}

#endif /* IHTTPRESPONSE_HPP_ */
