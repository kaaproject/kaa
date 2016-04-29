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

#ifndef ENDPOINT_OBJECT_HASH_HPP_
#define ENDPOINT_OBJECT_HASH_HPP_

#include <utility>
#include <cstring>
#include <vector>

#include <cstdint>
#include <boost/shared_array.hpp>

namespace kaa {

typedef std::pair<boost::shared_array<std::uint8_t>, std::uint32_t> SharedDataBuffer;
typedef std::vector<std::uint8_t> HashDigest;

/**
 * Used to calculate SHA-1 hash
 */
class EndpointObjectHash
{
public:
    EndpointObjectHash() { }

    /*
     * Specific constructors
     * Throws \ref KaaException when invalid data was passed (zero-sized or null buffer)
     */
    EndpointObjectHash(const std::string& str);
    EndpointObjectHash(const std::uint8_t* data, const std::uint32_t& dataSize);
    EndpointObjectHash(const SharedDataBuffer& endpointHash);
    EndpointObjectHash(const EndpointObjectHash& endpointHash);
    EndpointObjectHash(EndpointObjectHash&& endpointHash);

    /*
     * Copy operator
     * Throws \ref KaaException when invalid data was passed (zero-sized or null buffer)
     */
    EndpointObjectHash& operator=(const EndpointObjectHash& endpointHash);

    /*
     * Move operator
     * Throws \ref KaaException when invalid data was passed (zero-sized or null buffer)
     */
    EndpointObjectHash& operator=(EndpointObjectHash&& endpointHash);

    /**
     * Retrieves digest
     * @return Buffer with digest or empty one if no data was put
     */
    HashDigest getHashDigest();

    /**
     * Checks if two hashes are equal
     * @return the result of comparison
     */
    bool operator==(const EndpointObjectHash& endpointHash) { return hashDigest_ == endpointHash.hashDigest_; }

    /**
     * Checks if two hashes are not equal
     * @return the result of comparison
     */
    bool operator!=(const EndpointObjectHash& endpointHash) { return hashDigest_ != endpointHash.hashDigest_; }

    operator std::vector<std::uint8_t>();

private:
    void calculateHash(const std::uint8_t* data, std::uint32_t dataSize);

private:
    std::vector<std::uint8_t> hashDigest_;
};

} /* namespace kaa */

#endif /* ENDPOINT_OBJECT_HASH_HPP_ */
