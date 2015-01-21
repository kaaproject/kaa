/*
 * Copyright 2014 CyberVision, Inc.
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

#ifndef SYSTEM_LOGGER_H_
#define SYSTEM_LOGGER_H_

#include "../kaa_error.h"

/*
 * Write log message in system dependent way.
 */
void write_log(FILE * sink, const char * buffer, size_t message_size);

/*
 * Return current system time
 */
time_t get_systime();

/**
 * Put formated LOG prefix in buffer
 * LOG prefix format example:
 *      1970/01/01 2:30:36 [TRACE] [kaa_bootstrap.c:38] (0) -
 */
int kaa_format_sprintf(char * buffer, size_t buffer_size, const char * FORMAT, const char * LOG_LEVEL_NAME, const char * truncated_name, int lineno, kaa_error_t error_code);

/**
 * System depended snprintf implementation.
 */
int kaa_snpintf(char * buffer, size_t buffer_size, const char * format, ...);

/*
 * System depended snprintf with va_list implementation.
 */
int kaa_logger_sprintf(char * buffer, size_t buffer_size, const char * format, va_list args);

#endif /* SYSTEM_LOGGER_H_ */

