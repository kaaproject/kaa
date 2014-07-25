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

#ifndef IENCODERDECODER_HPP_
#define IENCODERDECODER_HPP_

#include <botan/rsa.h>
#include <boost/cstdint.hpp>

namespace kaa {

class IEncoderDecoder {
public:
    virtual ~IEncoderDecoder() {}

    virtual Botan::SecureVector<boost::uint8_t> getEncodedSessionKey() = 0;
    virtual std::string                         encodeData(const boost::uint8_t *data, size_t size) = 0;
    virtual std::string                         decodeData(const boost::uint8_t *data, size_t size) = 0;
    virtual Botan::SecureVector<boost::uint8_t> signData(const boost::uint8_t *data, size_t size) = 0;
    virtual bool                                verifySignature(const boost::uint8_t *data, size_t len, const boost::uint8_t *sig, size_t sigLen) = 0;
};

}  // namespace kaa


#endif /* IENCODERDECODER_HPP_ */
