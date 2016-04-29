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

#ifndef UUIDGENERATOR_HPP_
#define UUIDGENERATOR_HPP_

#include "kaa/KaaDefaults.hpp"

#include <string>
#include <sstream>
#include <boost/uuid/uuid.hpp>
#include <boost/uuid/uuid_io.hpp>
#include <boost/uuid/name_generator.hpp>
#include <boost/uuid/random_generator.hpp>

namespace kaa {

class UuidGenerator {
public:
    static std::string generateUuid() {
        boost::uuids::basic_random_generator<boost::mt19937> gen;
        boost::uuids::uuid uuid = gen();
        std::stringstream ss;
        ss << uuid;
        return ss.str();
    }

    static std::int32_t generateRandomInt() {
        boost::uuids::basic_random_generator<boost::mt19937> gen;
        boost::uuids::uuid uuid = gen();
		std::int32_t rand = (uuid.data[12] << 24) | (uuid.data[13] << 16)
				| (uuid.data[14] << 8) | (uuid.data[15]);
		return rand;
    }

    static void generateUuid(std::string& uuid_s) {
        boost::uuids::basic_random_generator<boost::mt19937> gen;
        boost::uuids::uuid uuid = gen();
        std::stringstream ss;
        ss << uuid;
        uuid_s.assign(ss.str());
    }

    static void generateUuid(std::string& uuid_s, std::string data) {
        namespace buuids = boost::uuids;
        buuids::uuid seed;
        buuids::name_generator generator(seed);
        buuids::uuid uuid = generator(data);

        std::stringstream ss;
        ss << uuid;
        uuid_s.assign(ss.str());
    }
};

} // namespace kaa

#endif /* UUIDGENERATOR_HPP_ */
