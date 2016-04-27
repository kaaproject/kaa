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

#ifndef LOGRECORD_HPP_
#define LOGRECORD_HPP_

#include <vector>
#include <cstdint>

#include "kaa/log/gen/LogDefinitions.hpp"
#include "kaa/common/AvroByteArrayConverter.hpp"
#include "kaa/common/exception/KaaException.hpp"

namespace kaa {

class LogRecord {
public:
    LogRecord(const KaaUserLogRecord& record)
    {
        AvroByteArrayConverter<KaaUserLogRecord> converter;  // TODO: make converter thread local when it would be possible
        converter.toByteArray(record, encodedRecord_);
    }

    LogRecord(const std::uint8_t *data, size_t size)
    {
        if (!data || !size) {
            throw KaaException("Null serialized log data");
        }

        encodedRecord_.assign(data, data + size);
    }

    std::vector<std::uint8_t>& getData() { return encodedRecord_; }

    std::vector<std::uint8_t>&& getRvalueData(){ return std::move(encodedRecord_); }

    std::size_t getSize() const { return encodedRecord_.size(); }

private:
    std::vector<std::uint8_t> encodedRecord_;
};

}  // namespace kaa

#endif /* LOGRECORD_HPP_ */
