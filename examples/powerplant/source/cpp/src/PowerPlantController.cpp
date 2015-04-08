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

#include "PowerPlantController.hpp"

#include <kaa/Kaa.hpp>
#include <kaa/configuration/storage/FileConfigurationStorage.hpp>

#include "ConfigurationConstants.hpp"

namespace power_plant {

PowerPlantController::PowerPlantController()
    : isShutdown_(false)
{
    kaa::Kaa::init();
    auto& kaaClient  = kaa::Kaa::getKaaClient();

    reportingManager_.reset(new ReportingManager(kaaClient));
    configuration_ = kaaClient.getConfiguration();

    validateConfiguration(configuration_);
    displayConfiguration(configuration_);

    solarPanels_.reserve(POWER_PLANT_MAX_SOLAR_PANEL_COUNT);
    for (std::size_t i = 0; i < POWER_PLANT_MAX_SOLAR_PANEL_COUNT; ++i) {
        solarPanels_.emplace_back(i);
    }

    kaaClient.setConfigurationStorage(std::make_shared<kaa::FileConfigurationStorage>(POWER_PLANT_CONFIGURATION_FILE));
    kaaClient.addConfigurationListener(*this);

    kaa::Kaa::start();
}

PowerPlantController::~PowerPlantController()
{
    kaa::Kaa::stop();
}

void PowerPlantController::run()
{
    while (!isShutdown_) {
        std::lock_guard<std::mutex> configurationLock(configurationGuard_);

        if (configuration_.enableReporting && configuration_.panelCount > 0) {
            std::vector<kaa_log::VoltageSample> samples;
            samples.reserve(configuration_.panelCount);

            for (std::int32_t i = 0; i < configuration_.panelCount; ++i) {
                samples.push_back(solarPanels_[i].getVoltageSample());
            }

            kaa::KaaUserLogRecord voltageReport;
            voltageReport.samples = std::move(samples);
            reportingManager_->addReport(voltageReport);
        }

        std::this_thread::sleep_for(std::chrono::milliseconds(configuration_.samplingFrequency));
    }
}

void PowerPlantController::onConfigurationUpdated(const kaa::KaaRootConfiguration &configuration)
{
    std::cout << "New configuration received:" << std::endl;
    std::lock_guard<std::mutex> configurationLock(configurationGuard_);

    configuration_ = configuration;
    displayConfiguration(configuration);

    if (!validateConfiguration(configuration_)) {
        displayConfiguration(configuration_);
    }

    reportingManager_->processConfiguration(configuration_);
}

void PowerPlantController::displayConfiguration(const kaa::KaaRootConfiguration &configuration)
{
    std::cout << "enableReporting: " << std::boolalpha << configuration.enableReporting << std::endl;
    std::cout << "panelCount: " << configuration.panelCount << std::endl;
    std::cout << "samplingFrequency: " << configuration.samplingFrequency << std::endl;
    std::cout << "reportingFrequency: " << configuration.reportingFrequency << std::endl;
}

bool PowerPlantController::validateConfiguration(kaa::KaaRootConfiguration &configuration)
{
    if ((0 < configuration.panelCount && configuration.panelCount > POWER_PLANT_MAX_SOLAR_PANEL_COUNT)
            || (configuration.samplingFrequency <= 0) || (configuration.reportingFrequency <= 0))
    {
        std::cout << "Unexpected configuration received. Set to defaults" << std::endl;

        configuration.panelCount = POWER_PLANT_MAX_SOLAR_PANEL_COUNT;
        configuration.samplingFrequency = POWER_PLANT_SAMPLING_FREQUENCY;
        configuration.reportingFrequency = POWER_PLANT_REPORTING_FREQUENCY;

        return false;
    }

    return true;
}

} /* namespace power_plant */
