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

#include "SolarPanel.hpp"

#include <string>

namespace power_plant {

SolarPanel::SolarPanel(std::int32_t zoneId, std::int32_t panelId)
    : zoneId_(zoneId), panelId_(panelId)
{
#if !POWER_PLANT_RANDOMIZER
    if (zoneId_ >= POWER_PLANT_MAX_SOLAR_PANEL_COUNT) {
        throw std::invalid_argument("Zone id number must be less than " + std::to_string(POWER_PLANT_MAX_SOLAR_PANEL_COUNT));
    }

    panelConnection_ = std::make_shared<mraa::Aio>(zoneId_);
#endif
}

kaa_log::VoltageSample SolarPanel::getVoltageSample()
{
    kaa_log::VoltageSample sample;

    sample.zoneId = zoneId_;
    sample.panelId = panelId_;

#if POWER_PLANT_RANDOMIZER
    sample.voltage = (double)rand() / ((double)rand() + 1);
#else
    sample.voltage = panelConnection_->read() * POWER_PLANT_ADC_FACTOR;
#endif

#if POWER_PLANT_DEBUG_LOGGING
    std::cout << "{";
    std::cout << "zoneId:" << sample.zoneId << ", ";
    std::cout << "panelId:" << sample.panelId << ", ";
    std::cout << "voltage:" << sample.voltage;
    std::cout << "}" << std::endl;
#endif

    return sample;
}

} /* namespace power_plant */
