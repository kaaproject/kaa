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

#ifndef LOGBUCKET_HPP_
#define LOGBUCKET_HPP_

#include <list>
#include <cstdint>
#include <utility>

#include "kaa/log/LogRecord.hpp"

namespace kaa {

/**
 * @brief The helper class which is used to transfer logs from @c LogStorage to @c LogCollector.
 *
 * @note The id should be unique across all available log buckets.
 */
class LogBucket {
public:

    /**
     * @brief Constructs empty @c LogBucket object.
     */
    LogBucket() {}

    /**
     * @brief Constructs @c LogBucket object.
     *
     * @param[in] id      The unique log bucket id.
     * @param[in] records Log records.
     */
    LogBucket(std::int32_t id, std::list<LogRecord>&& records)
        : id_(id), logRecords_(std::move(records)) { }

    /**
     * @brief Constructs @c LogBucket object.
     *
     * @param[in] id      The unique log bucket id.
     * @param[in] records Log records.
     */
    LogBucket(std::int32_t id, const std::list<LogRecord>& records)
        : id_(id), logRecords_(records) { }

    /**
     * @brief Returns a log bucket id.
     *
     * A log bucket id should be unique across all available buckets.
     *
     * @return The log bucket id.
     */
    std::int32_t getBucketId() const {
        return id_;
    }

    /**
     * @brief Returns log records of the bucket.
     *
     * @return The list of log records.
     */
    std::list<LogRecord>& getRecords() {
        return logRecords_;
    }

private:
    std::int32_t            id_ = 0;
    std::list<LogRecord>    logRecords_;
};

} /* namespace kaa */

#endif /* LOGBUCKET_HPP_ */
