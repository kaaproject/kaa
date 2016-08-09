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

#include "kaa/bootstrap/BootstrapManager.hpp"

#include <ctime>
#include <cstdlib>
#include <cstdint>
#include <algorithm>
#include <random>

#include "kaa/IKaaClient.hpp"
#include "kaa/KaaDefaults.hpp"
#include "kaa/logging/Log.hpp"
#include "kaa/logging/LoggingUtils.hpp"
#include "kaa/common/exception/KaaException.hpp"

namespace kaa {

void BootstrapManager::setFailoverStrategy(IFailoverStrategyPtr strategy)
{
    failoverStrategy_ = strategy;
}

void BootstrapManager::receiveOperationsServerList()
{
    if (bootstrapTransport_ != nullptr) {
        bootstrapTransport_->sync();
    }
}

BootstrapManager::OperationsServers BootstrapManager::getOPSByAccessPointId(std::int32_t id)
{
    OperationsServers servers;

    for (const auto& serversByProtocolId : operationServers_) {
        for (const auto& server : serversByProtocolId.second) {
            if (server->getAccessPointId() == id) {
                servers.push_back(server);
            }
        }
    }

    return servers;
}

void BootstrapManager::onOperationsServerFailed(const TransportProtocolId& protocolId, KaaFailoverReason reason)
{
    KAA_R_MUTEX_UNIQUE_DECLARE(lock, guard_);

    auto lastServerIt = lastOperationsServers_.find(protocolId);
    auto serverIt = operationServers_.find(protocolId);

    if (lastServerIt == lastOperationsServers_.end() || serverIt == operationServers_.end()) {
        throw KaaException("There are no available services at the time");
    }

    FailoverStrategyDecision decision = failoverStrategy_->onFailover(reason);
    switch (decision.getAction()) {
        case FailoverStrategyAction::NOOP:
            KAA_LOG_WARN(boost::format("No decision for '%s' failover") % LoggingUtils::toString(reason));
            break;
        case FailoverStrategyAction::RETRY_CURRENT_SERVER:
        {
            std::size_t period = decision.getRetryPeriod();

            KAA_LOG_WARN(boost::format("Attempt to reconnect to current Operations service will be made in %1% seconds") % period);

            auto currentOperationsServer = *(lastServerIt->second);

            retryTimer_.stop();
            retryTimer_.start(period,
                             [this, currentOperationsServer]
                                  {
                                       channelManager_->onTransportConnectionInfoUpdated(currentOperationsServer);
                                  });
            break;
        }
        case FailoverStrategyAction::USE_NEXT_OPERATIONS_SERVER:
        {
            OperationsServers::iterator nextOperationIterator = (lastServerIt->second) + 1;
            if (nextOperationIterator != serverIt->second.end()) {
                KAA_LOG_INFO(boost::format("New Operations service [%1%] will be used for %2%")
                                           % (*nextOperationIterator)->getAccessPointId()
                                           % LoggingUtils::toString(protocolId));

                lastOperationsServers_[protocolId] = nextOperationIterator;

                channelManager_->onTransportConnectionInfoUpdated(*nextOperationIterator);
            } else {
                KAA_LOG_WARN(boost::format("No Operations services are accessible for %1%.")
                                                         % LoggingUtils::toString(protocolId));

                onCurrentBootstrapServerFailed(KaaFailoverReason::ALL_OPERATIONS_SERVERS_NA);
            }
            break;
        }
        case FailoverStrategyAction::STOP_CLIENT:
            KAA_LOG_WARN("Stopping client according to failover strategy decision!");
            client_->stop();
            break;
        default:
            KAA_LOG_WARN(boost::format("Unexpected '%s' decision for Operations '%s' failover")
                                                                 % LoggingUtils::toString(decision)
                                                                 % LoggingUtils::toString(reason));
            break;
    }
}

void BootstrapManager::useNextOperationsServerByAccessPointId(std::int32_t id)
{
    KAA_R_MUTEX_UNIQUE_DECLARE(lock, guard_);

    KAA_LOG_DEBUG(boost::format("Going to use new operations service: access_point=0x%X") % id);

    auto servers = getOPSByAccessPointId(id);
    if (servers.size() > 0) {
        notifyChannelManangerAboutServer(servers);
    } else {
        serverToApply.reset(new std::int32_t(id));
        bootstrapTransport_->sync();
    }
}

void BootstrapManager::setTransport(IBootstrapTransport* transport)
{
    KAA_R_MUTEX_UNIQUE_DECLARE(lock, guard_);
    bootstrapTransport_ = dynamic_cast<BootstrapTransport*>(transport);
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

void BootstrapManager::onCurrentBootstrapServerFailed(KaaFailoverReason reason)
{
    KAA_LOG_TRACE(boost::format("Processing '%s' failover...") % LoggingUtils::toString(reason));

    auto channel = channelManager_->getChannelByTransportType(TransportType::BOOTSTRAP);
    if (channel) {
        channelManager_->onServerFailed(channel->getServer(), reason);
    }
}

void BootstrapManager::onServerListUpdated(const std::vector<ProtocolMetaData>& operationsServers)
{
    if (operationsServers.empty()) {
        KAA_LOG_WARN("Received empty operations service list");
        onCurrentBootstrapServerFailed(KaaFailoverReason::NO_OPERATIONS_SERVERS_RECEIVED);
        return;
    }

    KAA_R_MUTEX_UNIQUE_DECLARE(lock, guard_);

    KAA_LOG_INFO(boost::format("Received %1% new operations services") % operationsServers.size());

    lastOperationsServers_.clear();
    operationServers_.clear();

    for (const auto& serverMetaData : operationsServers) {
        ITransportConnectionInfoPtr connectionInfo(
                    new GenericTransportInfo(ServerType::OPERATIONS, serverMetaData));

        auto& servers = operationServers_[serverMetaData.protocolVersionInfo];
        servers.push_back(connectionInfo);
    }

    for (auto& transportSpecificServers : operationServers_) {
        std::shuffle (transportSpecificServers.second.begin()
                      , transportSpecificServers.second.end()
                      , std::default_random_engine(std::chrono::high_resolution_clock::now().time_since_epoch().count()));

        lastOperationsServers_[transportSpecificServers.first] =
                transportSpecificServers.second.begin();
    }

    if (serverToApply) {
        auto servers = getOPSByAccessPointId(*serverToApply);
        if (!servers.empty()) {
            KAA_LOG_DEBUG(boost::format("Found %1% servers by access point id %2%")
                          % servers.size() % *serverToApply);
            serverToApply.reset();
            notifyChannelManangerAboutServer(servers);
        }
    } else {
        for (const auto& transportSpecificServers : operationServers_) {
            channelManager_->onTransportConnectionInfoUpdated(transportSpecificServers.second.front());
        }
    }
}

void BootstrapManager::notifyChannelManangerAboutServer(const OperationsServers& servers)
{
    if (channelManager_ == nullptr) {
        KAA_LOG_ERROR("Channel manager was not specified");
        return;
    }

    for (const auto& connectionInfo : servers) {
        channelManager_->onTransportConnectionInfoUpdated(connectionInfo);
    }
}

}
