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

/*
 *@file kaa_client.c
*/
#include <sndc_sdk_api.h>
#include <sndc_sock_api.h>
#include <sndc_file_api.h>

#include <stdbool.h>
typedef long long int64_t;

#include "kaa_error.h"
#include "kaa_common.h"
#include "kaa.h"
#include "utilities/kaa_log.h"
#include "platform/ext_sha.h"
#include "platform/ext_transport_channel.h"
#include "platform/ext_system_logger.h"
#include "platform/time.h"
#include "platform-impl/common/kaa_tcp_channel.h"
#include "platform-impl/common/ext_log_upload_strategies.h"
#include "kaa_bootstrap_manager.h"
#include "kaa_channel_manager.h"
#include "kaa_configuration_manager.h"
#include "kaa_logging.h"
#include "platform/ext_log_storage.h"
#include "platform/ext_log_upload_strategy.h"
#include "econais_ec19d_file_utils.h"

#include "kaa_context.h"

#define KAA_CLIENT_T

typedef enum {
    BOOTSRAP_UNDEF,
    BOOTSRAP_STARTED,
    BOOTSRAP_FINISHED
} bootstrap_client_state_t;

struct kaa_client_t {
        char *thread_name;
        bool_t  operate;
        sndc_sem_t start_semophore;
        sndc_sem_t logging_semophore;
        unsigned long max_update_time;
        struct sndc_timeval timeval;
        kaa_context_t *kaa_context;
        kaa_transport_channel_interface_t bootstrap_channel;
        unsigned int bootstrap_channel_id;
        bootstrap_client_state_t bootstrap_state;
        kaa_transport_channel_interface_t operations_channel;
        unsigned int operations_channel_id;
        void *log_storage_context;
        void *log_upload_strategy_context;
        sndc_mem_stats_t mem_stat;
};

#define KAA_DEMO_UPLOAD_COUNT_THRESHOLD     1   /* Count of collected serialized logs needed to initiate log upload */

#define KAA_DEMO_LOG_GENERATION_FREQUENCY    3 /* seconds */

#include "platform/kaa_client.h"

//Forward declaration of internal functions
kaa_error_t kaa_init_security_stuff(void);

/* forward declarations */

void print_mem_stat(kaa_client_t *kaa_client);
/*
 * Strategy-specific configuration parameters used by Kaa log collection feature.
 */
#define KAA_DEMO_MAX_UPLOAD_THRESHOLD     15   /* Size of collected serialized logs needed to initiate log upload */
/* Max size of a log batch has been sent by SDK during one upload. */
#define KAA_DEMO_MAX_LOG_BUCKET_SIZE      (KAA_TCP_CHANNEL_OUT_BUFFER_SIZE >> 3)
#define KAA_DEMO_MAX_LOGS_IN_BUCKET       16   /* Max count of logs in one bucket */
#define KAA_DEMO_MAX_CLEANUP_THRESHOLD    100 /* Max size of an inner log storage. If size is exceeded, elder logs will be removed. */

#define KAA_DEMO_LOG_GENERATION_FREQUENCY    3 /* seconds */

_Static_assert(KAA_DEMO_MAX_LOG_BUCKET_SIZE, "Maximum bucket size cannot be 0!");

/*
 * Kaa status and public key storage file names.
 */
#define KAA_KEY_STORAGE       "key.txt"
#define KAA_STATUS_STORAGE    "status.conf"

static size_t kaa_public_key_length;
static char *kaa_public_key;
static kaa_digest kaa_public_key_hash;

static kaa_extension_id BOOTSTRAP_SERVICE[] = { KAA_EXTENSION_BOOTSTRAP };
static const int BOOTSTRAP_SERVICE_COUNT = sizeof(BOOTSTRAP_SERVICE) / sizeof(kaa_extension_id);

/*
 * Define services which should be used.
 * Don't define unused services, it may cause an error.
 */
static kaa_extension_id OPERATIONS_SERVICES[] = { KAA_EXTENSION_PROFILE
                                             , KAA_EXTENSION_CONFIGURATION
                                             , KAA_EXTENSION_USER
                                             , KAA_EXTENSION_EVENT
                                             , KAA_EXTENSION_LOGGING};
static const int OPERATIONS_SERVICES_COUNT = sizeof(OPERATIONS_SERVICES) / sizeof(kaa_extension_id);

void thread_run_fn(uintptr_t arg);

kaa_error_t kaa_client_create(kaa_client_t **kaa_client, kaa_client_props_t *props)
{
    KAA_RETURN_IF_NIL2(kaa_client, props, KAA_ERR_BADPARAM);

    kaa_error_t error_code = kaa_init_security_stuff();
    KAA_RETURN_IF_ERR(error_code);

    kaa_client_t *self = sndc_mem_calloc(1,sizeof(kaa_client_t));
    KAA_RETURN_IF_NIL(self,KAA_ERR_NOMEM);

    self->thread_name = sndc_mem_strdup("Kaa-Client-Thread");
    self->operate = true;
    self->max_update_time = props->max_update_time;

    print_mem_stat(self);
    sndc_printf("Initializing Kaa SDK...\n");
    sndc_thrd_delay(TRACE_DELAY * SNDC_MILLISECOND);

    error_code = kaa_init(&self->kaa_context);
    if (error_code) {
        sndc_printf("Error during Kaa context creation %d\n", error_code);
        sndc_thrd_delay(TRACE_DELAY * SNDC_MILLISECOND);
        goto error;
    }

    KAA_LOG_INFO(self->kaa_context->logger, KAA_ERR_NONE, "Kaa framework initialized.");

    print_mem_stat(self);
    error_code = kaa_log_collector_init(self);
    if (error_code) {
        sndc_printf("Failed to init Kaa log collector %d\n", error_code);
        sndc_thrd_delay(TRACE_DELAY * SNDC_MILLISECOND);
        goto error;
    }

    KAA_LOG_INFO(self->kaa_context->logger, KAA_ERR_NONE, "Kaa log collector initialized.");

    *kaa_client = self;

    /* initialize semaphore */
    sndc_sem_init(&self->start_semophore, 0);
    sndc_sem_init(&self->logging_semophore, 0);

    int status = sndc_thrd_create(
            self->thread_name,
            thread_run_fn,
            (uintptr_t)self,
            SNDC_THRD_PRIORITY_DEFAULT,
            THREAD_STACKSIZE_DMR_START); //Defined 4K-8 bytes of stack

    switch (status) {
        case 0:

            break;
        case -EINVAL:
            error_code = KAA_ERR_BADPARAM;
            break;
        case -ENOMEM:
            error_code = KAA_ERR_NOMEM;
            break;
        default:
            error_code = KAA_ERR_BADDATA;
            break;
    }

    return error_code;

error:

    sndc_printf("Kaa initialization failed. error_code %d\n", error_code);
    sndc_thrd_delay(TRACE_DELAY * SNDC_MILLISECOND);
    kaa_client_destroy(self);

    return error_code;
}

kaa_error_t kaa_client_stop(kaa_client_t *kaa_client)
{
    KAA_RETURN_IF_NIL(kaa_client, KAA_ERR_BADPARAM);
    kaa_client->operate = false;
    return kaa_stop(kaa_client->kaa_context);
}

void kaa_client_destroy(kaa_client_t *kaa_client)
{
    if(!kaa_client)
        return;

    if (kaa_client->start_semophore) {
        sndc_sem_destroy(kaa_client->start_semophore);
        kaa_client->start_semophore = NULL;
    }

    if (kaa_client->logging_semophore) {
        sndc_sem_destroy(kaa_client->logging_semophore);
        kaa_client->logging_semophore = NULL;
    }

    if (kaa_client->operations_channel.context) {
        kaa_client->operations_channel.destroy(kaa_client->operations_channel.context);
        kaa_client->operations_channel.context = NULL;
    }

    if (kaa_client->bootstrap_channel.context) {
        kaa_client->bootstrap_channel.destroy(kaa_client->bootstrap_channel.context);
        kaa_client->bootstrap_channel.context = NULL;
    }

    if (kaa_client->log_storage_context) {
        ext_log_storage_destroy(kaa_client->log_storage_context);
        kaa_client->log_storage_context = NULL;
    }

    if (kaa_client->kaa_context) {
        kaa_deinit(kaa_client->kaa_context);
        kaa_client->kaa_context = NULL;
    }

    if (kaa_client->thread_name) {
        sndc_mem_free(kaa_client->thread_name);
    }
    sndc_mem_free(kaa_client);
}

kaa_context_t* kaa_client_get_context(kaa_client_t *kaa_client)
{
    KAA_RETURN_IF_NIL(kaa_client,NULL);
    return kaa_client->kaa_context;
}

kaa_error_t kaa_client_init_operations_channel(kaa_client_t *kaa_client)
{
    KAA_RETURN_IF_NIL(kaa_client, KAA_ERR_BADPARAM);
    KAA_LOG_TRACE(kaa_client->kaa_context->logger, KAA_ERR_NONE, "Start operations channel initialization");
    kaa_error_t error_code = kaa_tcp_channel_create(&kaa_client->operations_channel,
            kaa_client->kaa_context->logger, OPERATIONS_SERVICES, OPERATIONS_SERVICES_COUNT);
    if (error_code) {
        KAA_LOG_ERROR(kaa_client->kaa_context->logger, error_code, "Operations channel initialization failed");
        return error_code;
    }

    KAA_LOG_TRACE(kaa_client->kaa_context->logger, KAA_ERR_NONE, "Initializing Kaa SDK Operations channel added to transport channel manager");

    error_code = kaa_channel_manager_add_transport_channel(kaa_client->kaa_context->channel_manager,
            &kaa_client->operations_channel, &kaa_client->operations_channel_id);
    if (error_code) {
        KAA_LOG_ERROR(kaa_client->kaa_context->logger, error_code, "Error during Kaa operations channel setting as transport");
        return error_code;
    }

    KAA_LOG_INFO(kaa_client->kaa_context->logger, KAA_ERR_NONE, "Operations channel initialized successfully");
    print_mem_stat(kaa_client);
    return error_code;
}

kaa_error_t on_kaa_tcp_channel_event(void *context
                                                 , kaa_tcp_channel_event_t event_type
                                                 , kaa_fd_t fd)
{
    KAA_RETURN_IF_NIL(context, KAA_ERR_BADPARAM);
    kaa_client_t *self = (kaa_client_t *)context;

    switch (event_type) {
        case SOCKET_CONNECTED:
            KAA_LOG_INFO(self->kaa_context->logger, KAA_ERR_NONE, "Bootstrap socket(%d) Connected", fd);
            if (self->bootstrap_state == BOOTSRAP_UNDEF)
                self->bootstrap_state = BOOTSRAP_STARTED;
            break;
        case SOCKET_DISCONNECTED:
            KAA_LOG_INFO(self->kaa_context->logger, KAA_ERR_NONE, "Bootstrap socket Disconnected");
            if (self->bootstrap_state == BOOTSRAP_STARTED)
                self->bootstrap_state = BOOTSRAP_FINISHED;
            break;
        default:
            KAA_LOG_ERROR(self->kaa_context->logger, KAA_ERR_NONE, "Bootstrap socket Error");
            self->bootstrap_state = BOOTSRAP_UNDEF;
            break;
    }

    return KAA_ERR_NONE;
}

kaa_error_t kaa_client_deinit_bootstrap_channel(kaa_client_t *kaa_client)
{
    KAA_RETURN_IF_NIL(kaa_client, KAA_ERR_BADPARAM);
    KAA_LOG_TRACE(kaa_client->kaa_context->logger, KAA_ERR_NONE, "Bootstrap channel deinitialization starting ....");
    print_mem_stat(kaa_client);

    kaa_error_t error_code = kaa_channel_manager_remove_transport_channel(
            kaa_client->kaa_context->channel_manager,kaa_client->bootstrap_channel_id);

    if (error_code) {
        KAA_LOG_TRACE(kaa_client->kaa_context->logger, error_code, "Bootstrap channel error removing from channel manager");
        return error_code;
    }

    kaa_client->bootstrap_channel.context = NULL;
    kaa_client->bootstrap_channel.destroy = NULL;
    kaa_client->bootstrap_channel.get_protocol_id = NULL;
    kaa_client->bootstrap_channel.get_supported_services = NULL;
    kaa_client->bootstrap_channel.init = NULL;
    kaa_client->bootstrap_channel.set_access_point = NULL;
    kaa_client->bootstrap_channel.sync_handler = NULL;

    kaa_client->bootstrap_state = BOOTSRAP_UNDEF;
    KAA_LOG_INFO(kaa_client->kaa_context->logger, KAA_ERR_NONE, "Bootstrap channel deinitialized");

    return error_code;
}


kaa_error_t kaa_client_start(kaa_client_t *kaa_client
                           , external_process_fn external_process
                           , void *external_process_context
                           , kaa_time_t max_delay)
{
    KAA_RETURN_IF_NIL(kaa_client, KAA_ERR_BADPARAM);
    KAA_LOG_TRACE(kaa_client->kaa_context->logger, KAA_ERR_NONE, "Kaa client starting ...");
    print_mem_stat(kaa_client);

    kaa_error_t error_code = kaa_check_readiness(kaa_client->kaa_context);
    if (error_code != KAA_ERR_NONE) {
        KAA_LOG_ERROR(kaa_client->kaa_context->logger, error_code, "Cannot start Kaa client: Kaa context is not fully initialized");
        return error_code;
    }

    error_code = kaa_tcp_channel_create(&kaa_client->bootstrap_channel
                                      , kaa_client->kaa_context->logger
                                      , BOOTSTRAP_SERVICE
                                      , BOOTSTRAP_SERVICE_COUNT);
    if (error_code) {
        KAA_LOG_ERROR(kaa_client->kaa_context->logger, error_code, "Error during Kaa bootstrap channel creation");
        return error_code;
    }

    error_code = kaa_tcp_channel_set_socket_events_callback(&kaa_client->bootstrap_channel,
                                                        on_kaa_tcp_channel_event, (void*)kaa_client);
    if (error_code) {
        KAA_LOG_ERROR(kaa_client->kaa_context->logger, error_code, "Error setting callback bootstrap channel");
        return error_code;
    }


    KAA_LOG_TRACE(kaa_client->kaa_context->logger, KAA_ERR_NONE, "Kaa client - bootstrap channel initialized");
    print_mem_stat(kaa_client);

    error_code = kaa_channel_manager_add_transport_channel(kaa_client->kaa_context->channel_manager
                                                         , &kaa_client->bootstrap_channel
                                                         , &kaa_client->bootstrap_channel_id);

    if (error_code) {
        KAA_LOG_ERROR(kaa_client->kaa_context->logger, error_code, "Error setting bootstrap channel setting as transport");
        return error_code;
    }

    //Push running thread
    sndc_sem_post(&kaa_client->start_semophore);
    KAA_LOG_INFO(kaa_client->kaa_context->logger, KAA_ERR_NONE, "Kaa client started");

    return KAA_ERR_NONE;
}

void thread_run_fn(uintptr_t arg)
{
    if(!arg) {
        sndc_printf("Kaa client thread function Error, no args\n");
        return;
    }

    kaa_client_t *self = (kaa_client_t *)arg;
    KAA_LOG_TRACE(self->kaa_context->logger, KAA_ERR_NONE, "Kaa client working thread started....");
    sndc_sem_post(&self->logging_semophore);

    sndc_sock_fdset r_set;
    sndc_sock_fdset w_set;
    sndc_sock_fdset x_set;
    int ops_fd = -1, bootstrap_fd = -1;
    bool_t fdset = false;
    uint16_t timeout = self->max_update_time;
    kaa_error_t error_code;
    KAA_LOG_TRACE(self->kaa_context->logger, KAA_ERR_NONE, "Kaa client working thread(%s) wait starting...", self->thread_name);
    sndc_sem_wait(&self->start_semophore);
    KAA_LOG_INFO(self->kaa_context->logger, KAA_ERR_NONE, "Kaa client working thread(%s) started", self->thread_name);

    while(self->operate) {
        int max_fd = 0;
        SNDC_FD_ZERO(&r_set);
        SNDC_FD_ZERO(&w_set);
        SNDC_FD_ZERO(&x_set);

        // This semaphore is used to synchronize main thread and kaa_client thread,
        // mostly for logging proposes.
        sndc_sem_tryWait(&self->logging_semophore);

        if (self->operations_channel.context)
            kaa_tcp_channel_get_descriptor(&self->operations_channel, &ops_fd);
        if(self->bootstrap_channel.context)
            kaa_tcp_channel_get_descriptor(&self->bootstrap_channel, &bootstrap_fd);
        KAA_LOG_DEBUG(self->kaa_context->logger, KAA_ERR_NONE, "IO LOOP: descriptors: bootstrap(%d), operations(%d)", bootstrap_fd, ops_fd);

        print_mem_stat(self);

        if (bootstrap_fd >= 0) {
            fdset = false;
            if (kaa_tcp_channel_is_ready(&self->bootstrap_channel, FD_READ)) {
                SNDC_FD_SET(bootstrap_fd, &r_set);
                KAA_LOG_DEBUG(self->kaa_context->logger,
                        KAA_ERR_NONE,
                        "IO LOOP: Bootstrap READ set wait");
                fdset = true;
            }
            if (kaa_tcp_channel_is_ready(&self->bootstrap_channel, FD_WRITE)) {
                SNDC_FD_SET(bootstrap_fd, &w_set);
                KAA_LOG_DEBUG(self->kaa_context->logger,
                        KAA_ERR_NONE,
                        "IO LOOP: Bootstrap WRITE set wait");
                fdset = true;
            }
            if (fdset) {
                max_fd = MAX(max_fd, bootstrap_fd);
                SNDC_FD_SET(bootstrap_fd, &x_set);
            }
        }
        if (ops_fd >= 0) {
            fdset = false;
            if (kaa_tcp_channel_is_ready(&self->operations_channel, FD_READ)) {
                SNDC_FD_SET(ops_fd, &r_set);
                KAA_LOG_DEBUG(self->kaa_context->logger,
                        KAA_ERR_NONE,
                        "IO LOOP: Operations READ set wait");
                fdset = true;
            }
            if (kaa_tcp_channel_is_ready(&self->operations_channel, FD_WRITE)) {
                SNDC_FD_SET(ops_fd, &w_set);
                KAA_LOG_DEBUG(self->kaa_context->logger,
                        KAA_ERR_NONE,
                        "IO LOOP: Operations WRITE set wait");
                fdset = true;
            }
            if (fdset) {
                max_fd = MAX(max_fd, ops_fd);
                SNDC_FD_SET(ops_fd, &x_set);
            }
        }

        kaa_tcp_channel_get_max_timeout(&self->operations_channel, &timeout);
        self->timeval.tv_sec = timeout;
        if (timeout > self->max_update_time)
            self->timeval.tv_sec = self->max_update_time;

        self->timeval.tv_usec = 0;
        sndc_sem_post(&self->logging_semophore);
        int r = sndc_sock_select(max_fd+1,&r_set,&w_set,&x_set,&self->timeval);
        sndc_sem_tryWait(&self->logging_semophore);
        if (r == 0) {
            uint32_t msec = sndc_sys_getTimestamp_msec();
            KAA_LOG_DEBUG(self->kaa_context->logger,
                    KAA_ERR_NONE,
                    "IO LOOP: timeout (%d) expired",
                    self->timeval.tv_sec);

            // TODO why is it unused?
            (void)msec;

            if (self->bootstrap_state == BOOTSRAP_FINISHED && bootstrap_fd == -1) {
                sndc_printf("Bootstrap channel deinit, Operations channel init %d\n", bootstrap_fd);
                KAA_LOG_INFO(self->kaa_context->logger,
                                    KAA_ERR_NONE,
                                    "IO LOOP: Bootstrap channel finish processing switching to Operations channel");
                kaa_client_deinit_bootstrap_channel(self);
                kaa_client_init_operations_channel(self);
            }

            if (self->bootstrap_channel.context) {
                error_code = kaa_tcp_channel_check_keepalive(&self->bootstrap_channel);
                if (error_code) {
                    KAA_LOG_ERROR(self->kaa_context->logger,
                            KAA_ERR_NONE,
                            "IO LOOP: Failed Keepalive Bootstrap(%d) check",
                            bootstrap_fd);
                }
            }
            if (self->operations_channel.context) {
                error_code = kaa_tcp_channel_check_keepalive(&self->operations_channel);
                if (error_code) {
                    KAA_LOG_ERROR(self->kaa_context->logger,
                            KAA_ERR_NONE,
                            "IO LOOP: Failed Keepalive Operations(%d) check",
                            ops_fd);
                }
            }
        } else if (r > 0) {
            sndc_printf("FD SET return %d events\n", r);
            KAA_LOG_DEBUG(self->kaa_context->logger,
                    KAA_ERR_NONE,
                    "IO LOOP: select() return %d events", r);
            if (bootstrap_fd >= 0) {
                fdset = false;
                if (SNDC_FD_ISSET(bootstrap_fd, &r_set)) {
                    fdset = true;
                    KAA_LOG_DEBUG(self->kaa_context->logger,
                                        KAA_ERR_NONE,
                                        "IO LOOP: Read Event Bootstrap(%d)", bootstrap_fd);
                    error_code = kaa_tcp_channel_process_event(&self->bootstrap_channel, FD_READ);
                    if (error_code) {
                        KAA_LOG_ERROR(self->kaa_context->logger,
                                        error_code,
                                        "IO LOOP: Failed Read Event Bootstrap(%d)", bootstrap_fd);
                    }
                }
                if (fdset)
                    r--;
                if (r > 0) {
                    fdset = false;
                    if (SNDC_FD_ISSET(bootstrap_fd, &w_set)) {
                        fdset = true;
                        KAA_LOG_DEBUG(self->kaa_context->logger,
                                        KAA_ERR_NONE,
                                        "IO LOOP: Write Event Bootstrap(%d)", bootstrap_fd);
                        error_code = kaa_tcp_channel_process_event(&self->bootstrap_channel, FD_WRITE);
                        if (error_code) {
                            KAA_LOG_ERROR(self->kaa_context->logger,
                                        error_code,
                                        "IO LOOP: Failed Write Event Bootstrap(%d)", bootstrap_fd);
                        }
                    }
                    if (fdset)
                        r--;
                }
                if (r > 0) {
                    fdset = false;
                    if (SNDC_FD_ISSET(bootstrap_fd, &x_set)) {
                        fdset = true;
                        sndc_printf("Exception Event Bootstrap %d\n", bootstrap_fd);
                        KAA_LOG_DEBUG(self->kaa_context->logger,
                                        KAA_ERR_NONE,
                                        "IO LOOP: Exception Event Bootstrap(%d)", bootstrap_fd);
                        error_code = kaa_tcp_channel_process_event(&self->bootstrap_channel, FD_EXCEPTION);
                        if (error_code) {
                            KAA_LOG_ERROR(self->kaa_context->logger,
                                        error_code,
                                        "IO LOOP: Failed Exception Event Bootstrap(%d)", bootstrap_fd);
                        }
                    }
                    if (fdset)
                        r--;
                }
            }
            if (r > 0 && ops_fd >= 0) {
                fdset = false;
                if (SNDC_FD_ISSET(ops_fd, &r_set)) {
                    fdset = true;
                    KAA_LOG_DEBUG(self->kaa_context->logger,
                                    KAA_ERR_NONE,
                                    "IO LOOP: Read Event Operations(%d)", ops_fd);
                    error_code = kaa_tcp_channel_process_event(&self->operations_channel, FD_READ);
                    if (error_code) {
                        KAA_LOG_ERROR(self->kaa_context->logger,
                                    error_code,
                                    "IO LOOP: Failed Read Event Operations(%d)", ops_fd);
                    }
                }
                if (fdset)
                    r--;
                if (r > 0) {
                    fdset = false;
                    if (SNDC_FD_ISSET(ops_fd, &w_set)) {
                        fdset = true;
                        KAA_LOG_DEBUG(self->kaa_context->logger,
                                        KAA_ERR_NONE,
                                        "IO LOOP: Write Event Operations(%d)", ops_fd);
                        error_code = kaa_tcp_channel_process_event(&self->operations_channel, FD_WRITE);
                        if (error_code) {
                            KAA_LOG_ERROR(self->kaa_context->logger,
                                        error_code,
                                        "IO LOOP: Failed Write Event Operations(%d)", ops_fd);
                        }
                    }
                }
                if (fdset)
                    r--;
                if (r > 0) {
                    fdset = false;
                    if (SNDC_FD_ISSET(ops_fd, &x_set)) {
                        fdset = true;
                        sndc_printf("Exception Event Operations %d\n", ops_fd);
                        KAA_LOG_DEBUG(self->kaa_context->logger,
                                        KAA_ERR_NONE,
                                        "IO LOOP: Exception Event Operations(%d)", ops_fd);
                        error_code = kaa_tcp_channel_process_event(&self->operations_channel, FD_EXCEPTION);
                        if (error_code) {
                            KAA_LOG_ERROR(self->kaa_context->logger,
                                        error_code,
                                        "IO LOOP: Failed Exception Event Operations(%d)", ops_fd);
                        }
                    }
                }
            }
        } else {
            KAA_LOG_ERROR(self->kaa_context->logger,
                        KAA_ERR_BAD_STATE,
                        "IO LOOP: Error %d returned from select()", r);
        }
    }

    KAA_LOG_INFO(self->kaa_context->logger,
                    KAA_ERR_NONE,
                    "IO LOOP: Finished.");
}




/*
 * External API to store/load the Kaa SDK status.
 */
void ext_status_read(char **buffer, size_t *buffer_size, bool *needs_deallocation)
{
    econais_ec19d_binary_file_read(KAA_STATUS_STORAGE, buffer, buffer_size, needs_deallocation);
}

void ext_status_store(const char *buffer, size_t buffer_size)
{
    econais_ec19d_binary_file_store(KAA_STATUS_STORAGE, buffer, buffer_size);
}


/*
 * External API to retrieve a cryptographic public key.
 */
void ext_get_endpoint_public_key(char **buffer, size_t *buffer_size, bool *needs_deallocation)
{
    *buffer = kaa_public_key;
    *buffer_size = kaa_public_key_length;
    *needs_deallocation = false;
}


kaa_error_t kaa_init_security_stuff(void)
{
    sndc_file_ref_t key_file = sndc_file_open(KAA_KEY_STORAGE, DE_FRDONLY);

    if (key_file) {
        sndc_file_seek(key_file, 0, SEEK_END);
        kaa_public_key_length = sndc_file_tell(key_file);
        kaa_public_key = (char*)sndc_mem_calloc(kaa_public_key_length, sizeof(char));

        if (kaa_public_key == NULL) {
            sndc_printf("Failed to allocate %u bytes for public key\n", kaa_public_key_length);
            return KAA_ERR_NOMEM;
        }

        sndc_file_seek(key_file, 0, SEEK_SET);
        if (sndc_file_read(key_file, kaa_public_key, kaa_public_key_length) == 0) {
            sndc_mem_free(kaa_public_key);
            sndc_printf("Failed to read public key (size %u)\n", kaa_public_key_length);
            return KAA_ERR_INVALID_PUB_KEY;
        }
        sndc_file_close(key_file);
        sndc_printf("Restored public key (size %u)\n", kaa_public_key_length);
    } else {
        sndc_printf("No RSA key file %s found\n", KAA_KEY_STORAGE);
    }

    ext_calculate_sha_hash(kaa_public_key, kaa_public_key_length, kaa_public_key_hash);
    sndc_printf("SHA calculated\n");
    return KAA_ERR_NONE;
}

/*
 * Initializes Kaa log collector.
 */
static kaa_error_t kaa_log_collector_init(kaa_client_t *kaa_client)
{
    KAA_RETURN_IF_NIL(kaa_client, KAA_ERR_BADPARAM);

        kaa_error_t error_code = ext_unlimited_log_storage_create(
                &kaa_client->log_storage_context,
                kaa_client->kaa_context->logger);
        if (error_code) {
            KAA_LOG_ERROR(kaa_client->kaa_context->logger,
                    error_code,
                    "Failed to create log storage");
            return error_code;
        }

        error_code = ext_log_upload_strategy_create(kaa_client->kaa_context
                                                  ,&kaa_client->log_upload_strategy_context
                                                  , KAA_LOG_UPLOAD_VOLUME_STRATEGY);

        if (error_code) {
            KAA_LOG_ERROR(kaa_client->kaa_context->logger,
                    error_code,
                    "Failed to create log upload strategy");
            return error_code;
        }

        error_code = ext_log_upload_strategy_set_threshold_count(kaa_client->log_upload_strategy_context
                                                                         , KAA_DEMO_UPLOAD_COUNT_THRESHOLD);
        if (error_code) {
            KAA_LOG_ERROR(kaa_client->kaa_context->logger,
                    error_code,
                    "Failed to create log upload strategy by volume set threshold count to %d",
                    KAA_DEMO_UPLOAD_COUNT_THRESHOLD);
            return error_code;
        }

        kaa_log_bucket_constraints_t bucket_sizes = {
            .max_bucket_size = KAA_DEMO_MAX_LOG_BUCKET_SIZE,
            .max_bucket_log_count = KAA_DEMO_MAX_LOGS_IN_BUCKET,
        };

        error_code = kaa_logging_init(kaa_client->kaa_context->log_collector
                                    , kaa_client->log_storage_context
                                    , kaa_client->log_upload_strategy_context
                                    , &bucket_sizes);

        if (error_code) {
            KAA_LOG_ERROR(kaa_client->kaa_context->logger,
                    error_code,
                    "Failed to logging init");
            return error_code;
        }
        KAA_LOG_INFO(kaa_client->kaa_context->logger,
                KAA_ERR_NONE,
                "Log collector init complete");
        return error_code;

}

///*
// * Example code for logging
// */
//kaa_error_t kaa_client_log_record(kaa_client_t *kaa_client, const char *record)
//{
//    if (!kaa_client || !record) {
//        return KAA_ERR_BADPARAM;
//    }
//
//    kaa_logging_record_t * kaa_record = kaa_logging_record_create();
//    if (!kaa_record) {
//        KAA_LOG_ERROR(kaa_client->kaa_context->logger,
//                KAA_ERR_NOT_INITIALIZED,
//                "Failed to allocate log record");
//        return KAA_ERR_NOT_INITIALIZED;
//    }
//
//    kaa_record->body = kaa_string_move_create(record, NULL);
//
//    //Wait until thread sleep in select()
//    sndc_sem_wait(&kaa_client->logging_semophore);
//
//    kaa_error_t error_code = kaa_logging_add_record(kaa_client->kaa_context->log_collector, kaa_record);
//    if (error_code) {
//        KAA_LOG_ERROR(kaa_client->kaa_context->logger,
//                error_code,
//                "Failed to add log record");
//    }
//
//    KAA_LOG_DEBUG(kaa_client->kaa_context->logger,
//                    KAA_ERR_NONE,
//                    "Kaa record %s logged", record);
//
//    kaa_record->destroy(kaa_record);
//
//    return KAA_ERR_NONE;
//}

/**
 * Example code for configuration update
 */
//kaa_error_t kaa_client_configuration_update(kaa_client_t *kaa_client, const kaa_root_configuration_t *configuration)
//{
//    if (!kaa_client || !configuration) {
//        return KAA_ERR_BADPARAM;
//    }
//    sndc_printf("New configuration update.... timeout %d\n", configuration->timeout);
//
//    KAA_LOG_DEBUG(kaa_client->kaa_context->logger,
//                    KAA_ERR_NONE,
//                    "New configuration update.... timeout %d", configuration->timeout);
//    return KAA_ERR_NONE;
//}

void ext_write_log(FILE * sink, const char * buffer, size_t message_size)
{
    if (!buffer) {
        return;
    }
    sndc_printf(buffer);
    sndc_thrd_delay(TRACE_DELAY * SNDC_MILLISECOND);
}

kaa_time_t ext_get_systime(void)
{
    return (time_t) sndc_sys_getTimestamp_msec() / 1000;
}

void print_mem_stat(kaa_client_t *kaa_client)
{
    if (!kaa_client)
        return;
    sndc_mem_getStats(&kaa_client->mem_stat);
    KAA_LOG_DEBUG(kaa_client->kaa_context->logger,
            KAA_ERR_NONE,
            "Memory: Total(%d)/Allocated(%d)",
            kaa_client->mem_stat.total,
            kaa_client->mem_stat.allocated);
}
