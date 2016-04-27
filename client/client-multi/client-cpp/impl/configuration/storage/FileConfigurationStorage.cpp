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

#include "kaa/configuration/storage/FileConfigurationStorage.hpp"

#include <fstream>
#include <iterator>
#include <cstdio>

namespace kaa {


void FileConfigurationStorage::saveConfiguration(const std::vector<std::uint8_t>& bytes)
{
    std::ofstream outFile(filename_, std::ofstream::binary);
    if (outFile.good()) {
        outFile.write(reinterpret_cast<const char *>(bytes.data()), bytes.size());
        outFile.close();
    }
}

std::vector<std::uint8_t> FileConfigurationStorage::loadConfiguration()
{
    std::ifstream inFile(filename_, std::ifstream::ate | std::ifstream::binary);
    if (inFile.good()) {
        std::vector<std::uint8_t> result;
        result.reserve(inFile.tellg());
        inFile.seekg(0, std::ifstream::beg);

        result.assign(std::istreambuf_iterator<char>(inFile),
                      std::istreambuf_iterator<char>());

        return result;
    }
    return std::vector<std::uint8_t>();
}

void FileConfigurationStorage::clearConfiguration()
{
    std::remove(filename_.c_str());
}

}

