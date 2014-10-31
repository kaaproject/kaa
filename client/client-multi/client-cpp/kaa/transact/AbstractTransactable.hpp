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

#ifndef ABSTRACTTRANSACTABLE_HPP_
#define ABSTRACTTRANSACTABLE_HPP_

#include <map>
#include "kaa/transact/ITransactable.hpp"

namespace kaa {

template<class Container>
class AbstractTransactable : public ITransactable {
public:
    virtual TransactionIdPtr beginTransaction() {
        TransactionIdPtr trxId(new TransactionId);
        do {
            KAA_LOCK(transactionsGuard_);
            transactions_.insert(std::make_pair(trxId, Container()));
            KAA_UNLOCK(transactionsGuard_);
        } while (0);
        return trxId;
    }

    virtual void rollback(TransactionIdPtr trxId) {
        KAA_LOCK(transactionsGuard_);
        auto it = transactions_.find(trxId);
        if (it != transactions_.end()) {
            transactions_.erase(it);
        }
        KAA_UNLOCK(transactionsGuard_);
    }

    Container & getContainerByTrxId(TransactionIdPtr trxId) {
        return transactions_[trxId];
    }

    virtual ~AbstractTransactable() {}

protected:
    KAA_R_MUTEX_DECLARE(pendingEventsGuard_);
    std::map<TransactionIdPtr, Container> transactions_;
};

}

#endif /* ABSTRACTTRANSACTABLE_HPP_ */
