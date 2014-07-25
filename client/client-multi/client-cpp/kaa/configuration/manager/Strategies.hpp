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

#ifndef STRATEGIES_HPP_
#define STRATEGIES_HPP_

#include "kaa/configuration/manager/AbstractStrategy.hpp"
#include <boost/function.hpp>
#include <list>
#include "kaa/common/types/ICommonArray.hpp"
#include "kaa/common/types/ICommonRecord.hpp"

namespace kaa {

/**
 * Strategy to process UUID field.
 * Subscribes and unsubscribes record by UUID using passed in the ctor routines.
 */
class UuidProcessStrategy : public AbstractStrategy {
public:
    UuidProcessStrategy(
                  boost::function<bool (uuid_t)> isSubscribed
                , boost::function<void (uuid_t, boost::shared_ptr<ICommonRecord>)> subscribe
                , boost::function<void (uuid_t)> unsubscribe)
        {
            isSubscribedFn_ = isSubscribed;
            subscribeFn_ = subscribe;
            unsubscribeFn_ = unsubscribe;
        }

    void run(boost::shared_ptr<ICommonRecord> parent, const std::string &field, const avro::GenericDatum &datum);

private:
    boost::function<bool (uuid_t)> isSubscribedFn_;
    boost::function<void (uuid_t, boost::shared_ptr<ICommonRecord>)> subscribeFn_;
    boost::function<void (uuid_t)> unsubscribeFn_;
};

/**
 * Strategy to process a record.
 * Can be used for processing both a "root" record (data should not be held in
 * the record as the field, but overwritten as separate object) and a simple record as the field.
 */
class RecordProcessStrategy : public AbstractStrategy {
public:
    RecordProcessStrategy(
              boost::function<bool (uuid_t)> isSubscribed
            , boost::function<void (uuid_t, boost::shared_ptr<ICommonRecord>)> subscribe
            , boost::function<void (uuid_t)> unsubscribe, bool isRootRecord = false)
    {
        isSubscribedFn_ = isSubscribed;
        subscribeFn_ = subscribe;
        unsubscribeFn_ = unsubscribe;
        isRootRecord_ = isRootRecord;
    }
    void run(boost::shared_ptr<ICommonRecord> parent, const std::string &field, const avro::GenericDatum &datum);
private:
    boost::function<bool (uuid_t)> isSubscribedFn_;
    boost::function<void (uuid_t, boost::shared_ptr<ICommonRecord>)> subscribeFn_;
    boost::function<void (uuid_t)> unsubscribeFn_;
    std::list<std::pair<uuid_t, boost::shared_ptr<ICommonRecord> > > recordToSubscribe;
    std::list<uuid_t> recordToUnSubscribe;

    bool isRootRecord_;
};

/**
 * Strategy to process an array.
 */
class ArrayProcessStrategy : public AbstractStrategy {
public:
    ArrayProcessStrategy(boost::function<bool (uuid_t)> isSubscribed
            , boost::function<void (uuid_t, boost::shared_ptr<ICommonRecord>)> subscribe
            , boost::function<void (uuid_t)> unsubscribe)
    {
        isSubscribedFn_ = isSubscribed;
        subscribeFn_ = subscribe;
        unsubscribeFn_ = unsubscribe;
    }
    void run(boost::shared_ptr<ICommonRecord> parent, const std::string &field, const avro::GenericDatum &datum);
private:
    static const std::string array_holder_field;
    boost::function<bool (uuid_t)> isSubscribedFn_;
    boost::function<void (uuid_t, boost::shared_ptr<ICommonRecord>)> subscribeFn_;
    boost::function<void (uuid_t)> unsubscribeFn_;
};

/**
 * Strategy to reset an array.
 */
class ArrayResetStrategy : public AbstractStrategy {
public:
    ArrayResetStrategy(boost::function<bool (uuid_t)> isSubscribed
            , boost::function<void (uuid_t)> unsubscribe)
    {
        isSubscribedFn_ = isSubscribed;
        unsubscribeFn_ = unsubscribe;
    }
    void run(boost::shared_ptr<ICommonRecord> parent, const std::string &field, const avro::GenericDatum &datum);
    void unregisterRecord(ICommonRecord &record);
    void unregisterArray(ICommonArray &record);
private:
    boost::function<bool (uuid_t)> isSubscribedFn_;
    boost::function<void (uuid_t)> unsubscribeFn_;
};

/**
 * Strategy to process NULL.
 */
class NullProcessStrategy : public AbstractStrategy {
public:
    void run(boost::shared_ptr<ICommonRecord> parent, const std::string &field, const avro::GenericDatum &datum);
};

/**
 * Strategy to process simple types (string, numbers, enums, fixed fields, byteArray fields)
 */
class CommonProcessStrategy : public AbstractStrategy {
public:
    void run(boost::shared_ptr<ICommonRecord> parent, const std::string &field, const avro::GenericDatum &datum);
};

}  // namespace kaa


#endif /* STRATEGIES_HPP_ */
