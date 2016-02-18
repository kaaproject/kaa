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
 * @file esp8266_serial.h
 *
 *  Created on: Mar 17, 2015
 *      Author: Andriy Panasenko <apanasenko@cybervisiontech.com>
 */

#ifndef ESP8266_SERIAL_H_
#define ESP8266_SERIAL_H_




#ifdef __cplusplus
extern "C" {
#endif

struct esp8266_serial_t;
typedef struct esp8266_serial_t esp8266_serial_t;

void esp8266_serial_end(esp8266_serial_t *serial);

bool esp8266_serial_available(esp8266_serial_t *serial);

uint8 esp8266_serial_read(esp8266_serial_t *serial);

void esp8266_serial_write(esp8266_serial_t *serial, const char *message);

void esp8266_serial_write_byte(esp8266_serial_t *serial, const uint8 byte);

void esp8266_serial_write_buffer(esp8266_serial_t *serial, const void *buffer, const uint32 size);

void esp8266_serial_write_command(esp8266_serial_t *serial, const char *command);

#ifdef __cplusplus
}      /* extern "C" */
#endif
#endif /* ESP8266_SERIAL_H_ */
