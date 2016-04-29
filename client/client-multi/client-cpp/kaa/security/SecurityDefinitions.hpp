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

#ifndef SECURITYDEFINITIONS_HPP_
#define SECURITYDEFINITIONS_HPP_

#include <string>
#include <botan/botan.h>

namespace kaa {

typedef Botan::secure_vector<std::uint8_t> PublicKey;
typedef std::string                       PrivateKey;

typedef Botan::SymmetricKey               SessionKey;
typedef Botan::secure_vector<std::uint8_t> EncodedSessionKey;

typedef Botan::secure_vector<std::uint8_t> Signature;

class KeyPair
{
public:
    KeyPair(const PublicKey& pubKey, const PrivateKey& privKey) : pubKey_(pubKey), privKey_(privKey) { }
    KeyPair(const KeyPair& other) : pubKey_(other.pubKey_), privKey_(other.privKey_) { }
    KeyPair& operator=(const KeyPair& other) { pubKey_ = other.pubKey_; privKey_ = other.privKey_; return *this; }
    ~KeyPair() { }

    const PublicKey& getPublicKey() const { return pubKey_; }
    const PrivateKey& getPrivateKey() const { return privKey_; }

private:
    PublicKey pubKey_;
    PrivateKey privKey_;
};

}

#endif /* KAA_SECURITY_SECURITYDEFINITIONS_HPP_ */
