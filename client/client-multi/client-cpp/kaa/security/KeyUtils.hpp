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

#ifndef KEYUTILS_HPP_
#define KEYUTILS_HPP_

#include <botan/botan.h>
#include <string>
#include <utility>
#include <cstdint>
#include <memory>
#include <boost/scoped_array.hpp>
#include "kaa/security/SecurityDefinitions.hpp"

namespace kaa {

class KeyUtils {
public:
    /**
     * Generates RSA private and public keys.
     *
     * @param length the length of the key in bits
     * @return the pair of keys. The first element of the pair is a public key and the second one - is a private key.
     */
    KeyPair generateKeyPair(std::size_t length);

    /**
     * Generates symmetric session key
     *
     * @param length the length of the key in bytes
     * @return the pair of key and initialization vector
     */
    SessionKey generateSessionKey(std::size_t length);

    /**
     * Checks consistency of the key pair.
     *
     * @param[in] keys     Target key pair
     * @retval    true     Keys are valid
     * @retval    false    Keys are invalid
     */
    bool checkKeyPair(const KeyPair &keys);

    static PublicKey loadPublicKey(const std::string& fileName);

    static PrivateKey loadPrivateKey(const std::string& fileName);

    static KeyPair loadKeyPair(const std::string& pubFileName, const std::string& privFileName);

    static void saveKeyPair(const KeyPair& pair, const std::string& pubFileName, const std::string& privFileName);

    static void savePublicKey(const PublicKey& key, const std::string& pubFileName);

    static void savePrivateKey(const PrivateKey& key, const std::string& privFileName);

private:
    static void readFile(const std::string& fileName, boost::scoped_array<char>& buf, std::size_t& len);

private:
    Botan::AutoSeeded_RNG     rng_;
};

}

#endif /* KEYUTILS_HPP_ */
