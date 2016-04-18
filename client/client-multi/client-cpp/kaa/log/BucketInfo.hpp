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

#ifndef BUCKETINFO_HPP_
#define BUCKETINFO_HPP_

#include <cstdint>

namespace kaa {

/**
 * @brief Describes a unique log bucket.
 *
 * By uniqueness it means that any of log records in a bucket is not repeated in any other log bucket.
 *
 * @note The id should be unique across all available log buckets.
 */
class BucketInfo {
public:
    /**
     * @brief Constructs an empty @c BucketInfo object.
     */
    BucketInfo() {}

    /**
     * @brief Constructs the @c BucketInfo object which contains a useful information about a log bucket.
     *
     * @param[in] bucketId The id of a bucket. @note The id should be unique across all available log buckets.
     * @param[in] logCount The number of logs the bucket contains.
     */
    BucketInfo(std::int32_t bucketId, std::size_t logCount)
        : bucketId_(bucketId), logCount_(logCount) {}

    /**
     * @brief Returns the id of a bucket.
     *
     * @note The id should be unique across all available log buckets.
     *
     * @return The id of a bucket.
     */
    std::int32_t getBucketId() const {
        return bucketId_;
    }

    /**
     * @return The number of logs a bucket contains.
     */
    std::size_t getLogCount() const {
        return logCount_;
    }

    bool operator<(const BucketInfo& info) const {
        return bucketId_ < info.bucketId_;
    }

    bool operator==(const BucketInfo& info) const {
        return bucketId_ == info.bucketId_;
    }

    bool operator!=(const BucketInfo& info) const {
        return bucketId_ != info.bucketId_;
    }

private:
    std::int32_t    bucketId_ = 0;
    std::size_t     logCount_ = 0;
};
} /* namespace kaa */

#endif /* BUCKETINFO_HPP_ */
