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

#include "kaa/logging/LoggerFactory.hpp"

#include "kaa/logging/DefaultLogger.hpp"

#include <sstream>

#include "kaa/logging/DefaultLogger.hpp"

namespace kaa {

class KaaLogger : public ILogger {
public:
    KaaLogger(LoggerPtr userLog) : logger_(userLog) {}

    void ftrace (const char *message) const { log(LogLevel::FINE_TRACE, message); }
    void debug  (const char *message) const { log(LogLevel::DEBUG, message); }
    void trace  (const char *message) const { log(LogLevel::TRACE, message); }
    void info   (const char *message) const { log(LogLevel::INFO, message); }
    void warn   (const char *message) const { log(LogLevel::WARNING, message); }
    void error  (const char *message) const { log(LogLevel::ERROR, message); }
    void fatal  (const char *message) const { log(LogLevel::FATAL, message); }

    void log    (LogLevel level, const char *message) const;

    void resetLogger(LoggerPtr logger) {
        logger_ = logger;
    }
private:
    LoggerPtr       logger_;
};

void KaaLogger::log(LogLevel level, const char *message) const
{
    if (logger_.get() != nullptr) {
        switch (level) {
        case LogLevel::FINE_TRACE:
            logger_->ftrace(message);
            break;
        case LogLevel::DEBUG:
            logger_->debug(message);
            break;
        case LogLevel::TRACE:
            logger_->trace(message);
            break;
        case LogLevel::INFO:
            logger_->info(message);
            break;
        case LogLevel::WARNING:
            logger_->warn(message);
            break;
        case LogLevel::ERROR:
            logger_->error(message);
            break;
        case LogLevel::FATAL:
            logger_->fatal(message);
            break;
        default:
            break;
        }
    }
}

static LoggerPtr getDefaultLogger() {
    return LoggerPtr(new KaaLogger(LoggerPtr(new DefaultLogger())));
}

LoggerPtr LoggerFactory::logger_ = getDefaultLogger();

const ILogger & LoggerFactory::getLogger() {
    if (logger_.get() == nullptr) {
        logger_ = getDefaultLogger();
    }
    return *logger_;

}

void LoggerFactory::initLogger(LoggerPtr logger) {
    dynamic_cast<KaaLogger *>(logger_.get())->resetLogger(logger);
}

}  // namespace kaa

