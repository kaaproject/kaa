/*
 * Copyright 2015 CyberVision, Inc.
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
 * @file kaa_client.c
 *
 *  Created on: Apr 14, 2015
 *      Author: Andriy Panasenko <apanasenko@cybervisiontech.com>
 */

typedef unsigned char uint8;
typedef unsigned int uint32;

#include <stdbool.h>
#include <stdint.h>
#include <stdlib.h>

#include "../leaf_time.h"
#include "../leaf_stdio.h"
#include "../../../../kaa_error.h"
#include "../../../../kaa_common.h"
#include "../../../../kaa.h"
#include "../../../../kaa_context.h"
#include "../../../../utilities/kaa_log.h"
#include "esp8266.h"
#include "chip_specififc.h"
#include "../../../../platform/kaa_client.h"
#include "../../../../platform/ext_sha.h"
#include "../../../../platform/ext_transport_channel.h"

#define DEFAULT_ESP8266_CONTROLER_BUFFER_SIZE 512

#define KAA_DEMO_UPLOAD_COUNT_THRESHOLD 1 /* Count of collected serialized logs needed to initiate log upload */
#define KAA_DEMO_LOG_GENERATION_FREQUENCY 3 /* seconds */

static kaa_digest kaa_public_key_hash;

static kaa_service_t BOOTSTRAP_SERVICE[] = { KAA_SERVICE_BOOTSTRAP };
static const int BOOTSTRAP_SERVICE_COUNT = sizeof(BOOTSTRAP_SERVICE) / sizeof(kaa_service_t);
/*
* Define services which should be used.
* Don't define unused services, it may cause an error.
*/
static kaa_service_t OPERATIONS_SERVICES[] = { KAA_SERVICE_PROFILE
, KAA_SERVICE_CONFIGURATION
, KAA_SERVICE_USER
, KAA_SERVICE_EVENT
, KAA_SERVICE_LOGGING};
static const int OPERATIONS_SERVICES_COUNT = sizeof(OPERATIONS_SERVICES) / sizeof(kaa_service_t);


typedef enum {
    KAA_CLIENT_ESP8266_STATE_UNINITED = 0,
    KAA_CLIENT_ESP8266_STATE_INIT_OK,
    KAA_CLIENT_WIFI_STATE_UNCONNECTED,
    KAA_CLIENT_WIFI_STATE_CONNECTED,
} kaa_client_connection_state_t;

typedef enum {
    KAA_CLIENT_CHANNEL_STATE_NOT_CONNECTED = 0,
    KAA_CLIENT_CHANNEL_STATE_CONNECTED,
} kaa_client_channel_state_t;

struct kaa_client_t {
    kaa_client_connection_state_t connection_state;
    esp8266_t *controler;
    kaa_context_t *kaa_context;
    bool operate;
    kaa_transport_channel_interface_t channel;
    int channel_id;
    external_process_fn external_process_fn;
    void *external_process_context;
    time_t external_process_max_delay;
    time_t external_process_last_call;
    const char *wifi_ssid;
    const char *wifi_pswd;
    void *log_storage_context;
    void *log_upload_strategy_context;
};

kaa_error_t kaa_client_esp8266_error(kaa_client_t *kaa_client);
kaa_error_t kaa_init_security_stuff(const char *kaa_public_key, const size_t kaa_public_key_length);
kaa_error_t kaa_log_collector_init(kaa_client_t *kaa_client);

kaa_error_t kaa_client_state_process(kaa_client_t *kaa_client) {
    KAA_RETURN_IF_NIL(kaa_client, KAA_ERR_BADPARAM);

    kaa_error_t error_code = KAA_ERR_NONE;
    esp8266_error_t esp8266_error;
    switch (kaa_client->connection_state) {
    case KAA_CLIENT_ESP8266_STATE_UNINITED:
        esp8266_error = esp8266_init(kaa_client->controler);
        if (esp8266_error == ESP8266_ERR_NONE) {
            kaa_client->connection_state = KAA_CLIENT_ESP8266_STATE_INIT_OK;
            KAA_LOG_INFO(kaa_client->kaa_context->logger, KAA_ERR_NONE,
                    "ESP8266 initialized successfully");
        } else {
            KAA_LOG_ERROR(kaa_client->kaa_context->logger, KAA_ERR_NONE,
                    "ESP8266 initialization failed: %d", esp8266_error);
            esp8266_reset();
        }
        break;
    case KAA_CLIENT_ESP8266_STATE_INIT_OK:
        esp8266_error = esp8266_connect_wifi(kaa_client->controler,
                kaa_client->wifi_ssid, kaa_client->wifi_pswd);
        if (esp8266_error == ESP8266_ERR_NONE) {
            kaa_client->connection_state = KAA_CLIENT_WIFI_STATE_CONNECTED;
            KAA_LOG_INFO(kaa_client->kaa_context->logger, KAA_ERR_NONE,
                    "ESP8266 WiFi to %s network connected",
                    kaa_client->wifi_ssid);
        } else {
            KAA_LOG_ERROR(kaa_client->kaa_context->logger, KAA_ERR_NONE,
                    "ESP8266 connect to WiFi %s failed: %d",
                    kaa_client->wifi_ssid, esp8266_error);
            kaa_client->connection_state = KAA_CLIENT_WIFI_STATE_UNCONNECTED;
        }
        break;
    case KAA_CLIENT_WIFI_STATE_CONNECTED:
        break;
    default:
        kaa_client->connection_state = KAA_CLIENT_ESP8266_STATE_UNINITED;
        break;
    }

    return error_code;
}

kaa_error_t kaa_client_create(kaa_client_t **kaa_client,
        kaa_client_props_t *props) {
    KAA_RETURN_IF_NIL2(kaa_client, props, KAA_ERR_BADPARAM);

    kaa_error_t error_code = KAA_ERR_NONE;

    kaa_client_t *self = calloc(1, sizeof(kaa_client_t));
    KAA_RETURN_IF_NIL(self, KAA_ERR_NOMEM);

    esp8266_error_t esp8266_error = esp8266_create(&self->controler,
            props->serial, DEFAULT_ESP8266_CONTROLER_BUFFER_SIZE);
    if (esp8266_error) {
        debug("Error during esp8266 creation %d\n", esp8266_error);
        kaa_client_destroy(self);
        return KAA_ERR_BADDATA;
    }

    error_code = kaa_init_security_stuff(props->kaa_public_key, props->kaa_public_key_length);
    if (error_code) {
        debug("Error generate SHA1 diges form Public Key, error %d", error_code);
        kaa_client_destroy(self);
        return error_code;
    }

    error_code = kaa_init(&self->kaa_context);
    if (error_code) {
        debug("Error during Kaa context creation %d\n", error_code);
        kaa_client_destroy(self);
        return error_code;
    }

    KAA_LOG_INFO(self->kaa_context->logger, KAA_ERR_NONE,
            "Kaa framework initialized.");

    self->wifi_ssid = props->wifi_ssid;
    self->wifi_pswd = props->wifi_pswd;
    self->operate = true;

    error_code = kaa_log_collector_init(self);
    if (error_code) {
        KAA_LOG_ERROR(self->kaa_context->logger, error_code, "Failed to init Kaa log collector %d", error_code);
        kaa_client_destroy(self);
        return error_code;
    }

    KAA_LOG_INFO(self->kaa_context->logger, KAA_ERR_NONE, "Kaa log collector initialized.");

    *kaa_client = self;

    return error_code;
}

void kaa_client_destroy(kaa_client_t *self) {
    if (!self)
        return;

    self->controler = NULL;

    if (self->channel.context) {
        self->channel.destroy(self->channel.context);
        self->channel.context = NULL;
    }

//    if (self->log_storage_context) {
//        ext_log_storage_destroy(self->log_storage_context);
//        self->log_storage_context = NULL;
//    }

    if (self->kaa_context) {
        kaa_deinit(self->kaa_context);
        self->kaa_context = NULL;
    }

    free(self);
}

kaa_context_t* kaa_client_get_context(kaa_client_t *kaa_client) {
    KAA_RETURN_IF_NIL(kaa_client, NULL);
    return kaa_client->kaa_context;
}

kaa_error_t kaa_client_start(kaa_client_t *kaa_client,
        external_process_fn external_process, void *external_process_context,
        time_t max_delay) {
    KAA_RETURN_IF_NIL(kaa_client, KAA_ERR_BADPARAM);

    kaa_error_t error_code = KAA_ERR_NONE;

    kaa_client->external_process_fn = external_process;
    kaa_client->external_process_context = external_process_context;
    kaa_client->external_process_max_delay = max_delay;
    kaa_client->external_process_last_call = get_sys_milis();

    KAA_LOG_INFO(kaa_client->kaa_context->logger, KAA_ERR_NONE,
            "Starting Kaa client...");

    while (kaa_client->operate) {
        if ((get_sys_milis() - kaa_client->external_process_last_call)
                >= kaa_client->external_process_max_delay) {
            if (kaa_client->external_process_fn) {
                kaa_client->external_process_fn(
                        kaa_client->external_process_context);
            }
            kaa_client->external_process_last_call = get_sys_milis();
        }

        if (kaa_client->connection_state == KAA_CLIENT_WIFI_STATE_CONNECTED) {
            if ((get_sys_milis() / 500) % 2) {
                ledOn();
            } else {
                ledOff();
            }
            error_code = esp8266_process(kaa_client->controler,
                    kaa_client->external_process_max_delay);
            if (error_code) {
                kaa_client_esp8266_error(kaa_client);
            }
            //Check Kaa channel is ready to transmit something

        } else {
            kaa_client_state_process(kaa_client);
        }
    }

    KAA_LOG_INFO(kaa_client->kaa_context->logger, KAA_ERR_NONE,
            "Kaa client Stopped");

    return error_code;
}

kaa_error_t kaa_client_stop(kaa_client_t *kaa_client) {
    KAA_RETURN_IF_NIL(kaa_client, KAA_ERR_BADPARAM);

    kaa_error_t error_code = KAA_ERR_NONE;

    kaa_client->operate = false;

    KAA_LOG_INFO(kaa_client->kaa_context->logger, KAA_ERR_NONE,
            "Stopping Kaa client...");

    return error_code;
}

kaa_error_t kaa_client_esp8266_error(kaa_client_t *kaa_client) {
    KAA_RETURN_IF_NIL(kaa_client, KAA_ERR_BADPARAM);

    kaa_error_t error_code = KAA_ERR_NONE;

    KAA_LOG_INFO(kaa_client->kaa_context->logger, KAA_ERR_NONE,
            "ESP8266 Error found, reset.... and restart.");
    kaa_client->connection_state = KAA_CLIENT_ESP8266_STATE_UNINITED;

    ledOff();

    return error_code;
}

kaa_error_t kaa_init_security_stuff(const char *kaa_public_key, const size_t kaa_public_key_length)
{
    KAA_RETURN_IF_NIL2(kaa_public_key, kaa_public_key_length, KAA_ERR_BADPARAM);
    ext_calculate_sha_hash(kaa_public_key, kaa_public_key_length, kaa_public_key_hash);
    debug("SHA calculated\r\n");
    return KAA_ERR_NONE;
}

/*
* Initializes Kaa log collector.
*/
kaa_error_t kaa_log_collector_init(kaa_client_t *kaa_client)
{
    KAA_RETURN_IF_NIL(kaa_client, KAA_ERR_BADPARAM)
    kaa_error_t error_code = ext_unlimited_log_storage_create(
                &kaa_client->log_storage_context,
                kaa_client->kaa_context->logger);

    if (error_code) {
       KAA_LOG_ERROR(kaa_client->kaa_context->logger, error_code, "Failed to create log storage");
       return error_code;
    }

    error_code = ext_log_upload_strategy_by_volume_create(&kaa_client->log_upload_strategy_context
                                                                , kaa_client->kaa_context->channel_manager
                                                                , kaa_client->kaa_context->bootstrap_manager);
    if (error_code) {
        KAA_LOG_ERROR(kaa_client->kaa_context->logger, error_code, "Failed to create log upload strategy");
        return error_code;
    }

    error_code = ext_log_upload_strategy_by_volume_set_threshold_count(kaa_client->log_upload_strategy_context
                                                , KAA_DEMO_UPLOAD_COUNT_THRESHOLD);
    if (error_code) {
        KAA_LOG_ERROR(kaa_client->kaa_context->logger,
                                                error_code,
                                                "Failed to create log upload strategy by volume set threshold count to %d",
                                                KAA_DEMO_UPLOAD_COUNT_THRESHOLD);
        return error_code;
    }

    error_code = kaa_logging_init(kaa_client->kaa_context->log_collector
                                                , kaa_client->log_storage_context
                                                , kaa_client->log_upload_strategy_context);
    if (error_code) {
        KAA_LOG_ERROR(kaa_client->kaa_context->logger, error_code,"Failed to logging init");
        return error_code;
    }

    KAA_LOG_INFO(kaa_client->kaa_context->logger, KAA_ERR_NONE, "Log collector init complete");
    return error_code;
}
