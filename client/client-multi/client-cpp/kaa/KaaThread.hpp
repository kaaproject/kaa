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

#ifndef KAA_KAATHREAD_HPP_
#define KAA_KAATHREAD_HPP_

#include "kaa/KaaDefaults.hpp"

//#ifdef KAA_THREADSAFE

#include <mutex>
#include <atomic>
#include <condition_variable>

#define KAA_MUTEX       std::mutex
#define KAA_R_MUTEX     std::recursive_mutex

#define KAA_MUTEX_UNIQUE       std::unique_lock<KAA_MUTEX>
#define KAA_R_MUTEX_UNIQUE     std::unique_lock<KAA_R_MUTEX>

#define KAA_LOCK(mtx)     mtx.lock()
#define KAA_UNLOCK(mtx)   mtx.unlock()

#define KAA_CONDITION_VARIABLE                      std::condition_variable
#define KAA_CONDITION_WAIT(cond, lck)               cond.wait(lck)
#define KAA_CONDITION_WAIT_PRED(cond, lck, pred)    cond.wait(lck, pred)
#define KAA_CONDITION_NOTIFY(cond)                  cond.notify_one()
#define KAA_CONDITION_NOTIFY_ALL(cond)              cond.notify_all()

#define KAA_CONDITION_VARIABLE_DECLARE(name)    KAA_CONDITION_VARIABLE name
#define KAA_MUTEX_DECLARE(name)                 KAA_MUTEX name
#define KAA_MUTEX_MUTABLE_DECLARE(name)         mutable KAA_MUTEX_DECLARE(name)
#define KAA_R_MUTEX_DECLARE(name)               KAA_R_MUTEX name
#define KAA_R_MUTEX_MUTABLE_DECLARE(name)       mutable KAA_R_MUTEX_DECLARE(name)
#define KAA_MUTEX_UNIQUE_DECLARE(name, mtx)     KAA_MUTEX_UNIQUE name(mtx)
#define KAA_R_MUTEX_UNIQUE_DECLARE(name, mtx)   KAA_R_MUTEX_UNIQUE name(mtx)

#ifdef _WIN32
typedef std::atomic<bool> bool_type;
#else
typedef std::atomic_bool bool_type;
#endif
typedef std::atomic_int_fast32_t RequestId;

#define kaa_thread_local thread_local

#if 0

#define KAA_MUTEX
#define KAA_R_MUTEX

#define KAA_MUTEX_UNIQUE
#define KAA_R_MUTEX_UNIQUE

#define KAA_LOCK(mtx)
#define KAA_UNLOCK(mtx)

#define KAA_CONDITION_VARIABLE
#define KAA_CONDITION_WAIT(cond, lck)
#define KAA_CONDITION_WAIT_PRED(cond, lck, pred)
#define KAA_CONDITION_NOTIFY(cond)
#define KAA_CONDITION_NOTIFY_ALL(cond)

#define KAA_CONDITION_VARIABLE_DECLARE(name)
#define KAA_MUTEX_DECLARE(name)
#define KAA_MUTEX_MUTABLE_DECLARE(name)
#define KAA_R_MUTEX_DECLARE(name)
#define KAA_R_MUTEX_MUTABLE_DECLARE(name)
#define KAA_MUTEX_UNIQUE_DECLARE(name, mtx)
#define KAA_R_MUTEX_UNIQUE_DECLARE(name, mtx)

typedef bool bool_type;
typedef std::int32_t RequestId;

#define kaa_thread_local

#endif


#endif /* KAA_KAATHREAD_HPP_ */
