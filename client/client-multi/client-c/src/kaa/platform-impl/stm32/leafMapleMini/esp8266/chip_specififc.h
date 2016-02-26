/**
 *  Copyright 2014-2016 CyberVision, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

/*
 * chip_specififc.h
 *
 *  Created on: Mar 19, 2015
 *      Author: Andriy Panasenko <apanasenko@cybervisiontech.com>
 */

#ifndef LIBRARIES_ESP8266_CHIP_SPECIFIFC_H_
#define LIBRARIES_ESP8266_CHIP_SPECIFIFC_H_

#ifdef __cplusplus
extern "C" {
#endif

uint32_t get_sys_max(uint32_t s1, uint32_t s2);

time_t get_sys_milis(void);

void debug(const char* format, ...);

void ledOn(void);

void ledOff(void);

void lightOn(bool left, bool right);
void lightOff(bool left, bool right);

void esp8266_reset(void);

#ifdef __cplusplus
}      /* extern "C" */
#endif
#endif /* LIBRARIES_ESP8266_CHIP_SPECIFIFC_H_ */
