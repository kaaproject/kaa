/*
 * Copyright 2014 CyberVision, Inc.
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

#ifndef KAATCPCOMMON_HPP_
#define KAATCPCOMMON_HPP_

#include <boost/cstdint.hpp>

namespace kaa {

enum class KaaTcpMessageType: boost::uint8_t
{
    MESSAGE_UNKNOWN = 0x00,
    MESSAGE_CONNECT = 0x01,
    MESSAGE_CONNACK = 0x02,
    MESSAGE_PINGREQ = 0x0C,
    MESSAGE_PINGRESP = 0x0D,
    MESSAGE_DISCONNECT = 0x0E,
    MESSAGE_KAASYNC = 0x0F
};

class KaaTcpCommon
{
public:
    static const boost::uint8_t FIRST_BIT = 0x80;
    static const boost::uint8_t MAX_MESSAGE_TYPE_LENGTH = 0x0F;
    static const boost::uint32_t MAX_MESSAGE_LENGTH = 0x0FFFFFFF;
    static const boost::uint8_t PROTOCOL_VERSION = 0x01;

    static const boost::uint8_t KAA_SYNC_HEADER_LENGTH = 12;
    static const boost::uint8_t KAA_SYNC_ZIPPED_BIT = 0x02;
    static const boost::uint8_t KAA_SYNC_ENCRYPTED_BIT = 0x04;
    static const boost::uint8_t KAA_SYNC_REQUEST_BIT = 0x01;

    static const boost::uint8_t KAA_CONNECT_HEADER_LENGTH = 14;
    static const boost::uint8_t KAA_CONNECT_SESSION_KEY_FLAGS = 0x11;
    static const boost::uint8_t KAA_CONNECT_SIGNATURE_FLAGS = 0x01;

    static const char * const KAA_TCP_NAME;
    static const boost::uint16_t KAA_TCP_NAME_LENGTH = 6;

    static boost::uint8_t createBasicHeader(boost::uint8_t messageType, boost::uint32_t length, char *message);

};

}



#endif /* KAATCPCOMMON_HPP_ */
