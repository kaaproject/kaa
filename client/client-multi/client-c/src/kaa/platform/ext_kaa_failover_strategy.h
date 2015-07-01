/*
 * ext_failover_strategy.h
 *
 *  Created on: Jun 24, 2015
 *      Author: architec
 */

#ifndef EXT_FAILOVER_STRATEGY_H_
#define EXT_FAILOVER_STRATEGY_H_

#ifndef KAA_FAILOVER_STRATEGY
    #define KAA_FAILOVER_STRATEGY
        typedef struct kaa_failover_strategy_t       kaa_failover_strategy_t;
#endif

#include <stddef.h>
#include "../kaa_error.h"
#include "../platform/time.h"

typedef enum {
    NO_BOOTSTRAP_SERVERS = 0, /*!< No accessible bootstrap servers. */
    NO_OPERATION_SERVERS,    /*!< Initiate log upload. */
    ALL_OPERATION_SERVERS_NA,
    NO_CONNECTIVITY
} kaa_failover_t;

typedef enum {
    KAA_NOOP = 0, /*!< Nothing to be done. */
    KAA_RETRY,    /*!< Initiate log upload. */
    KAA_STOP_APP
} kaa_failover_strategy_action_t;

typedef struct {
    kaa_failover_strategy_action_t action;
    kaa_time_t retry_period;
} kaa_failover_decision_t;

kaa_error_t kaa_failover_strategy_set_decision(kaa_failover_strategy_t *self, kaa_failover_decision_t *decision);

#endif /* EXT_FAILOVER_STRATEGY_H_ */
