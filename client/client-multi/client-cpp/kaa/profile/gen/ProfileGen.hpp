/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


#ifndef KAA_PROFILE_GEN_PROFILEGEN_HPP_2380542215__H_
#define KAA_PROFILE_GEN_PROFILEGEN_HPP_2380542215__H_


#include <sstream>
#include "boost/any.hpp"
#include "avro/Specific.hh"
#include "avro/Encoder.hh"
#include "avro/Decoder.hh"

namespace kaa_profile {
struct JbtProfile {
    std::string name;
    std::string role;
    std::string type;
    std::string timeZone;
    std::string equipmentModel;
    std::string deviceModel;
    std::string serialNumber;
    std::string softwareVersion;
    JbtProfile() :
        name(std::string()),
        role(std::string()),
        type(std::string()),
        timeZone(std::string()),
        equipmentModel(std::string()),
        deviceModel(std::string()),
        serialNumber(std::string()),
        softwareVersion(std::string())
        { }
};

}
namespace avro {
template<> struct codec_traits<kaa_profile::JbtProfile> {
    static void encode(Encoder& e, const kaa_profile::JbtProfile& v) {
        avro::encode(e, v.name);
        avro::encode(e, v.role);
        avro::encode(e, v.type);
        avro::encode(e, v.timeZone);
        avro::encode(e, v.equipmentModel);
        avro::encode(e, v.deviceModel);
        avro::encode(e, v.serialNumber);
        avro::encode(e, v.softwareVersion);
    }
    static void decode(Decoder& d, kaa_profile::JbtProfile& v) {
        if (avro::ResolvingDecoder *rd =
            dynamic_cast<avro::ResolvingDecoder *>(&d)) {
            const std::vector<size_t> fo = rd->fieldOrder();
            for (std::vector<size_t>::const_iterator it = fo.begin();
                it != fo.end(); ++it) {
                switch (*it) {
                case 0:
                    avro::decode(d, v.name);
                    break;
                case 1:
                    avro::decode(d, v.role);
                    break;
                case 2:
                    avro::decode(d, v.type);
                    break;
                case 3:
                    avro::decode(d, v.timeZone);
                    break;
                case 4:
                    avro::decode(d, v.equipmentModel);
                    break;
                case 5:
                    avro::decode(d, v.deviceModel);
                    break;
                case 6:
                    avro::decode(d, v.serialNumber);
                    break;
                case 7:
                    avro::decode(d, v.softwareVersion);
                    break;
                default:
                    break;
                }
            }
        } else {
            avro::decode(d, v.name);
            avro::decode(d, v.role);
            avro::decode(d, v.type);
            avro::decode(d, v.timeZone);
            avro::decode(d, v.equipmentModel);
            avro::decode(d, v.deviceModel);
            avro::decode(d, v.serialNumber);
            avro::decode(d, v.softwareVersion);
        }
    }
};

}
#endif
