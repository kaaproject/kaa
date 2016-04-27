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

#ifndef ABSTRACTTRANSACTABLE_HPP_
#define ABSTRACTTRANSACTABLE_HPP_

#include <map>
#include "kaa/KaaThread.hpp"
#include "kaa/logging/Log.hpp"
#include "kaa/transact/ITransactable.hpp"

namespace kaa {

template<class Container>
class AbstractTransactable : public ITransactable {
public:
    virtual TransactionIdPtr beginTransaction(IKaaClientContext &context_) {
        TransactionIdPtr trxId(new TransactionId);
        KAA_MUTEX_LOCKING("transactionsGuard_")
        KAA_MUTEX_UNIQUE_DECLARE(transactionsLock, transactionsGuard_);
        KAA_MUTEX_LOCKED("transactionsGuard_")

        transactions_.insert(std::make_pair(trxId, Container()));

        return trxId;
    }

    virtual void rollback(TransactionIdPtr trxId, IKaaClientContext &context_) {
        KAA_MUTEX_LOCKING("transactionsGuard_")
        KAA_MUTEX_UNIQUE_DECLARE(transactionsLock, transactionsGuard_);
        KAA_MUTEX_LOCKED("transactionsGuard_")

        auto it = transactions_.find(trxId);
        if (it != transactions_.end()) {
            transactions_.erase(it);
        }
    }

    Container & getContainerByTrxId(TransactionIdPtr trxId, IKaaClientContext &context_) {
        KAA_MUTEX_LOCKING("transactionsGuard_")
        KAA_MUTEX_UNIQUE_DECLARE(transactionsLock, transactionsGuard_);
        KAA_MUTEX_LOCKED("transactionsGuard_")

        auto it = transactions_.find(trxId);

        if (it != transactions_.end()) {
            return it->second;
        } else {
            KAA_LOG_DEBUG(boost::format("Transaction with id %1% was not found. Creating new instance") % trxId->getId());
            return transactions_[trxId];
        }
    }

    virtual ~AbstractTransactable() {}
protected:
    std::map<TransactionIdPtr, Container> transactions_;
    KAA_MUTEX_DECLARE(transactionsGuard_);
};

}

#endif /* ABSTRACTTRANSACTABLE_HPP_ */
