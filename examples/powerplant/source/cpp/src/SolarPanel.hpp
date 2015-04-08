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

#ifndef SOLARPANEL_HPP_
#define SOLARPANEL_HPP_

#include <memory>

#include <mraa.hpp>

#include <kaa/log/gen/LogDefinitions.hpp>

namespace power_plant {

class SolarPanel {
public:
    SolarPanel(std::size_t panelId);

    kaa_log::VoltageSample getVoltageSample();

private:
    static const double ADC_FACTOR;

private:
    std::int32_t                  panelId_;
    std::shared_ptr<mraa::Aio>    panelConnection_;
};

} /* namespace power_plant */

#endif /* SOLARPANEL_HPP_ */
