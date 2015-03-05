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

#ifndef COMMONVALUETYPES_HPP_
#define COMMONVALUETYPES_HPP_

namespace kaa {

/**
 * Describes types which @link CommonValue @endlink may contain.
 */
enum class CommonValueType {
    COMMON_NULL, COMMON_BOOL, COMMON_INT32, COMMON_INT64, COMMON_FLOAT, COMMON_DOUBLE, COMMON_STRING, COMMON_BYTES,

    COMMON_SCHEMA_INDEPENDENT, // schema dependency marker

    COMMON_FIXED,
    COMMON_ENUM,
    COMMON_ARRAY,
    COMMON_RECORD,

    COMMON_UNKNOWN
};

} // namespace kaa

#endif /* COMMONVALUETYPES_HPP_ */
