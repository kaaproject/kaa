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

#ifndef DEFAULTLOGGER_HPP_
#define DEFAULTLOGGER_HPP_

#include "kaa/logging/ILogger.hpp"

#include <boost/log/trivial.hpp>

namespace kaa {

class DefaultLogger : public ILogger {
public:
    void ftrace (const char *message) const { log(LogLevel::FINE_TRACE, message); }
    void debug  (const char *message) const { log(LogLevel::DEBUG, message); }
    void trace  (const char *message) const { log(LogLevel::TRACE, message); }
    void info   (const char *message) const { log(LogLevel::INFO, message); }
    void warn   (const char *message) const { log(LogLevel::WARNING, message); }
    void error  (const char *message) const { log(LogLevel::ERROR, message); }
    void fatal  (const char *message) const { log(LogLevel::FATAL, message); }

private:
    void log    (LogLevel level, const char *message) const {
        switch (level) {
            case LogLevel::FINE_TRACE:
            case LogLevel::TRACE:
                BOOST_LOG_TRIVIAL(trace) << message;
                break;
            case LogLevel::DEBUG:
                BOOST_LOG_TRIVIAL(debug) << message;
                break;
            case LogLevel::INFO:
                BOOST_LOG_TRIVIAL(info) << message;
                break;
            case LogLevel::WARNING:
                BOOST_LOG_TRIVIAL(warning) << message;
                break;
            case LogLevel::ERROR:
                BOOST_LOG_TRIVIAL(error) << message;
                break;
            case LogLevel::FATAL:
                BOOST_LOG_TRIVIAL(fatal) << message;
                break;
            default:
                break;
            }
    }
};

}  // namespace kaa


#endif /* DEFAULTLOGGER_HPP_ */
