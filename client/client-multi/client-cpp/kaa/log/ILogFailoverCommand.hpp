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

#ifndef ILOGFAILOVERCOMMAND_HPP_
#define ILOGFAILOVERCOMMAND_HPP_

#include <cstddef>

#include "kaa/log/IAccessPointCommand.hpp"

namespace kaa {

class ILogFailoverCommand : public IAccessPointCommand {
public:
    virtual void retryLogUpload() = 0;

    virtual void retryLogUpload(std::size_t delay) = 0;

    virtual ~ILogFailoverCommand() {}
};

} /* namespace kaa */

#endif /* ILOGFAILOVERCOMMAND_HPP_ */
