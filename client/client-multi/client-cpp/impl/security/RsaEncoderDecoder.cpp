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

#include "kaa/security/RsaEncoderDecoder.hpp"
#include <botan/look_pk.h>
#include <botan/pk_keys.h>
#include <botan/pubkey.h>
#include <sstream>
#include <boost/cstdint.hpp>

#include "kaa/logging/Log.hpp"
#include "kaa/logging/LoggingUtils.hpp"

namespace kaa {

RsaEncoderDecoder::RsaEncoderDecoder(
        const Botan::MemoryVector<boost::uint8_t>& pubKey,
        const std::string& privKey,
        const Botan::MemoryVector<boost::uint8_t>& remoteKey)
    : pubKey_(NULL), privKey_(NULL), remoteKey_(NULL), sessionKey_(KeyUtils().generateSessionKey(16))
{
    KAA_LOG_TRACE("Creating MessageEncoderDecoder with following parameters: ");

    if (!pubKey.empty()) {
        Botan::DataSource_Memory pubMem(pubKey);
        pubKey_.reset(Botan::X509::load_key(pubMem));
    }

    KAA_LOG_TRACE(boost::format("PublicKey: %1%") % ( pubKey_ ? LoggingUtils::ByteArrayToString(
        pubKey_->x509_subject_public_key().begin(), pubKey_->x509_subject_public_key().size()) : "empty"));

    if (!privKey.empty()) {
        Botan::DataSource_Memory privMem(privKey);
        privKey_.reset(Botan::PKCS8::load_key(privMem, rng_));
    }

    if (!remoteKey.empty()) {
        Botan::DataSource_Memory remoteMem(remoteKey);
        remoteKey_.reset(Botan::X509::load_key(remoteMem));
    }

    KAA_LOG_TRACE(boost::format("RemotePublicKey: %1%") % ( remoteKey_ ? LoggingUtils::ByteArrayToString(
            remoteKey_->x509_subject_public_key().begin(), remoteKey_->x509_subject_public_key().size()) : "empty"));
}

Botan::SecureVector<boost::uint8_t> RsaEncoderDecoder::getEncodedSessionKey()
{
    Botan::PK_Encryptor_EME enc(*remoteKey_, "EME-PKCS1-v1_5");
    return enc.encrypt(sessionKey_.bits_of(), rng_);
}

std::string RsaEncoderDecoder::cipherPipe(const boost::uint8_t *data, size_t size, Botan::Cipher_Dir dir)
{
    Botan::Pipe pipe(Botan::get_cipher("AES-128/ECB/PKCS7", sessionKey_, dir));
    std::ostringstream stream;
    pipe.process_msg(data, size);
    stream << pipe;
    return stream.str();
}

std::string RsaEncoderDecoder::encodeData(const boost::uint8_t *data, size_t size)
{
    return cipherPipe(data, size, Botan::ENCRYPTION);
}

std::string RsaEncoderDecoder::decodeData(const boost::uint8_t *data, size_t size)
{
    return cipherPipe(data, size, Botan::DECRYPTION);
}

Botan::SecureVector<boost::uint8_t> RsaEncoderDecoder::signData(const boost::uint8_t *data, size_t size)
{
    Botan::PK_Signer signer(*privKey_, "EMSA3(SHA-1)");
    return signer.sign_message(data, size, rng_);
}

bool RsaEncoderDecoder::verifySignature(const boost::uint8_t *data, size_t len, const boost::uint8_t *sig, size_t sigLen)
{
    Botan::PK_Verifier verifier(*remoteKey_, "EMSA3(SHA-1)");
    return verifier.verify_message(data, len, sig, sigLen);
}

}

