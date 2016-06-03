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

#include "kaa_error.h"
#include <platform/defaults.h>
#include <platform/stdio.h>
#include <stdint.h>

#ifdef __cplusplus
extern "C" {
#endif


/** Use as the max log level to switch logging off */
#define KAA_LOG_LEVEL_NONE  0
/** Use for severe errors that cause premature program termination */
#define KAA_LOG_LEVEL_FATAL 1
/** Use for runtime errors or unexpected conditions that the program might gracefully recover from */
#define KAA_LOG_LEVEL_ERROR 2
/** Use for unexpected or undesirable runtime conditions that are not necessarily affecting the program */
#define KAA_LOG_LEVEL_WARN  3
/** Use for important or interesting runtime events that help understanding what the program is doing */
#define KAA_LOG_LEVEL_INFO  4
/** Use to log detailed information on the logic flow through the system */
#define KAA_LOG_LEVEL_DEBUG 5
/** Use to log most detailed information intended for development and debugging purposes only */
#define KAA_LOG_LEVEL_TRACE 6

#ifndef KAA_MAX_LOG_LEVEL
/** Use KAA_LOG_TRACE as the max log level by default */
#define KAA_MAX_LOG_LEVEL   KAA_LOG_LEVEL_TRACE
#endif

#define KAA_LOG_LEVEL_FATAL_ENABLED     (KAA_MAX_LOG_LEVEL >= KAA_LOG_LEVEL_FATAL)
#define KAA_LOG_LEVEL_ERROR_ENABLED     (KAA_MAX_LOG_LEVEL >= KAA_LOG_LEVEL_ERROR)
#define KAA_LOG_LEVEL_WARN_ENABLED      (KAA_MAX_LOG_LEVEL >= KAA_LOG_LEVEL_WARN)
#define KAA_LOG_LEVEL_INFO_ENABLED      (KAA_MAX_LOG_LEVEL >= KAA_LOG_LEVEL_INFO)
#define KAA_LOG_LEVEL_DEBUG_ENABLED     (KAA_MAX_LOG_LEVEL >= KAA_LOG_LEVEL_DEBUG)
#define KAA_LOG_LEVEL_TRACE_ENABLED     (KAA_MAX_LOG_LEVEL >= KAA_LOG_LEVEL_TRACE)


struct kaa_logger_t;
typedef struct kaa_logger_t kaa_logger_t;

/**
 * @brief Log level type
 */
typedef uint8_t kaa_log_level_t;

/**
 * @brief Creates and initializes a logger instance.
 *
 * @param[in,out]   logger          Address of a pointer to the newly created logger.
 * @param[in]       buffer_size     Size of the log message buffer to allocate to the logger.
 * @param[in]       max_log_level   Max log level to be used. Use @link KAA_LOG_LEVEL_NONE @endlink to switch the logger off.
 * @param[in]       sink            Valid, opened file to write logs to. Will use @c stdout if @c NULL is provided.
 * @return                          Error code.
 */
kaa_error_t kaa_log_create(kaa_logger_t **logger, size_t buffer_size, kaa_log_level_t max_log_level, FILE* sink);

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
 * @param[in]   self    Pointer to a logger.
 *
 * @return Log level.
 * @retval KAA_LOG_LEVEL_NONE @p self is @c NULL.
 */
kaa_log_level_t kaa_get_max_log_level(const kaa_logger_t *self);

/**
 * @brief Sets the maximum log level.
 *
 * @param[in]   self            Pointer to a logger.
 * @param[in]   max_log_level   Max log level to be used. Use @ref KAA_LOG_LEVEL_NONE to switch the logger off.
 * @return                      Error code.
 */
kaa_error_t kaa_set_max_log_level(kaa_logger_t *self, kaa_log_level_t max_log_level);

/**
 * @brief Sets user sink for log output.
 *
 * @param[in]   self            Pointer to a logger.
 * @param[in]   sink            Pointer to FILE structure where logs will be written to.
 * @return                      Error code.
 */
kaa_error_t kaa_log_set_sink(kaa_logger_t *self, FILE *sink);

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
 * @param[in] self          Pointer to a logger.
 * @param[in] source_file   The source file that the message is logged from.
 * @param[in] lineno        The line number in the source file that the message is logged from.
 * @param[in] log_level     The message log level to log with.
 * @param[in] error_code    The message error code.
 * @param[in] format        The format of the message to log.
 */
void kaa_log_write(kaa_logger_t *self, const char* source_file, int lineno, kaa_log_level_t log_level
        , kaa_error_t error_code, const char* format, ...);

/*
 * Shortcut macros for logging at various log levels
 */
#if KAA_LOG_LEVEL_FATAL_ENABLED
#define KAA_LOG_FATAL(logger, err, ...) kaa_log_write(logger, __FILE__, __LINE__, KAA_LOG_LEVEL_FATAL, err, __VA_ARGS__)
#else
#define KAA_LOG_FATAL(logger, err, ...) do { (void)(logger); (void)(err); } while (0)
#endif

#if KAA_LOG_LEVEL_ERROR_ENABLED
#define KAA_LOG_ERROR(logger, err, ...) kaa_log_write(logger, __FILE__, __LINE__, KAA_LOG_LEVEL_ERROR, err, __VA_ARGS__)
#else
#define KAA_LOG_ERROR(logger, err, ...) do { (void)(logger); (void)(err); } while (0)
#endif

#if KAA_LOG_LEVEL_WARN_ENABLED
#define KAA_LOG_WARN(logger, err, ...)  kaa_log_write(logger, __FILE__, __LINE__, KAA_LOG_LEVEL_WARN, err, __VA_ARGS__)
#else
#define KAA_LOG_WARN(logger, err, ...) do { (void)(logger); (void)(err); } while (0)
#endif

#if KAA_LOG_LEVEL_INFO_ENABLED
#define KAA_LOG_INFO(logger, err, ...)  kaa_log_write(logger, __FILE__, __LINE__, KAA_LOG_LEVEL_INFO, err, __VA_ARGS__)
#else
#define KAA_LOG_INFO(logger, err, ...) do { (void)(logger); (void)(err); } while (0) 
#endif

#if KAA_LOG_LEVEL_DEBUG_ENABLED
#define KAA_LOG_DEBUG(logger, err, ...) kaa_log_write(logger, __FILE__, __LINE__, KAA_LOG_LEVEL_DEBUG, err, __VA_ARGS__)
#else
#define KAA_LOG_DEBUG(logger, err, ...) do { (void)(logger); (void)(err); } while (0)
#endif

#if KAA_LOG_LEVEL_TRACE_ENABLED
#define KAA_LOG_TRACE(logger, err, ...) kaa_log_write(logger, __FILE__, __LINE__, KAA_LOG_LEVEL_TRACE, err, __VA_ARGS__)
#else
#define KAA_LOG_TRACE(logger, err, ...) do { (void)(logger); (void)(err); } while (0)
#endif

#if KAA_LOCAL_DBG
#define KAA_LOG_TRACE_LDB(logger, err, ...) KAA_LOG_TRACE(logger, err, ...)
#else
#define KAA_LOG_TRACE_LDB(logger, err, ...) do { (void)(logger); (void)(err); } while (0)
#endif

#ifdef __cplusplus
}      /* extern "C" */
#endif
#endif /* KAA_LOG_H_ */
