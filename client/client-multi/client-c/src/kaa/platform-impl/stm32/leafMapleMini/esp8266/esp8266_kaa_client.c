/**
 *  Copyright 2014-2016 CyberVision, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
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
#include <string.h>

#include "../leaf_time.h"
#include "../leaf_stdio.h"
#include "../../../../kaa_error.h"
#include "../../../../kaa_common.h"
#include "../../../../kaa.h"
#include "../../../../kaa_context.h"
#include "../../../../kaa_configuration_manager.h"
#include "../../../../utilities/kaa_log.h"
#include "esp8266.h"
#include "chip_specififc.h"
#include "../../../../platform/kaa_client.h"
#include "../../../../platform/ext_sha.h"
#include "../../../../platform/ext_transport_channel.h"
#include "../../../../platform-impl/common/ext_log_upload_strategies.h"
#include "esp8266_kaa_tcp_channel.h"



#define DEFAULT_ESP8266_CONTROLER_BUFFER_SIZE 512

#define KAA_DEMO_UPLOAD_COUNT_THRESHOLD 1 /* Count of collected serialized logs needed to initiate log upload */

#define KAA_DEMO_TWO_DAYS_UPLOAD_TIMEOUT 2 * 24 * 60 * 60



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

/* Logging constraints */
#define MAX_LOG_COUNT           SIZE_MAX
#define MAX_LOG_BUCKET_SIZE     (KAA_TCP_CHANNEL_OUT_BUFFER_SIZE >> 3)

_Static_assert(MAX_LOG_BUCKET_SIZE, "Maximum bucket size cannot be 0!");

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

typedef enum {
    KAA_CLIENT_CHANNEL_TYPE_BOOTSTRAP = 0,
    KAA_CLIENT_CHANNEL_TYPE_OPERATIONS,
} kaa_client_channel_type_t;

struct kaa_client_t {
    kaa_client_connection_state_t         connection_state;
    esp8266_t                             *controler;
    kaa_context_t                         *kaa_context;
    bool                                  operate;
    kaa_transport_channel_interface_t     channel;
    kaa_client_channel_state_t            channel_state;
    uint32_t                              channel_id;
    int                                   channel_fd;
    bool                                  boostrap_complete;
    external_process_fn                   external_process_fn;
    void                                  *external_process_context;
    time_t                                external_process_max_delay;
    time_t                                external_process_last_call;
    const char                            *wifi_ssid;
    const char                            *wifi_pswd;
    void                                  *log_storage_context;
    void                                  *log_upload_strategy_context;
    uint32_t                              blink_timeout;
    uint8                                 blink_prev;
};



kaa_error_t kaa_client_esp8266_error(kaa_client_t *kaa_client);
kaa_error_t kaa_init_security_stuff(const char *kaa_public_key, const size_t kaa_public_key_length);
kaa_error_t kaa_log_collector_init(kaa_client_t *kaa_client);
kaa_error_t kaa_client_channel_error(kaa_client_t *kaa_client);
kaa_error_t kaa_client_init_channel(kaa_client_t *kaa_client, kaa_client_channel_type_t channel_type);
kaa_error_t kaa_client_deinit_channel(kaa_client_t *kaa_client);



void esp8266_tcp_receive_fn(void *context, int id, const uint8 *buffer, const int receive_size)
{
    if (!context)
        return;

    kaa_client_t *kaa_client = (kaa_client_t *)context;
    kaa_error_t error;

    if (buffer) {
        if (receive_size > 0) {
            KAA_LOG_TRACE(kaa_client->kaa_context->logger, KAA_ERR_NONE, "Kaa channel(0x%08X) receive %d bytes",
                                                                            kaa_client->channel_id, receive_size);
            error = kaa_tcp_channel_read_bytes(&kaa_client->channel, buffer, receive_size);
            if (error) {
                KAA_LOG_ERROR(kaa_client->kaa_context->logger, error, "Kaa channel error reading bytes");
            }
        }

    }
    if (receive_size == -1) {
       KAA_LOG_TRACE(kaa_client->kaa_context->logger, KAA_ERR_NONE, "Kaa channel(0x%08X) connection termined by peer",
                                                                                               kaa_client->channel_id);
        error = kaa_client_channel_error(kaa_client);
       if (error) {
           KAA_LOG_ERROR(kaa_client->kaa_context->logger, error, "Kaa channel error dropping connection");
       }

   }
}

kaa_error_t kaa_client_state_process(kaa_client_t *kaa_client)
{
    KAA_RETURN_IF_NIL(kaa_client, KAA_ERR_BADPARAM);

    kaa_error_t error_code = KAA_ERR_NONE;
    esp8266_error_t esp8266_error;
    switch (kaa_client->connection_state) {
    case KAA_CLIENT_ESP8266_STATE_UNINITED:
        esp8266_error = esp8266_init(kaa_client->controler);
        if (esp8266_error == ESP8266_ERR_NONE) {
            kaa_client->connection_state = KAA_CLIENT_ESP8266_STATE_INIT_OK;
            KAA_LOG_INFO(kaa_client->kaa_context->logger, KAA_ERR_NONE, "ESP8266 initialized successfully");
        } else {
            KAA_LOG_ERROR(kaa_client->kaa_context->logger, KAA_ERR_NONE, "ESP8266 initialization failed: %d", esp8266_error);
            esp8266_reset();
        }
        break;
    case KAA_CLIENT_ESP8266_STATE_INIT_OK:
        ledOn();
        esp8266_error = esp8266_connect_wifi(kaa_client->controler,
                kaa_client->wifi_ssid, kaa_client->wifi_pswd);
        if (esp8266_error == ESP8266_ERR_NONE) {
            kaa_client->connection_state = KAA_CLIENT_WIFI_STATE_CONNECTED;
            KAA_LOG_INFO(kaa_client->kaa_context->logger, KAA_ERR_NONE, "ESP8266 WiFi to %s network connected",
                                                                                        kaa_client->wifi_ssid);
        } else {
            KAA_LOG_ERROR(kaa_client->kaa_context->logger, KAA_ERR_NONE, "ESP8266 connect to WiFi %s failed: %d",
                                                                                kaa_client->wifi_ssid, esp8266_error);
            kaa_client->connection_state = KAA_CLIENT_WIFI_STATE_UNCONNECTED;
        }
        ledOff();
        break;
    case KAA_CLIENT_WIFI_STATE_CONNECTED:
        break;
    default:
        kaa_client->connection_state = KAA_CLIENT_ESP8266_STATE_UNINITED;
        break;
    }

    return error_code;
}

kaa_error_t kaa_client_create(kaa_client_t **kaa_client, kaa_client_props_t *props)
{
    KAA_RETURN_IF_NIL2(kaa_client, props, KAA_ERR_BADPARAM);

    kaa_error_t error_code = KAA_ERR_NONE;

    kaa_client_t *self = calloc(1, sizeof(kaa_client_t));
    KAA_RETURN_IF_NIL(self, KAA_ERR_NOMEM);

    esp8266_error_t esp8266_error = esp8266_create(&self->controler, props->serial, DEFAULT_ESP8266_CONTROLER_BUFFER_SIZE);
    if (esp8266_error) {
        debug("Error during esp8266 creation %d\n", esp8266_error);
        kaa_client_destroy(self);
        return KAA_ERR_BADDATA;
    }

    esp8266_error = esp8266_tcp_register_receive_callback(self->controler, esp8266_tcp_receive_fn, (void *)self);
    if (esp8266_error) {
        debug("Error during esp8266 registering receive callback %d\n", esp8266_error);
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

    KAA_LOG_INFO(self->kaa_context->logger, KAA_ERR_NONE, "Kaa framework initialized.");

    self->wifi_ssid = props->wifi_ssid;
    self->wifi_pswd = props->wifi_pswd;
    self->operate = true;
    self->blink_timeout = 500;

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

void kaa_client_destroy(kaa_client_t *self)
{
    if (!self)
        return;

    self->controler = NULL;

    if (self->channel.context) {
        self->channel.destroy(self->channel.context);
        self->channel.context = NULL;
    }

    if (self->channel.context) {
        self->channel.destroy(self->channel.context);
        self->channel.context = NULL;
    }

    if (self->log_storage_context) {
        ext_log_storage_destroy(self->log_storage_context);
        self->log_storage_context = NULL;
    }

    if (self->kaa_context) {
        kaa_deinit(self->kaa_context);
        self->kaa_context = NULL;
    }

    free(self);
}

kaa_context_t* kaa_client_get_context(kaa_client_t *kaa_client)
{
    KAA_RETURN_IF_NIL(kaa_client, NULL);
    return kaa_client->kaa_context;
}

kaa_error_t configuration_update(void *context, const kaa_root_configuration_t *configuration)
{
    KAA_RETURN_IF_NIL(context, KAA_ERR_BADPARAM);
    KAA_LOG_INFO(((kaa_client_t *)context)->kaa_context->logger, KAA_ERR_NONE, "New configuration received");
    return KAA_ERR_NONE;
}


kaa_error_t kaa_client_process_channel_connected(kaa_client_t *kaa_client)
{
    KAA_RETURN_IF_NIL(kaa_client, KAA_ERR_BADPARAM);

    kaa_error_t error_code = KAA_ERR_NONE;
    esp8266_error_t esp_error = ESP8266_ERR_NONE;


    error_code = kaa_tcp_channel_check_keepalive(&kaa_client->channel);
    if (error_code) {
        KAA_LOG_ERROR(kaa_client->kaa_context->logger, error_code, "Kaa tcp channel error check keepalive");
    }

    int tmp = (get_sys_milis() / kaa_client->blink_timeout) % 2;
    if (kaa_client->blink_prev != (uint8)tmp) {
        kaa_client->blink_prev = (uint8)tmp;
        if (kaa_client->blink_prev) {
            ledOn();
            if (kaa_client->boostrap_complete) {
                KAA_LOG_INFO(kaa_client->kaa_context->logger, KAA_ERR_NONE, "Process Bootstrap channel, state connected");
            } else {
                KAA_LOG_INFO(kaa_client->kaa_context->logger, KAA_ERR_NONE, "Process Operation channel, state connected");
            }
        } else
            ledOff();
    }

    uint8 * buffer = NULL;
    size_t buffer_size = 0;
    error_code = kaa_tcp_channel_get_buffer_for_send(&kaa_client->channel, &buffer, &buffer_size);
    if (error_code) {
        KAA_LOG_ERROR(kaa_client->kaa_context->logger, error_code, "Kaa tcp channel get buffer for send failed");
        kaa_client_channel_error(kaa_client);
    }
    if (buffer_size > 0) {
        esp_error = esp8266_send_tcp(kaa_client->controler, kaa_client->channel_fd, buffer, buffer_size);
        if (esp_error) {
            if (esp_error != ESP8266_ERR_COMMAND_BUSY) {
                KAA_LOG_ERROR(kaa_client->kaa_context->logger,
                        KAA_ERR_NONE, "ESP8266 send tcp failed, code: %d", esp_error);
                kaa_client_channel_error(kaa_client);
            }
        } else {
            KAA_LOG_TRACE(kaa_client->kaa_context->logger, KAA_ERR_NONE,
                    "Channel(0x%08X) sending %lu bytes", kaa_client->channel_id, buffer_size);
            error_code = kaa_tcp_channel_free_send_buffer(&kaa_client->channel, buffer_size);
            if (error_code) {
                KAA_LOG_ERROR(kaa_client->kaa_context->logger, error_code, "Kaa tcp channel error free buffer");
            }
        }
    }
    if (kaa_tcp_channel_connection_is_ready_to_terminate(&kaa_client->channel)) {
        esp_error = esp8266_disconnect_tcp(kaa_client->controler, kaa_client->channel_fd);
        if (esp_error) {
            KAA_LOG_ERROR(kaa_client->kaa_context->logger, KAA_ERR_NONE, "ESP8266 send tcp failed, code: %d", esp_error);
        }
        KAA_LOG_INFO(kaa_client->kaa_context->logger, KAA_ERR_NONE,
                    "Channel(0x%08X) connection terminated", kaa_client->channel_id);

        error_code = kaa_client_channel_error(kaa_client);
        if (error_code) {
            KAA_LOG_ERROR(kaa_client->kaa_context->logger, error_code, "Kaa tcp channel error");
        }
    }


    return error_code;
}

kaa_error_t kaa_client_process_channel_disconnected(kaa_client_t *kaa_client)
{
    KAA_RETURN_IF_NIL(kaa_client, KAA_ERR_BADPARAM);

    kaa_error_t error_code = KAA_ERR_NONE;
    esp8266_error_t esp_error = ESP8266_ERR_NONE;

    char *hostname = NULL;
    size_t hostname_size = 0;
    uint16_t port = 0;
    error_code = kaa_tcp_channel_get_access_point(&kaa_client->channel, &hostname, &hostname_size, &port);
    if (error_code) {
        KAA_LOG_ERROR(kaa_client->kaa_context->logger, error_code, "Kaa tcp channel get access point failed");
        kaa_client_channel_error(kaa_client);
    }
    if (hostname && hostname_size && port) {
        esp_error = esp8266_connect_tcp(kaa_client->controler,
                hostname, hostname_size, port, &kaa_client->channel_fd);
        if (esp_error) {
            KAA_LOG_ERROR(kaa_client->kaa_context->logger,
                    KAA_ERR_NONE, "ESP8266 connect tcp failed, code: %d", esp_error);
            kaa_client_channel_error(kaa_client);
            //Need to reinitialize WiFi connection and ESP
            kaa_client->connection_state = KAA_CLIENT_ESP8266_STATE_UNINITED;

        } else {
            KAA_LOG_INFO(kaa_client->kaa_context->logger, KAA_ERR_NONE,
                        "Channel(0x%08X) connected to  port %d, fd=%d", kaa_client->channel_id,
                        port, kaa_client->channel_fd);
            kaa_client->channel_state = KAA_CLIENT_CHANNEL_STATE_CONNECTED;
            error_code = kaa_tcp_channel_connected(&kaa_client->channel);
        }
    }


    return error_code;
}

kaa_error_t kaa_client_start(kaa_client_t *kaa_client
                           , external_process_fn external_process
                           , void *external_process_context
                           , kaa_time_t max_delay)
{
    KAA_RETURN_IF_NIL(kaa_client, KAA_ERR_BADPARAM);

    kaa_error_t error_code = KAA_ERR_NONE;
    esp8266_error_t esp_error = ESP8266_ERR_NONE;

    kaa_client->external_process_fn = external_process;
    kaa_client->external_process_context = external_process_context;
    kaa_client->external_process_max_delay = max_delay;
    kaa_client->external_process_last_call = get_sys_milis();

    KAA_LOG_INFO(kaa_client->kaa_context->logger, KAA_ERR_NONE, "Starting Kaa client...");

    const kaa_configuration_root_receiver_t config_receiver = { kaa_client, configuration_update };
    error_code = kaa_configuration_manager_set_root_receiver(kaa_client->kaa_context->configuration_manager, &config_receiver);
    if (error_code) {
        KAA_LOG_ERROR(kaa_client->kaa_context->logger, error_code, "Error registering Configuration root receiver");
        return error_code;
    }

    const kaa_root_configuration_t *config = kaa_configuration_manager_get_configuration(kaa_client->kaa_context->configuration_manager);
    if (config) {
        configuration_update(kaa_client, config);
    }

    uint8 *buffer = NULL;
    size_t buffer_size = 0;
    int send_c = 0;
    int tmp;

    while (kaa_client->operate) {
        if ((get_sys_milis() - kaa_client->external_process_last_call)
                >= kaa_client->external_process_max_delay) {
            if (kaa_client->external_process_fn) {
                kaa_client->external_process_fn(kaa_client->external_process_context);
            }
            kaa_client->external_process_last_call = get_sys_milis();
        }

        if (kaa_client->connection_state == KAA_CLIENT_WIFI_STATE_CONNECTED) {
            esp_error = esp8266_process(kaa_client->controler,
                                            kaa_client->external_process_max_delay);
            if (esp_error) {
                KAA_LOG_ERROR(kaa_client->kaa_context->logger, KAA_ERR_NONE, "ESP8266 process failed code: %d", esp_error);
                kaa_client_esp8266_error(kaa_client);
            }
            //Check Kaa channel is ready to transmit something
            if (kaa_client->channel_id > 0) {
                if (kaa_client->channel_state == KAA_CLIENT_CHANNEL_STATE_NOT_CONNECTED) {
                    error_code = kaa_client_process_channel_disconnected(kaa_client);
                } else  if (kaa_client->channel_state == KAA_CLIENT_CHANNEL_STATE_CONNECTED) {
                    error_code = kaa_client_process_channel_connected(kaa_client);
                }
            } else {
                //No initialized channels
                if (kaa_client->boostrap_complete) {
                    KAA_LOG_INFO(kaa_client->kaa_context->logger, KAA_ERR_NONE,
                                "Channel(0x%08X) Boostrap complete, reinitializing to Operations ...", kaa_client->channel_id);
                    kaa_client->boostrap_complete = false;
                    kaa_client_deinit_channel(kaa_client);
                    kaa_client_init_channel(kaa_client, KAA_CLIENT_CHANNEL_TYPE_OPERATIONS);
                } else {
                    KAA_LOG_INFO(kaa_client->kaa_context->logger, KAA_ERR_NONE,
                                "Channel(0x%08X) Operations error, reinitializing to Bootstrap ...", kaa_client->channel_id);
                    kaa_client->boostrap_complete = true;
                    kaa_client_deinit_channel(kaa_client);

                    kaa_client_init_channel(kaa_client, KAA_CLIENT_CHANNEL_TYPE_BOOTSTRAP);
                }
            }
        } else {
            kaa_client_state_process(kaa_client);
        }
    }

    KAA_LOG_INFO(kaa_client->kaa_context->logger, KAA_ERR_NONE, "Kaa client Stopped");

    return error_code;
}

kaa_error_t kaa_client_stop(kaa_client_t *kaa_client)
{
    KAA_RETURN_IF_NIL(kaa_client, KAA_ERR_BADPARAM);

    kaa_client->operate = false;

    KAA_LOG_INFO(kaa_client->kaa_context->logger, KAA_ERR_NONE, "Stopping Kaa client...");

    return kaa_stop(kaa_client->kaa_context);
}

kaa_error_t kaa_client_channel_error(kaa_client_t *kaa_client)
{
    KAA_RETURN_IF_NIL(kaa_client, KAA_ERR_BADPARAM);

    kaa_error_t error_code = KAA_ERR_NONE;

    ledOff();

    KAA_LOG_INFO(kaa_client->kaa_context->logger, KAA_ERR_NONE, "Kaa tcp channel error");
    kaa_client->channel_state = KAA_CLIENT_CHANNEL_STATE_NOT_CONNECTED;

    if (kaa_client->channel_id > 0 ) {
        error_code = kaa_tcp_channel_disconnected(&kaa_client->channel);
    }

    kaa_client_deinit_channel(kaa_client);

    return error_code;
}

kaa_error_t kaa_client_esp8266_error(kaa_client_t *kaa_client)
{
    KAA_RETURN_IF_NIL(kaa_client, KAA_ERR_BADPARAM);

    kaa_error_t error_code = KAA_ERR_NONE;

    KAA_LOG_INFO(kaa_client->kaa_context->logger, KAA_ERR_NONE, "ESP8266 Error found, reset.... and restart.");

    error_code = kaa_client_channel_error(kaa_client);

    kaa_client->connection_state = KAA_CLIENT_ESP8266_STATE_UNINITED;

    ledOff();

    return error_code;
}

kaa_error_t kaa_client_init_channel(kaa_client_t *kaa_client, kaa_client_channel_type_t channel_type)
{
    KAA_RETURN_IF_NIL(kaa_client, KAA_ERR_BADPARAM);

    kaa_error_t error_code = KAA_ERR_NONE;

    KAA_LOG_TRACE(kaa_client->kaa_context->logger, KAA_ERR_NONE, "Initializing channel....");

    switch (channel_type) {
        case KAA_CLIENT_CHANNEL_TYPE_BOOTSTRAP:
            error_code = kaa_tcp_channel_create(&kaa_client->channel,
                    kaa_client->kaa_context->logger,
                    BOOTSTRAP_SERVICE, BOOTSTRAP_SERVICE_COUNT);
            break;
        case KAA_CLIENT_CHANNEL_TYPE_OPERATIONS:
            error_code = kaa_tcp_channel_create(&kaa_client->channel
                                              , kaa_client->kaa_context->logger
                                              , OPERATIONS_SERVICES
                                              , OPERATIONS_SERVICES_COUNT);
            break;
    }
    if (error_code) {
        KAA_LOG_ERROR(kaa_client->kaa_context->logger, error_code, "Error initializing channel %d", channel_type);
        return error_code;
    }

    error_code = kaa_channel_manager_add_transport_channel(kaa_client->kaa_context->channel_manager,
                                            &kaa_client->channel,
                                            &kaa_client->channel_id);
    if (error_code) {
        KAA_LOG_ERROR(kaa_client->kaa_context->logger, error_code, "Error register channel %d as transport", channel_type);
        return error_code;
    }

    KAA_LOG_INFO(kaa_client->kaa_context->logger, KAA_ERR_NONE, "Channel(type=%d,id=%08X) initialized successfully"
                                                                                , channel_type, kaa_client->channel_id);

    char *hostname = NULL;
    size_t hostname_size = 0;
    uint16_t port = 0;

    error_code = kaa_tcp_channel_get_access_point(&kaa_client->channel, &hostname, &hostname_size, &port);
    if (error_code) {
        KAA_LOG_ERROR(kaa_client->kaa_context->logger, error_code, "Kaa tcp channel get access point failed");
    } else if (hostname_size > 0){
        char *n_hostname = strndup(hostname, hostname_size);
        KAA_LOG_INFO(kaa_client->kaa_context->logger, KAA_ERR_NONE,
                        "Channel(type=%d,id=%08X) destination %s:%d", channel_type
                        , kaa_client->channel_id, n_hostname, port);

        free(n_hostname);
    }

    return error_code;
}

kaa_error_t kaa_client_deinit_channel(kaa_client_t *kaa_client)
{
    KAA_RETURN_IF_NIL(kaa_client, KAA_ERR_BADPARAM);

    kaa_error_t error_code = KAA_ERR_NONE;

    if (kaa_client->channel_id == 0)
        return KAA_ERR_NONE;

    KAA_LOG_TRACE(kaa_client->kaa_context->logger, KAA_ERR_NONE, "Deinitializing channel....");

    error_code = kaa_channel_manager_remove_transport_channel(
            kaa_client->kaa_context->channel_manager,kaa_client->channel_id);
    if (error_code) {
        KAA_LOG_TRACE(kaa_client->kaa_context->logger, error_code, "Bootstrap channel error removing from channel manager");
        return error_code;
    }

    kaa_client->channel_id = 0;
    kaa_client->channel.context = NULL;
    kaa_client->channel.destroy = NULL;
    kaa_client->channel.get_protocol_id = NULL;
    kaa_client->channel.get_supported_services = NULL;
    kaa_client->channel.init = NULL;
    kaa_client->channel.set_access_point = NULL;
    kaa_client->channel.sync_handler = NULL;

    KAA_LOG_INFO(kaa_client->kaa_context->logger, KAA_ERR_NONE, "Channel deinitialized successfully");


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
    KAA_RETURN_IF_NIL(kaa_client, KAA_ERR_BADPARAM);
    kaa_error_t error_code = ext_unlimited_log_storage_create(&kaa_client->log_storage_context
                                                            , kaa_client->kaa_context->logger);

    if (error_code) {
       KAA_LOG_ERROR(kaa_client->kaa_context->logger, error_code, "Failed to create log storage");
       return error_code;
    }

    error_code = ext_log_upload_strategy_create(kaa_client->kaa_context
                                              ,&kaa_client->log_upload_strategy_context
                                              , KAA_LOG_UPLOAD_VOLUME_STRATEGY);
    if (error_code) {
        KAA_LOG_ERROR(kaa_client->kaa_context->logger, error_code, "Failed to create log upload strategy");
        return error_code;
    }

    // Due to unknown problems with networking via ESP8266, some server responses are lost.
    // It leads to log delivery timeouts.
    error_code = ext_log_upload_strategy_set_upload_timeout(kaa_client->log_upload_strategy_context
                                                                    , KAA_DEMO_TWO_DAYS_UPLOAD_TIMEOUT);
    if (error_code) {
        KAA_LOG_ERROR(kaa_client->kaa_context->logger,
                                                error_code,
                                                "Failed to create log upload strategy by volume set upload timeout to %d",
                                                KAA_DEMO_TWO_DAYS_UPLOAD_TIMEOUT);
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
        .max_bucket_size = MAX_LOG_BUCKET_SIZE,
        .max_bucket_log_count = MAX_LOG_COUNT,
    };

    error_code = kaa_logging_init(kaa_client->kaa_context->log_collector
                                                , kaa_client->log_storage_context
                                                , kaa_client->log_upload_strategy_context
                                                , &bucket_sizes);
    if (error_code) {
        KAA_LOG_ERROR(kaa_client->kaa_context->logger, error_code,"Failed to logging init");
        return error_code;
    }

    KAA_LOG_INFO(kaa_client->kaa_context->logger, KAA_ERR_NONE, "Log collector init complete");
    return error_code;
}

// dummy method
void ext_configuration_delete(void)
{}
