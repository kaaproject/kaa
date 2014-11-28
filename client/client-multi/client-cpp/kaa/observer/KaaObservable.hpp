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

#ifndef KAA_OBSERVER_KAAOBSERVABLE_HPP_
#define KAA_OBSERVER_KAAOBSERVABLE_HPP_

#include <functional>
#include <unordered_map>
#include <unordered_set>
#include <memory>

#include "kaa/KaaThread.hpp"

namespace kaa {

template<class Signature, class Key, class Function = std::function<Signature>>
class KaaObservable
{
public:
    KaaObservable() : isNotifying_(false) { }
    ~KaaObservable() { }

    bool addCallback(const Key& key, const Function& f)
    {
        if (isNotifying_) {
            KAA_MUTEX_UNIQUE_DECLARE(lock, modificationGuard_);
            auto it = slots_.find(key);
            if (it != slots_.end() && !it->second.isRemoved()) {
                return false;
            }
            slotsToRemove_.erase(key);
            slotsToAdd_.insert(std::make_pair(key, CallbackWrapper(f)));
            return true;
        } else {
            KAA_MUTEX_UNIQUE_DECLARE(lock, mainGuard_);
            return slots_.insert(std::make_pair(key, CallbackWrapper(f))).second;
        }
    }

    void removeCallback(const Key& key)
    {
        if (isNotifying_) {
            KAA_MUTEX_UNIQUE_DECLARE(lock, modificationGuard_);
            slotsToAdd_.erase(key);
            slotsToRemove_.insert(key);

            auto it = slots_.find(key);
            if (it != slots_.end()) {
                it->second.remove();
            }
        } else {
            KAA_MUTEX_UNIQUE_DECLARE(lock, mainGuard_);
            slots_.erase(key);
        }
    }

    template <typename... Args>
    void operator()(Args&... args)
    {
        isNotifying_ = true;
        KAA_MUTEX_UNIQUE_DECLARE(lock, mainGuard_);
        for (auto& pair : slots_) {
            pair.second(args...);
        }
        isNotifying_ = false;
        KAA_MUTEX_UNIQUE_DECLARE(modLock, modificationGuard_);

        for (auto it = slotsToAdd_.begin(); it != slotsToAdd_.end(); ++it) {
            slots_[it->first] = it->second;
        }
        slotsToAdd_.clear();

        for (auto key : slotsToRemove_) {
            slots_.erase(key);
        }
        slotsToRemove_.clear();
    }

private:
    class CallbackWrapper
    {
    public:
        CallbackWrapper() : isRemoved_(false) { }
        CallbackWrapper(const Function& f) : callback_(f), isRemoved_(false) { }
        CallbackWrapper(const CallbackWrapper& o) : callback_(o.callback_), isRemoved_((bool) o.isRemoved_) { }
        CallbackWrapper& operator=(const CallbackWrapper& o)  { callback_ = o.callback_; isRemoved_ = (bool) o.isRemoved_; return *this; }

        template <typename... Args>
        void operator()(Args&... args)
        {
            if (!isRemoved_) {
                callback_(args...);
            }
        }

        bool isRemoved() const { return isRemoved_; }
        void remove() { isRemoved_ = true; }

    private:
        Function callback_;
        bool_type isRemoved_;
    };

    std::unordered_map<Key, CallbackWrapper> slots_;
    std::unordered_map<Key, CallbackWrapper> slotsToAdd_;
    std::unordered_set<Key> slotsToRemove_;

    bool_type isNotifying_;

    KAA_MUTEX_DECLARE(mainGuard_);
    KAA_MUTEX_DECLARE(modificationGuard_);
};

}


#endif /* KAA_OBSERVER_KAAOBSERVABLE_HPP_ */
