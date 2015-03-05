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

#if KAA_LOG_LEVEL > KAA_LOG_LEVEL_NONE

#include "kaa/logging/DefaultLogger.hpp"

#include <sstream>

#include "kaa/logging/DefaultLogger.hpp"

namespace kaa {

static LoggerPtr getDefaultLogger()
{
    return LoggerPtr(new DefaultLogger());
}

LoggerPtr LoggerFactory::logger_ = getDefaultLogger();

const ILogger & LoggerFactory::getLogger()
{
    if (logger_.get() == nullptr) {
        logger_ = getDefaultLogger();
    }
    return *logger_;

}

void LoggerFactory::initLogger(LoggerPtr logger)
{
    logger_ = logger;
}

}  // namespace kaa

#endif

