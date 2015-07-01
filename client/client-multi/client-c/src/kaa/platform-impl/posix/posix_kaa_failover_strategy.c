
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

#include <time.h>
#include <stdint.h>
#include <stdio.h>
#include "../../kaa_common.h"
#include "../../utilities/kaa_mem.h"
#include "../../utilities/kaa_log.h"
#include "../../kaa_bootstrap_manager.h"
#include "posix_kaa_failover_strategy.h"

#define KAA_DEFAULT_RETRY_PERIOD    5


typedef struct {
    kaa_access_point_t *acess_point;
    kaa_server_type_t server;
    kaa_transport_protocol_id_t protocol_id;
    kaa_time_t next_execution_time;
} failover_meta_info;

struct kaa_failover_strategy_t {
    kaa_failover_decision_t decision;
    failover_meta_info      metadata;
    kaa_logger_t            *logger;
};

kaa_error_t kaa_failover_strategy_create(kaa_failover_strategy_t** strategy, kaa_logger_t *logger)
{
    KAA_RETURN_IF_NIL2(strategy, logger, KAA_ERR_BADPARAM);
    *strategy = (kaa_failover_strategy_t *) KAA_CALLOC(1, sizeof(kaa_failover_strategy_t));
    KAA_RETURN_IF_NIL(*strategy, KAA_ERR_NOMEM);
    (*strategy)->decision.action = KAA_RETRY;
    (*strategy)->decision.retry_period = KAA_DEFAULT_RETRY_PERIOD;
    (*strategy)->logger = logger;
    return KAA_ERR_NONE;
}

void kaa_set_failover_metainfo(kaa_failover_strategy_t *strategy, kaa_access_point_t *acess_point
                    , kaa_server_type_t server, kaa_transport_protocol_id_t *protocol_id, kaa_time_t next_execution_time)
{
    KAA_RETURN_IF_NIL(strategy, );

    if (acess_point)
        strategy->metadata.acess_point = acess_point;

        strategy->metadata.protocol_id = *protocol_id;
        strategy->metadata.server = server;
        strategy->metadata.next_execution_time = next_execution_time;
}

void kaa_get_failover_metainfo(kaa_failover_strategy_t *self, kaa_access_point_t **acess_point
                             , kaa_server_type_t *server, kaa_transport_protocol_id_t *protocol_id, kaa_time_t *next_execution_time)
{
    KAA_RETURN_IF_NIL4(self, acess_point, protocol_id, next_execution_time, );

    *acess_point = self->metadata.acess_point;
    *protocol_id = self->metadata.protocol_id;
    *server = self->metadata.server;
    *next_execution_time = self->metadata.next_execution_time;
}

kaa_failover_decision_t kaa_failover_strategy_get_decision(kaa_failover_strategy_t* strategy)
{
    return strategy->decision;
}

void kaa_failover_execute(kaa_failover_strategy_t* self, kaa_access_point_t *access_point, kaa_server_type_t server, kaa_transport_protocol_id_t *protocol_id)
{
    KAA_RETURN_IF_NIL(self, );
    switch (self->decision.action) {
    case KAA_NOOP:
        KAA_LOG_INFO(self->logger, KAA_ERR_NONE, "Nothing to be done...");
        break;
    case KAA_RETRY:
        KAA_LOG_INFO(self->logger, KAA_ERR_NONE, "Going to retry in %u seconds...", self->decision.retry_period);
        kaa_set_failover_metainfo(self, access_point, server, protocol_id, KAA_TIME() + self->decision.retry_period);
        break;
    case KAA_STOP_APP:
        KAA_LOG_INFO(self->logger, KAA_ERR_NONE, "Stopping application according to the failover strategy...");
        exit(0);
    }
}

void kaa_failover_strategy_reset_next_execution_time(kaa_failover_strategy_t *self)
{
    KAA_RETURN_IF_NIL(self, );
    self->metadata.next_execution_time = 0;
}
