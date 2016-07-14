#ifndef FAILOVERCOMMON_HPP_
#define FAILOVERCOMMON_HPP_

#include <cstddef>

namespace kaa {

enum class KaaFailoverReason {
    /**
     * An endpoint could not establish a connection/session to all Bootstrap server.
     */
    ALL_BOOTSTRAP_SERVERS_NA = 0,

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
    NOOP = 0,

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
