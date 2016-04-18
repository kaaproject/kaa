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


#ifndef KAA_GEN_PROFILEGEN_HPP_419439815__H_
#define KAA_GEN_PROFILEGEN_HPP_419439815__H_


#include <sstream>
#include "boost/any.hpp"
#include "avro/Specific.hh"
#include "avro/Encoder.hh"
#include "avro/Decoder.hh"

namespace kaa_profile {
struct Profile {
    Profile()
        { }
};

}
namespace avro {
template<> struct codec_traits<kaa_profile::Profile> {
    static void encode(Encoder& e, const kaa_profile::Profile& v) {
    }
    static void decode(Decoder& d, kaa_profile::Profile& v) {
    }
};

}
#endif
