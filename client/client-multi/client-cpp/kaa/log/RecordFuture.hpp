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

#ifndef RECORDFUTURE_HPP_
#define RECORDFUTURE_HPP_

#include <future>
#include <atomic>

#include "kaa/log/RecordInfo.hpp"

namespace kaa {

class RecordFuture {
public:
    RecordFuture(std::future<RecordInfo>&& future)
        : future_(std::move(future)), recordFutureId_(recordFutureCounter++) {}

    RecordFuture(RecordFuture&& recordFuture)
        : future_(std::move(recordFuture.future_)), recordFutureId_(recordFuture.recordFutureId_) {}

    RecordFuture operator=(RecordFuture&& recordFuture) {
        return RecordFuture(std::move(recordFuture));
    }

    RecordFuture(const RecordFuture&) = delete;
    RecordFuture operator=(const RecordFuture&) = delete;

    std::size_t getRecordFutureId() const {
        return recordFutureId_;
    }

    bool operator<(const RecordFuture& recordFuture) const {
        return recordFutureId_ < recordFuture.recordFutureId_;
    }

    bool operator==(const RecordFuture& recordFuture) const {
        return recordFutureId_ == recordFuture.recordFutureId_;
    }

    bool operator!=(const RecordFuture& recordFuture) const {
        return recordFutureId_ != recordFuture.recordFutureId_;
    }

    /*
     * START: Partial future interface.
     */
    RecordInfo get() {
        return future_.get();
    }

    void wait() const {
        future_.wait();
    }

    /*
     * END: Partial future interface.
     */

private:
    typedef std::atomic_size_t RecordFutureCounterType;
    static RecordFutureCounterType recordFutureCounter;

private:
    std::future<RecordInfo> future_;
    std::size_t recordFutureId_;
};

} /* namespace kaa */

namespace std {
template<>
struct hash<kaa::RecordFuture>
{
    typedef kaa::RecordFuture argument_type;
    typedef std::size_t result_type;
    result_type operator()(argument_type const& s) const
    {
        return s.getRecordFutureId();
    }
};
}

#endif /* RECORDFUTURE_HPP_ */
