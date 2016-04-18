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

#ifndef SYNC_DATA_PROCESSOR_HPP_
#define SYNC_DATA_PROCESSOR_HPP_

#include "kaa/channel/IKaaDataMultiplexer.hpp"
#include "kaa/channel/IKaaDataDemultiplexer.hpp"
#include "kaa/common/AvroByteArrayConverter.hpp"
#include "kaa/gen/EndpointGen.hpp"

#include "kaa/channel/transport/IMetaDataTransport.hpp"
#include "kaa/channel/transport/IConfigurationTransport.hpp"
#include "kaa/channel/transport/IUserTransport.hpp"
#include "kaa/channel/transport/IEventTransport.hpp"
#include "kaa/channel/transport/ILoggingTransport.hpp"
#include "kaa/channel/transport/INotificationTransport.hpp"
#include "kaa/channel/transport/IProfileTransport.hpp"
#include "kaa/channel/transport/IRedirectionTransport.hpp"
#include "kaa/channel/transport/IBootstrapTransport.hpp"
#include "kaa/IKaaClientStateStorage.hpp"
#include "kaa/IKaaClientContext.hpp"

namespace kaa {

typedef std::shared_ptr<IMetaDataTransport>       IMetaDataTransportPtr;
typedef std::shared_ptr<IBootstrapTransport>      IBootstrapTransportPtr;
typedef std::shared_ptr<IConfigurationTransport>  IConfigurationTransportPtr;
typedef std::shared_ptr<INotificationTransport>   INotificationTransportPtr;
typedef std::shared_ptr<IUserTransport>           IUserTransportPtr;
typedef std::shared_ptr<IEventTransport>          IEventTransportPtr;
typedef std::shared_ptr<ILoggingTransport>        ILoggingTransportPtr;
typedef std::shared_ptr<IRedirectionTransport>    IRedirectionTransportPtr;

class SyncDataProcessor : public IKaaDataMultiplexer, public IKaaDataDemultiplexer {
public:
    SyncDataProcessor(IMetaDataTransportPtr
                    , IBootstrapTransportPtr
                    , IProfileTransportPtr
                    , IConfigurationTransportPtr
                    , INotificationTransportPtr
                    , IUserTransportPtr
                    , IEventTransportPtr
                    , ILoggingTransportPtr
                    , IRedirectionTransportPtr
                    , IKaaClientContext&);

    virtual std::vector<std::uint8_t> compileRequest(const std::map<TransportType, ChannelDirection>& transportTypes);
    virtual DemultiplexerReturnCode processResponse(const std::vector<std::uint8_t> &response);
private:
    AvroByteArrayConverter<SyncRequest>     requestConverter_;
    AvroByteArrayConverter<SyncResponse>    responseConverter_;

    IMetaDataTransportPtr       metaDataTransport_;
    IBootstrapTransportPtr      bootstrapTransport_;
    IProfileTransportPtr        profileTransport_;
    IConfigurationTransportPtr  configurationTransport_;
    INotificationTransportPtr   notificationTransport_;
    IUserTransportPtr           userTransport_;
    IEventTransportPtr          eventTransport_;
    ILoggingTransportPtr        loggingTransport_;
    IRedirectionTransportPtr    redirectionTransport_;

    IKaaClientStateStoragePtr   clientStatus_;

    std::int32_t                requestId;

    IKaaClientContext &context_;
};

}  // namespace kaa


#endif /* SYNC_DATA_PROCESSOR_HPP_ */
