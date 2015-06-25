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

#include <chrono>

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

    solarPanels_.reserve(POWER_PLANT_MAX_SOLAR_PANEL_COUNT);
    for (std::size_t i = 0; i < POWER_PLANT_MAX_SOLAR_PANEL_COUNT; ++i) {
        solarPanels_.emplace_back(i, rand() % 100000);
    }

    kaaClient.setConfigurationStorage(std::make_shared<kaa::FileConfigurationStorage>(POWER_PLANT_CONFIGURATION_FILE));
    kaaClient.addConfigurationListener(*this);

    configuration_ = kaaClient.getConfiguration();

#if POWER_PLANT_DEBUG_LOGGING
    std::cout << "Current configuration:" << std::endl;
#endif
    displayConfiguration(configuration_);

    if (!validateConfiguration(configuration_)) {
        displayConfiguration(configuration_);
    }

    kaa::Kaa::start();
}

PowerPlantController::~PowerPlantController()
{
    kaa::Kaa::stop();
}

void PowerPlantController::run()
{
    while (!isShutdown_) {
        KaaRootConfiguration configuration = getConfiguration();

        if (configuration.enableReporting) {
            kaa::KaaUserLogRecord voltageReport;
            voltageReport.timestamp = std::chrono::time_point_cast<std::chrono::milliseconds>(std::chrono::high_resolution_clock::now())
                                                                                                            .time_since_epoch().count();

            std::vector<kaa_log::VoltageSample> samples;
            samples.reserve(configuration.panelCount);

            for (std::int32_t i = 0; i < configuration.panelCount; ++i) {
                samples.push_back(solarPanels_[i].getVoltageSample());
            }

            voltageReport.samples = std::move(samples);
            reportingManager_->addReport(voltageReport);

            std::this_thread::sleep_for(std::chrono::milliseconds(configuration.samplingFrequency));
        } else {
#if POWER_PLANT_DEBUG_LOGGING
            std::cout << "Reporting is disabled. Wait..." << std::endl;
#endif
            std::unique_lock<std::mutex> configurationLock(configurationGuard_);
            onEnabledReporting_.wait(configurationLock, [&] { return configuration_.enableReporting; });
        }
    }
}

kaa::KaaRootConfiguration PowerPlantController::getConfiguration()
{
    std::unique_lock<std::mutex> configurationLock(configurationGuard_);
    return configuration_;
}

void PowerPlantController::onConfigurationUpdated(const kaa::KaaRootConfiguration &configuration)
{
    std::lock_guard<std::mutex> configurationLock(configurationGuard_);
    bool allowReporting = (!configuration_.enableReporting && configuration.enableReporting);

    configuration_ = configuration;

#if POWER_PLANT_DEBUG_LOGGING
    std::cout << "New configuration received:" << std::endl;
#endif
    displayConfiguration(configuration_);

    if (!validateConfiguration(configuration_)) {
        allowReporting = POWER_PLANT_ENABLED_REPORTING;
        displayConfiguration(configuration_);
    }

    reportingManager_->processConfiguration(configuration_);

    if (allowReporting) {
#if POWER_PLANT_DEBUG_LOGGING
        std::cout << "Reporting is enabled. Continue..." << std::endl;
#endif
        onEnabledReporting_.notify_one();
    }
}

void PowerPlantController::displayConfiguration(const kaa::KaaRootConfiguration &configuration)
{
#if POWER_PLANT_DEBUG_LOGGING
    std::cout << "enableReporting: " << std::boolalpha << configuration.enableReporting << std::endl;
    std::cout << "panelCount: " << configuration.panelCount << std::endl;
    std::cout << "samplingFrequency: " << configuration.samplingFrequency << std::endl;
    std::cout << "reportingFrequency: " << configuration.reportingFrequency << std::endl;
#endif
}

bool PowerPlantController::validateConfiguration(kaa::KaaRootConfiguration &configuration)
{
    if ((0 >= configuration.panelCount || configuration.panelCount > POWER_PLANT_MAX_SOLAR_PANEL_COUNT)
            || (configuration.samplingFrequency <= 0) || (configuration.reportingFrequency <= 0))
    {
#if POWER_PLANT_DEBUG_LOGGING
        std::cout << "Unexpected configuration received. Set to defaults" << std::endl;
#endif

        configuration.enableReporting = POWER_PLANT_ENABLED_REPORTING;
        configuration.panelCount = POWER_PLANT_MAX_SOLAR_PANEL_COUNT;
        configuration.samplingFrequency = POWER_PLANT_SAMPLING_FREQUENCY;
        configuration.reportingFrequency = POWER_PLANT_REPORTING_FREQUENCY;

        return false;
    }

    return true;
}

} /* namespace power_plant */
