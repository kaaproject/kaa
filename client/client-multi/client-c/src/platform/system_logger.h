/*
 * system_logger.h
 *
 *  Created on: Dec 31, 2014
 *      Author: avp
 */

#ifndef SRC_KAA_UTILITIES_SYSTEM_LOGGER_H_
#define SRC_KAA_UTILITIES_SYSTEM_LOGGER_H_

#include "../kaa_error.h"

void write_log(FILE * sink, const char * buffer, size_t message_size);
time_t get_systime();

int kaa_format_sprintf(char * buffer, size_t buffer_size, const char * FORMAT, const char * LOG_LEVEL_NAME, const char * truncated_name, int lineno, kaa_error_t error_code);

int kaa_snpintf(char * buffer, size_t buffer_size, const char * format, ...);

int kaa_logger_sprintf(char * buffer, size_t buffer_size, const char * format, va_list args);

//void system_log(const char * format, ...);

#endif /* SRC_KAA_UTILITIES_SYSTEM_LOGGER_H_ */
