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

#ifndef MOCKLOGSTORAGESTATUS_HPP_
#define MOCKLOGSTORAGESTATUS_HPP_

#include "kaa/log/ILogStorageStatus.hpp"

namespace kaa {

class MockLogStorageStatus: public ILogStorageStatus {
public:
    virtual std::size_t getConsumedVolume()
    {
        ++onGetConsumedVolume_;
        return consumedVolume_;
    }

    virtual std::size_t getRecordsCount()
    {
        ++onGetRecordsCount_;
        return recordsCount_;
    }

public:
    std::size_t consumedVolume_;
    std::size_t recordsCount_;

    std::size_t onGetConsumedVolume_ = 0;
    std::size_t onGetRecordsCount_ = 0;
};

} /* namespace kaa */

#endif /* MOCKLOGSTORAGESTATUS_HPP_ */
