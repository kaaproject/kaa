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

#ifndef TRANSPORTTYPE_HPP_
#define TRANSPORTTYPE_HPP_

namespace kaa {

/**
 * TransportTypes - enum with list of all possible transport types which
 * every Channel can support.
 *
 */
enum class TransportType {
    BOOTSTRAP = 0,
    PROFILE,
    CONFIGURATION,
    NOTIFICATION,
    USER,
    EVENT,
    LOGGING
};

}  // namespace kaa


#endif /* TRANSPORTTYPE_HPP_ */
