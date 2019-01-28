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

#include "kaa/security/RsaEncoderDecoder.hpp"
#include <botan/pubkey.h>
#include <botan/pkcs8.h>
#include <botan/pipe.h>
#include <botan/key_filt.h>
#include <sstream>

#include "kaa/logging/Log.hpp"
#include "kaa/logging/LoggingUtils.hpp"

namespace kaa {

RsaEncoderDecoder::RsaEncoderDecoder(const PublicKey& pubKey,
        const PrivateKey& privKey,
        const PublicKey& remoteKey, IKaaClientContext &context)
    : pubKey_(nullptr), privKey_(nullptr), remoteKey_(nullptr), sessionKey_(KeyUtils().generateSessionKey(16)), context_(context)
{
    KAA_LOG_TRACE("Creating MessageEncoderDecoder with following parameters: ");

    if (!pubKey.empty()) {
        Botan::DataSource_Memory pubMem(pubKey);
        pubKey_.reset(Botan::X509::load_key(pubMem));
    }

    KAA_LOG_TRACE(boost::format("PublicKey: %1%") % ( pubKey_ ? LoggingUtils::toString(
        pubKey_->x509_subject_public_key().data(), pubKey_->x509_subject_public_key().size()) : "empty"));

    if (!privKey.empty()) {
        Botan::DataSource_Memory privMem(privKey);
        privKey_.reset(Botan::PKCS8::load_key(privMem, rng_));
    }

    if (!remoteKey.empty()) {
        Botan::DataSource_Memory remoteMem(remoteKey);
        remoteKey_.reset(Botan::X509::load_key(remoteMem));
    }

    KAA_LOG_TRACE(boost::format("RemotePublicKey: %1%") % ( remoteKey_ ? LoggingUtils::toString(
            remoteKey_->x509_subject_public_key().data(), remoteKey_->x509_subject_public_key().size()) : "empty"));
}

EncodedSessionKey RsaEncoderDecoder::getEncodedSessionKey()
{
    Botan::PK_Encryptor_EME enc(*remoteKey_, "EME-PKCS1-v1_5");
    auto &&v = enc.encrypt(sessionKey_.bits_of(), rng_);
    return Botan::secure_vector<std::uint8_t>(v.begin(), v.end());
}

std::string RsaEncoderDecoder::cipherPipe(const std::uint8_t *data, std::size_t size, Botan::Cipher_Dir dir)
{
    Botan::Pipe pipe(Botan::get_cipher("AES-128/ECB/PKCS7", sessionKey_, dir));
    std::ostringstream stream;
    pipe.process_msg(data, size);
    stream << pipe;
    return stream.str();
}

std::string RsaEncoderDecoder::encodeData(const std::uint8_t *data, std::size_t size)
{
    return cipherPipe(data, size, Botan::ENCRYPTION);
}

std::string RsaEncoderDecoder::decodeData(const std::uint8_t *data, std::size_t size)
{
    return cipherPipe(data, size, Botan::DECRYPTION);
}

Signature RsaEncoderDecoder::signData(const std::uint8_t *data, std::size_t size)
{
    Botan::PK_Signer signer(*privKey_, "EMSA3(SHA-1)");
    auto &&sgn = signer.sign_message(data, size, rng_);
    return Botan::secure_vector<std::uint8_t>(sgn.begin(), sgn.end());
}

bool RsaEncoderDecoder::verifySignature(const std::uint8_t *data, std::size_t len, const std::uint8_t *sig, std::size_t sigLen)
{
    Botan::PK_Verifier verifier(*remoteKey_, "EMSA3(SHA-1)");
    return verifier.verify_message(data, len, sig, sigLen);
}

}

