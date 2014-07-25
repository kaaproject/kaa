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
#include <boost/scoped_ptr.hpp>
#include <boost/cstdint.hpp>

namespace kaa {

class RsaEncoderDecoder : public IEncoderDecoder {
public:
    RsaEncoderDecoder(const Botan::MemoryVector<boost::uint8_t>& pubKey,
            const std::string& privKey,
            const Botan::MemoryVector<boost::uint8_t>& remoteKey);
    ~RsaEncoderDecoder() { }

    Botan::SecureVector<boost::uint8_t> getEncodedSessionKey();
    std::string encodeData(const boost::uint8_t *data, size_t size);
    std::string decodeData(const boost::uint8_t *data, size_t size);
    Botan::SecureVector<boost::uint8_t> signData(const boost::uint8_t *data, size_t size);
    bool verifySignature(const boost::uint8_t *data, size_t len, const boost::uint8_t *sig, size_t sigLen);

private:
    std::string cipherPipe(const boost::uint8_t *data, size_t size, Botan::Cipher_Dir dir);

private:
    Botan::AutoSeeded_RNG rng_;
    boost::scoped_ptr<Botan::X509_PublicKey>   pubKey_;
    boost::scoped_ptr<Botan::PKCS8_PrivateKey> privKey_;
    boost::scoped_ptr<Botan::X509_PublicKey>   remoteKey_;

    Botan::SymmetricKey sessionKey_;
};

}


#endif /* RSAENCODERDECODER_HPP_ */
