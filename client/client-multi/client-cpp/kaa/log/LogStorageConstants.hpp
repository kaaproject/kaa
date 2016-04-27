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

#ifndef LOGSTORAGECONSTANTS_HPP_
#define LOGSTORAGECONSTANTS_HPP_

#include <cstdint>
#include <string>

namespace kaa {

class LogStorageConstants {
public:
    static const std::size_t DEFAULT_MAX_BUCKET_SIZE         = 16 * 1024;
    static const std::size_t DEFAULT_MAX_BUCKET_RECORD_COUNT = 256;

    static const std::string DEFAULT_LOG_DB_STORAGE /* logs.db */;
};

} /* namespace kaa */

#endif /* LOGSTORAGECONSTANTS_HPP_ */
