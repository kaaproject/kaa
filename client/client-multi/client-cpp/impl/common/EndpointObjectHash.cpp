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

#include <vector>

#include <botan/sha160.h>

#include "kaa/common/EndpointObjectHash.hpp"
#include "kaa/common/exception/KaaException.hpp"

namespace kaa {

EndpointObjectHash::EndpointObjectHash(const std::uint8_t *data, const std::uint32_t &dataSize)
{
    calculateHash(data, dataSize);
}

EndpointObjectHash::EndpointObjectHash(const std::string& str)
{
    calculateHash(reinterpret_cast<const std::uint8_t *>(str.c_str()), str.size());
}

EndpointObjectHash::EndpointObjectHash(const SharedDataBuffer& endpointHash)
{
    calculateHash(endpointHash.first.get(), endpointHash.second);
}

EndpointObjectHash::EndpointObjectHash(const EndpointObjectHash& endpointHash) : hashDigest_(endpointHash.hashDigest_)
{

}

EndpointObjectHash::EndpointObjectHash(EndpointObjectHash&& endpointHash) : hashDigest_(std::move(endpointHash.hashDigest_))
{

}

EndpointObjectHash& EndpointObjectHash::operator=(const EndpointObjectHash& endpointHash)
{
    hashDigest_ = endpointHash.hashDigest_;
    return *this;
}

EndpointObjectHash& EndpointObjectHash::operator=(EndpointObjectHash&& endpointHash)
{
    hashDigest_ = std::move(endpointHash.hashDigest_);
    return *this;
}

std::vector<std::uint8_t> EndpointObjectHash::getHashDigest()
{
    return hashDigest_;
}

void EndpointObjectHash::calculateHash(const std::uint8_t* data, std::uint32_t dataSize)
{
    if (!data && dataSize != 0) {
        throw KaaException("empty raw data or null size");
    }
    Botan::SHA_160 sha;
    const auto& result = sha.process(data, dataSize);
    hashDigest_.assign(result.begin(), result.end());
}

EndpointObjectHash::operator std::vector<std::uint8_t>()
{
    return hashDigest_;
}

} /* namespace kaa */
