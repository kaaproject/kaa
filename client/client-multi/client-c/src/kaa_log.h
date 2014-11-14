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

#ifndef KAA_LOG_H_
#define KAA_LOG_H_

#include <stdio.h>

#include "kaa_error.h"

#ifdef __cplusplus
extern "C" {
    #define CLOSE_EXTERN }
#else
    #define CLOSE_EXTERN
#endif

/**
 * <p>The maximum size of a log message.</p>
 *
 * <p>If the length of a log message is greater than @link KAA_MAX_LOG_MESSAGE_LENGTH @endlink ,
 * the message will be truncated to match the @link KAA_MAX_LOG_MESSAGE_LENGTH @endlink .</p>
 *
 */
#define KAA_MAX_LOG_MESSAGE_LENGTH   512

typedef enum kaa_log_level_t {
    /**
     * Used to switch off logging.
     */
    KAA_LOG_NONE,

    /**
     * Used to configure the log level.
     */
    KAA_LOG_FATAL,
    KAA_LOG_ERROR,
    KAA_LOG_WARN,
    KAA_LOG_INFO,
    KAA_LOG_DEBUG,
    KAA_LOG_TRACE,

    /**
     * <p>Used for the service purpose.</p>
     *
     * <p><b>NOTE:</b> Do not use these values directly.</p>
     */
    KAA_LOG_LEVEL_MIN = KAA_LOG_NONE,
    KAA_LOG_LEVEL_MAX = KAA_LOG_TRACE
} kaa_log_level_t;

/**
 * <p>Initialize the logger.</p>
 *
 * <p>To configure log level, one of @link KAA_LOG_FATAL @endlink,
 * @link KAA_LOG_ERROR @endlink, @link KAA_LOG_WARN @endlink,
 * @link KAA_LOG_INFO @endlink, @link KAA_LOG_DEBUG @endlink,
 * @link KAA_LOG_TRACE @endlink values can be used.</p>
 *
 * <p>In order to switch off logging use @link KAA_LOG_NONE @endlink .</p>
 *
 * <p><If <i>sink</i> parameter is set to <i>NULL</i>, standard output
 * (<i>stdout</i>) will be used.</p>
 *
 * @param level Max log level to be used.
 * @param sink Destination file for the logs.
 *
 * @see kaa_log_level_t
 *
 */
void kaa_log_init(kaa_log_level_t level, FILE* sink);

/**
 * <p>Deinitialize Kaa logger.</p>
 *
 * <p>Close the user sink, if it was used.</p>
 *
 */
void kaa_log_deinit();

/**
 * <p>Set the sink where logs to be put.</p>
 *
 * <p><If <i>sink</i> is set to <i>NULL</i>, standard output
 * (<i>stdout</i>) will be used.</p>
 *
 */
void kaa_set_log_sync(FILE* sink);

/**
 * <p>Retrieve the current log level.</p>
 */
kaa_log_level_t kaa_get_log_level();

/**
 * <p>Set the log level.</p>
 *
 * <p>To configure the log level, one of @link KAA_LOG_FATAL @endlink,
 * @link KAA_LOG_ERROR @endlink, @link KAA_LOG_WARN @endlink,
 * @link KAA_LOG_INFO @endlink, @link KAA_LOG_DEBUG @endlink,
 * @link KAA_LOG_TRACE @endlink values can be used.</p>
 *
 * <p>To switch off logging use @link KAA_LOG_NONE @endlink .</p>
 *
 * @param new_log_level Maximum log level to be used.
 *
 * @see kaa_log_level_t
 */
void kaa_set_log_level(kaa_log_level_t new_log_level);

/**
 * <p>Form a log message and put it in the sink..</p>
 *
 * <p>The message format is the following:
 * "YYYY/MM/DD HH:MM:SS [LOG LEVEL] [FILE:LINENO] (ERROR_CODE) - MESSAGE"</p>
 *
 * <p><b>NOTE:</b> Do not use it directly. Use @link KAA_LOG_FATAL @endlink,
 * @link KAA_LOG_ERROR @endlink, @link KAA_LOG_WARN @endlink,
 * @link KAA_LOG_INFO @endlink, @link KAA_LOG_DEBUG @endlink,
 * @link KAA_LOG_TRACE @endlink macros instead.</p>
 *
 * <p>If the length of a log message is greater than @link KAA_MAX_LOG_MESSAGE_LENGTH @endlink ,
 * the message will be truncated to match the @link KAA_MAX_LOG_MESSAGE_LENGTH @endlink .</p>
 *
 * @see KAA_MAX_LOG_MESSAGE_LENGTH
 * @see kaa_log_level_t
 * @see kaa_error_t
 *
 */
void kaa_log_write(const char* filename, int lineno, kaa_log_level_t level
                            , kaa_error_t error_code, const char* format, ...);

/**
 * <p>The log message with the @link KAA_LOG_FATAL @endlink level.</p>
 *
 * <p>For more details see @link kaa_log_write @endlink .</p>
 *
 * @see kaa_error_t
 */
#define KAA_LOG_FATAL(err, ...) kaa_log_write(__FILE__, __LINE__, KAA_LOG_FATAL, err, __VA_ARGS__);

/**
 * <p>The log message with the @link KAA_LOG_ERROR @endlink level.</p>
 *
 * <p>For more details see @link kaa_log_write @endlink .</p>
 *
 * @see kaa_error_t
 */
#define KAA_LOG_ERROR(err, ...) kaa_log_write(__FILE__, __LINE__, KAA_LOG_ERROR, err, __VA_ARGS__);

/**
 * <p>The log message with the @link KAA_LOG_WARN @endlink level.</p>
 *
 * <p>For more details see @link kaa_log_write @endlink .</p>
 *
 * @see kaa_error_t
 */
#define KAA_LOG_WARN(err, ...)  kaa_log_write(__FILE__, __LINE__, KAA_LOG_WARN, err, __VA_ARGS__);

/**
 * <p>The log message with the @link KAA_LOG_INFO @endlink level.</p>
 *
 * <p>For more details see @link kaa_log_write @endlink .</p>
 *
 * @see kaa_error_t
 */
#define KAA_LOG_INFO(err, ...)  kaa_log_write(__FILE__, __LINE__, KAA_LOG_INFO, err, __VA_ARGS__);

/**
 * <p>The log message with the @link KAA_LOG_DEBUG @endlink level.</p>
 *
 * <p>For more details see @link kaa_log_write @endlink .</p>
 *
 * @see kaa_error_t
 */
#define KAA_LOG_DEBUG(err, ...) kaa_log_write(__FILE__, __LINE__, KAA_LOG_DEBUG, err, __VA_ARGS__);

/**
 * <p>The log message with the @link KAA_LOG_TRACE @endlink level.</p>
 *
 * <p>For more details see @link kaa_log_write @endlink .</p>
 *
 * @see kaa_error_t
 */
#define KAA_LOG_TRACE(err, ...) kaa_log_write(__FILE__, __LINE__, KAA_LOG_TRACE, err, __VA_ARGS__);

CLOSE_EXTERN
#endif /* KAA_LOG_H_ */
