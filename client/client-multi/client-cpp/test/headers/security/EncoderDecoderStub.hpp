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

#ifndef ENCODERDECODERSTUB_HPP_
#define ENCODERDECODERSTUB_HPP_

#include "kaa/security/IEncoderDecoder.hpp"

namespace kaa {

class EncoderDecoderStub : public IEncoderDecoder {
public:
    EncoderDecoderStub()
        : encodedSessionKey_()
        , encodeData_("")
        , decodeData_("")
        , signData_()
        , signatureVerified_(false)
    {

    }

    EncodedSessionKey getEncodedSessionKey() {
        return encodedSessionKey_;
    }
    void setEncodedSessionKey(EncodedSessionKey key) {
        encodedSessionKey_ = key;
    }

    std::string encodeData(const std::uint8_t *data, size_t size) {
        data_.assign(reinterpret_cast<const char *>(data), size);
        return data_;
    }
    void setEncodeData(const std::string& data) {
        data_ = data;
    }

    std::string decodeData(const std::uint8_t *data, size_t size) {
        data_.assign(reinterpret_cast<const char *>(data), size);
        return data_;
    }
    void setDecodeData(const std::string& data) {
        data_ = data;
    }

    Signature signData(const std::uint8_t *data, size_t size) {
        return signData_;
    }
    void setSignData(Botan::SecureVector<std::uint8_t> data) {
        signData_ = data;
    }

    bool verifySignature(const std::uint8_t *data, size_t len, const std::uint8_t *sig, size_t sigLen) {
        return signatureVerified_;
    }
    void setSignatureVerified(bool signatureVerified) {
        signatureVerified_ = signatureVerified;
    }

private:
    EncodedSessionKey                   encodedSessionKey_;
    std::string                         encodeData_;
    std::string                         decodeData_;
    Signature                           signData_;
    bool                                signatureVerified_;
    std::string                         data_;
};

}  // namespace kaa


#endif /* ENCODERDECODERSTUB_HPP_ */
