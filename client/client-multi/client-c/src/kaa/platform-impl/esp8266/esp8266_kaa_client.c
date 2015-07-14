#include <stdint.h>
#include <stdio.h>

#include <kaa.h>
#include <kaa_context.h>
#include <platform/kaa_client.h>
#include <platform/ext_transport_channel.h>
#include <utilities/kaa_mem.h>
#include <utilities/kaa_log.h>
#include <kaa_logging.h>
#include <platform/time.h>

typedef enum {
    KAA_CLIENT_CHANNEL_STATE_CONNECTED = 0,
    KAA_CLIENT_CHANNEL_STATE_NOT_CONNECTED
} kaa_client_channel_state_t;

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

#ifndef KAA_DISABLE_FEATURE_LOGGING

extern kaa_error_t ext_unlimited_log_storage_create(void **log_storage_context_p
                                                  , kaa_logger_t *logger);

extern kaa_error_t ext_log_upload_strategy_by_volume_create(void **strategy_p
                                                          , kaa_channel_manager_t *channel_manager
                                                          , kaa_bootstrap_manager_t *bootstrap_manager);
kaa_error_t kaa_log_collector_init(kaa_client_t *client);
#endif

#define KAA_RETURN_IF_ERR_MSG(E, msg) \
        { if(E) { printf("Error %i. \"%s\"\n",(E), (msg)); return (E); } }

kaa_error_t kaa_client_create(kaa_client_t **client, kaa_client_props_t *props) {
    KAA_RETURN_IF_NIL(client, KAA_ERR_BADPARAM);

    kaa_error_t error_code = KAA_ERR_NONE;

    kaa_client_t *self = (kaa_client_t*)KAA_CALLOC(1, sizeof(kaa_client_t));
    KAA_RETURN_IF_NIL(self, KAA_ERR_NOMEM);
    error_code = kaa_init(&self->context);

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

kaa_context_t *kaa_client_get_context(kaa_client_t *client) {
    KAA_RETURN_IF_NIL(client, NULL);
    return client->context;
}

kaa_error_t kaa_client_start(kaa_client_t *kaa_client,
                             external_process_fn external_process,
                             void *external_process_context,
                             time_t max_delay) {
    KAA_RETURN_IF_NIL(kaa_client, KAA_ERR_BADPARAM);

    kaa_error_t error_code = KAA_ERR_NONE; 
    
    kaa_client->external_process = external_process;
    kaa_client->external_process_context = external_process_context;
    kaa_client->external_process_max_delay = max_delay;
    kaa_client->external_process_last_call = KAA_TIME();

    KAA_LOG_INFO(kaa_client->context->logger, KAA_ERR_NONE, "Starting Kaa client...");

    while(kaa_client->operate) {
        printf("in kaa_client_start loop\t\n"); 
    }

    return error_code;
}

kaa_error_t kaa_client_stop(kaa_client_t *kaa_client)
{
    KAA_RETURN_IF_NIL(kaa_client, KAA_ERR_BADPARAM);

    KAA_LOG_INFO(kaa_client->context->logger, KAA_ERR_NONE, "Going to stop Kaa client...");
    kaa_client->operate = false;

    return KAA_ERR_NONE;
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
kaa_error_t kaa_log_collector_init(kaa_client_t *kaa_client) {
    KAA_RETURN_IF_NIL(kaa_client, KAA_ERR_BADPARAM);
    kaa_error_t error_code  = ext_unlimited_log_storage_create(&kaa_client->log_storage_context,
                                                               kaa_client->context->logger);

    if (error_code) {
       KAA_LOG_ERROR(kaa_client->context->logger, error_code, "Failed to create log storage");
       return error_code;
    }

    error_code = ext_log_upload_strategy_by_volume_create(&kaa_client->log_upload_strategy_context
                                                                , kaa_client->context->channel_manager
                                                                , kaa_client->context->bootstrap_manager);
    if (error_code) {
        KAA_LOG_ERROR(kaa_client->context->logger, error_code, "Failed to create log upload strategy");
        return error_code;
    }

    error_code = kaa_logging_init(kaa_client->context->log_collector
                                , kaa_client->log_storage_context
                                , kaa_client->log_upload_strategy_context);
    if (error_code) {
        KAA_LOG_ERROR(kaa_client->context->logger, error_code,"Failed to init log collector");
        return error_code;
    }

    KAA_LOG_INFO(kaa_client->context->logger, KAA_ERR_NONE, "Log collector init completed");
    return error_code;
}
#endif

