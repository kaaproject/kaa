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

#include "kaa/configuration/manager/ConfigurationManager.hpp"

#include <avro/Generic.hh>
#include <boost/bind.hpp>
#include <vector>

#include "kaa/common/exception/KaaException.hpp"
#include "kaa/common/types/ICommonValue.hpp"
#include "kaa/common/types/ICommonRecord.hpp"
#include "kaa/common/CommonTypesFactory.hpp"
#include "kaa/configuration/manager/FieldProcessor.hpp"
#include "kaa/configuration/manager/Strategies.hpp"

#include "kaa/logging/Log.hpp"
#include "kaa/logging/LoggingUtils.hpp"

namespace kaa {

void ConfigurationManager::subscribeForConfigurationChanges(IConfigurationReceiver &receiver)
{
    boost::signals2::connection c = configurationReceivers_.connect(
            boost::bind(&IConfigurationReceiver::onConfigurationUpdated, &receiver, _1));
    if (!c.connected()) {
        throw KaaException("Failed to add a configuration changes subscriber.");
    }
}

void ConfigurationManager::unsubscribeFromConfigurationChanges(IConfigurationReceiver &receiver)
{
    configurationReceivers_.disconnect(
                boost::bind(&IConfigurationReceiver::onConfigurationUpdated, &receiver, _1));
}

ICommonRecord &ConfigurationManager::getConfiguration()
{
    KAA_MUTEX_LOCKING("configurationGuard_");
    lock_type lock(configurationGuard_);
    KAA_MUTEX_LOCKED("configurationGuard_");

    if (root_.get() == NULL) {
        throw KaaException("Attempting to get empty configuration.");
    }
    return *root_;
}

void ConfigurationManager::onDeltaRecevied(int index, const avro::GenericDatum &datum, bool full_resync)
{
    KAA_MUTEX_LOCKING("configurationGuard_");
    lock_type lock(configurationGuard_);
    KAA_MUTEX_LOCKED("configurationGuard_");

    const avro::GenericRecord & data = datum.value<avro::GenericRecord>();
    avro::GenericFixed uuid_field = data.field("__uuid").value<avro::GenericFixed>();
    uuid_t uuid;
    std::copy(uuid_field.value().begin(), uuid_field.value().end(), uuid.begin());

    KAA_LOG_INFO(boost::format("Going to process delta for object with UUID %1%") % LoggingUtils::ByteArrayToString(uuid.data, uuid.size()));

    if (full_resync) {
        KAA_LOG_DEBUG("Processing configuration full resync. Removing existing UUID-based subscriptions.");
        records_.clear();
    }

    auto it = records_.find(uuid);
    if (it != records_.end()) {
        updateRecord(it->second, datum);
    } else {
        if (full_resync || !root_ || !std::equal    (uuid.begin(), uuid.end(), root_->getUuid().begin())) {
            root_ = CommonTypesFactory::createCommonRecord(uuid, data.schema());
        }

        updateRecord(root_, datum);
    }

    KAA_LOG_DEBUG(boost::format("Full configuration after delta applied is %1%") % root_->toString());
}

void ConfigurationManager::onConfigurationProcessed()
{
    if (root_.get() == NULL) {
        throw KaaException("Configuration processed but no record was created.");
    }
    ICommonRecord &record = *root_;
    configurationReceivers_(record);
}

bool ConfigurationManager::isSubscribed(uuid_t uuid)
{
    auto it = records_.find(uuid);
    return (it != records_.end());
}

void ConfigurationManager::subscribe(uuid_t uuid, boost::shared_ptr<ICommonRecord> record)
{
    KAA_LOG_DEBUG(boost::format("Going to subscribe object with UUID %1%") % LoggingUtils::ByteArrayToString(uuid.data, uuid.size()));
    uuid_t root_uuid = root_->getUuid();
    if (std::equal(root_uuid.begin(), root_uuid.end(), uuid.begin())) {
        return;
    }

    auto res = records_.insert(std::make_pair(uuid, record));
    if (!res.second) {
        throw KaaException("Record is already subscribed");
    }
}

void ConfigurationManager::unsubscribe(uuid_t uuid)
{
    KAA_LOG_DEBUG(boost::format("Going to unsubscribe object with UUID %1%") % LoggingUtils::ByteArrayToString(uuid.data, uuid.size()));
    if (!isSubscribed(uuid)) {
        throw KaaException("Can not unsubscribe. Record was not subscribed");
    }
    records_.erase(uuid);
}

void ConfigurationManager::updateRecord(boost::shared_ptr<ICommonRecord> rec, const avro::GenericDatum &datum)
{
    boost::scoped_ptr<FieldProcessor> fp(new FieldProcessor(rec, ""));
    fp->setStrategy(static_cast<AbstractStrategy *>(new RecordProcessStrategy(
              boost::bind(&ConfigurationManager::isSubscribed, this, _1)
            , boost::bind(&ConfigurationManager::subscribe, this, _1, _2)
            , boost::bind(&ConfigurationManager::unsubscribe, this, _1)
            , true)));
    fp->process(datum);
}

}  // namespace kaa
