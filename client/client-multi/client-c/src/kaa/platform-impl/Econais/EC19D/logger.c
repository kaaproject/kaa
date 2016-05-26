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

#include <sndc_sdk_api.h>

#include "econais_ec19d_time.h"
#include "platform/ext_system_logger.h"



//void cext_write_log(FILE * sink, const char * buffer, size_t message_size)
//{
//    if (!buffer) {
//        return;
//    }
//    sndc_printf(buffer);
//}

//time_t ext_get_systime()
//{
//    return (time_t) sndc_sys_getTimestamp_msec();
//}

int ext_format_sprintf(char * buffer, size_t buffer_size, const char * format,
        const char * log_level_name, const char * truncated_name, int lineno,
        kaa_error_t error_code)
{
    time_t t = ext_get_systime();
    struct tm* tp = gmtime(&t);

    return snprintf(buffer, buffer_size, format, 1900 + tp->tm_year,
            tp->tm_mon + 1, tp->tm_mday, tp->tm_hour, tp->tm_min, tp->tm_sec,
            log_level_name, truncated_name, lineno, error_code);
}

int ext_snpintf(char * buffer, size_t buffer_size, const char * format, ...)
{
    va_list args;
    va_start(args, format);
    int res_len = ext_logger_sprintf(buffer, buffer_size, format, args);
    va_end(args);
    return res_len;
}

typedef enum {
    DEFAULT, PERCENT_FIRST_FOUND, PERCENT_FOUND
} sprintfState;
typedef char* char_p;
typedef void* void_p;
typedef unsigned int uint;
typedef long int longInt;
typedef long long int longLongInt;
typedef long unsigned int uLongInt;
typedef long long unsigned int uLongLongInt;

#define _INT_SPRINTF(type) \
        type carg = (type)va_arg(args, type); \
        int cp = snprintf(buffer_p, buffer_size_l, p_format, carg); \
        res_len += cp; \
        buffer_p += cp; \
        if (buffer_size_l <= cp) { \
            buffer_size_l = 0; \
            no_more_space_in_buffer = true; \
        } else { \
            buffer_size_l -= cp; \
        } \
        last_format = p + 1; \
        sndc_mem_free(p_format); \
        percent_pos = NULL; \
        state = DEFAULT;

#define SPRNTF(type) \
        size_t p_format_size = (p - last_format)+2; \
        char *p_format = sndc_mem_malloc(p_format_size); \
        if (p_format == NULL) { \
            no_more_space_in_buffer = true; \
            break; \
        } \
        strncpy(p_format,last_format,p_format_size-1);\
        _INT_SPRINTF(type)

#define SPRINTF_SUBST(type,symbols) \
        size_t formated_size = percent_pos - last_format; \
        size_t symbols_size = strlen(symbols); \
        size_t p_format_size = formated_size + symbols_size +2;\
        char *p_format = sndc_mem_malloc(p_format_size); \
        if (p_format == NULL) { \
            no_more_space_in_buffer = true; \
            break; \
        } \
        strncpy(p_format,last_format,formated_size);\
        strncpy(p_format + formated_size, symbols, symbols_size); \
        _INT_SPRINTF(type)

int ext_logger_sprintf(char * buffer, size_t buffer_size, const char * format, va_list args)
{
    int format_length = strnlen(format, buffer_size);
    int i = 0, res_len = 0;
    const char *zu_symbols = "%lu";
    const char *p_symbols = "0x%08X";
    char *p = (char *) format;
    char ch = *p;
    char *last_format = p;
    char *buffer_p = buffer;
    char *percent_pos = NULL;
    size_t buffer_size_l = buffer_size;
    bool_t no_more_space_in_buffer = false;
    sprintfState state = DEFAULT;
    bool_t longUsed = false;
    bool_t doubleLongUsed = false;
    bool_t zuFound = false;
    for (i = 0; i < format_length; i++) {
        ch = *p;
        switch (state) {
        case PERCENT_FIRST_FOUND:
            if (ch == '%') {
                //%% in format
                state = DEFAULT;
                break;
            }
            state = PERCENT_FOUND;
        case PERCENT_FOUND:
            if (ch == 'd') {
                //found correct integer
                if (doubleLongUsed) {
                    SPRNTF(longLongInt);
                } else if (longUsed) {
                    SPRNTF(longInt);
                } else {
                    SPRNTF(int);
                }
            } else if (ch == 'u' || ch == 'x' || ch == 'X') {
                //found unsigned integer
                if (doubleLongUsed) {
                    SPRNTF(uLongLongInt);
                } else if (longUsed) {
                    SPRNTF(uLongInt);
                } else if (zuFound) {
                    SPRINTF_SUBST(uLongInt, zu_symbols);
                } else {
                    SPRNTF(uint);
                }
            } else if (ch == 's') {
                //found char *
                SPRNTF(char_p);
            } else if (ch == 'z') {
                zuFound = true;
            } else if (ch == 'p') {
                SPRINTF_SUBST(void_p, p_symbols);
            } else if (ch == 'l') {
                if (longUsed) {
                    doubleLongUsed = true;
                } else {
                    longUsed = true;
                }
            }
            break;
        default:
            if (ch == '%') {
                state = PERCENT_FIRST_FOUND;
                percent_pos = p;
            }
            break;
        }
        p++;
        if (no_more_space_in_buffer)
            break;
    }
    if (!no_more_space_in_buffer) {
        if (p >= last_format) {
            //Need to copy least bytes from format
            size_t tocopy = buffer_size_l;
            if (tocopy > (p - last_format)) {
                tocopy = p - last_format;
            }
            strncpy(buffer_p, last_format, tocopy);
            res_len += tocopy;
        }
    }
    return res_len;
}
