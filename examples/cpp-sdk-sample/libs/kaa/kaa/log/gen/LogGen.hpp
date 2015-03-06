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


#ifndef KAA_GEN_LOGGEN_HPP_392481303__H_
#define KAA_GEN_LOGGEN_HPP_392481303__H_


#include <sstream>
#include "boost/any.hpp"
#include "avro/Specific.hh"
#include "avro/Encoder.hh"
#include "avro/Decoder.hh"

namespace kaa {
enum Level {
    DEBUG,
    ERROR,
    FATAL,
    INFO,
    TRACE,
    WARN,
};

struct LogData {
    Level level;
    std::string tag;
    std::string message;
    LogData() :
        level(Level()),
        tag(std::string()),
        message(std::string())
        { }
};

}
namespace avro {
template<> struct codec_traits<kaa::Level> {
    static void encode(Encoder& e, kaa::Level v) {
		if (v < kaa::DEBUG || v > kaa::WARN)
		{
			std::ostringstream error;
			error << "enum value " << v << " is out of bound for kaa::Level and cannot be encoded";
			throw avro::Exception(error.str());
		}
        e.encodeEnum(v);
    }
    static void decode(Decoder& d, kaa::Level& v) {
		size_t index = d.decodeEnum();
		if (index < kaa::DEBUG || index > kaa::WARN)
		{
			std::ostringstream error;
			error << "enum value " << index << " is out of bound for kaa::Level and cannot be decoded";
			throw avro::Exception(error.str());
		}
        v = static_cast<kaa::Level>(index);
    }
};

template<> struct codec_traits<kaa::LogData> {
    static void encode(Encoder& e, const kaa::LogData& v) {
        avro::encode(e, v.level);
        avro::encode(e, v.tag);
        avro::encode(e, v.message);
    }
    static void decode(Decoder& d, kaa::LogData& v) {
        avro::decode(d, v.level);
        avro::decode(d, v.tag);
        avro::decode(d, v.message);
    }
};

}
#endif
