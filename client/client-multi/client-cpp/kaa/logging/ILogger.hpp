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

#ifndef ILOGGER_HPP_
#define ILOGGER_HPP_

#include <boost/smart_ptr/shared_ptr.hpp>

namespace kaa {

enum class LogLevel {
    FINE_TRACE = 0,
    DEBUG,
    TRACE,
    INFO,
    WARNING,
    ERROR,
    FATAL
};

class ILogger {
public:
    virtual ~ILogger() {}

    virtual void ftrace (const char *message) const = 0;
    virtual void debug  (const char *message) const = 0;
    virtual void trace  (const char *message) const = 0;
    virtual void info   (const char *message) const = 0;
    virtual void warn   (const char *message) const = 0;
    virtual void error  (const char *message) const = 0;
    virtual void fatal  (const char *message) const = 0;

    virtual void log(LogLevel level, const char *message) const = 0;
};

typedef boost::shared_ptr<ILogger> LoggerPtr;

}  // namespace kaa


#endif /* ILOGGER_HPP_ */
