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


#ifndef KAA_GEN_LOGGEN_HPP_392481303__H_
#define KAA_GEN_LOGGEN_HPP_392481303__H_


#include <sstream>
#include "boost/any.hpp"
#include "avro/Specific.hh"
#include "avro/Encoder.hh"
#include "avro/Decoder.hh"

namespace kaa_log {
struct SuperRecord {
    std::string logdata;
    SuperRecord() :
        logdata(std::string())
        { }
};

}
namespace avro {
template<> struct codec_traits<kaa_log::SuperRecord> {
    static void encode(Encoder& e, const kaa_log::SuperRecord& v) {
        avro::encode(e, v.logdata);
    }
    static void decode(Decoder& d, kaa_log::SuperRecord& v) {
        avro::decode(d, v.logdata);
    }
};

}
#endif
