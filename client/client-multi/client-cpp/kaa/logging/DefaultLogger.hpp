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

#ifndef DEFAULTLOGGER_HPP_
#define DEFAULTLOGGER_HPP_

#include <string>
#include <boost/log/sinks/sync_frontend.hpp>
#include <boost/log/sinks/text_ostream_backend.hpp>
#include <boost/smart_ptr/shared_ptr.hpp>
#include "kaa/KaaDefaults.hpp"

#include "kaa/logging/ILogger.hpp"

namespace kaa {

class DefaultLogger : public ILogger {
public:
  DefaultLogger(const std::string& clientId, const std::string& logFileName = std::string());

  void log(LogLevel level, const char *message) const;

private:
    std::string clientId_;
    using text_sink = boost::log::sinks::synchronous_sink< boost::log::sinks::text_ostream_backend >;
    boost::shared_ptr< text_sink > pSink_;
};

}  // namespace kaa

#endif /* DEFAULTLOGGER_HPP_ */
