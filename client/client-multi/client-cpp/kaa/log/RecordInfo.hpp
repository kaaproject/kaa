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

#ifndef RECORDINFO_HPP_
#define RECORDINFO_HPP_

#include <cstdint>

#include "kaa/log/BucketInfo.hpp"
#include "kaa/utils/TimeUtils.hpp"

namespace kaa {

/**
 * @brief Describes unique log record delivery info.
 */
class RecordInfo {
public:
    /**
     * @brief Returns the parent bucket.
     *
     * @return The bucket info.
     */
    BucketInfo getBucketInfo() const {
        return bucketInfo_;
    }

    void setBucketInfo(const BucketInfo& bucketInfo) {
        bucketInfo_ = bucketInfo;
    }

    /**
     * @brief Returns the timestamp indicating when log record was scheduled for delivery.
     *
     * @return The timestamp in milliseconds.
     */
    std::size_t getRecordAddedTimestampMs() const {
        return recordAddedTimestampMs_;
    }

    void setRecordAddedTimestampMs(std::size_t recordAddedTimestampMs) {
        recordAddedTimestampMs_ = recordAddedTimestampMs;
    }

    /**
     * @brief Returns the total spent time to deliver log record in milliseconds.
     *
     * @return The log delivery time in milliseconds.
     */
    std::size_t getRecordDeliveryTimeMs() const {
        return recordDeliveryTimeMs_;
    }

    void setRecordDeliveryTimeMs(std::size_t recordDeliveryTimeMs) {
        recordDeliveryTimeMs_ = recordDeliveryTimeMs;
    }

private:
    BucketInfo     bucketInfo_;
    std::size_t    recordAddedTimestampMs_ = TimeUtils::getCurrentTimeInMs();
    std::size_t    recordDeliveryTimeMs_ = 0;

};

} /* namespace kaa */

#endif /* RECORDINFO_HPP_ */
