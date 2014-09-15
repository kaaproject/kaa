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

#include "kaa/channel/KaaChannelManager.hpp"

#include <sstream>

#include "kaa/logging/Log.hpp"
#include "kaa/logging/LoggingUtils.hpp"
#include "kaa/gen/BootstrapGen.hpp"
#include "kaa/bootstrap/IBootstrapManager.hpp"
#include "kaa/common/exception/KaaException.hpp"
#include "kaa/channel/server/AbstractServerInfo.hpp"

namespace kaa {

KaaChannelManager::KaaChannelManager(IBootstrapManager& manager, const BootstrapServers& servers)
    : bootstrapManager_(manager)
{
    for (const auto& si : servers) {
        auto& list = bootstrapServers_[si->getChannelType()];
        list.push_back(si);
    }
}

void KaaChannelManager::onServerFailed(IServerInfoPtr server) {
    if (!server) {
        KAA_LOG_WARN("Failed to process server failure: bad input data")
        throw KaaException("empty server pointer");
    }

    if (server->getServerType() == ServerType::BOOTSTRAP) {
        onServerUpdated(getNextBootstrapServer(server));
    } else {
        bootstrapManager_.useNextOperationsServer(server->getChannelType());
    }
}

void KaaChannelManager::onServerUpdated(IServerInfoPtr server) {
    if (!server) {
        KAA_LOG_WARN("Failed to update server: bad input data")
        throw KaaException("empty server pointer");
    }

    ChannelType type = server->getChannelType();
    if (server->getServerType() == ServerType::OPERATIONS) {
        lastServers_[type] = server;
    }

    for (auto& channel : channels_) {
        if (channel->getServerType() == server->getServerType() && channel->getChannelType() == type) {
            KAA_LOG_DEBUG(boost::format("Setting a new server for channel \"%1%\" type %2%")
                        % channel->getId() % LoggingUtils::ChannelTypeToString(channel->getChannelType()));
            channel->setServer(server);
        }
    }
}

void KaaChannelManager::addChannel(IDataChannelPtr channel)
{
    if (!channel) {
        KAA_LOG_WARN("Failed to add channel: bad input data")
        throw KaaException("empty channel pointer");
    }

    auto res = channels_.insert(channel);

    if (res.second) {
        (*res.first)->setConnectivityChecker(connectivityChecker_);

        IServerInfoPtr server;

        if (channel->getServerType() == ServerType::BOOTSTRAP) {
            server = getCurrentBootstrapServer(channel->getChannelType());
        } else {
            auto it = lastServers_.find(channel->getChannelType());
            if (it != lastServers_.end()) {
                server = it->second;
            }
        }

        if (server) {
            KAA_LOG_DEBUG(boost::format("Setting a new server for channel \"%1%\" type %2%")
                        % channel->getId() % LoggingUtils::ChannelTypeToString(channel->getChannelType()));
            channel->setServer(server);
        } else {
            KAA_LOG_WARN(boost::format("Failed to find server for channel \"%1%\" type %2%")
                        % channel->getId() % LoggingUtils::ChannelTypeToString(channel->getChannelType()))
        }

        useNewChannel(channel);
    } else {
        KAA_LOG_WARN("Failed to add channel: duplicate");
        throw KaaException("duplicate data channel");
    }
}

void KaaChannelManager::useNewChannel(IDataChannelPtr channel)
{
    const auto& types = channel->getSupportedTransportTypes();
    for (const auto& type : types) {
        if (type.second == ChannelDirection::UP || type.second == ChannelDirection::BIDIRECTIONAL) {
            KAA_LOG_INFO(boost::format("New channel (id='%1%') will be used for %2% transport type")
                                                                    % channel->getId() % LoggingUtils::TransportTypeToString(type.first));
            mappedChannels_[type.first] = channel;
        }
    }
}

void KaaChannelManager::removeChannel(IDataChannelPtr channel)
{
    if (!channel) {
        KAA_LOG_WARN("Failed to remove channel: bad input data")
        throw KaaException("empty channel pointer");
    }

    channels_.erase(channel);

    for (const auto& channelInfo : mappedChannels_) {
        if (channelInfo.second == channel) {
            useNewChannelForType(channelInfo.first);
        }
    }
}

void KaaChannelManager::useNewChannelForType(TransportType type) {
    for (const auto& channel : channels_) {
        const auto& types = channel->getSupportedTransportTypes();
        auto it = types.find(type);

        if (it != types.end() && (it->second == ChannelDirection::UP || it->second == ChannelDirection::BIDIRECTIONAL)) {
            KAA_LOG_INFO(boost::format("New channel (id='%1%') will be used for %2% transport type")
                                                                    % channel->getId() % LoggingUtils::TransportTypeToString(type));
            mappedChannels_[type] = channel;
            return;
        }
    }

    mappedChannels_[type] = IDataChannelPtr();
}

std::list<IDataChannelPtr> KaaChannelManager::getChannels()
{
    std::list<IDataChannelPtr> channels(channels_.begin(), channels_.end());
    return channels;
}

std::list<IDataChannelPtr> KaaChannelManager::getChannelsByType(ChannelType type)
{
    std::list<IDataChannelPtr> channels;

    for (auto& channel : channels_) {
        if (channel->getChannelType() == type) {
            channels.push_back(channel);
        }
    }

    return channels;
}

IDataChannelPtr KaaChannelManager::getChannelByTransportType(TransportType type)
{
    IDataChannelPtr channel = nullptr;
    auto it = mappedChannels_.find(type);

    if (it != mappedChannels_.end()) {
        channel = it->second;
    }

    return channel;
}

IDataChannelPtr KaaChannelManager::getChannel(const std::string& channelId)
{
    IDataChannelPtr channel = nullptr;

    for (const auto& c : channels_) {
        if (c->getId() == channelId) {
            channel = c;
        }
    }

    return channel;
}

void KaaChannelManager::clearChannelList()
{
    channels_.clear();
    mappedChannels_.clear();
}

IServerInfoPtr KaaChannelManager::getCurrentBootstrapServer(ChannelType type)
{
    IServerInfoPtr si;
    auto it = lastBSServers_.find(type);
    if (it == lastBSServers_.end()) {
        auto serverTypeIt = bootstrapServers_.find(type);
        if (serverTypeIt != bootstrapServers_.end()) {
            si = (*serverTypeIt).second.front();
            lastBSServers_[type] = si;
        }
    } else {
        si = (*it).second;
    }

    return si;
}

IServerInfoPtr KaaChannelManager::getNextBootstrapServer(IServerInfoPtr currentServer)
{
    IServerInfoPtr si;

    auto serverTypeIt = bootstrapServers_.find(currentServer->getChannelType());
    if (serverTypeIt != bootstrapServers_.end()) {
        const auto& list = (*serverTypeIt).second;
        auto serverIt = std::find(list.begin(), list.end(), currentServer);

        if (serverIt != list.end()) {
            if (++serverIt != list.end()) {
                si = (*serverIt);
            } else {
                si = list.front();
            }
        }
    }

    return si;
}

void KaaChannelManager::setConnectivityChecker(ConnectivityCheckerPtr checker) {
    connectivityChecker_ = checker;
    for (auto& channel : channels_) {
        channel->setConnectivityChecker(connectivityChecker_);
    }
}

} /* namespace kaa */
