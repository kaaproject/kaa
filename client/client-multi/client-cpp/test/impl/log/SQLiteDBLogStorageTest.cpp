/*
 * Copyright 2014-2015 CyberVision, Inc.
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

#include <boost/test/unit_test.hpp>

#include <string>

#include "../../../kaa/log/SQLiteDBLogStorage.hpp"
#include "kaa/log/LogRecord.hpp"
#include "kaa/common/exception/KaaException.hpp"

namespace kaa {

BOOST_AUTO_TEST_SUITE(FileLogStorageTestSuite)

BOOST_AUTO_TEST_CASE(AddLogRecord)
{
    std::string logStorageName("logs.db");
    SQLiteDBLogStorage logStorage(logStorageName);

    KaaUserLogRecord record;
    record.logdata = "veryfdsfdksfjdklsfjdsfkldjsfkldsjfd;sjfdklsfjdsklfjs";

    LogRecordPtr rp(new LogRecord(record));
    logStorage.addLogRecord(rp);
    logStorage.addLogRecord(rp);
    logStorage.addLogRecord(rp);

    logStorage.getRecordBlock(110);

//    logStorage.removeRecordBlock(1);
}

BOOST_AUTO_TEST_SUITE_END()

}
