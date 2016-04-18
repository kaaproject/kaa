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

#ifndef RSAENCODERDECODER_HPP_
#define RSAENCODERDECODER_HPP_

#include "kaa/security/KeyUtils.hpp"
#include "kaa/security/IEncoderDecoder.hpp"
#include "kaa/IKaaClientContext.hpp"
#include <botan/rsa.h>
#include <botan/cipher_mode.h>
#include <cstdint>
#include <memory>

namespace kaa {

class RsaEncoderDecoder : public IEncoderDecoder {
public:
    RsaEncoderDecoder(const PublicKey& pubKey,
                      const PrivateKey& privKey,
                      const PublicKey& remoteKey,
                      IKaaClientContext &context);
    ~RsaEncoderDecoder() { }

    virtual EncodedSessionKey getEncodedSessionKey();
    virtual std::string encodeData(const std::uint8_t *data, std::size_t size);
    virtual std::string decodeData(const std::uint8_t *data, std::size_t size);
    virtual Signature signData(const std::uint8_t *data, std::size_t size);
    virtual bool verifySignature(const std::uint8_t *data, std::size_t len, const std::uint8_t *sig, std::size_t sigLen);

private:
    std::string cipherPipe(const std::uint8_t *data, std::size_t size, Botan::Cipher_Dir dir);

private:
    Botan::AutoSeeded_RNG rng_;
    std::unique_ptr<Botan::X509_PublicKey>   pubKey_;
    std::unique_ptr<Botan::PKCS8_PrivateKey> privKey_;
    std::unique_ptr<Botan::X509_PublicKey>   remoteKey_;

    SessionKey sessionKey_;

    IKaaClientContext &context_;
};

}


#endif /* RSAENCODERDECODER_HPP_ */
