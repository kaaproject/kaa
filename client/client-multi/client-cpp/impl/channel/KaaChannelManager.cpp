/**
 *  Copyright 2014-2016 CyberVision, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

#include "kaa/channel/KaaChannelManager.hpp"

#include <sstream>

#include "kaa/logging/Log.hpp"
#include "kaa/logging/LoggingUtils.hpp"
#include "kaa/bootstrap/IBootstrapManager.hpp"
#include "kaa/common/exception/KaaException.hpp"

namespace kaa {

KaaChannelManager::KaaChannelManager(IBootstrapManager& manager, const BootstrapServers& servers, IKaaClientContext &context)
    : bootstrapManager_(manager), retryTimer_("KaaChannelManager retryTimer"), isShutdown_(false), isPaused_(false), bsTransportId_(0,0), context_(context)
{
    for (const auto& connectionInfo : servers) {
        auto& list = bootstrapServers_[connectionInfo->getTransportId()];
        list.push_back(connectionInfo);
        lastBSServers_[connectionInfo->getTransportId()] = list.begin();
    }
}

void KaaChannelManager::setFailoverStrategy(IFailoverStrategyPtr strategy) {
    if (isShutdown_) {
        KAA_LOG_WARN("Can't set failover strategy. Channel manager is down");
        return;
    }

    if (!strategy) {
        KAA_LOG_WARN("Failover strategy is null");
        return;
    }

    failoverStrategy_ = strategy;

    KAA_MUTEX_LOCKING("channelGuard_");
    KAA_R_MUTEX_UNIQUE_DECLARE(channelLock, channelGuard_);
    KAA_MUTEX_LOCKED("channelGuard_");

    bootstrapManager_.setFailoverStrategy(failoverStrategy_);
    for (auto& channel : channels_) {
        channel->setFailoverStrategy(failoverStrategy_);
    }
}

void KaaChannelManager::onServerFailed(ITransportConnectionInfoPtr connectionInfo) {
    if (isShutdown_) {
        KAA_LOG_WARN("Can't update server. Channel manager is down");
        return;
    }
    if (!connectionInfo) {
        KAA_LOG_WARN("Failed to process server failure: bad input data")
        throw KaaException("empty connection info pointer");
    }

    if (connectionInfo->isFailedState()) {
        KAA_LOG_DEBUG("Connection already failed. Ignoring connection failover!");
        return;
    } else {
        connectionInfo->setFailedState();
    }

    if (connectionInfo->getServerType() == ServerType::BOOTSTRAP) {
        ITransportConnectionInfoPtr nextConnectionInfo = getNextBootstrapServer(connectionInfo->getTransportId(), false);
        if (nextConnectionInfo) {
            onTransportConnectionInfoUpdated(nextConnectionInfo);
        } else {
            FailoverStrategyDecision decision = failoverStrategy_->onFailover(Failover::BOOTSTRAP_SERVERS_NA);
            switch (decision.getAction()) {
                 case FailoverStrategyAction::NOOP:
                     KAA_LOG_WARN("No operation is performed according to failover strategy decision.");
                     break;
                 case FailoverStrategyAction::RETRY:
                 {
                     std::size_t period = decision.getRetryPeriod();
                     KAA_LOG_WARN(boost::format("Attempt to reconnect to first bootstrap server will be made in %1% secs "
                             "according to failover strategy decision.") % period);
                     bsTransportId_ = connectionInfo->getTransportId();
                     retryTimer_.stop();
                     retryTimer_.start(period, [&]
                         {
                             onTransportConnectionInfoUpdated(getNextBootstrapServer(bsTransportId_, true));
                         });
                     break;
                 }
                 case FailoverStrategyAction::STOP_APP:
                     KAA_LOG_WARN("Stopping application according to failover strategy decision!");
                     exit(EXIT_FAILURE);
                     break;
                 default:
                    break;
             }
        }
    } else {
        bootstrapManager_.useNextOperationsServer(connectionInfo->getTransportId());
    }
}

void KaaChannelManager::onTransportConnectionInfoUpdated(ITransportConnectionInfoPtr connectionInfo) {
    if (isShutdown_) {
        KAA_LOG_WARN("Can't update server. Channel manager is down");
        return;
    }
    if (!connectionInfo) {
        KAA_LOG_WARN("Failed to update connection info: bad input data")
        throw KaaException("empty connection info pointer");
    }

    connectionInfo->resetFailedState();

    TransportProtocolId protocolId = connectionInfo->getTransportId();
    if (connectionInfo->getServerType() == ServerType::OPERATIONS) {
        KAA_MUTEX_LOCKING("lastOpsServersGuard_");
        KAA_MUTEX_UNIQUE_DECLARE(lastOpsServers_Lock, lastOpsServersGuard_);
        KAA_MUTEX_LOCKED("lastOpsServersGuard_");

        lastOpsServers_[protocolId] = connectionInfo;
    }

    KAA_MUTEX_LOCKING("channelGuard_");
    KAA_R_MUTEX_UNIQUE_DECLARE(channelLock, channelGuard_);
    KAA_MUTEX_LOCKED("channelGuard_");

    for (auto& channel : channels_) {
        if (channel->getServerType() == connectionInfo->getServerType() && channel->getTransportProtocolId() == protocolId) {
            KAA_LOG_DEBUG(boost::format("Setting a new connection data for channel \"%1%\" %2%")
                        % channel->getId() % LoggingUtils::TransportProtocolIdToString(protocolId));
            channel->setServer(connectionInfo);
        }
    }
}

bool KaaChannelManager::addChannelToList(IDataChannelPtr channel)
{
    KAA_MUTEX_LOCKING("channelGuard_");
    KAA_R_MUTEX_UNIQUE_DECLARE(channelLock, channelGuard_);
    KAA_MUTEX_LOCKED("channelGuard_");

    auto res = channels_.insert(channel);

    if (res.second) {
        channel->setFailoverStrategy(failoverStrategy_);
        channel->setConnectivityChecker(connectivityChecker_);

        ITransportConnectionInfoPtr connectionInfo;

        TransportProtocolId protocolId = channel->getTransportProtocolId();
        if (channel->getServerType() == ServerType::BOOTSTRAP) {
            connectionInfo = getCurrentBootstrapServer(protocolId);
        } else {
            KAA_MUTEX_LOCKING("lastOpsServersGuard_");
            KAA_MUTEX_UNIQUE_DECLARE(lastOpsServers_Lock, lastOpsServersGuard_);
            KAA_MUTEX_LOCKED("lastOpsServersGuard_");

            auto it = lastOpsServers_.find(channel->getTransportProtocolId());
            if (it != lastOpsServers_.end()) {
                connectionInfo = it->second;
            }
        }

        if (connectionInfo) {
            KAA_LOG_DEBUG(boost::format("Setting a new server for channel \"%1%\" %2%")
                        % channel->getId() % LoggingUtils::TransportProtocolIdToString(protocolId));
            channel->setServer(connectionInfo);
        } else {
            if (channel->getServerType() == ServerType::BOOTSTRAP) {
                KAA_LOG_WARN(boost::format("Failed to find bootstrap server for channel \"%1%\" %2%")
                            % channel->getId() % LoggingUtils::TransportProtocolIdToString(protocolId));
            } else {
                KAA_LOG_INFO(boost::format("Failed to find operations server for channel \"%1%\" %2%")
                            % channel->getId() % LoggingUtils::TransportProtocolIdToString(protocolId));
            }
        }
    }

    return res.second;
}

void KaaChannelManager::setChannel(TransportType type, IDataChannelPtr channel)
{
    if (isShutdown_) {
        KAA_LOG_WARN("Can't set channel. Channel manager is down");
        return;
    }
    if (!channel) {
        KAA_LOG_WARN("Failed to set channel: bad input data")
        throw KaaException("empty channel pointer");
    }
    const auto &types = channel->getSupportedTransportTypes();
    auto res = types.find(type);
    if (res == types.end() || !useChannelForType(*res, channel)) {
        KAA_LOG_WARN(boost::format("Can't apply Channel (id='%1%') for transport %2%")
                        % channel->getId()
                        % LoggingUtils::TransportTypeToString(type));
        throw KaaException("invalid channel or transport type");
    }
    if (isPaused_) {
        channel->pause();
    }
    addChannelToList(channel);
}

void KaaChannelManager::addChannel(IDataChannelPtr channel)
{
    if (isShutdown_) {
        KAA_LOG_WARN("Can't set channel. Channel manager is down");
        return;
    }
    if (!channel) {
        KAA_LOG_WARN("Failed to add channel: bad input data")
        throw KaaException("empty channel pointer");
    }
    if (isPaused_) {
        channel->pause();
    }

    if (addChannelToList(channel)) {
        useNewChannel(channel);
    }
}

bool KaaChannelManager::useChannelForType(const std::pair<TransportType, ChannelDirection>& type, IDataChannelPtr channel)
{
    if (type.second == ChannelDirection::UP || type.second == ChannelDirection::BIDIRECTIONAL) {
        KAA_LOG_INFO(boost::format("Channel (id='%1%') will be used for %2% transport type")
                            % channel->getId() % LoggingUtils::TransportTypeToString(type.first));

        KAA_MUTEX_LOCKING("mappedChannelGuard_");
        KAA_R_MUTEX_UNIQUE_DECLARE(mappedChannelLock, mappedChannelGuard_);
        KAA_MUTEX_LOCKED("mappedChannelGuard_");

        mappedChannels_[type.first] = channel;
        return true;
    }
    return false;
}

void KaaChannelManager::useNewChannel(IDataChannelPtr channel)
{
    const auto& types = channel->getSupportedTransportTypes();
    for (const auto& type : types) {
        useChannelForType(type, channel);
    }
}

void KaaChannelManager::replaceChannel(IDataChannelPtr channel)
{
    KAA_MUTEX_LOCKING("mappedChannelGuard_");
    KAA_R_MUTEX_UNIQUE_DECLARE(mappedChannelLock, mappedChannelGuard_);
    KAA_MUTEX_LOCKED("mappedChannelGuard_");

    for (const auto& channelInfo : mappedChannels_) {
        if (channelInfo.second == channel) {
            useNewChannelForType(channelInfo.first);
        }
    }

    KAA_UNLOCK(mappedChannelLock);

    channel->shutdown();
}

void KaaChannelManager::removeChannel(IDataChannelPtr channel)
{

    if (!channel) {
        KAA_LOG_WARN("Failed to remove channel: bad input data")
        throw KaaException("empty channel pointer");
    }

    KAA_MUTEX_LOCKING("channelGuard_");
    KAA_R_MUTEX_UNIQUE_DECLARE(channelLock, channelGuard_);
    KAA_MUTEX_LOCKED("channelGuard_");

    if (channels_.erase(channel)) {
        replaceChannel(channel);
    }
}

void KaaChannelManager::removeChannel(const std::string& id)
{
    KAA_MUTEX_LOCKING("channelGuard_");
    KAA_R_MUTEX_UNIQUE_DECLARE(channelLock, channelGuard_);
    KAA_MUTEX_LOCKED("channelGuard_");

    for (auto it = channels_.begin(); it != channels_.end(); ++it) {
        if ((*it)->getId() == id) {
            IDataChannelPtr channel = *it;
            channels_.erase(it);
            replaceChannel(channel);
            return;
        }
    }
}

void KaaChannelManager::useNewChannelForType(TransportType type) {
    for (const auto& channel : channels_) {
        const auto& types = channel->getSupportedTransportTypes();
        auto it = types.find(type);

        if (it != types.end() && useChannelForType(*it, channel)) {
            return;
        }
    }

    mappedChannels_[type] = IDataChannelPtr();
}

std::list<IDataChannelPtr> KaaChannelManager::getChannels()
{
    KAA_MUTEX_LOCKING("channelGuard_");
    KAA_R_MUTEX_UNIQUE_DECLARE(channelLock, channelGuard_);
    KAA_MUTEX_LOCKED("channelGuard_");

    std::list<IDataChannelPtr> channels(channels_.begin(), channels_.end());
    return channels;
}

IDataChannelPtr KaaChannelManager::getChannelByTransportType(TransportType type)
{
    KAA_MUTEX_LOCKING("mappedChannelGuard_");
    KAA_R_MUTEX_UNIQUE_DECLARE(mappedChannelLock, mappedChannelGuard_);
    KAA_MUTEX_LOCKED("mappedChannelGuard_");

    IDataChannelPtr channel = nullptr;
    auto it = mappedChannels_.find(type);

    if (it != mappedChannels_.end()) {
        channel = it->second;
    }

    return channel;
}

IDataChannelPtr KaaChannelManager::getChannel(const std::string& channelId)
{
    KAA_MUTEX_LOCKING("channelGuard_");
    KAA_R_MUTEX_UNIQUE_DECLARE(channelLock, channelGuard_);
    KAA_MUTEX_LOCKED("channelGuard_");

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
    KAA_MUTEX_LOCKING("channelGuard_");
    KAA_R_MUTEX_UNIQUE_DECLARE(channelLock, channelGuard_);
    KAA_MUTEX_LOCKED("channelGuard_");

    KAA_MUTEX_LOCKING("mappedChannelGuard_");
    KAA_R_MUTEX_UNIQUE_DECLARE(mappedChannelLock, mappedChannelGuard_);
    KAA_MUTEX_LOCKED("mappedChannelGuard_");

    channels_.clear();
    mappedChannels_.clear();
}

ITransportConnectionInfoPtr KaaChannelManager::getCurrentBootstrapServer(const TransportProtocolId& protocolId)
{
    ITransportConnectionInfoPtr connectionInfo;
    auto it = lastBSServers_.find(protocolId);
    if (it == lastBSServers_.end()) {
        auto serverTypeIt = bootstrapServers_.find(protocolId);
        if (serverTypeIt != bootstrapServers_.end()) {
            connectionInfo = (*serverTypeIt).second.front();
            lastBSServers_[protocolId] = (*serverTypeIt).second.begin();
        }
    } else {
        connectionInfo = *(it->second);
    }

    return connectionInfo;
}

ITransportConnectionInfoPtr KaaChannelManager::getNextBootstrapServer(const TransportProtocolId& protocolId, bool forceFirstElement)
{
    ITransportConnectionInfoPtr nextConnectionInfo;
    auto lastServerIt = lastBSServers_.find(protocolId);
    auto serverIt = bootstrapServers_.find(protocolId);

    if (lastServerIt != lastBSServers_.end() && serverIt != bootstrapServers_.end()) {
        BootstrapServers::iterator nextBsIterator = (lastServerIt->second)+1;
        if (nextBsIterator != serverIt->second.end()) {
            nextConnectionInfo = *(nextBsIterator);
            lastBSServers_[protocolId] = nextBsIterator;
        } else if (forceFirstElement) {
            nextConnectionInfo = (*serverIt).second.front();
            lastBSServers_[protocolId] = (*serverIt).second.begin();
        }
    }
    return nextConnectionInfo;
}

void KaaChannelManager::setConnectivityChecker(ConnectivityCheckerPtr checker) {
    if (isShutdown_) {
        KAA_LOG_WARN("Can't set connectivity checker. Channel manager is down");
        return;
    }

    if (!checker) {
        KAA_LOG_WARN("Connectivity checker is null");
        return;
    }

    connectivityChecker_ = checker;

    KAA_MUTEX_LOCKING("channelGuard_");
    KAA_R_MUTEX_UNIQUE_DECLARE(channelLock, channelGuard_);
    KAA_MUTEX_LOCKED("channelGuard_");

    for (auto& channel : channels_) {
        channel->setConnectivityChecker(connectivityChecker_);
    }
}

void KaaChannelManager::doShutdown()
{
    if (!isShutdown_) {
        isShutdown_ = true;

        KAA_MUTEX_LOCKING("mappedChannelGuard_");
        KAA_R_MUTEX_UNIQUE_DECLARE(mappedChannelLock, mappedChannelGuard_);
        KAA_MUTEX_LOCKED("mappedChannelGuard_");

        for (auto& channel : mappedChannels_) {
            channel.second->shutdown();
        }
    }
}

void KaaChannelManager::shutdown()
{
    doShutdown();
}

void KaaChannelManager::pause()
{
    if (isShutdown_) {
        KAA_LOG_WARN("Can't pause. Channel manager is down");
        return;
    }
    if (!isPaused_) {
        isPaused_ = true;

        KAA_MUTEX_LOCKING("mappedChannelGuard_");
        KAA_R_MUTEX_UNIQUE_DECLARE(mappedChannelLock, mappedChannelGuard_);
        KAA_MUTEX_LOCKED("mappedChannelGuard_");

        for (auto& channel : mappedChannels_) {
            channel.second->pause();
        }
    }
}

void KaaChannelManager::resume()
{
    if (isShutdown_) {
        KAA_LOG_WARN("Can't resume. Channel manager is down");
        return;
    }
    if (isPaused_) {
        isPaused_ = false;

        KAA_MUTEX_LOCKING("mappedChannelGuard_");
        KAA_R_MUTEX_UNIQUE_DECLARE(mappedChannelLock, mappedChannelGuard_);
        KAA_MUTEX_LOCKED("mappedChannelGuard_");

        for (auto& channel : mappedChannels_) {
            channel.second->resume();
        }
    }
}

} /* namespace kaa */
