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

#ifndef KAA_LOG_ILOGUPLOADFAILOVERSTRATEGY_HPP_
#define KAA_LOG_ILOGUPLOADFAILOVERSTRATEGY_HPP_

#include "kaa/gen/EndpointGen.hpp"

namespace kaa {

class ILogUploadFailoverStrategy {
public:
    virtual bool isUploadApproved() = 0;

    virtual void onTimeout() = 0;
    virtual void onFailure(LogDeliveryErrorCode code) = 0;

    virtual ~ILogUploadFailoverStrategy()
    {
    }
};

} /* namespace kaa */

#endif /* KAA_LOG_ILOGUPLOADFAILOVERSTRATEGY_HPP_ */
