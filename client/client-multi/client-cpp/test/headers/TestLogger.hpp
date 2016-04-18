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

#ifndef TESTLOGGER_HPP_
#define TESTLOGGER_HPP_

#include "kaa/logging/ILogger.hpp"
#include "kaa/logging/LoggerFactory.hpp"
#include <fstream>

namespace kaa {

class TestLogger : public ILogger
{
public:
    TestLogger(const std::string &filename) {
        filename_ = filename;
        std::ofstream of(filename_, std::ios::openmode::_S_trunc);
        of.close();
    }

private:
    void log    (LogLevel level, const char *message) const {
        std::ofstream of(filename_, std::ios::openmode::_S_app);
        // TODO: timestamps...
        switch (level) {
        case LogLevel::KAA_TRACE:
            of << "[TRACE]\t" << message << std::endl;
            break;
        case LogLevel::KAA_DEBUG:
            of << "[DEBUG]\t" << message << std::endl;
            break;
        case LogLevel::KAA_INFO:
            of << "[INFO]\t" << message << std::endl;
            break;
        case LogLevel::KAA_WARNING:
            of << "[WARN]\t" << message << std::endl;
            break;
        case LogLevel::KAA_ERROR:
            of << "[ERROR]\t" << message << std::endl;
            break;
        case LogLevel::KAA_FATAL:
            of << "[FATAL]\t" << message << std::endl;
            break;
        default:
            break;
        }
        of.close();
    }
    std::string filename_;
};

}

#endif /* TESTLOGGER_HPP_ */
