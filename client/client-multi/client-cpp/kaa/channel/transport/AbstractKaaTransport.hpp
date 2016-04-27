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

#ifndef ABSTRACTKAATRANSPORT_HPP_
#define ABSTRACTKAATRANSPORT_HPP_

#include "kaa/channel/transport/IKaaTransport.hpp"
#include "kaa/channel/IKaaChannelManager.hpp"
#include "kaa/common/exception/KaaException.hpp"
#include "kaa/IKaaClientStateStorage.hpp"
#include "kaa/IKaaClientContext.hpp"

namespace kaa {

template <TransportType Type>
class AbstractKaaTransport : public IKaaTransport
{
public:
    virtual ~AbstractKaaTransport() {}

    AbstractKaaTransport(IKaaChannelManager& channelManager, IKaaClientContext &context)
        : type_(Type), channelManager_(channelManager), context_(context) {}

private:
    IDataChannelPtr getChannel(TransportType transportType = Type)
    {
        IDataChannelPtr channel = channelManager_.getChannelByTransportType(transportType);
        if (channel) {
            return channel;
        }
        throw KaaException("Cannot find appropriate channel");
    }

protected:
    void syncByType(TransportType transportType = Type)
    {
        getChannel(transportType)->sync(transportType);
    }

    void syncAll()
    {
        getChannel()->syncAll();
    }

    void syncAck(TransportType transportType = Type)
    {
        getChannel(transportType)->syncAck(transportType);
    }

protected:
    const TransportType         type_;
    IKaaChannelManager          &channelManager_;
    IKaaClientContext           &context_;
};

}  // namespace kaa


#endif /* ABSTRACTKAATRANSPORT_HPP_ */
