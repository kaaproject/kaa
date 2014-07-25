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

#include "kaa/security/KeyUtils.hpp"
#include "kaa/common/exception/KaaException.hpp"
#include <botan/rsa.h>
#include <fstream>

namespace kaa {

KeyPair KeyUtils::generateKeyPair(size_t length)
{
    Botan::RSA_PrivateKey key(rng_, length);
    return std::make_pair(Botan::X509::BER_encode(key), Botan::PKCS8::PEM_encode(key));
}

Botan::SymmetricKey KeyUtils::generateSessionKey(size_t length)
{
    return Botan::SymmetricKey(rng_, length);
}

void KeyUtils::readFile(const std::string& fileName, boost::scoped_array<char>& buf, size_t& len)
{
    std::ifstream file(fileName, std::ifstream::binary);
    if (file.good()) {
        file.seekg(0, file.end);
        len = file.tellg();
        if (!len) {
            throw KaaException("Key file " + fileName + " is empty");
        }
        file.seekg(0, file.beg);

        buf.reset(new char[len]);
        file.read(buf.get(), len);
        file.close();

    } else {
        file.close();
        throw KaaException("Failed to read key file " + fileName);
    }
}

Botan::MemoryVector<boost::uint8_t> KeyUtils::loadPublicKey(const std::string& fileName)
{
    size_t length = 0;
    boost::scoped_array<char> buf;
    readFile(fileName, buf, length);
    return Botan::MemoryVector<boost::uint8_t>(reinterpret_cast<const boost::uint8_t *>(buf.get()), length);
}

std::string KeyUtils::loadPrivateKey(const std::string& fileName)
{
    size_t length = 0;
    boost::scoped_array<char> buf;
    readFile(fileName, buf, length);
    return std::string(buf.get(), length);
}

KeyPair KeyUtils::loadKeyPair(const std::string& pubFileName, const std::string& privFileName)
{
    const Botan::MemoryVector<boost::uint8_t>& pub = loadPublicKey(pubFileName);
    const std::string& priv = loadPrivateKey(privFileName);
    return std::make_pair(pub, priv);
}

void KeyUtils::savePublicKey(const Botan::MemoryVector<boost::uint8_t>& key, const std::string& pubFileName)
{
    std::ofstream file(pubFileName, std::ofstream::binary);
    file.write(reinterpret_cast<const char *>(key.begin()), key.size());
    file.close();
}

void KeyUtils::savePrivateKey(const std::string& key, const std::string& privFileName)
{
    std::ofstream file(privFileName);
    file << key;
    file.close();
}

void KeyUtils::saveKeyPair(const KeyPair& pair, const std::string& pubFileName, const std::string& privFileName)
{
    savePublicKey(pair.first, pubFileName);
    savePrivateKey(pair.second, privFileName);
}

}


