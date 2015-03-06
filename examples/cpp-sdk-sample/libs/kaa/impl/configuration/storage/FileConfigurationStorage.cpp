/*
 * Copyright 2014-2015 CyberVision, Inc.
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

namespace kaa {


void FileConfigurationStorage::saveConfiguration(std::vector<std::uint8_t>&& bytes)
{
    std::ofstream outFile(filename_, std::ifstream::binary);
    if (outFile.good()) {
        outFile.write(reinterpret_cast<char *>(bytes.data()), bytes.size());
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

        result.assign(std::istream_iterator<std::uint8_t>(inFile),
                      std::istream_iterator<std::uint8_t>());

        return result;
    }
    return std::vector<std::uint8_t>();
}

}

