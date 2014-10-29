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

#ifndef RSAENCODERDECODER_HPP_
#define RSAENCODERDECODER_HPP_

#include "kaa/security/KeyUtils.hpp"
#include "kaa/security/IEncoderDecoder.hpp"
#include <botan/rsa.h>
#include <cstdint>
#include <memory>

namespace kaa {

class RsaEncoderDecoder : public IEncoderDecoder {
public:
    RsaEncoderDecoder(const Botan::MemoryVector<std::uint8_t>& pubKey,
            const std::string& privKey,
            const Botan::MemoryVector<std::uint8_t>& remoteKey);
    ~RsaEncoderDecoder() { }

    Botan::SecureVector<std::uint8_t> getEncodedSessionKey();
    std::string encodeData(const std::uint8_t *data, std::size_t size);
    std::string decodeData(const std::uint8_t *data, std::size_t size);
    Botan::SecureVector<std::uint8_t> signData(const std::uint8_t *data, std::size_t size);
    bool verifySignature(const std::uint8_t *data, std::size_t len, const std::uint8_t *sig, std::size_t sigLen);

private:
    std::string cipherPipe(const std::uint8_t *data, std::size_t size, Botan::Cipher_Dir dir);

private:
    Botan::AutoSeeded_RNG rng_;
    std::unique_ptr<Botan::X509_PublicKey>   pubKey_;
    std::unique_ptr<Botan::PKCS8_PrivateKey> privKey_;
    std::unique_ptr<Botan::X509_PublicKey>   remoteKey_;

    Botan::SymmetricKey sessionKey_;
};

}


#endif /* RSAENCODERDECODER_HPP_ */
