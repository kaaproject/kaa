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

#ifndef ILOGDELIVERYLISTENER_HPP_
#define ILOGDELIVERYLISTENER_HPP_

#include <memory>

namespace kaa {

class BucketInfo;

/**
 * @brief Interface of a log delivery listener.
 */
class ILogDeliveryListener {
public:
    /**
     * @brief Callback is used when a log bucket is delivered to a server.
     * @param[in] bucketInfo The information about a log bucket.
     * @see BucketInfo
     */
    virtual void onLogDeliverySuccess(const BucketInfo& bucketInfo) = 0;

    /**
     * @brief Callback is used when a log bucket is not delivered due to some failure.
     * @note The bucket will be re-sent to a server.
     * @param[in] bucketInfo The information about a log bucket.
     * @see BucketInfo
     */
    virtual void onLogDeliveryFailure(const BucketInfo& bucketInfo) = 0;

    /**
     * @brief Callback is used when a timeout is occurred while waiting a delivery status from a server.
     * @note The bucket will be re-sent to a server.
     * @param[in] bucketInfo The information about a log bucket.
     * @see BucketInfo
     */
    virtual void onLogDeliveryTimeout(const BucketInfo& bucketInfo) = 0;

    virtual ~ILogDeliveryListener() {}
};

typedef std::shared_ptr<ILogDeliveryListener> ILogDeliveryListenerPtr;

} /* namespace kaa */

#endif /* ILOGDELIVERYLISTENER_HPP_ */
