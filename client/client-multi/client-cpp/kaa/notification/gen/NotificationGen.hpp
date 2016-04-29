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


#ifndef CLIENT_CLIENT_MULTI_CLIENT_CPP_KAA_GEN_NOTIFICATIONGEN_HPP_3229282092__H_
#define CLIENT_CLIENT_MULTI_CLIENT_CPP_KAA_GEN_NOTIFICATIONGEN_HPP_3229282092__H_


#include <sstream>
#include "boost/any.hpp"
#include "avro/Specific.hh"
#include "avro/Encoder.hh"
#include "avro/Decoder.hh"

namespace kaa_notification {
struct BasicNotification {
    std::string data;
    BasicNotification() :
        data(std::string())
        { }
};

}
namespace avro {
template<> struct codec_traits<kaa_notification::BasicNotification> {
    static void encode(Encoder& e, const kaa_notification::BasicNotification& v) {
        avro::encode(e, v.data);
    }
    static void decode(Decoder& d, kaa_notification::BasicNotification& v) {
        avro::decode(d, v.data);
    }
};

}
#endif
