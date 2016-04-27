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

#include "kaa/kaatcp/KaaTcpParser.hpp"
#include "kaa/logging/Log.hpp"
#include "kaa/logging/LoggingUtils.hpp"
#include "kaa/common/exception/KaaException.hpp"

namespace kaa {

void KaaTcpParser::onMessageDone()
{
    KAA_LOG_DEBUG("KaaTcp: payload is fully received");
    messages_.push_back(std::make_pair(messageType_, std::make_pair(messagePayload_, messageLength_)));
    resetParser();
}

void KaaTcpParser::processByte(char byte)
{
    switch (state_) {
        case KaaTcpParserState::NONE:
            retrieveMessageType(byte);
            KAA_LOG_DEBUG(boost::format("KaaTcp: retrieved message's type %1%") % (int) messageType_);
            state_ = KaaTcpParserState::PROCESSING_LENGTH;
            break;
        case KaaTcpParserState::PROCESSING_LENGTH:
            messageLength_ += ((std::uint8_t)byte & ~KaaTcpCommon::FIRST_BIT) * lenghtMultiplier_;
            lenghtMultiplier_ *= KaaTcpCommon::FIRST_BIT;
            if (!((std::uint8_t)byte & KaaTcpCommon::FIRST_BIT)) {
                KAA_LOG_DEBUG(boost::format("KaaTcp: retrieved message's size %1%") % (std::uint32_t) messageLength_);
                if (messageLength_) {
                    messagePayload_.reset(new char[messageLength_]);
                    state_ = KaaTcpParserState::PROCESSING_PAYLOAD;
                } else {
                    onMessageDone();
                }
            }
            break;
        default:
            break;
    }
}

void KaaTcpParser::retrieveMessageType(char byte)
{
    int messageType = ((std::uint8_t)(byte) >> 4);
    if (messageType != (int)KaaTcpMessageType::MESSAGE_CONNACK
        && messageType != (int)KaaTcpMessageType::MESSAGE_PINGRESP
        && messageType != (int)KaaTcpMessageType::MESSAGE_DISCONNECT
        && messageType != (int)KaaTcpMessageType::MESSAGE_KAASYNC) {
            throw KaaException(boost::format("KaaTcp: unexpected message type: %1%") % messageType);
    }
    messageType_ = (KaaTcpMessageType) messageType;
}

void KaaTcpParser::parseBuffer(const char *buffer, std::uint32_t size)
{
    auto cursor = buffer;
    while (cursor != buffer + size) {
        if (state_ == KaaTcpParserState::PROCESSING_PAYLOAD) {
            std::uint32_t remainingSize = messageLength_ - processedPayloadLength_;
            std::uint32_t bufferRemainingSize = buffer + size - cursor;
            std::uint32_t bytesToRead = (remainingSize > bufferRemainingSize) ? bufferRemainingSize : remainingSize;
            std::copy(cursor, cursor + bytesToRead, messagePayload_.get() + processedPayloadLength_);
            cursor += bytesToRead;
            processedPayloadLength_ += bytesToRead;
            KAA_LOG_DEBUG(boost::format("KaaTcp: processed payload. Remaining buffer size is %1%") % ((buffer + size) - cursor));
            if (messageLength_ == processedPayloadLength_) {
                onMessageDone();
            }
        } else {
            processByte(*(cursor++));
        }
    }
}

MessageRecordList KaaTcpParser::releaseMessages()
{
    MessageRecordList result = messages_;
    messages_.clear();
    return result;
}

void KaaTcpParser::resetParser()
{
    state_ = KaaTcpParserState::NONE;
    messagePayload_.reset();
    messageLength_ = 0;
    processedPayloadLength_ = 0;
    messageType_ = KaaTcpMessageType::MESSAGE_UNKNOWN;
    lenghtMultiplier_ = 1;
}

}


