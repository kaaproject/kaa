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

#ifndef FILECONFIGURATIONSTORAGE_HPP_
#define FILECONFIGURATIONSTORAGE_HPP_


#include "kaa/configuration/storage/IConfigurationStorage.hpp"

#include <string>

namespace kaa {

class FileConfigurationStorage : public IConfigurationStorage {
public:
    FileConfigurationStorage(const std::string& filename) : filename_(filename) { }
    FileConfigurationStorage(std::string&& filename) : filename_(std::move(filename)) { }

    virtual void saveConfiguration(const std::vector<std::uint8_t>& bytes);
    virtual std::vector<std::uint8_t> loadConfiguration();
    virtual void clearConfiguration();

private:
    std::string filename_;
};

}



#endif /* FILECONFIGURATIONSTORAGE_HPP_ */
