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


#ifndef FAILOVERCOMMON_HPP_
#define FAILOVERCOMMON_HPP_

#include <cstddef>

namespace kaa {

enum class KaaFailoverReason {
    /**
     * An endpoint could not establish a connection/session to all Bootstrap server.
     */
    ALL_BOOTSTRAP_SERVERS_NA,

    /**
     * An endpoint received the empty list of Operations servers.
     */
    NO_OPERATIONS_SERVERS_RECEIVED,

    /**
     * An endpoint could not establish a connection/session to all Operations server.
     */
    ALL_OPERATIONS_SERVERS_NA,

    /**
     * An endpoint could not establish a connection/session to a Bootstrap server.
     */
    CURRENT_BOOTSTRAP_SERVER_NA,

    /**
     * An endpoint could not establish a connection/session to a Operations server.
     */
    CURRENT_OPERATIONS_SERVER_NA,

    /**
     * No physical connection.
     */
    NO_CONNECTIVITY,

    /**
     * An endpoint provided a server with not enough or incorrect information
     * about itself.
     */
    ENDPOINT_NOT_REGISTERED,

    /**
     * The endpoint credentials were revoked from a server.
     */
    CREDENTIALS_REVOKED
};

enum class FailoverStrategyAction {
    /**
     *  No action.
     */
    NOOP,

    /**
     *  Connect again to a current Bootstrap/Operations server.
     */
    RETRY_CURRENT_SERVER,

    /**
     *  Connect to a next Bootstrap server.
     *
     *  @note In conjunction with KaaFailoverReason::ALL_BOOTSTRAP_SERVERS_NA lead
     *        to switching to the first Bootstrap server.
     */
    USE_NEXT_BOOTSTRAP_SERVER,

    /**
     *  Connect to a next Operations server.
     */
    USE_NEXT_OPERATIONS_SERVER,

    /**
     *  Stop a Kaa client.
     */
    STOP_CLIENT
};

class FailoverStrategyDecision {
public:
    FailoverStrategyDecision(FailoverStrategyAction action, std::size_t retryPeriod = 0)
        : action_(action), retryPeriod_(retryPeriod) {}

    FailoverStrategyAction getAction() const {
        return action_;
    }

    std::size_t getRetryPeriod() const {
        return retryPeriod_;
    }

private:
    FailoverStrategyAction action_;
    std::size_t retryPeriod_ = 0;
};

}

#endif /* FAILOVERCOMMON_HPP_ */
