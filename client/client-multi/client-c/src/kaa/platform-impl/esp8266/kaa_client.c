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

#include <stdint.h>
#include <stdio.h>

#include <kaa.h>
#include <kaa_context.h>
#include <platform/kaa_client.h>
#include "platform/ext_transport_channel.h"
#include "utilities/kaa_mem.h"
#include "utilities/kaa_log.h"
#include <platform/time.h>
#include "kaa_channel_manager.h"
#include "platform-impl/common/kaa_tcp_channel.h"
#include "platform-impl/common/ext_log_upload_strategies.h"
#include "platform/ext_kaa_failover_strategy.h"

#ifndef KAA_DISABLE_FEATURE_LOGGING
#include "kaa_logging.h"
#include "kaa_logging_private.h"

static kaa_error_t kaa_log_collector_init(kaa_client_t *kaa_client);
#endif

#include "kaa_private.h"

typedef enum {
    KAA_CLIENT_CHANNEL_STATE_CONNECTED = 0,
    KAA_CLIENT_CHANNEL_STATE_NOT_CONNECTED
} kaa_client_channel_state_t;

typedef enum {
    KAA_CLIENT_CHANNEL_TYPE_BOOTSTRAP = 0,
    KAA_CLIENT_CHANNEL_TYPE_OPERATIONS
} kaa_client_channel_type_t;

static kaa_extension_id BOOTSTRAP_SERVICE[] = { KAA_EXTENSION_BOOTSTRAP };
static const int BOOTSTRAP_SERVICE_COUNT = sizeof(BOOTSTRAP_SERVICE) / sizeof(kaa_extension_id);

static kaa_extension_id OPERATIONS_SERVICES[] = { KAA_EXTENSION_PROFILE
                                             , KAA_EXTENSION_USER
#ifndef KAA_DISABLE_FEATURE_CONFIGURATION
                                             , KAA_EXTENSION_CONFIGURATION
#endif
#ifndef KAA_DISABLE_FEATURE_EVENTS
                                             , KAA_EXTENSION_EVENT
#endif
#ifndef KAA_DISABLE_FEATURE_LOGGING
                                             , KAA_EXTENSION_LOGGING
#endif
#ifndef KAA_DISABLE_FEATURE_NOTIFICATION
                                             , KAA_EXTENSION_NOTIFICATION
#endif
                                             };
static const int OPERATIONS_SERVICES_COUNT = sizeof(OPERATIONS_SERVICES) / sizeof(kaa_extension_id);

/* Logging constraints */
#define MAX_LOG_COUNT           SIZE_MAX
#define MAX_LOG_BUCKET_SIZE     (KAA_TCP_CHANNEL_OUT_BUFFER_SIZE >> 3)

_Static_assert(MAX_LOG_BUCKET_SIZE, "Maximum bucket size cannot be 0!");

struct kaa_client_t {
    kaa_context_t                       *context;
    bool                                operate;

    kaa_transport_channel_interface_t   channel;
    kaa_client_channel_state_t          channel_state;
    uint32_t                            channel_id;
    bool                                channel_socket_closed;

    bool                                bootstrap_complete;

    external_process_fn                 external_process;
    void                                *external_process_context;
    time_t                              external_process_max_delay;
    time_t                              external_process_last_call;

#ifndef KAA_DISABLE_FEATURE_LOGGING
    void                                *log_storage_context;
    void                                *log_upload_strategy_context;
#endif
};

static kaa_error_t kaa_client_init_channel(kaa_client_t *kaa_client, kaa_client_channel_type_t channel_type);
static kaa_error_t kaa_client_deinit_channel(kaa_client_t *kaa_client);
static kaa_error_t on_kaa_tcp_channel_event(void *context, kaa_tcp_channel_event_t event_type, kaa_fd_t fd);

#define KAA_RETURN_IF_ERR_MSG(E, msg) \
        { if(E) { printf("Error %i. \"%s\"\n",(E), (msg)); return (E); } }


kaa_error_t on_kaa_tcp_channel_event(void *context, kaa_tcp_channel_event_t event_type, kaa_fd_t fd)
{
    (void)fd;
    KAA_RETURN_IF_NIL(context, KAA_ERR_BADPARAM);

    if (event_type == SOCKET_DISCONNECTED) {
        ((kaa_client_t *)context)->channel_socket_closed = true;
    }

    return KAA_ERR_NONE;
}

kaa_error_t kaa_client_create(kaa_client_t **client, kaa_client_props_t *props) {
    (void)props;
    KAA_RETURN_IF_NIL(client, KAA_ERR_BADPARAM);

    kaa_client_t *self = (kaa_client_t*)KAA_CALLOC(1, sizeof(kaa_client_t));
    KAA_RETURN_IF_NIL(self, KAA_ERR_NOMEM);
    kaa_error_t error_code = kaa_init(&self->context);

    if(error_code) {
        printf("Error initialising kaa_context\n");
        kaa_client_destroy(self);
        return error_code;
    }

    self->operate = true;

#ifndef KAA_DISABLE_FEATURE_LOGGING
    error_code = kaa_log_collector_init(self);
    if (error_code) {
        KAA_LOG_ERROR(self->context->logger, error_code, "Failed to init Kaa log collector, error %d", error_code);
        kaa_client_destroy(self);
        return error_code;
    }
#endif

    KAA_LOG_INFO(self->context->logger,KAA_ERR_NONE, "Kaa client initiallized");
    *client = self;
    return error_code;
}

kaa_context_t *kaa_client_get_context(kaa_client_t *client)
{
    KAA_RETURN_IF_NIL(client, NULL);
    return client->context;
}

static uint16_t get_poll_timeout(kaa_client_t *kaa_client)
{
    KAA_RETURN_IF_NIL(kaa_client, 0);

    uint16_t select_timeout;
    kaa_tcp_channel_get_max_timeout(&kaa_client->channel, &select_timeout);


    if ((kaa_client->external_process_max_delay > 0) && (select_timeout > kaa_client->external_process_max_delay)) {
        select_timeout = kaa_client->external_process_max_delay;
    }

    if ((KAA_BOOTSTRAP_RESPONSE_PERIOD > 0) && (select_timeout > KAA_BOOTSTRAP_RESPONSE_PERIOD)) {
        select_timeout = KAA_BOOTSTRAP_RESPONSE_PERIOD;
    }

    return select_timeout;
}


kaa_error_t kaa_client_process_channel_connected(kaa_client_t *kaa_client)
{
    KAA_RETURN_IF_NIL(kaa_client, KAA_ERR_BADPARAM);

    fd_set read_fds, write_fds, except_fds;
    struct timeval select_tv = { get_poll_timeout(kaa_client), 0 };
    int channel_fd = 0;

    FD_ZERO(&read_fds);
    FD_ZERO(&write_fds);
    FD_ZERO(&except_fds);

    kaa_error_t error_code = kaa_tcp_channel_get_descriptor(&kaa_client->channel, &channel_fd);
    if(error_code) KAA_LOG_ERROR(kaa_client->context->logger, error_code, "No descriptor provided!");

    if (kaa_tcp_channel_is_ready(&kaa_client->channel, FD_READ))
        FD_SET(channel_fd, &read_fds);
    if (kaa_tcp_channel_is_ready(&kaa_client->channel, FD_WRITE))
        FD_SET(channel_fd, &write_fds);

    int poll_result = select(channel_fd + 1, &read_fds, &write_fds, NULL, &select_tv);
    if (poll_result == 0) {
       error_code =  kaa_tcp_channel_check_keepalive(&kaa_client->channel);
    } else if (poll_result > 0) {
        if (channel_fd >= 0) {
            if (FD_ISSET(channel_fd, &read_fds)) {
                KAA_LOG_DEBUG(kaa_client->context->logger, KAA_ERR_NONE,
                        "Processing IN event for the client socket %d", channel_fd);
                error_code = kaa_tcp_channel_process_event(&kaa_client->channel, FD_READ);
                if (error_code) {
                    KAA_LOG_ERROR(kaa_client->context->logger, error_code,
                            "Failed to process IN event for the client socket %d", channel_fd);
                }
            }
            if (FD_ISSET(channel_fd, &write_fds)) {
                KAA_LOG_DEBUG(kaa_client->context->logger, KAA_ERR_NONE,
                        "Processing OUT event for the client socket %d", channel_fd);

                error_code = kaa_tcp_channel_process_event(&kaa_client->channel, FD_WRITE);
                if (error_code) {
                    KAA_LOG_ERROR(kaa_client->context->logger, error_code,
                            "Failed to process OUT event for the client socket %d", channel_fd);
                }
            }
        }
    } else {
        KAA_LOG_ERROR(kaa_client->context->logger, KAA_ERR_BAD_STATE, "Failed to poll descriptors");
        error_code = KAA_ERR_BAD_STATE;
    }

    if (kaa_client->channel_socket_closed) {
        KAA_LOG_INFO(kaa_client->context->logger, KAA_ERR_NONE,
                "Channel [0x%08X] connection terminated", kaa_client->channel_id);

        kaa_client->channel_state = KAA_CLIENT_CHANNEL_STATE_NOT_CONNECTED;
        if (error_code != KAA_ERR_EVENT_NOT_ATTACHED) {
            kaa_client_deinit_channel(kaa_client);
        }
    }

    return error_code;
}

kaa_error_t kaa_client_process_channel_disconnected(kaa_client_t *kaa_client)
{
    KAA_RETURN_IF_NIL(kaa_client, KAA_ERR_BADPARAM);

    kaa_error_t error_code = kaa_tcp_channel_check_keepalive(&kaa_client->channel);
    if (error_code) {
        KAA_LOG_ERROR(kaa_client->context->logger, error_code, "Failed to connect channel [0x%08X]", kaa_client->channel_id);
    } else {
        KAA_LOG_TRACE(kaa_client->context->logger, error_code, "Channel [0x%08X] successfully connected", kaa_client->channel_id);
        kaa_client->channel_state = KAA_CLIENT_CHANNEL_STATE_CONNECTED;
    }

    return error_code;
}

kaa_error_t kaa_client_start(kaa_client_t *kaa_client,
                             external_process_fn external_process,
                             void *external_process_context,
                             kaa_time_t max_delay) {
    KAA_RETURN_IF_NIL(kaa_client, KAA_ERR_BADPARAM);

    kaa_error_t error_code = kaa_check_readiness(kaa_client->context);
    if (error_code != KAA_ERR_NONE) {
        KAA_LOG_ERROR(kaa_client->context->logger, error_code, "Cannot start Kaa client: Kaa context is not fully initialized");
        return error_code;
    }

    kaa_client->external_process = external_process;
    kaa_client->external_process_context = external_process_context;
    kaa_client->external_process_max_delay = max_delay;
    kaa_client->external_process_last_call = KAA_TIME();

    KAA_LOG_INFO(kaa_client->context->logger, KAA_ERR_NONE, "Starting Kaa client...");

    while (kaa_client->operate) {
        if (kaa_client->external_process) {
            if (KAA_TIME() - kaa_client->external_process_last_call >= (kaa_time_t)kaa_client->external_process_max_delay) {
                kaa_client->external_process(kaa_client->external_process_context);
                kaa_client->external_process_last_call = KAA_TIME();
            }
        }
        if (kaa_process_failover(kaa_client->context)) {
            kaa_client->bootstrap_complete = false;
        } else {
            if(kaa_client->channel_id>0) {
                if (kaa_client->channel_state == KAA_CLIENT_CHANNEL_STATE_NOT_CONNECTED) {
                    error_code = kaa_client_process_channel_disconnected(kaa_client);
                } else  if (kaa_client->channel_state == KAA_CLIENT_CHANNEL_STATE_CONNECTED) {
                    error_code = kaa_client_process_channel_connected(kaa_client);
                    if (error_code == KAA_ERR_TIMEOUT)
                        kaa_client_deinit_channel(kaa_client);
                }
            } else {
                //No initialized channels
                if (kaa_client->bootstrap_complete) {
                    KAA_LOG_INFO(kaa_client->context->logger, KAA_ERR_NONE,
                            "Channel [0x%08X] Boostrap complete, reinitializing to Operations ...", kaa_client->channel_id);
                    kaa_client->bootstrap_complete = false;
                    kaa_client_deinit_channel(kaa_client);
                    kaa_client_init_channel(kaa_client, KAA_CLIENT_CHANNEL_TYPE_OPERATIONS);
                    if (error_code == KAA_ERR_BAD_STATE) {
                        kaa_client_deinit_channel(kaa_client);
                        kaa_client->bootstrap_complete = false;
                    }
                } else {
                    KAA_LOG_INFO(kaa_client->context->logger, KAA_ERR_NONE,
                            "Channel [0x%08X] Operations error, reinitializing to Bootstrap ...", kaa_client->channel_id);
                    kaa_client->bootstrap_complete = true;
                    kaa_client_deinit_channel(kaa_client);

                    kaa_client_init_channel(kaa_client, KAA_CLIENT_CHANNEL_TYPE_BOOTSTRAP);
                }
            }
        }
#ifndef KAA_DISABLE_FEATURE_LOGGING
      ext_log_upload_timeout(kaa_client->context->log_collector);
#endif
    }
    KAA_LOG_INFO(kaa_client->context->logger, KAA_ERR_NONE, "Kaa client stopped");

    return error_code;
}

kaa_error_t kaa_client_init_channel(kaa_client_t *kaa_client, kaa_client_channel_type_t channel_type)
{
    KAA_RETURN_IF_NIL(kaa_client, KAA_ERR_BADPARAM);

    kaa_error_t error_code = KAA_ERR_NONE;

    KAA_LOG_TRACE(kaa_client->context->logger, KAA_ERR_NONE, "Initializing channel....");

    switch (channel_type) {
        case KAA_CLIENT_CHANNEL_TYPE_BOOTSTRAP:
            error_code = kaa_tcp_channel_create(&kaa_client->channel
                                              , kaa_client->context->logger
                                              , BOOTSTRAP_SERVICE
                                              , BOOTSTRAP_SERVICE_COUNT);
            break;
        case KAA_CLIENT_CHANNEL_TYPE_OPERATIONS:
            error_code = kaa_tcp_channel_create(&kaa_client->channel
                                              , kaa_client->context->logger
                                              , OPERATIONS_SERVICES
                                              , OPERATIONS_SERVICES_COUNT);
            break;
    }

    if (error_code) {
        KAA_LOG_ERROR(kaa_client->context->logger, error_code, "Failed to create transport channel, type %d", channel_type);
        return error_code;
    }

    error_code = kaa_tcp_channel_set_socket_events_callback(&kaa_client->channel, &on_kaa_tcp_channel_event, kaa_client);
    if (error_code) {
        KAA_LOG_ERROR(kaa_client->context->logger, error_code,
                "Failed to set socket events callback, channel type %d", channel_type);
        return error_code;
    }

    error_code = kaa_channel_manager_add_transport_channel(kaa_client->context->channel_manager
                                                         , &kaa_client->channel
                                                         , &kaa_client->channel_id);
    if (error_code) {
        KAA_LOG_ERROR(kaa_client->context->logger, error_code, "Failed to add transport channel, type %d", channel_type);
        return error_code;
    }

    KAA_LOG_INFO(kaa_client->context->logger, KAA_ERR_NONE, "Channel [0x%08X] initialized successfully (type %d)"
                                                                                , kaa_client->channel_id, channel_type);

    return error_code;
}

kaa_error_t kaa_client_deinit_channel(kaa_client_t *kaa_client)
{
    KAA_RETURN_IF_NIL(kaa_client, KAA_ERR_BADPARAM);

    kaa_error_t error_code = KAA_ERR_NONE;

    if (kaa_client->channel_id == 0)
        return KAA_ERR_NONE;

    KAA_LOG_TRACE(kaa_client->context->logger, KAA_ERR_NONE, "Deinitializing channel....");

    error_code = kaa_channel_manager_remove_transport_channel(kaa_client->context->channel_manager,kaa_client->channel_id);
    if (error_code) {
        KAA_LOG_TRACE(kaa_client->context->logger, error_code, "Bootstrap channel error removing from channel manager");
        return error_code;
    }

    kaa_client->channel_id = 0;
    kaa_client->channel_socket_closed = false;
    kaa_client->channel.context = NULL;
    kaa_client->channel.destroy = NULL;
    kaa_client->channel.get_protocol_id = NULL;
    kaa_client->channel.get_supported_services = NULL;
    kaa_client->channel.init = NULL;
    kaa_client->channel.set_access_point = NULL;
    kaa_client->channel.sync_handler = NULL;

    KAA_LOG_TRACE(kaa_client->context->logger, KAA_ERR_NONE, "Channel deinitialized successfully");

    return error_code;
}


kaa_error_t kaa_client_stop(kaa_client_t *kaa_client)
{
    KAA_RETURN_IF_NIL(kaa_client, KAA_ERR_BADPARAM);

    KAA_LOG_INFO(kaa_client->context->logger, KAA_ERR_NONE, "Going to stop Kaa client...");
    kaa_client->operate = false;

    return kaa_stop(kaa_client->context);
}


void kaa_client_destroy(kaa_client_t *self)
{
    KAA_RETURN_IF_NIL(self, );

    if (self->context) {
        kaa_deinit(self->context);
    }

    KAA_FREE(self);
}



#ifndef KAA_DISABLE_FEATURE_LOGGING
static kaa_error_t kaa_log_collector_init(kaa_client_t *kaa_client)
{
    KAA_RETURN_IF_NIL(kaa_client, KAA_ERR_BADPARAM);
    kaa_error_t error_code  = ext_unlimited_log_storage_create(&kaa_client->log_storage_context,
                                                               kaa_client->context->logger);

    if (error_code) {
       KAA_LOG_ERROR(kaa_client->context->logger, error_code, "Failed to create log storage");
       return error_code;
    }

    error_code = ext_log_upload_strategy_create(kaa_client->context
                                              ,&kaa_client->log_upload_strategy_context
                                              , KAA_LOG_UPLOAD_VOLUME_STRATEGY);
    if (error_code) {
        KAA_LOG_ERROR(kaa_client->context->logger, error_code, "Failed to create log upload strategy");
        return error_code;
    }

    kaa_log_bucket_constraints_t bucket_sizes = {
        .max_bucket_size = MAX_LOG_BUCKET_SIZE,
        .max_bucket_log_count = MAX_LOG_COUNT,
    };

    error_code = kaa_logging_init(kaa_client->context->log_collector
                                , kaa_client->log_storage_context
                                , kaa_client->log_upload_strategy_context
                                , &bucket_sizes);
    if (error_code) {
        KAA_LOG_ERROR(kaa_client->context->logger, error_code,"Failed to init log collector");
        return error_code;
    }

    KAA_LOG_INFO(kaa_client->context->logger, KAA_ERR_NONE, "Log collector init completed");
    return error_code;
}
#endif
