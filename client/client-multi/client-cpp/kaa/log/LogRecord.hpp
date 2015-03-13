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

#ifndef LOGRECORD_HPP_
#define LOGRECORD_HPP_

#include <memory>
#include <cstdint>

#include "kaa/KaaThread.hpp"
#include "kaa/gen/EndpointGen.hpp"
#include "kaa/log/ILogCollector.hpp"
#include "kaa/common/AvroByteArrayConverter.hpp"

namespace kaa {

class LogRecord {
public:
    LogRecord(const KaaUserLogRecord& record)
    {
        converter_.toByteArray(record, serializedLog_.data);
    }

    const std::vector<std::uint8_t>& getData() const { return serializedLog_.data; }
    size_t getSize() const { return serializedLog_.data.size(); }

    const LogEntry& getLogEntry() { return serializedLog_; }

private:
    static kaa_thread_local AvroByteArrayConverter<KaaUserLogRecord> converter_;

private:
    LogEntry serializedLog_;
};

typedef std::shared_ptr<LogRecord> LogRecordPtr;

}  // namespace kaa

#endif /* LOGRECORD_HPP_ */
