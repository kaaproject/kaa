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

#include <vector>

#include "kaa/common/EndpointObjectHash.hpp"
#include "kaa/common/exception/KaaException.hpp"

namespace kaa {

EndpointObjectHash::EndpointObjectHash()
    : Botan::SHA_160(), withData_(false) {}

EndpointObjectHash::EndpointObjectHash(const boost::uint8_t* data, const boost::uint32_t& dataSize)
    : Botan::SHA_160(), withData_(true)
{
    clone(data, dataSize);
}

EndpointObjectHash::EndpointObjectHash(const std::string& str)
    : Botan::SHA_160(), withData_(true)
{
    update(str);
}

EndpointObjectHash::EndpointObjectHash(const SharedDataBuffer& endpointHash)
    : Botan::SHA_160(), withData_(true)
{
    clone(endpointHash.first.get(), endpointHash.second);
}

EndpointObjectHash::EndpointObjectHash(EndpointObjectHash& endpointHash)
    : Botan::SHA_160()
{
    withData_ = endpointHash.withData_;

    if (endpointHash.withData_) {
        hashBuffer_ = endpointHash.getHash();
    }
}

EndpointObjectHash& EndpointObjectHash::operator=(EndpointObjectHash& endpointHash)
{
    withData_ = endpointHash.withData_;

    if (endpointHash.withData_) {
        hashBuffer_ = endpointHash.getHash();
    }

    return *this;
}

SharedDataBuffer EndpointObjectHash::getHash()
{
    if (withData_ && !hashBuffer_.first) {
        boost::uint32_t size = output_length();

        hashBuffer_.first.reset(new boost::uint8_t[size]);
        hashBuffer_.second = size;
        final(hashBuffer_.first.get());
    }

    return hashBuffer_;
}

void EndpointObjectHash::clone(const boost::uint8_t* data, const boost::uint32_t& dataSize)
{
    if (!data || dataSize == 0) {
        throw KaaException("empty raw data or null size");
    }

    update(reinterpret_cast<const Botan::byte*>(data), dataSize);
}

bool EndpointObjectHash::isEqual(SharedDataBuffer left, SharedDataBuffer right)
{
    if (left.first && left.second > 0 && right.first && right.second > 0 &&
            left.second == right.second)
    {
        return !memcmp(left.first.get(), right.first.get(), left.second);
    }

    return false;
}

EndpointObjectHash::operator std::vector<boost::uint8_t>()
{
    SharedDataBuffer buffer = getHash();
    std::vector<boost::uint8_t> result;

    for (size_t i = 0; i < buffer.second; ++i) {
        result.push_back(buffer.first[i]);
    }
    return result;
}

} /* namespace kaa */
