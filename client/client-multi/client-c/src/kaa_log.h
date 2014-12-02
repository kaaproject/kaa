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

/**
 * @file kaa_log.h
 * @brief Simple logger for Kaa C Endpoint.
 *
 * Supports runtime limitation of the maximum log level to be logged.
 * Expects externally provided and managed valid @c FILE* reference to log data to.
 * Not thread safe.
 */

#ifndef KAA_LOG_H_
#define KAA_LOG_H_

#include <stdio.h>
#include "kaa_error.h"
#define KAA_MAX_LOG_MESSAGE_LENGTH  256

#ifdef __cplusplus
extern "C" {
#endif

#ifndef KAA_MAX_LOG_LEVEL
#define KAA_MAX_LOG_LEVEL   6   /**< Using KAA_LOG_TRACE as the max log level by default */
#endif

#define KAA_LOG_LEVEL_FATAL_ENABLED     (KAA_MAX_LOG_LEVEL > 0)
#define KAA_LOG_LEVEL_ERROR_ENABLED     (KAA_MAX_LOG_LEVEL > 1)
#define KAA_LOG_LEVEL_WARN_ENABLED      (KAA_MAX_LOG_LEVEL > 2)
#define KAA_LOG_LEVEL_INFO_ENABLED      (KAA_MAX_LOG_LEVEL > 3)
#define KAA_LOG_LEVEL_DEBUG_ENABLED     (KAA_MAX_LOG_LEVEL > 4)
#define KAA_LOG_LEVEL_TRACE_ENABLED     (KAA_MAX_LOG_LEVEL > 5)

/**
 * @brief Kaa logger type
 */
typedef struct kaa_logger_t kaa_logger_t;

/**
 * @brief Log levels
 */
typedef enum {
    KAA_LOG_NONE,   /**< Use as the max log level to switch logging off */
#if KAA_LOG_LEVEL_FATAL_ENABLED
    KAA_LOG_FATAL,  /**< Use for severe errors that cause premature program termination */
#if KAA_LOG_LEVEL_ERROR_ENABLED
    KAA_LOG_ERROR,  /**< Use for runtime errors or unexpected conditions that the program might gracefully recover from */
#if KAA_LOG_LEVEL_WARN_ENABLED
    KAA_LOG_WARN,   /**< Use for unexpected or undesirable runtime conditions that are not necessarily affecting the program */
#if KAA_LOG_LEVEL_INFO_ENABLED
    KAA_LOG_INFO,   /**< Use for important or interesting runtime events that help understanding what the program is doing */
#if KAA_LOG_LEVEL_DEBUG_ENABLED
    KAA_LOG_DEBUG,  /**< Use to log detailed information on the logic flow through the system */
#if KAA_LOG_LEVEL_TRACE_ENABLED
    KAA_LOG_TRACE   /**< Use to log most detailed information intended for development and debugging purposes only */
#endif // KAA_LOG_LEVEL_TRACE_ENABLED
#endif // KAA_LOG_LEVEL_DEBUG_ENABLED
#endif // KAA_LOG_LEVEL_INFO_ENABLED
#endif // KAA_LOG_LEVEL_WARN_ENABLED
#endif // KAA_LOG_LEVEL_ERROR_ENABLED
#endif // KAA_LOG_LEVEL_FATAL_ENABLED
} kaa_log_level_t;

/**
 * @brief Creates and initializes a logger instance.
 *
 * @param[in,out]   logger          Address of a pointer to the newly created logger.
 * @param[in]       buffer_size     Size of the log message buffer to allocate to the logger.
 * @param[in]       max_log_level   Max log level to be used. Use @link KAA_LOG_NONE @endlink to switch the logger off.
 * @param[in]       sink            Valid, opened file to write logs to. Will use @c stdout if @c NULL is provided.
 * @return                          Error code.
 */
kaa_error_t kaa_log_create(kaa_logger_t **logger_p, size_t buffer_size, kaa_log_level_t max_log_level, FILE* sink);

/**
 * @brief Deinitializes and destroys the logger instance.
 *
 * @param[in,out]   logger  Pointer to a logger.
 * @return                  Error code.
 */
kaa_error_t kaa_log_destroy(kaa_logger_t *logger);

/**
 * @brief Retrieves the current log level.
 *
 * @param[in]   this    Pointer to a logger.
 * @return              Log level. Returns @link KAA_LOG_NONE @endlink if this is NULL.
 */
kaa_log_level_t kaa_get_max_log_level(const kaa_logger_t *this);

/**
 * @brief Sets the maximum log level.
 *
 * @param[in]   this            Pointer to a logger.
 * @param[in]   max_log_level   Max log level to be used. Use @link KAA_LOG_NONE @endlink to switch the logger off.
 * @return                      Error code.
 */
kaa_error_t kaa_set_max_log_level(kaa_logger_t *this, kaa_log_level_t max_log_level);

/**
 * @brief Compiles a log message and puts it into the sink.
 *
 * The message format is as follows:
 * @code YYYY/MM/DD HH:MM:SS [LOG LEVEL] [FILE:LINENO] (ERROR_CODE) - MESSAGE @endcode
 *
 * <b>NOTE:</b> Do not use directly. Use one of @link KAA_LOG_FATAL @endlink,
 * @link KAA_LOG_ERROR @endlink, @link KAA_LOG_WARN @endlink,
 * @link KAA_LOG_INFO @endlink, @link KAA_LOG_DEBUG @endlink,
 * @link KAA_LOG_TRACE @endlink macros instead.
 *
 * The log message gets truncated if it is longer than @c buffer_size specified to @link kaa_log_create @endlink.
 *
 * @param[in] this          Pointer to a logger.
 * @param[in] source_file   The source file that the message is logged from.
 * @param[in] lineno        The line number in the source file that the message is logged from.
 * @param[in] log_level     The message log level to log with.
 * @param[in] error_code    The message error code.
 * @param[in] format        The format of the message to log.
 */
void kaa_log_write(kaa_logger_t *this, const char* source_file, int lineno, kaa_log_level_t log_level
        , kaa_error_t error_code, const char* format, ...);

/*
 * Shortcut macros for logging at various log levels
 */
#if KAA_LOG_LEVEL_FATAL_ENABLED
#define KAA_LOG_FATAL(logger, err, ...) kaa_log_write(logger, __FILE__, __LINE__, KAA_LOG_FATAL, err, __VA_ARGS__);
#else
#define KAA_LOG_FATAL(...)
#endif

#if KAA_LOG_LEVEL_ERROR_ENABLED
#define KAA_LOG_ERROR(logger, err, ...) kaa_log_write(logger, __FILE__, __LINE__, KAA_LOG_ERROR, err, __VA_ARGS__);
#else
#define KAA_LOG_ERROR(...)
#endif

#if KAA_LOG_LEVEL_WARN_ENABLED
#define KAA_LOG_WARN(logger, err, ...)  kaa_log_write(logger, __FILE__, __LINE__, KAA_LOG_WARN, err, __VA_ARGS__);
#else
#define KAA_LOG_WARN(...)
#endif

#if KAA_LOG_LEVEL_INFO_ENABLED
#define KAA_LOG_INFO(logger, err, ...)  kaa_log_write(logger, __FILE__, __LINE__, KAA_LOG_INFO, err, __VA_ARGS__);
#else
#define KAA_LOG_INFO(...)
#endif

#if KAA_LOG_LEVEL_DEBUG_ENABLED
#define KAA_LOG_DEBUG(logger, err, ...) kaa_log_write(logger, __FILE__, __LINE__, KAA_LOG_DEBUG, err, __VA_ARGS__);
#else
#define KAA_LOG_DEBUG(...)
#endif

#if KAA_LOG_LEVEL_TRACE_ENABLED
#define KAA_LOG_TRACE(logger, err, ...) kaa_log_write(logger, __FILE__, __LINE__, KAA_LOG_TRACE, err, __VA_ARGS__);
#else
#define KAA_LOG_TRACE(...)
#endif

/*
 * Shortcut macros for tracing through the program
 */
#define KAA_TRACE_IN(logger)  KAA_LOG_TRACE(logger, KAA_ERR_NONE, "--> %s()", __FUNCTION__);
#define KAA_TRACE_OUT(logger) KAA_LOG_TRACE(logger, KAA_ERR_NONE, "<-- %s()", __FUNCTION__);

#ifdef __cplusplus
}      /* extern "C" */
#endif
#endif /* KAA_LOG_H_ */
