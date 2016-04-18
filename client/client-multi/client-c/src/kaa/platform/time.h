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


#ifndef TIME_H_
#define TIME_H_

#ifdef ECONAIS_PLATFORM
#include "../platform-impl/Econais/EC19D/econais_ec19d_time.h"
#else
#ifdef STM32_LEAF_PLATFORM
#include "../platform-impl/stm32/leafMapleMini/leaf_time.h"
#else
#ifdef ESP8266_PLATFORM
#include "../platform-impl/esp8266/esp8266_time.h"
#else
#ifdef CC32XX_PLATFORM
#include "../platform-impl/cc32xx/cc32xx_time.h"
#else
#include "../platform-impl/posix/posix_time.h"
#endif //#ifdef STM32_LEAF_PLATFORM
#endif /* ESP8266_PLATFORM */
#endif //ifdef CC32XX_PLATFORM
#endif //ifdef ECONAIS_PLATFORM

#endif /* TIME_H_ */
