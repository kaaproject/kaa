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

#ifndef TRANSPORTNOTFOUNDEXCEPTION_HPP_
#define TRANSPORTNOTFOUNDEXCEPTION_HPP_

#include "kaa/common/exception/KaaException.hpp"

namespace kaa {

/**
 * @brief The exception is thrown to indicate that Kaa SDK isn't fully initialized.
 *
 * In normal workflow this kind of errors shouldn't happen.
 */
class TransportNotFoundException: public KaaException {
public:
    TransportNotFoundException(boost::format f)
        : KaaException(f) {}

    TransportNotFoundException(const std::string& message)
        : KaaException(message) {}
};

} /* namespace kaa */

#endif /* TRANSPORTNOTFOUNDEXCEPTION_HPP_ */
