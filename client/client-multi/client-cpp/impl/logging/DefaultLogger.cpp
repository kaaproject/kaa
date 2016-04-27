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

#include <boost/log/expressions.hpp>
#include <boost/log/trivial.hpp>
#include <boost/thread/thread.hpp>
#include <boost/log/utility/setup/file.hpp>
#include <boost/log/sources/global_logger_storage.hpp>
#include <boost/log/attributes/current_thread_id.hpp>
#include <boost/log/utility/setup/common_attributes.hpp>
#include <boost/move/utility.hpp>
#include <boost/log/attributes/scoped_attribute.hpp>
#include <boost/date_time/posix_time/posix_time.hpp>
#include <boost/log/utility/setup/file.hpp>
#include <boost/log/sources/record_ostream.hpp>
#include <boost/thread/thread.hpp>
#include <fstream>

#include "kaa/logging/DefaultLogger.hpp"

namespace kaa {

DefaultLogger::DefaultLogger(const std::string& clientId, const std::string& logFileName): clientId_(clientId), pSink_(new text_sink)
{
    text_sink::locked_backend_ptr pBackend = pSink_->locked_backend();
    boost::shared_ptr< std::ostream > consoleStream(&std::clog, [](const void *)->void const {});
    pBackend->add_stream(consoleStream);
    if (!logFileName.empty()) {
        boost::shared_ptr< std::ofstream > fileStream(new std::ofstream(logFileName.c_str()));
        pBackend->add_stream(fileStream);
    }

    pSink_->set_formatter(boost::log::expressions::stream << '[' << boost::log::expressions::attr< std::string > ("id")
                          << ']' << '[' << boost::log::expressions::attr< boost::posix_time::ptime >("TimeStamp") << ']'
                          << '[' <<  boost::log::expressions::attr< boost::log::attributes::current_thread_id::value_type >("ThreadID")
                          << ']' << '[' << boost::log::expressions::attr< boost::log::trivial::severity_level >("Severity") << ']' <<  boost::log::expressions::smessage);

    pSink_->set_filter(boost::log::expressions::begins_with(boost::log::expressions::attr< std::string >("id"), clientId_.c_str()));
    pBackend->auto_flush(true);
    boost::log::core::get()->add_global_attribute("TimeStamp", boost::log::attributes::local_clock());
    boost::log::core::get()->add_global_attribute("ThreadID", boost::log::attributes::current_thread_id());
    boost::log::core::get()->add_sink(pSink_);

}

void DefaultLogger::log(LogLevel level, const char *message) const
{
    BOOST_LOG_SCOPED_THREAD_TAG("id", clientId_.c_str());
    BOOST_LOG_STREAM_WITH_PARAMS(boost::log::trivial::logger::get(),
                                (boost::log::keywords::severity = (boost::log::trivial::severity_level)level)) << message;
}

}  // namespace kaa
