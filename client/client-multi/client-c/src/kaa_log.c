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

#include "kaa_log.h"

#include <time.h>
#include <stdarg.h>
#include <string.h>

#include "kaa_common.h"

// TODO: Print milliseconds
#define KAA_LOG_PREFIX_FORMAT    "%04d/%02d/%02d %d:%02d:%02d [%s] [%s:%d] (%d) - "

// Internal variables
static char  log_buffer[KAA_MAX_LOG_MESSAGE_LENGTH + 1];
static const size_t log_buffer_size = sizeof(log_buffer) / sizeof(char);
/**
 * Printable loglevels
 * @see kaa_log_level_t
 */
static char* kaa_log_level_name[] =
{
      "NONE"
    , "FATAL"
    , "ERROR"
    , "WARNING"
    , "INFO"
    , "DEBUG"
    , "TRACE"
};

// Variables exposed via functions
static FILE* __sink = NULL;
static kaa_log_level_t __max_log_level = KAA_LOG_TRACE;


void kaa_log_init(kaa_log_level_t max_log_level, FILE* sink)
{
    __sink = sink;
    __max_log_level = max_log_level;
}

void kaa_log_deinit()
{
    __sink = NULL;
    __max_log_level = KAA_LOG_TRACE;
}

void kaa_set_log_sink(FILE* sink)
{
    __sink = sink;
}

static void write_log(const char* log, size_t len)
{
    if (log && len > 0) {
        FILE* sink = __sink ? __sink : stdout;
        fwrite(log, sizeof(char), len, sink);
        fflush(sink);
    }
}

kaa_log_level_t kaa_get_max_log_level()
{
    return __max_log_level;
}

void kaa_set_max_log_level(kaa_log_level_t max_log_level)
{
    __max_log_level = max_log_level;
}

void kaa_log_write(const char* source_file, int lineno, kaa_log_level_t log_level
        , kaa_error_t error_code, const char* format, ...)
{
    if (log_level > __max_log_level) {
        return;
    }

    int res_len = 0;
    size_t consumed_len = 0;

    // Truncate the file name
    char* path_separator_pos = strrchr(source_file, '/');
    path_separator_pos = (path_separator_pos ? path_separator_pos : strrchr(source_file, '\\'));
    const char* truncated_name = (path_separator_pos ? path_separator_pos + 1 : source_file);

    // Convert to UTC time
    time_t t = time(NULL);
    struct tm* tp = gmtime(&t);

    // TODO: Need to print milliseconds. For this purpose, timespec() from C11
    // standard may be used, but GCC 4.6.4 doesn't support this API.

    // Load data into the buffer
    res_len = snprintf(log_buffer, log_buffer_size, KAA_LOG_PREFIX_FORMAT
            , 1900 + tp->tm_year, tp->tm_mon + 1, tp->tm_mday
            , tp->tm_hour, tp->tm_min, tp->tm_sec
            , kaa_log_level_name[log_level], truncated_name, lineno, error_code);

    if (res_len > 0) {
        consumed_len += res_len;
    }

    va_list args;
    va_start(args, format);
    res_len = vsnprintf(log_buffer + consumed_len
            , log_buffer_size - consumed_len
            , format
            , args);
    va_end(args);

    if (res_len > 0) {
        consumed_len += res_len;
    }

    if (consumed_len > 0) {
        if (consumed_len > KAA_MAX_LOG_MESSAGE_LENGTH) {
            consumed_len = KAA_MAX_LOG_MESSAGE_LENGTH;
        }

        log_buffer[consumed_len++] = '\n';
        write_log(log_buffer, consumed_len);
    }
}
