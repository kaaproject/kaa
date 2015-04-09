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

#ifndef POWERPLANTCONTROLLER_HPP_
#define POWERPLANTCONTROLLER_HPP_

#include <vector>
#include <memory>
#include <mutex>
#include <condition_variable>

#include <kaa/IKaaClient.hpp>
#include <kaa/configuration/gen/ConfigurationDefinitions.hpp>
#include <kaa/configuration/manager/IConfigurationReceiver.hpp>

#include "SolarPanel.hpp"
#include "ReportingManager.hpp"

namespace power_plant {

class PowerPlantController : public kaa::IConfigurationReceiver {
public:
    PowerPlantController();
    ~PowerPlantController();

    void run();

private:
    kaa::KaaRootConfiguration getConfiguration();

    virtual void onConfigurationUpdated(const kaa::KaaRootConfiguration &configuration);
    void displayConfiguration(const kaa::KaaRootConfiguration &configuration);
    bool validateConfiguration(kaa::KaaRootConfiguration &configuration);

private:
    bool isShutdown_;

    std::vector<SolarPanel> solarPanels_;

    std::unique_ptr<ReportingManager> reportingManager_;

    kaa::KaaRootConfiguration configuration_;
    std::mutex configurationGuard_;
    std::condition_variable onEnabledReporting_;
};

} /* namespace power_plant */

#endif /* POWERPLANTCONTROLLER_HPP_ */
