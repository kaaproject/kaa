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

#include <stdexcept>

#include "PowerPlantController.hpp"

int main()
{
    std::cout << "Going to start power plant demo application..." << std::endl;

    try {
        power_plant::PowerPlantController controller;
        controller.run();

        std::cout << "Power plant demo application stopped" << std::endl;
    } catch (std::exception& e) {
        std::cerr << "Power plant demo application stopped in unexpected way: " << e.what() << std::endl;
    }

    return 0;
}
