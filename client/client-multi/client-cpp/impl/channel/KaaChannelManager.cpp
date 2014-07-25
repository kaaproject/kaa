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

void KaaChannelManager::onServerFailed(IServerInfoPtr server) {
    if (!server) {
        KAA_LOG_WARN("Failed to process server failure: bad input data")
        throw KaaException("empty server pointer");
    }

    if (server->getType() == ChannelType::BOOTSTRAP) {
        ++bootstrapServerIt_;

        if (bootstrapServerIt_ == bootstrapServers_.end()) {
            bootstrapServerIt_ = bootstrapServers_.begin();
        }

        IServerInfoPtr newServer(new BootstrapServerInfo(
                bootstrapServerIt_->getHost(), bootstrapServerIt_->getPort(), bootstrapServerIt_->getPublicKey()));
        onServerUpdated(newServer);
    } else {
        bootstrapManager_.useNextOperationsServer(server->getType());
    }
}

void KaaChannelManager::onServerUpdated(IServerInfoPtr server) {
    if (!server) {
        KAA_LOG_WARN("Failed to update server: bad input data")
        throw KaaException("empty server pointer");
    }

    ChannelType type = server->getType();
    lastServers_[type] = server;

    for (auto& channel : channels_) {
        if (channel->getType() == type) {
            KAA_LOG_DEBUG(boost::format("Setting a new server for channel \"%1%\" type %2%")
                        % channel->getId() % LoggingUtils::ChannelTypeToString(channel->getType()));
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
        IServerInfoPtr server;

        if (channel->getType() == ChannelType::BOOTSTRAP) {
            server.reset(new BootstrapServerInfo(bootstrapServerIt_->getHost()
                    , bootstrapServerIt_->getPort(), bootstrapServerIt_->getPublicKey()));
        } else {
            auto it = lastServers_.find(channel->getType());
            if (it != lastServers_.end()) {
                server = it->second;
            }
        }

        if (server) {
            KAA_LOG_DEBUG(boost::format("Setting a new server for channel \"%1%\" type %2%")
                        % channel->getId() % LoggingUtils::ChannelTypeToString(channel->getType()));
            channel->setServer(server);
        } else {
            KAA_LOG_WARN(boost::format("Failed to find server for channel \"%1%\" type %2%")
                        % channel->getId() % LoggingUtils::ChannelTypeToString(channel->getType()))
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
        if (channel->getType() == type) {
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

} /* namespace kaa */
