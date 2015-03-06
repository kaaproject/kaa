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


#ifndef KAA_GEN_PROFILEGEN_HPP_1535262921__H_
#define KAA_GEN_PROFILEGEN_HPP_1535262921__H_


#include <sstream>
#include "boost/any.hpp"
#include "avro/Specific.hh"
#include "avro/Encoder.hh"
#include "avro/Decoder.hh"

namespace kaa {
enum OS {
    Android,
    iOS,
    Linux,
};

struct Profile {
    std::string id;
    OS os;
    std::string os_version;
    std::string build;
    Profile() :
        id(std::string()),
        os(OS()),
        os_version(std::string()),
        build(std::string())
        { }
};

}
namespace avro {
template<> struct codec_traits<kaa::OS> {
    static void encode(Encoder& e, kaa::OS v) {
		if (v < kaa::Android || v > kaa::Linux)
		{
			std::ostringstream error;
			error << "enum value " << v << " is out of bound for kaa::OS and cannot be encoded";
			throw avro::Exception(error.str());
		}
        e.encodeEnum(v);
    }
    static void decode(Decoder& d, kaa::OS& v) {
		size_t index = d.decodeEnum();
		if (index < kaa::Android || index > kaa::Linux)
		{
			std::ostringstream error;
			error << "enum value " << index << " is out of bound for kaa::OS and cannot be decoded";
			throw avro::Exception(error.str());
		}
        v = static_cast<kaa::OS>(index);
    }
};

template<> struct codec_traits<kaa::Profile> {
    static void encode(Encoder& e, const kaa::Profile& v) {
        avro::encode(e, v.id);
        avro::encode(e, v.os);
        avro::encode(e, v.os_version);
        avro::encode(e, v.build);
    }
    static void decode(Decoder& d, kaa::Profile& v) {
        avro::decode(d, v.id);
        avro::decode(d, v.os);
        avro::decode(d, v.os_version);
        avro::decode(d, v.build);
    }
};

}
#endif
