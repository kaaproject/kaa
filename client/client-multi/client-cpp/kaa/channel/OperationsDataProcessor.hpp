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

#ifndef OPERATIONSPROCESSOR_HPP_
#define OPERATIONSPROCESSOR_HPP_

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
#include "kaa/IKaaClientStateStorage.hpp"

namespace kaa {

typedef boost::shared_ptr<IMetaDataTransport>       IMetaDataTransportPtr;
typedef boost::shared_ptr<IProfileTransport>        IProfileTransportPtr;
typedef boost::shared_ptr<IConfigurationTrasnport>  IConfigurationTransportPtr;
typedef boost::shared_ptr<INotificationTransport>   INotificationTransportPtr;
typedef boost::shared_ptr<IUserTransport>           IUserTransportPtr;
typedef boost::shared_ptr<IEventTransport>          IEventTransportPtr;
typedef boost::shared_ptr<ILoggingTransport>        ILoggingTransportPtr;
typedef boost::shared_ptr<IRedirectionTransport>    IRedirectionTransportPtr;

class OperationsDataProcessor : public IKaaDataMultiplexer, public IKaaDataDemultiplexer {
public:
    OperationsDataProcessor(  IMetaDataTransportPtr
                        , IProfileTransportPtr
                        , IConfigurationTransportPtr
                        , INotificationTransportPtr
                        , IUserTransportPtr
                        , IEventTransportPtr
                        , ILoggingTransportPtr
                        , IRedirectionTransportPtr
                        , IKaaClientStateStoragePtr
    );

    virtual std::vector<boost::uint8_t> compileRequest(const std::map<TransportType, ChannelDirection>& transportTypes);
    virtual void processResponse(const std::vector<boost::uint8_t> &response);
private:
    AvroByteArrayConverter<SyncRequest>     requestConverter_;
    AvroByteArrayConverter<SyncResponse>    responseConverter_;

    IMetaDataTransportPtr       metaDataTransport_;
    IProfileTransportPtr        profileTransport_;
    IConfigurationTransportPtr  configurationTransport_;
    INotificationTransportPtr   notificationTransport_;
    IUserTransportPtr           userTransport_;
    IEventTransportPtr          eventTransport_;
    ILoggingTransportPtr        loggingTransport_;
    IRedirectionTransportPtr    redirectionTransport_;

    IKaaClientStateStoragePtr   clientStatus_;

    boost::int32_t              requestId;
};

}  // namespace kaa


#endif /* OPERATIONSPROCESSOR_HPP_ */
