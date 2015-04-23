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

#ifndef REPORTINGMANAGER_HPP_
#define REPORTINGMANAGER_HPP_

#include <memory>
#include <mutex>

#include <kaa/IKaaClient.hpp>
#include <kaa/log/DefaultLogUploadStrategy.hpp>

namespace power_plant {

class ReportingManager {
public:
    ReportingManager(kaa::IKaaClient& kaaClient);

    void addReport(const kaa::KaaUserLogRecord& report);

    void processConfiguration(const kaa::KaaRootConfiguration& configuration);

private:
    kaa::IKaaClient& kaaClient_;

    std::shared_ptr<kaa::DefaultLogUploadStrategy> logUploadStrategy_;
    std::mutex strategyGuard_;
};

} /* namespace power_plant */

#endif /* REPORTINGMANAGER_HPP_ */
