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

#ifndef LOG_HPP_
#define LOG_HPP_

#include "kaa/KaaDefaults.hpp"

#if KAA_LOG_LEVEL > KAA_LOG_LEVEL_NONE
#include "kaa/logging/ILogger.hpp"

#include <string.h>
#include <boost/format.hpp>


#ifdef _WIN32
#define PATH_SEPARATOR '\\'
#else
#define PATH_SEPARATOR '/'
#endif

#define __LOGFILE (strrchr(__FILE__, PATH_SEPARATOR) ? strrchr(__FILE__, PATH_SEPARATOR) + 1 : __FILE__)

#endif

namespace kaa {

#if KAA_LOG_LEVEL > KAA_LOG_LEVEL_NONE
void kaa_log_message(const ILogger & logger, LogLevel level, const char *message, const char *file, size_t lineno);
void kaa_log_message(const ILogger & logger, LogLevel level, const std::string &message, const char *file, size_t lineno);
void kaa_log_message(const ILogger & logger, LogLevel level, const boost::format& message, const char *file, size_t lineno);

#endif

#if KAA_LOG_LEVEL >= KAA_LOG_LEVEL_FINE_TRACE
    #define KAA_LOG_FTRACE(message) kaa_log_message(context_.getLogger(), LogLevel::KAA_TRACE,      (message), __LOGFILE, __LINE__);
#else
    #define KAA_LOG_FTRACE(message)
#endif
#if KAA_LOG_LEVEL >= KAA_LOG_LEVEL_TRACE
    #define KAA_LOG_TRACE(message)  kaa_log_message(context_.getLogger(), LogLevel::KAA_TRACE,      (message), __LOGFILE, __LINE__);
#else
    #define KAA_LOG_TRACE(message)
#endif
#if KAA_LOG_LEVEL >= KAA_LOG_LEVEL_DEBUG
    #define KAA_LOG_DEBUG(message)  kaa_log_message(context_.getLogger(), LogLevel::KAA_DEBUG,      (message), __LOGFILE, __LINE__);
#else
    #define KAA_LOG_DEBUG(message)
#endif
#if KAA_LOG_LEVEL >= KAA_LOG_LEVEL_INFO
    #define KAA_LOG_INFO(message)   kaa_log_message(context_.getLogger(), LogLevel::KAA_INFO,       (message), __LOGFILE, __LINE__);
#else
    #define KAA_LOG_INFO(message)
#endif
#if KAA_LOG_LEVEL >= KAA_LOG_LEVEL_WARNING
    #define KAA_LOG_WARN(message)   kaa_log_message(context_.getLogger(), LogLevel::KAA_WARNING,    (message), __LOGFILE, __LINE__);
#else
    #define KAA_LOG_WARN(message)
#endif
#if KAA_LOG_LEVEL >= KAA_LOG_LEVEL_ERROR
    #define KAA_LOG_ERROR(message)  kaa_log_message(context_.getLogger(), LogLevel::KAA_ERROR,      (message), __LOGFILE, __LINE__);
#else
    #define KAA_LOG_ERROR(message)
#endif
#if KAA_LOG_LEVEL >= KAA_LOG_LEVEL_FATAL
    #define KAA_LOG_FATAL(message)  kaa_log_message(context_.getLogger(), LogLevel::KAA_FATAL,      (message), __LOGFILE, __LINE__);
#else
    #define KAA_LOG_FATAL(message)
#endif

#if defined(KAA_THREADSAFE) && defined(KAA_MUTEX_LOGGING_ENABLED) && KAA_LOG_LEVEL > 4
    #define KAA_MUTEX_LOCKING(mutex_name)   KAA_LOG_DEBUG("Locking " mutex_name " mutex");
    #define KAA_MUTEX_LOCKED(mutex_name)    KAA_LOG_DEBUG("Locked " mutex_name " mutex");
    #define KAA_MUTEX_UNLOCKING(mutex_name) KAA_LOG_DEBUG("Unlocking " mutex_name " mutex");
    #define KAA_MUTEX_UNLOCKED(mutex_name)  KAA_LOG_DEBUG("Unlocked " mutex_name " mutex");
#else
    #define KAA_MUTEX_LOCKING(mutex_name)
    #define KAA_MUTEX_LOCKED(mutex_name)
    #define KAA_MUTEX_UNLOCKING(mutex_name)
    #define KAA_MUTEX_UNLOCKED(mutex_name)
#endif

}  // namespace kaa

#endif /* LOG_HPP_ */
