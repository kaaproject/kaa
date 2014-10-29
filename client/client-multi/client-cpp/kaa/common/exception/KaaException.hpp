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

#ifndef KAAEXCEPTION_HPP_
#define KAAEXCEPTION_HPP_

#include <boost/format.hpp>
#include <exception>
#include <sstream>
#include <execinfo.h>

namespace kaa {

class KaaException : public std::exception {
public:
    KaaException() : message_("") {}

    KaaException(boost::format f) {
        std::stringstream ss;
        ss << "[Kaa OpenSource Project] Instruction failed! Details: \"" << f
           << "\" Original message: " << std::exception::what();
        void *trace[16];
        char **messages = (char **)nullptr;
        int i, trace_size = 0;
        trace_size = backtrace(trace, 16);
        messages = backtrace_symbols(trace, trace_size);
        ss << std::endl << "Backtrace: " << std::endl;
        for (i = 0; i < trace_size; ++i) {
            ss << "[" << i << "] " << messages[i] << std::endl;
        }
        message_ = ss.str();
    }

    KaaException(const std::string &message) {
        std::stringstream ss;
        ss << "[Kaa OpenSource Project] Instruction failed! Details: \"" << message
           << "\" Original message: " << std::exception::what();
        void *trace[16];
        char **messages = (char **)nullptr;
        int i, trace_size = 0;
        trace_size = backtrace(trace, 16);
        messages = backtrace_symbols(trace, trace_size);
        ss << std::endl << "Backtrace: " << std::endl;
        for (i = 0; i < trace_size; ++i) {
            ss << "[" << i << "] " << messages[i] << std::endl;
        }
        message_ = ss.str();
    }

    virtual const char * what() const throw () {
        return message_.c_str();
    }

    virtual ~KaaException() throw () {}

private:
    std::string message_;
};

}  // namespace kaa


#endif /* KAAEXCEPTION_HPP_ */
