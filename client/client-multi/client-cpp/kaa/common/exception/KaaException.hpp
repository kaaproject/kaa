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

#ifndef KAAEXCEPTION_HPP_
#define KAAEXCEPTION_HPP_

#include <boost/format.hpp>
#include <exception>
#include <sstream>

#ifndef NDEBUG
#ifdef _WIN32
#define WIN32_LEAN_AND_MEAN
#define NOMINMAX
#include <Windows.h>
#undef  NOMINMAX
#include <DbgHelp.h>
#undef WIN32_LEAN_AND_MEAN
#else
#include <execinfo.h>
#endif
#endif // NDEBUG

namespace kaa {

class KaaException : public std::exception {
public:
    KaaException() : message_("") {}

    KaaException(boost::format& f) {
        std::stringstream ss;
        ss << "[Kaa OpenSource Project] Instruction failed! Details: \"" << f
           << "\" Original message: " << std::exception::what();
        captureStack(ss);
        message_ = ss.str();
    }

    KaaException(const std::string& message) {
        std::stringstream ss;
        ss << "[Kaa OpenSource Project] Instruction failed! Details: \"" << message
           << "\" Original message: " << std::exception::what();
        captureStack(ss);
        message_ = ss.str();
    }

    KaaException(const std::exception& e) {
        std::stringstream ss;
        ss << "[Kaa OpenSource Project] Instruction failed! Details: \"" << e.what();
        captureStack(ss);
        message_ = ss.str();
    }

    virtual const char * what() const throw() {
        return message_.c_str();
    }

    virtual ~KaaException() noexcept = default;

private:
    std::string message_;

    void captureStack(std::stringstream& ss)  {
#ifndef NDEBUG
         void *trace[16];
         int i, trace_size = 0;
#ifdef _WIN32
         SYMBOL_INFO  * symbol;
         HANDLE process;
         process = GetCurrentProcess();
         SymInitialize( process, NULL, TRUE );
         trace_size = CaptureStackBackTrace( 0, 16, trace, NULL );
         symbol = ( SYMBOL_INFO * )calloc( sizeof( SYMBOL_INFO ) + 256 * sizeof( char ), 1 );
         symbol->MaxNameLen   = 255;
         symbol->SizeOfStruct = sizeof( SYMBOL_INFO );
#else
         char **messages = (char **)nullptr;
         trace_size = backtrace(trace, 16);
         messages = backtrace_symbols(trace, trace_size);
#endif
         ss << std::endl << "Backtrace: " << std::endl;
         for (i = 0; i < trace_size; ++i) {
#ifdef _WIN32
             SymFromAddr( process, ( DWORD64 )( trace[ i ] ), 0, symbol );
             ss << "[" << trace_size - i - 1 << "] " << symbol->Name << " - " << boost::format("0x%0X")%symbol->Address << std::endl;
#else
             ss << "[" << i << "] " << messages[i] << std::endl;
#endif
         }
#ifdef _WIN32
         free( symbol );
#endif
#else
         static_cast<void>(ss);
#endif // NDEBUG
     }
};

}  // namespace kaa


#endif /* KAAEXCEPTION_HPP_ */
