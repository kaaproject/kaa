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

#ifndef ITRANSACTABLE_HPP_
#define ITRANSACTABLE_HPP_

#include "kaa/transact/TransactionId.hpp"

namespace kaa {

/**
 * Interface to provide transactional behavior.<br>
 *
 * @see kaa::TransactionId
 */
class ITransactable {
public:

    /**
     * Create new transaction.
     *
     * @return TransactionId object which must be used to submit or rollback the transaction.
     */
    virtual TransactionIdPtr beginTransaction() = 0;

    /**
     * Finish the transaction.
     *
     * @param trxId Identifier of the transaction which must be finished.
     */
    virtual void commit(TransactionIdPtr trxId) = 0;

    /**
     * Rollback changes for given transaction.
     *
     * @param trxId Identifier of the transaction which must be removed withot applying changes.
     */
    virtual void rollback(TransactionIdPtr trxId) = 0;

    virtual ~ITransactable()
    {
    }
};

}

#endif /* ITRANSACTABLE_HPP_ */
