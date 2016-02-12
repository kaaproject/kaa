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

#ifndef MOCKLOGDELIVERYLISTENER_HPP_
#define MOCKLOGDELIVERYLISTENER_HPP_

#include <cstdint>

#include "kaa/log/ILogDeliveryListener.hpp"

namespace kaa {

class MockLogDeliveryListener : public ILogDeliveryListener {
public:
    virtual void onLogDeliverySuccess(const BucketInfo& bucketInfo) { ++onSuccess_; }
    virtual void onLogDeliveryFailure(const BucketInfo& bucketInfo) { ++onFailure_; }
    virtual void onLogDeliveryTimeout(const BucketInfo& bucketInfo) { ++onTimeout_; }

public:
    std::size_t onSuccess_ = 0;
    std::size_t onFailure_ = 0;
    std::size_t onTimeout_ = 0;
};

} /* namespace kaa */

#endif /* MOCKLOGDELIVERYLISTENER_HPP_ */
