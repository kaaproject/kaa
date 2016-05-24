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

#include <stdio.h>
#include <stdarg.h>

#include <esp_system.h>

#include <platform/ext_system_logger.h>

int snprintf(char *str, size_t count, const char *fmt, ...);
int vsnprintf(char *str, size_t count, const char *fmt, va_list arg);

kaa_time_t ext_get_systime(void)
{
    return system_get_rtc_time()*((system_rtc_clock_cali_proc()*1000)>>12)/1000;
}

void ext_write_log(FILE *sink, const char *buffer, size_t message_size)
{
    (void)sink;
    (void)message_size;
    printf("%s", buffer);
}

int ext_logger_sprintf(char *buffer,size_t buffer_size, const char *format, va_list args)
{
    return vsnprintf(buffer, buffer_size, format, args);
}

int ext_snpintf(char *buffer, size_t buffer_size, const char *format, ...)
{
    va_list args;
    va_start(args, format);
    int res_len = vsnprintf(buffer, buffer_size, format, args);
    va_end(args);
    return res_len;

}

int ext_format_sprintf(char * buffer, size_t buffer_size, const char * format,
        const char * log_level_name, const char * truncated_name, int lineno,
        kaa_error_t error_code)
{

    return snprintf(buffer, buffer_size, format, 0,
            0, 0, 0, 0, 0,
            log_level_name, truncated_name, lineno, error_code);
}
