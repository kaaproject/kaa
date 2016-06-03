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

#ifndef EXT_SYSTEM_LOGGER_H_
#define EXT_SYSTEM_LOGGER_H_

#include "kaa_error.h"
#include <platform/time.h>

#ifdef __cplusplus
extern "C" {
#endif

/*
 * @brief Write log message in system dependent way.
 * Write @c log message from buffer with length message_size, possibly using FILE sink.
 * @param[in]   sink            FILE of opened stream or NULL if used some other way to print log.
 * @param[in]   buffer          Byte buffer with log message.
 * @param[in]   message_size    Length of message size.
 *
 */
void ext_write_log(FILE * sink, const char * buffer, size_t message_size);

/*
 * @brief Return current system time.
 * Return system current time in seconds since begin of Epoch.
 *
 * @return  time_t      Time in seconds.
 */
kaa_time_t ext_get_systime(void);

/**
 * @brief Put formated LOG prefix in buffer.
 * LOG prefix format example:
 *      1970/01/01 2:30:36 [TRACE] [kaa_bootstrap.c:38] (0) -
 * @param[in,out]   buffer          Buffer to store formated log prefix.
 * @param[in]       buffer_size     Size of buffer.
 * @param[in]       format          Prefix format string.
 * @param[in]       log_level_name  Log level name for format.
 * @param[in]       truncated_name  Truncated file name.
 * @param[in]       lineno          Line number in source file.
 * @param[in]       error_code      Error code of log message.
 *
 * @return int number of bytes written in buffer.
 */
int ext_format_sprintf(char * buffer, size_t buffer_size, const char * format, const char * log_level_name, const char * truncated_name, int lineno, kaa_error_t error_code);

/**
 * @brief System depended snprintf implementation.
 * Snprintf with "..." in arguments
 * @param[in,out]   buffer          Buffer to store formated message.
 * @param[in]       buffer_size     Size of buffer.
 * @param[in]       format          Message format.
 * @param[in]       ...             Other arguments for message.
 */
int ext_snpintf(char * buffer, size_t buffer_size, const char * format, ...);

/*
 * @brief System depended snprintf with va_list implementation.
 * System depended snprintf with va_list implementation.
 * @param[in,out]   buffer          Buffer to store formated message.
 * @param[in]       buffer_size     Size of buffer.
 * @param[in]       format          Message format.
 * @param[in]       args            Other arguments for message as va_list.
 */
int ext_logger_sprintf(char * buffer, size_t buffer_size, const char * format, va_list args);

#ifdef __cplusplus
}      /* extern "C" */
#endif

#endif /* EXT_SYSTEM_LOGGER_H_ */

