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

#include "kaa/bootstrap/BootstrapManager.hpp"

#include <ctime>
#include <cstdlib>
#include <cstdint>
#include <algorithm>

#include "kaa/KaaDefaults.hpp"
#include "kaa/logging/Log.hpp"
#include "kaa/logging/LoggingUtils.hpp"
#include "kaa/common/exception/KaaException.hpp"
#include "kaa/channel/server/HttpServerInfo.hpp"
#include "kaa/channel/server/HttpLPServerInfo.hpp"
#include "kaa/channel/server/KaaTcpServerInfo.hpp"

namespace kaa {

void BootstrapManager::receiveOperationsServerList()
{
    if (bootstrapTransport_ != nullptr) {
        bootstrapTransport_->sync();
    }
}

const OperationsServer* BootstrapManager::getOPSByDnsName(const std::string& name)
{
    for (const OperationsServer& ops : operationServerList_) {
        if (ops.name.compare(name) == 0) {
            return &ops;
        }
    }
    return nullptr;
}

IServerInfoPtr BootstrapManager::getServerInfoByChannel(const OperationsServer& server, const SupportedChannel& channel)
{
    switch (channel.channelType) {
        case ChannelType::HTTP: {
            HTTPComunicationParameters params = channel.communicationParameters.get_HTTPComunicationParameters();
            PublicKey pubKey(server.publicKey.data(), server.publicKey.size());
            KAA_LOG_DEBUG(boost::format("Server name: %1%, Parameters: %2%:%3%")
                     % server.name % params.hostName % params.port);
            IServerInfoPtr info(new HttpServerInfo(ServerType::OPERATIONS, params.hostName, params.port, pubKey));
            return info;
        }
        case ChannelType::HTTP_LP: {
            HTTPLPComunicationParameters params = channel.communicationParameters.get_HTTPLPComunicationParameters();
            PublicKey pubKey(server.publicKey.data(), server.publicKey.size());
            KAA_LOG_DEBUG(boost::format("Server name: %1%, Parameters: %2%:%3%")
                     % server.name % params.hostName % params.port);
            IServerInfoPtr info(new HttpLPServerInfo(ServerType::OPERATIONS, params.hostName, params.port, pubKey));
            return info;
        }
        case ChannelType::KAATCP: {
            KaaTCPComunicationParameters params = channel.communicationParameters.get_KaaTCPComunicationParameters();
            PublicKey pubKey(server.publicKey.data(), server.publicKey.size());
            IServerInfoPtr info(new KaaTcpServerInfo(ServerType::OPERATIONS, params.hostName, params.port, pubKey));
            return info;
        }
        default:
            break;
    }

    throw KaaException("Failed to find appropriate server info");
}

IServerInfoPtr BootstrapManager::getServerInfoByChannelType(const OperationsServer& server, ChannelType channelType)
{
    for (const SupportedChannel& cs : server.supportedChannelsArray) {
        if (channelType == cs.channelType) {
            return getServerInfoByChannel(server, cs);
        }
    }
    throw KaaException("Failed to find appropriate server info");
}

void BootstrapManager::useNextOperationsServer(ChannelType type)
{
    KAA_R_MUTEX_UNIQUE_DECLARE(lock, guard_);
    auto it = operationServersIterators_.find(type);
    auto collectionIt = operationServers_.find(type);
    if (it != operationServersIterators_.end() && collectionIt != operationServers_.end()) {
        if (++(it->second) != collectionIt->second.size()) {
            KAA_LOG_INFO(boost::format("Going to switch to server: %1% for channel type: %2%")
                            % LoggingUtils::OperationServerToString(collectionIt->second.at(it->second))
                            % LoggingUtils::ChannelTypeToString(it->first));
            if (channelManager_ != nullptr) {
                channelManager_->onServerUpdated(getServerInfoByChannelType(collectionIt->second.at(it->second), it->first));
            } else {
                KAA_LOG_ERROR("Can not process server change. Channel manager was not specified");
            }
        } else {
            KAA_LOG_INFO(boost::format("No available server for channel type %1%.") % LoggingUtils::ChannelTypeToString(type));
            bootstrapTransport_->sync();
        }
    } else {
        throw KaaException("There are no available servers at the time");
    }
}

void BootstrapManager::useNextOperationsServerByDnsName(const std::string& name)
{
    KAA_R_MUTEX_UNIQUE_DECLARE(lock, guard_);
    KAA_LOG_DEBUG(boost::format("Going to use new operations server: %1%") % name);
    const OperationsServer * ops = getOPSByDnsName(name);
    if (ops != nullptr) {
        notifyChannelManangerAboutServer(*ops);
    } else {
        serverToApply = name;
        bootstrapTransport_->sync();
    }
}

void BootstrapManager::setTransport(IBootstrapTransport* transport)
{
    KAA_R_MUTEX_UNIQUE_DECLARE(lock, guard_);
    bootstrapTransport_ = dynamic_cast<BootstrapTransport *>(transport);
    if (bootstrapTransport_ != nullptr) {
        return;
    }
    throw KaaException("Bad bootstrap transport");
}

void BootstrapManager::setChannelManager(IKaaChannelManager* manager)
{
    KAA_R_MUTEX_UNIQUE_DECLARE(lock, guard_);
    channelManager_ = manager;
}

void BootstrapManager::onServerListUpdated(const OperationsServerList& list)
{
    KAA_R_MUTEX_UNIQUE_DECLARE(lock, guard_);

    KAA_LOG_INFO(boost::format("Received new operations servers list: %1%")
        % LoggingUtils::OperationServerListToString(list));

    operationServerList_ = list.operationsServerArray;
    operationServersIterators_.clear();
    operationServers_.clear();

    for (OperationsServer server : operationServerList_) {
        auto supportedChannels = server.supportedChannelsArray;
        for (SupportedChannel channel : supportedChannels) {
            switch (channel.channelType) {
                case ChannelType::HTTP:
                case ChannelType::HTTP_LP:
                case ChannelType::KAATCP:
                    break;
                default:
                    throw KaaException("Unsupported channel type");
            }
            auto it = operationServers_.find(channel.channelType);
            if (it != operationServers_.end()) {
                it->second.push_back(server);
            } else {
                std::vector<OperationsServer> singleServerList;
                singleServerList.reserve(10);
                singleServerList.push_back(server);
                operationServers_.insert(std::make_pair(channel.channelType, singleServerList));
            }
        }
    }

    for (auto it = operationServers_.begin(); it != operationServers_.end(); ++it) {
        it->second.shrink_to_fit();
        std::random_shuffle(it->second.begin(), it->second.end());
        std::sort(it->second.begin(), it->second.end()
                , [&](const OperationsServer& a, const OperationsServer& b) -> bool { return a.priority > b.priority; });

        KAA_LOG_FTRACE(boost::format("Operations servers by type %1% shuffled: %2%")
                % LoggingUtils::ChannelTypeToString(it->first)
                % LoggingUtils::OperationServerArrayToString(it->second)
        );

        operationServersIterators_[it->first] = 0;
    }

    KAA_LOG_INFO(boost::format("Processed new operations servers list. Going to use server %1%") % (serverToApply.empty() ? "null" : serverToApply));

    if (!serverToApply.empty()) {
        const OperationsServer* ops = getOPSByDnsName(serverToApply);
        KAA_LOG_DEBUG(boost::format("Found server by name %1%. Object is %2%") % serverToApply % ops);
        if (ops != nullptr) {
            serverToApply.clear();
            notifyChannelManangerAboutServer(*ops);
        }
    } else {
        for (const auto& typeToIteratorPair : operationServersIterators_) {
            KAA_LOG_DEBUG(boost::format("Updating server type = %1%")
                    % LoggingUtils::ChannelTypeToString(typeToIteratorPair.first)
            );
            if (channelManager_ != nullptr) {
                channelManager_->onServerUpdated(getServerInfoByChannelType(operationServers_[typeToIteratorPair.first].at(typeToIteratorPair.second), typeToIteratorPair.first));
            } else {
                KAA_LOG_ERROR("Can not process server change. Channel manager was not specified");
            }
        }
    }
}

const std::vector<OperationsServer>& BootstrapManager::getOperationsServerList()
{
    KAA_R_MUTEX_UNIQUE_DECLARE(lock, guard_);
    return operationServerList_;
}

void BootstrapManager::notifyChannelManangerAboutServer(const OperationsServer& server)
{
    const auto & suppordetChannels = server.supportedChannelsArray;
    KAA_LOG_INFO(boost::format("Updating to server %1%")
                    % LoggingUtils::OperationServerToString(server));
    for (const SupportedChannel &sc : suppordetChannels) {
        if (channelManager_ != nullptr) {
            channelManager_->onServerUpdated(getServerInfoByChannel(server, sc));
        } else {
            KAA_LOG_ERROR(boost::format("Can not process server change for channel (%1%). Channel manager was not specified")
                                % LoggingUtils::SupportedChannelToString(sc));
        }
    }
}

}

