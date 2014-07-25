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

#ifndef LOG_HPP_
#define LOG_HPP_

#include "kaa/logging/LoggerFactory.hpp"

#include <string.h>
#include <boost/format.hpp>
#include <boost/thread/mutex.hpp>
#include <boost/thread/lock_options.hpp>


#ifdef _WIN32
#define PATH_SEPARATOR '\\'
#else
#define PATH_SEPARATOR '/'
#endif

#define __LOGFILE (strrchr(__FILE__, PATH_SEPARATOR) ? strrchr(__FILE__, PATH_SEPARATOR) + 1 : __FILE__)

namespace kaa {

void kaa_log(const ILogger & logger, LogLevel level, const char *message, const char *file, size_t lineno);
void kaa_log(const ILogger & logger, LogLevel level, const std::string &message, const char *file, size_t lineno);
void kaa_log(const ILogger & logger, LogLevel level, boost::format message, const char *file, size_t lineno);

#define KAA_LOG_FTRACE(message) kaa_log(LoggerFactory::getLogger(), LogLevel::FINE_TRACE, (message), __LOGFILE, __LINE__);
#define KAA_LOG_DEBUG(message)  kaa_log(LoggerFactory::getLogger(), LogLevel::DEBUG,      (message), __LOGFILE, __LINE__);
#define KAA_LOG_TRACE(message)  kaa_log(LoggerFactory::getLogger(), LogLevel::TRACE,      (message), __LOGFILE, __LINE__);
#define KAA_LOG_INFO(message)   kaa_log(LoggerFactory::getLogger(), LogLevel::INFO,       (message), __LOGFILE, __LINE__);
#define KAA_LOG_WARN(message)   kaa_log(LoggerFactory::getLogger(), LogLevel::WARNING,    (message), __LOGFILE, __LINE__);
#define KAA_LOG_ERROR(message)  kaa_log(LoggerFactory::getLogger(), LogLevel::ERROR,      (message), __LOGFILE, __LINE__);
#define KAA_LOG_FATAL(message)  kaa_log(LoggerFactory::getLogger(), LogLevel::FATAL,      (message), __LOGFILE, __LINE__);

#ifdef KAA_MUTEX_LOGGING_ENABLED
template <typename LockType, typename MutableObject>
class MutexScopedLockLogger {
public:
    MutexScopedLockLogger(const char *name, MutableObject& m, const char *file, size_t line)
        : name_(name)
        , file_(file)
        , line_(line)
        , lock_(m, boost::defer_lock_t()) {
        kaa_log(LoggerFactory::getLogger(), LogLevel::DEBUG, (boost::format("Locking %1% mutex") % name_).str(), file_, line_);
        lock_.lock();
        kaa_log(LoggerFactory::getLogger(), LogLevel::DEBUG, (boost::format("Locked %1% mutex") % name_).str(), file_, line_);
    }
    ~MutexScopedLockLogger() {
        kaa_log(LoggerFactory::getLogger(), LogLevel::DEBUG, (boost::format("Unlocking %1% mutex") % name_).str(), file_, line_);
    }
private:
    const char *name_;
    const char *file_;
    size_t      line_;
    LockType   lock_;
};

    #define KAA_MUTEX_LOCKING(mutex_name)   KAA_LOG_DEBUG("Locking " mutex_name " mutex");
    #define KAA_MUTEX_LOCKED(mutex_name)    KAA_LOG_DEBUG("Locked " mutex_name " mutex");
    #define KAA_MUTEX_UNLOCKING(mutex_name) KAA_LOG_DEBUG("Unlocking " mutex_name " mutex");
    #define KAA_MUTEX_UNLOCKED(mutex_name)  KAA_LOG_DEBUG("Unlocked " mutex_name " mutex");
    #define KAA_MUTEX_LOG_AND_LOCK(LockType, MutableType, MutableObject) MutexScopedLockLogger<LockType, MutableType> MutableObject##Lock(#MutableObject, MutableObject, __LOGFILE, __LINE__)
#else
    #define KAA_MUTEX_LOCKING(mutex_name)
    #define KAA_MUTEX_LOCKED(mutex_name)
    #define KAA_MUTEX_UNLOCKING(mutex_name)
    #define KAA_MUTEX_UNLOCKED(mutex_name)
    #define KAA_MUTEX_LOG_AND_LOCK(LockType, MutableType, MutableObject) LockType MutableObject##Lock(MutableObject)
#endif

}  // namespace kaa

#endif /* LOG_HPP_ */
