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

// TODO: Need to print milliseconds
#define KAA_LOG_PREFIX_FORMAT    "%04d/%02d/%02d %d:%02d:%02d [%s] [%s:%d] (%d) - "

static char* kaa_log_level_name[] = {
          "OFF"
        , "FATAL"
        , "ERROR"
        , "WARNING"
        , "INFO"
        , "DEBUG"
        , "TRACE"
};

static FILE* user_sink = NULL;

static int max_log_level = KAA_LOG_LEVEL_MAX;
static char log_buffer[KAA_MAX_LOG_MESSAGE_LENGTH + 1];
static const size_t log_buffer_size= sizeof(log_buffer) / sizeof(char);

static void write_log(char* log, size_t len);

void kaa_log_init(kaa_log_level_t level, FILE* sink) {
    max_log_level = level;
    kaa_set_log_sync(sink);
}

void kaa_log_deinit() {
    if (user_sink && (user_sink != stdout) && (user_sink != stderr)) {
        fclose(user_sink);
        user_sink = NULL;
    }
}

void kaa_set_log_sync(FILE* sink) {
    if (sink != stdin) {
        kaa_log_deinit();
        user_sink = sink;
    }
}

kaa_log_level_t kaa_get_log_level() {
    return max_log_level;
}

void kaa_set_log_level(kaa_log_level_t new_log_level) {
    if (KAA_LOG_LEVEL_MIN <= new_log_level && new_log_level <= KAA_LOG_LEVEL_MAX) {
        max_log_level = new_log_level;
    }
}

void kaa_log_write(const char* filename, int lineno, kaa_log_level_t level
                            , kaa_error_t error_code, const char* format, ...)
{
    if (level > max_log_level) {
        return;
    }

    int res_len = 0;
    size_t consumed_len = 0;

    // Truncate full file name
    char* path_separator_pos = strrchr(filename, '/');
    path_separator_pos = (path_separator_pos ? path_separator_pos : strrchr(filename, '\\'));
    const char* trancated_name = (path_separator_pos ? path_separator_pos + 1 : filename);

    // Convert to UTC time
    time_t t = time(NULL);
    struct tm* tp = gmtime(&t);

    // TODO: Need to print milliseconds. May be used timespec() from C11 standard,
    // but GCC 4.6.4 doesn't support this API.

    // Load data into a buffer
    res_len = snprintf(log_buffer, log_buffer_size, KAA_LOG_PREFIX_FORMAT,
                        1900 + tp->tm_year, tp->tm_mon + 1, tp->tm_mday,
                        tp->tm_hour, tp->tm_min, tp->tm_sec,
                        kaa_log_level_name[level], trancated_name, lineno, error_code);

    if (res_len > 0) { consumed_len += res_len; }

    va_list args;
    va_start(args, format);
    res_len = vsnprintf(log_buffer + consumed_len
                      , log_buffer_size - consumed_len
                      , format
                      , args);
    va_end(args);

    if (res_len > 0) { consumed_len += res_len; }

    if (consumed_len > 0) {
        write_log(log_buffer ,consumed_len);
    }
}

static void write_log(char* log, size_t len) {
    if (log) {
        if (len > KAA_MAX_LOG_MESSAGE_LENGTH) {
            len = KAA_MAX_LOG_MESSAGE_LENGTH;
        }

        log[len++] = '\n';

        FILE* sink = stdout;
        if (user_sink) {
            sink = user_sink;
        }

        fwrite(log, sizeof(char), len, sink);
        fflush(sink);
    }
}
