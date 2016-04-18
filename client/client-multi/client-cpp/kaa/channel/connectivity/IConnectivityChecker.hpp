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

#ifndef ICONNECTIVITYCHECKER_HPP_
#define ICONNECTIVITYCHECKER_HPP_

#include <memory>

namespace kaa {

/**
 * Interface for a platform-dependent checker of a network connectivity.
 */
class IConnectivityChecker {
public:
    /**
     * Check whether network connectivity exists.
     *
     * @return True if connection exists, false otherwise.
     */
    virtual bool checkConnectivity() = 0;

    virtual ~IConnectivityChecker() = default;
};

typedef std::shared_ptr<IConnectivityChecker> ConnectivityCheckerPtr;

} /* namespace kaa */

#endif /* ICONNECTIVITYCHECKER_HPP_ */
