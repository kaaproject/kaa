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

#ifndef TRANSACTIONID_HPP_
#define TRANSACTIONID_HPP_

#include "kaa/common/UuidGenerator.hpp"
#include <string>
#include <memory>

namespace kaa {

/**
 * Class representing unique transaction id for transactions initiated using
 * @link kaa::ITransactable @endlink.<br>
 *
 * @see kaa::ITransactable
 * @see kaa::AbstractTransactable
 * @see kaa::IEventManager
 */
class TransactionId {
public:
    /**
     * Default constructor<br>
     * <br>
     * Generates random id object.
     */
    TransactionId() : id_(UuidGenerator::generateUuid()) {}

    /**
     * Copy constructor<br>
     * <br>
     * Copies TransactionId object.
     */
    TransactionId(const TransactionId & trxId) : id_(trxId.id_) {}

    /**
     * Constructs object from string value.
     */
    TransactionId(const std::string & id) {
        this->id_ = id;
    }

    /**
     * Get string id representation.
     */
    const std::string & getId() {
        return this->id_;
    }

private:
    std::string id_;
};

typedef std::shared_ptr<TransactionId> TransactionIdPtr;

}

#endif /* TRANSACTIONID_HPP_ */
