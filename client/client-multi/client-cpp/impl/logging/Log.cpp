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

#include "kaa/logging/Log.hpp"

#if KAA_LOG_LEVEL > KAA_LOG_LEVEL_NONE

#include <boost/format.hpp>

namespace kaa {

static const char * const KAA_LOG_FMT = "[%1%:%2%]:\t%3%";

void kaa_log_message(const ILogger & logger, LogLevel level, const char *message, const char *file, size_t lineno)
{
    boost::format logline = boost::format(KAA_LOG_FMT) % file % lineno % message;
    logger.log(level, logline.str().c_str());
}

void kaa_log_message(const ILogger & logger, LogLevel level, const std::string &message, const char *file, size_t lineno)
{
    kaa_log_message(logger, level, message.c_str(), file, lineno);
}

void kaa_log_message(const ILogger & logger, LogLevel level, const boost::format& message, const char *file, size_t lineno)
{
    kaa_log_message(logger, level, message.str().c_str(), file, lineno);
}

}  // namespace kaa

#endif
