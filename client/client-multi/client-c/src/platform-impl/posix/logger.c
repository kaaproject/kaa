/*
 * logger.c
 *
 *  Created on: Jan 14, 2015
 *      Author: avp
 */

#include <stdio.h>
#include <time.h>
#include <stdarg.h>
#include "../../platform/system_logger.h"

void write_log(FILE * sink, const char * buffer, size_t message_size) {
	if (!buffer || !sink) {
		return;
	}
	fwrite(buffer, sizeof(char), message_size, sink);
	fflush(sink);
}

time_t get_systime() {
	return time(NULL);
}

int kaa_format_sprintf(char * buffer, size_t buffer_size, const char * FORMAT, const char * LOG_LEVEL_NAME, const char * truncated_name, int lineno, kaa_error_t error_code) {
	time_t t = get_systime();
	struct tm* tp = gmtime(&t);

	return snprintf(buffer, buffer_size, FORMAT
            , 1900 + tp->tm_year, tp->tm_mon + 1, tp->tm_mday
            , tp->tm_hour, tp->tm_min, tp->tm_sec
            , LOG_LEVEL_NAME, truncated_name, lineno, error_code);
}

int kaa_snpintf(char * buffer, size_t buffer_size, const char * format, ...) {
	va_list args;
	va_start(args, format);
	int res_len = vsnprintf(buffer,buffer_size,format, args);
	va_end(args);
	return res_len;
}

int kaa_logger_sprintf(char * buffer, size_t buffer_size, const char * format, va_list args) {
	return vsnprintf(buffer
	               , buffer_size
	               , format
	               , args);
}
