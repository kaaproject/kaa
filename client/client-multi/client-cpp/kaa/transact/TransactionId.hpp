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

#ifndef TRANSACTIONID_HPP_
#define TRANSACTIONID_HPP_

#include "kaa/common/UuidGenerator.hpp"
#include <boost/shared_ptr.hpp>
#include <string>

namespace kaa {

class TransactionId {
public:
    TransactionId() : id_(UuidGenerator::generateUuid()) {}
    TransactionId(const TransactionId & trxId) : id_(trxId.id_) {}
    TransactionId(const std::string & id) {
        this->id_ = id;
    }
    const std::string & getId() {
        return this->id_;
    }

private:
    std::string id_;
};

typedef boost::shared_ptr<TransactionId> TransactionIdPtr;

}

#endif /* TRANSACTIONID_HPP_ */
