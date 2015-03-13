/*
 * Copyright 2014-2015 CyberVision, Inc.
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

#ifndef MOCKLOGSTORAGE_HPP_
#define MOCKLOGSTORAGE_HPP_

#include <cstdint>

#include "kaa/log/ILogStorage.hpp"

#include "headers/log/MockLogStorageStatus.hpp"

namespace kaa {

class MockLogStorage: public ILogStorage {
public:
    virtual void addLogRecord(LogRecordPtr record) { ++onAddLogRecord_; }
    virtual ILogStorageStatus& getStatus() { ++onGetStatus_; return storageStatus_; }
    virtual RecordPack getRecordBlock(std::size_t blockSize) { ++onGetRecordBlock_; blockSize_ = blockSize; return recordPack_; }
    virtual void removeRecordBlock(RecordBlockId id) { ++onRemoveRecordBlock_; }
    virtual void notifyUploadFailed(RecordBlockId id) { ++onNotifyUploadFailed_; }

public:
    RecordPack recordPack_;
    MockLogStorageStatus storageStatus_;

    std::size_t blockSize_ = SIZE_MAX;

    std::size_t onAddLogRecord_ = 0;
    std::size_t onGetStatus_ = 0;
    std::size_t onGetRecordBlock_ = 0;
    std::size_t onRemoveRecordBlock_ = 0;
    std::size_t onNotifyUploadFailed_ = 0;
};

} /* namespace kaa */

#endif /* MOCKLOGSTORAGE_HPP_ */
