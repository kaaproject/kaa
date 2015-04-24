/*
 * Copyright 2014-2015 CyberVision, Inc.
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

#include "ReportingManager.hpp"

#include "ConfigurationConstants.hpp"

namespace power_plant {

ReportingManager::ReportingManager(kaa::IKaaClient& kaaClient)
    : kaaClient_(kaaClient)
    , logUploadStrategy_(std::make_shared<kaa::DefaultLogUploadStrategy>(&kaaClient_.getChannelManager()))
{
    logUploadStrategy_->setCountThreshold(POWER_PLANT_REPORTING_FREQUENCY);
    kaaClient_.setLogUploadStrategy(logUploadStrategy_);
}

void ReportingManager::processConfiguration(const kaa::KaaRootConfiguration& configuration)
{
    std::lock_guard<std::mutex> configurationLock(strategyGuard_);
    logUploadStrategy_->setCountThreshold(configuration.reportingFrequency);
}

void ReportingManager::addReport(const kaa::KaaUserLogRecord& report)
{
    std::lock_guard<std::mutex> configurationLock(strategyGuard_);
    kaaClient_.addLogRecord(report);
}

} /* namespace power_plant */
