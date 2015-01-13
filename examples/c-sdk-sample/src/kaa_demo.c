/*
 * Copyright 2014 CyberVision, Inc.
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

#include <stdio.h>
#include <stdint.h>
#include <unistd.h>
#include <fcntl.h>
#include <errno.h>
#include <string.h>
#include <stdbool.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <sys/time.h>
#include <sys/select.h>
#include <netdb.h>
#include <stdlib.h>
#include <signal.h>
#include <execinfo.h>

#include <openssl/rsa.h>
#include <openssl/pem.h>

#include <kaatcp.h>

#include <kaa.h>
#include <kaa_common.h>
#include <kaa_channel_manager.h>
#include <kaa_common_schema.h>
#include <kaa_context.h>
#include <kaa_defaults.h>
#include <kaa_event.h>
#include <kaa_external.h>
#include <kaa_platform_common.h>
#include <kaa_platform_protocol.h>
#include <kaa_profile.h>
#include <kaa_user.h>
#include <kaa_logging.h>
#include <log/kaa_memory_log_storage.h>
#include <utilities/kaa_log.h>
#include <utilities/kaa_mem.h>

#define SAMPLE_PROFILE_ID "sampleid"
#define SAMPLE_OS_VERSION "1.0"
#define SAMPLE_BUILD_INFO "3cbaf67e"

#define SAMPLE_LOG_TAG     "Log tag"
#define SAMPLE_LOG_MESSAGE "Sample log message"

#define SOCKET_READ_BUFFER_SIZE 1024
#define SOCKET_WRITE_BUFFER_SIZE 1024

// 200 seconds
#define KEEP_ALIVE_TIMEOUT 200
// 100 seconds
#define PING_TIMEOUT       100

#define KAA_KEY_STORAGE "key.txt"
#define KAA_STATUS_STORAGE "status.conf"

#define KAA_THRESHOLD_RECORD_COUNT   1

static kaa_service_t SUPPORTED_SERVICES[] = { KAA_SERVICE_BOOTSTRAP, KAA_SERVICE_PROFILE, KAA_SERVICE_USER, KAA_SERVICE_EVENT, KAA_SERVICE_LOGGING };
static const int SUPPORTED_SERVICES_COUNT = 5;

static char socket_read_buffer[SOCKET_READ_BUFFER_SIZE];
static char socket_write_buffer[SOCKET_WRITE_BUFFER_SIZE];

static bool is_shutdown = false;
static bool is_connected = false;

static kaatcp_parser_t kaatcp_parser;

static kaatcp_bootstrap_response_t *operation_servers = NULL;
static uint32_t current_server_index = 0;

static int kaa_client_socket = -1;

static char *kaa_public_key = NULL;
static uint32_t kaa_public_key_length = 0;
static kaa_digest kaa_public_key_hash;

kaa_context_t *kaa_context_ = NULL;
kaa_memory_log_storage_t *log_storage = NULL;
kaa_log_storage_t log_storage_interface;

static kaa_log_upload_properties_t kaa_log_upload_properties = {
      128   /**< max_log_block_size */
    , 256   /**< max_log_upload_threshold */
    , 1024  /**< max_log_storage_volume */
};

static kaa_log_upload_decision_t is_log_upload_needed(void *context, const kaa_log_storage_t *log_storage);

static kaa_log_upload_strategy_t kaa_log_upload_strategy = { &kaa_log_upload_properties, &is_log_upload_needed };

void kaa_read_status_ext(char **buffer, size_t *buffer_size, bool *needs_deallocation)
{
    *buffer = NULL;
    *buffer_size = 0;
    *needs_deallocation = true;

    FILE* status_file = fopen(KAA_STATUS_STORAGE, "rb");

    if (!status_file) {
        return;
    }

    fseek(status_file, 0, SEEK_END);
    *buffer_size = ftell(status_file);
    *buffer = (char*) KAA_MALLOC(*buffer_size);

    if (*buffer == NULL) {
        return;
    }

    *needs_deallocation = true;
    fseek(status_file, 0, SEEK_SET);
    if (fread(*buffer, *buffer_size, 1, status_file) == 0) {
        free(*buffer);
        return;
    }

    fclose(status_file);
}

void kaa_store_status_ext(const char *buffer, size_t buffer_size)
{
    if (!buffer || buffer_size == 0) {
        return;
    }

    FILE* status_file = fopen(KAA_STATUS_STORAGE, "wb");

    if (status_file) {
        fwrite(buffer, buffer_size, 1, status_file);
        fclose(status_file);
    }
}

void kaa_get_endpoint_public_key(char **buffer, size_t *buffer_size, bool *needs_deallocation)
{
    *buffer = kaa_public_key;
    *buffer_size = kaa_public_key_length;
    *needs_deallocation = false;
}

typedef struct kaa_demo_sized_buffer_t {
    char *buffer;
    size_t buffer_size;
} kaa_demo_sized_buffer_t;

static char * demo_allocator(void *allocation_context, size_t buffer_size)
{
    kaa_demo_sized_buffer_t *buf_ptr = (kaa_demo_sized_buffer_t *)allocation_context;
    buf_ptr->buffer = (char *)malloc(sizeof(char) * buffer_size);
    buf_ptr->buffer_size = buffer_size;
    return buf_ptr->buffer;
}

int kaa_demo_create_socket_and_connect(const char *hostname, uint16_t port)
{
    if (!hostname || !port) {
        KAA_LOG_ERROR(kaa_context_->logger, KAA_ERR_BADPARAM, "Bad params for connect: hostname=%p, port=%u", hostname, port);
        return -1;
    }
    struct addrinfo *server_info;

    struct addrinfo hints;
    memset(&hints, 0, sizeof(struct addrinfo));
    hints.ai_socktype = SOCK_STREAM;

    char port_str[6];
    snprintf(port_str, 6, "%u", port);
    int addrinfo_rval = getaddrinfo(hostname, port_str, &hints, &server_info);
    if (addrinfo_rval) {
        KAA_LOG_ERROR(kaa_context_->logger, KAA_ERR_NOT_FOUND, "Failed to resolve server %s:%u. %s", hostname, port, gai_strerror(addrinfo_rval));
        return addrinfo_rval;
    }

    kaa_client_socket = socket(server_info->ai_family, SOCK_STREAM, 0);
    if (kaa_client_socket < 0) {
        KAA_LOG_ERROR(kaa_context_->logger, KAA_ERR_BADPARAM, "Failed to create socket for server %s:%u. %s", hostname, port, strerror(errno));
        freeaddrinfo(server_info);
        return -1;
    }

    KAA_LOG_DEBUG(kaa_context_->logger, KAA_ERR_NONE, "For server %s:%u created socket %d", hostname, port, kaa_client_socket);

    int connect_rval = connect(kaa_client_socket, server_info->ai_addr, server_info->ai_addrlen);
    if (connect_rval) {
        KAA_LOG_ERROR(kaa_context_->logger, KAA_ERR_WRITE_FAILED, "Failed to connect to server %s:%u, socket %d. %s", hostname, port, kaa_client_socket, strerror(errno));
        freeaddrinfo(server_info);
        return connect_rval;
    }
    freeaddrinfo(server_info);

    KAA_LOG_DEBUG(kaa_context_->logger, KAA_ERR_NONE, "Successfully connected to server %s:%u, socket %d", hostname, port, kaa_client_socket);
    return 0;
}

void kaa_demo_close_socket()
{
    if (kaa_client_socket >= 0) {
        KAA_LOG_DEBUG(kaa_context_->logger, KAA_ERR_NONE, "Closing socket %d", kaa_client_socket);
        is_connected = false;
        close(kaa_client_socket);
        kaa_client_socket = -1;
        kaatcp_parser_reset(&kaatcp_parser);
    }
}

void kaa_demo_destroy_operation_servers()
{
    if (operation_servers) {
        kaatcp_parser_bootstrap_destroy(operation_servers);
        operation_servers = NULL;
    }
}

int kaatcp_write_to_socket(uint32_t buf_size)
{
    KAA_LOG_TRACE(kaa_context_->logger, KAA_ERR_NONE, "Writing %u bytes to socket %d", buf_size, kaa_client_socket);
    if (write(kaa_client_socket, socket_write_buffer, buf_size) < 0) {
        KAA_LOG_TRACE(kaa_context_->logger, KAA_ERR_NONE, "Failed to write to socket %d. %s", kaa_client_socket, strerror(errno));
        return -1;
    }
    return 0;
}

int kaatcp_send_connect_message()
{
    kaa_demo_sized_buffer_t buf;
    buf.buffer = NULL;
    buf.buffer_size = 0;

    char *buffer = NULL;
    size_t buffer_size = 0;
    kaa_serialize_info_t serialize_info = { SUPPORTED_SERVICES, SUPPORTED_SERVICES_COUNT, &demo_allocator, &buf };
    kaa_error_t error_code = kaa_platform_protocol_serialize_client_sync(kaa_context_->platfrom_protocol
                                                                       , &serialize_info
                                                                       , &buffer
                                                                       , &buffer_size);

    if (error_code != KAA_ERR_NONE) {
        KAA_FREE(buf.buffer);
        return error_code;
    }

    kaatcp_connect_t message;
    kaatcp_fill_connect_message(KEEP_ALIVE_TIMEOUT, KAA_PLATFORM_PROTOCOL_ID, buffer, buffer_size, NULL, 0, NULL, 0, &message);
    KAA_LOG_DEBUG(kaa_context_->logger, KAA_ERR_NONE, "Sending Connect message (socket %d)", kaa_client_socket);
    uint32_t message_size = SOCKET_WRITE_BUFFER_SIZE;
    kaatcp_error_t rval = kaatcp_get_request_connect(&message, socket_write_buffer, &message_size);
    if (rval) {
        KAA_FREE(buf.buffer);
        return rval;
    }
    int write_error = kaatcp_write_to_socket(message_size);
    KAA_FREE(buf.buffer);
    return write_error;
}

int kaatcp_send_ping_message()
{
    KAA_LOG_DEBUG(kaa_context_->logger, KAA_ERR_NONE, "Sending Ping message (socket %d)", kaa_client_socket);
    uint32_t message_size = SOCKET_WRITE_BUFFER_SIZE;
    kaatcp_error_t rval = kaatcp_get_request_ping(socket_write_buffer, &message_size);
    if (rval) {
        KAA_LOG_ERROR(kaa_context_->logger, KAA_ERR_WRITE_FAILED, "Failed to send Ping message (socket %d)", kaa_client_socket);
        return rval;
    }
    return kaatcp_write_to_socket(message_size);
}

int kaatcp_send_bootstrap_message()
{
    kaatcp_bootstrap_request_t message;
    kaatcp_fill_bootstrap_message(APPLICATION_TOKEN, 0, &message);
    KAA_LOG_DEBUG(kaa_context_->logger, KAA_ERR_NONE, "Sending Bootstrap request message (Token=\"%s\") (socket %d)", APPLICATION_TOKEN, kaa_client_socket);
    uint32_t message_size = SOCKET_WRITE_BUFFER_SIZE;
    kaatcp_error_t rval = kaatcp_get_request_bootstrap(&message, socket_write_buffer, &message_size);
    if (rval) {
        KAA_LOG_ERROR(kaa_context_->logger, KAA_ERR_WRITE_FAILED, "Failed to send Bootstrap message (socket %d)", kaa_client_socket);
        return rval;
    }
    return kaatcp_write_to_socket(message_size);
}


int kaatcp_send_kaasync_message(kaa_service_t service)
{
    kaa_demo_sized_buffer_t buf;
    buf.buffer = NULL;
    buf.buffer_size = 0;

    char *buffer = NULL;
    size_t buffer_size = 0;
    kaa_serialize_info_t serialize_info = { SUPPORTED_SERVICES, SUPPORTED_SERVICES_COUNT, &demo_allocator, &buf };
    kaa_error_t error_code = kaa_platform_protocol_serialize_client_sync(kaa_context_->platfrom_protocol
                                                                       , &serialize_info
                                                                       , &buffer
                                                                       , &buffer_size);
    if (error_code) {
        KAA_FREE(buf.buffer);
        return error_code;
    }

    kaatcp_kaasync_t message;
    kaatcp_fill_kaasync_message(buffer, buffer_size, 0, 0, 0, &message);

    KAA_LOG_DEBUG(kaa_context_->logger, KAA_ERR_NONE, "Sending Kaasync message (socket %d)", kaa_client_socket);
    uint32_t message_size = SOCKET_WRITE_BUFFER_SIZE;
    kaatcp_error_t rval = kaatcp_get_request_kaasync(&message, socket_write_buffer, &message_size);
    if (rval) {
        KAA_FREE(buf.buffer);
        KAA_LOG_ERROR(kaa_context_->logger, KAA_ERR_WRITE_FAILED, "Failed to send Kaasync message (socket %d)", kaa_client_socket);
        return rval;
    }

    int write_result = kaatcp_write_to_socket(message_size);

    KAA_FREE(buf.buffer);

    return write_result;
}

int kaatcp_send_disconnect_message(kaatcp_disconnect_reason_t reason)
{
    is_connected = false;
    kaatcp_disconnect_t message;
    kaatcp_fill_disconnect_message(reason, &message);
    KAA_LOG_DEBUG(kaa_context_->logger, KAA_ERR_NONE, "Sending Disconnect message, reason %x (socket %d)", message.reason, kaa_client_socket);
    uint32_t message_size = SOCKET_WRITE_BUFFER_SIZE;
    kaatcp_error_t rval = kaatcp_get_request_disconnect(&message, socket_write_buffer, &message_size);
    if (rval) {
        KAA_LOG_ERROR(kaa_context_->logger, KAA_ERR_WRITE_FAILED, "Failed to send Disconnect message (socket %d)", kaa_client_socket);
        return rval;
    }
    return kaatcp_write_to_socket(message_size);
}

void kaatcp_on_connack_message(kaatcp_connack_t message)
{
    if (message.return_code == KAATCP_CONNACK_SUCCESS) {
        is_connected = true;
        KAA_LOG_DEBUG(kaa_context_->logger, KAA_ERR_NONE, "Connection with server successfully established (socket %d) ", kaa_client_socket);
    } else {
        KAA_LOG_ERROR(kaa_context_->logger, KAA_ERR_BAD_STATE, "Connection to server failed (socket %d). Connack result=%x ", kaa_client_socket, message.return_code);
        kaa_demo_close_socket();
    }
}

void kaatcp_on_disconnect_message(kaatcp_disconnect_t message)
{
    KAA_LOG_DEBUG(kaa_context_->logger, KAA_ERR_NONE, "Disconnect message received, reason %x (socket %d)", message.reason, kaa_client_socket);
    if (message.reason) {
        KAA_LOG_ERROR(kaa_context_->logger, KAA_ERR_BAD_STATE, "Server error %x occurred (socket %d)", message.reason, kaa_client_socket);
    }
    kaa_demo_close_socket();
}

void kaatcp_on_kaasync_message(kaatcp_kaasync_t *message)
{
    KAA_LOG_DEBUG(kaa_context_->logger, KAA_ERR_NONE, "Kaasync message received (socket %d)", kaa_client_socket);

    kaa_platform_protocol_process_server_sync(kaa_context_->platfrom_protocol, message->sync_request, message->sync_request_size);

    kaatcp_parser_kaasync_destroy(message);
}

void kaatcp_on_bootstrap_message(kaatcp_bootstrap_response_t *message)
{
    kaa_demo_destroy_operation_servers();
    KAA_LOG_DEBUG(kaa_context_->logger, KAA_ERR_NONE, "Bootstrap response received (socket %d)", kaa_client_socket);

#if KAA_LOG_LEVEL_TRACE_ENABLED
    for (uint32_t i = 0; i < message->server_count; ++i) {
        kaatcp_server_record_t *server = message->servers + i;
        KAA_LOG_TRACE(kaa_context_->logger, KAA_ERR_NONE, "Added server %s, priority %u", server->server_name, server->server_priority);
        for (uint32_t j = 0; j < server->supported_channels_count; ++j) {
            kaatcp_supported_channel_t *channel = server->supported_channels + j;
            KAA_LOG_TRACE(kaa_context_->logger, KAA_ERR_NONE, "Server[%s]: added channel type %u, hostname %s, port %u", server->server_name, channel->channel_type, channel->hostname, channel->port);
        }
    }
#endif

    operation_servers = message;
    current_server_index = 0;
    kaatcp_send_disconnect_message(KAATCP_DISCONNECT_NONE);
}

void kaatcp_on_ping_message()
{
    KAA_LOG_DEBUG(kaa_context_->logger, KAA_ERR_NONE, "Ping message received (socket %d)", kaa_client_socket);
}

void kaatcp_init_parser()
{
    KAA_LOG_TRACE(kaa_context_->logger, KAA_ERR_NONE, "Initializing Kaa Tcp parser...");
    kaatcp_parser_handlers_t handlers = { &kaatcp_on_connack_message,
                                          &kaatcp_on_disconnect_message,
                                          &kaatcp_on_kaasync_message,
                                          &kaatcp_on_bootstrap_message,
                                          &kaatcp_on_ping_message };
    kaatcp_parser_init(&kaatcp_parser, &handlers);
}

int kaatcp_read_from_socket()
{
    ssize_t size = 0;
    while ((size = read(kaa_client_socket, socket_read_buffer, SOCKET_READ_BUFFER_SIZE)) > 0) {
        KAA_LOG_TRACE(kaa_context_->logger, KAA_ERR_NONE, "Received %d bytes from server (socket %d)", size, kaa_client_socket);
        int parser_result = kaatcp_parser_process_buffer(&kaatcp_parser, socket_read_buffer, size);
        if (parser_result) {
            KAA_LOG_ERROR(kaa_context_->logger, KAA_ERR_NONE, "Failed to parse KaaTCP message");
            return parser_result;
        }
    }
    if (size < 0 && errno != EAGAIN) {
        KAA_LOG_TRACE(kaa_context_->logger, KAA_ERR_NONE, "Failed to read from socket %d. %s", kaa_client_socket, strerror(errno));
        return -1;
    }
    return 0;
}

int kaa_demo_receive_operation_server_list()
{
    KAA_LOG_TRACE(kaa_context_->logger, KAA_ERR_NONE, "Going to receive operation servers list");
    kaa_channel_info_t kaatcp_info = KAA_BOOTSTRAP_SERVERS[0].channels[KAATCP];
    if (!kaatcp_info.host || !kaatcp_info.port) {
        KAA_LOG_ERROR(kaa_context_->logger, KAA_ERR_BADDATA, "Failed to get bootstrap KaaTCP channel. Hostname=%p, port=%u", kaatcp_info.host, kaatcp_info.port);
        return -1;
    }
    if (kaa_demo_create_socket_and_connect(kaatcp_info.host, kaatcp_info.port)) {
        KAA_LOG_ERROR(kaa_context_->logger, KAA_ERR_BAD_STATE, "Failed to connect to bootstrap server");
        return -1;
    }

    if (kaatcp_send_bootstrap_message()) {
        KAA_LOG_ERROR(kaa_context_->logger, KAA_ERR_WRITE_FAILED, "Failed to send bootstrap request");
        kaa_demo_close_socket();
        return -1;
    }
    if (kaatcp_read_from_socket() && is_connected) {
        KAA_LOG_ERROR(kaa_context_->logger, KAA_ERR_READ_FAILED, "Failed to read a bootstrap response");
        kaa_demo_close_socket();
        return -1;
    }

    kaa_demo_close_socket();
    return 0;
}

int kaa_demo_connect_to_next_operation_server()
{
    if (operation_servers) {
        kaatcp_server_record_t *server = operation_servers->servers + current_server_index;
        if (++current_server_index >= operation_servers->server_count) {
            current_server_index = 0;
        }
        for (uint32_t i = 0; i < server->supported_channels_count; ++i) {
            kaatcp_supported_channel_t *channel = server->supported_channels + i;
            if (channel->channel_type == KAA_BOOTSTRAP_CHANNEL_KAATCP) {
                KAA_LOG_DEBUG(kaa_context_->logger, KAA_ERR_NONE, "Found KaaTCP channel %s:%u", channel->hostname, channel->port);
                // Connecting to the operation server
                if (kaa_demo_create_socket_and_connect(channel->hostname, channel->port)) {
                    KAA_LOG_ERROR(kaa_context_->logger, KAA_ERR_BAD_STATE, "Failed to connect to KaaTCP server %s:%u", channel->hostname, channel->port);
                    kaa_demo_close_socket();
                    return -1;
                }
                // Setting NONBLOCK flag to the client socket
                int flags = fcntl(kaa_client_socket, F_GETFL, 0);
                if (fcntl(kaa_client_socket, F_SETFL, flags | O_NONBLOCK)) {
                    KAA_LOG_ERROR(kaa_context_->logger, KAA_ERR_BADDATA, "Socket %d: ", kaa_client_socket, strerror(errno));
                    return -1;
                }
                // Sending Connect message
                if (kaatcp_send_connect_message()) {
                    KAA_LOG_ERROR(kaa_context_->logger, KAA_ERR_WRITE_FAILED, "Failed to send Connect message to KaaTCP server %s:%u", channel->hostname, channel->port);
                    kaa_demo_close_socket();
                    return -1;
                }
                return 0;
            }
        }
        KAA_LOG_ERROR(kaa_context_->logger, KAA_ERR_NOT_FOUND, "Failed to find KaaTCP server. Server count %u", operation_servers->server_count);
        return -1;
    } else {
        KAA_LOG_ERROR(kaa_context_->logger, KAA_ERR_BAD_STATE, "Failed to connect to operation server. Server list is NULL");
        return -1;
    }
}

void kaa_demo_generate_pub_key()
{
    const int kBits = 2048;
    const int kExp = 65537;

    RSA *rsa = RSA_generate_key(kBits, kExp, 0, 0);

    BIO *bio_pem = BIO_new(BIO_s_mem());
    i2d_RSA_PUBKEY_bio(bio_pem, rsa);

    kaa_public_key_length = BIO_pending(bio_pem);
    kaa_public_key = (char *) malloc(kaa_public_key_length );
    BIO_read(bio_pem, kaa_public_key, kaa_public_key_length);

    printf("Generated public key (size %u)\n", kaa_public_key_length);

    BIO_free_all(bio_pem);
    RSA_free(rsa);
}

int kaa_init_security_stuff()
{
    FILE* key_file = fopen(KAA_KEY_STORAGE, "rb");

    if (key_file) {
        fseek(key_file, 0, SEEK_END);
        kaa_public_key_length = ftell(key_file);
        kaa_public_key = (char*)calloc(kaa_public_key_length, sizeof(char));

        if (kaa_public_key == NULL) {
            printf("Failed to allocate %u bytes for public key\n", kaa_public_key_length);
            return 1;
        }

        fseek(key_file, 0, SEEK_SET);
        if (fread(kaa_public_key, kaa_public_key_length, 1, key_file) == 0) {
            free(kaa_public_key);
            printf("Failed to read public key (size %u)\n", kaa_public_key_length);
            return 1;
        }

        printf("Restored public key (size %u)\n", kaa_public_key_length);
        fclose(key_file);
    } else {
        kaa_demo_generate_pub_key();
        FILE* file = fopen(KAA_KEY_STORAGE, "wb");
        if (file) {
            fwrite(kaa_public_key, kaa_public_key_length, 1, file);
            fclose(file);
            printf("Public key (size %u) persisted\n", kaa_public_key_length);
        } else {
            printf("Failed to store public key: %s\n", strerror(errno));
        }
    }

    kaa_calculate_sha_hash(kaa_public_key, kaa_public_key_length, kaa_public_key_hash);
    return 0;
}

void kaa_sdk_on_sync(const kaa_service_t services[], size_t service_count)
{
    for (size_t i = 0; i < service_count; ++i) {
        if (services[i] == KAA_SERVICE_BOOTSTRAP) {
            if (is_connected) {
                kaatcp_send_disconnect_message(KAATCP_DISCONNECT_NONE);
            }
            kaa_demo_close_socket();
            if (kaa_demo_receive_operation_server_list()) {
                KAA_LOG_ERROR(kaa_context_->logger, KAA_ERR_READ_FAILED, "Failed to receive server list. Aborting...");
            }
            kaa_demo_connect_to_next_operation_server();
            break;
        } else {
            if (kaatcp_send_kaasync_message(services[i])) {
                KAA_LOG_ERROR(kaa_context_->logger, KAA_ERR_BAD_STATE, "Failed to sync service %d", services[i]);
            }
        }
    }
}

kaa_log_upload_decision_t is_log_upload_needed(void *context, const kaa_log_storage_t *log_storage)
{
    if (log_storage && context) {
        kaa_log_upload_properties_t *log_upload_properties = (kaa_log_upload_properties_t *)context;

        if ((*log_storage->get_total_size)(log_storage->context) > log_upload_properties->max_log_storage_volume) {
            return CLEANUP;
        } else if (log_storage->get_records_count(log_storage->context) >= KAA_THRESHOLD_RECORD_COUNT) {
            return UPLOAD;
        }
    }

    return NOOP;
}

kaa_error_t kaa_log_collector_init()
{
    kaa_error_t error_code = kaa_memory_log_storage_create(&log_storage, kaa_context_->logger);
    if (error_code) {
        KAA_LOG_ERROR(kaa_context_->logger, error_code, "Failed to create log storage: error_code = %d", error_code);
        return error_code;
    }

    error_code = kaa_memory_log_storage_get_interface(log_storage, &log_storage_interface);
    if (error_code) {
        KAA_LOG_ERROR(kaa_context_->logger, error_code, "Failed to create common log storage interface: error_code = %d", error_code);
        return error_code;
    }

    error_code = kaa_logging_init(kaa_context_->log_collector
                                , &log_storage_interface
                                , &kaa_log_upload_strategy
                                , &kaa_log_upload_properties);

    return error_code;
}

kaa_error_t kaa_sdk_init()
{
    printf("Initializing Kaa SDK...");
    kaa_error_t error_code = kaa_init(&kaa_context_);
    if (error_code) {
        KAA_LOG_ERROR(kaa_context_->logger, error_code, "Error during kaa context creation %d", error_code);
        return error_code;
    }

    error_code = kaa_log_collector_init();
    if (error_code) {
        KAA_LOG_ERROR(kaa_context_->logger, error_code, "Failed to init Kaa log collector %d", error_code);
        return error_code;
    }

    KAA_LOG_TRACE(kaa_context_->logger, KAA_ERR_NONE, "Creating endpoint profile");
    kaa_profile_t *profile = kaa_profile_profile_create();
    profile->id = kaa_string_move_create(SAMPLE_PROFILE_ID, NULL);
    profile->os = ENUM_OS_Linux;
    profile->os_version = kaa_string_move_create(SAMPLE_OS_VERSION, NULL);
    profile->build = kaa_string_move_create(SAMPLE_BUILD_INFO, NULL);
    KAA_LOG_TRACE(kaa_context_->logger, KAA_ERR_NONE, "Setting profile");
    kaa_profile_update_profile(kaa_context_->profile_manager, profile);
    profile->destroy(profile);

    KAA_LOG_TRACE(kaa_context_->logger, KAA_ERR_NONE, "Setting Sync handler");
    kaa_channel_manager_add_sync_handler(kaa_context_->channel_manager, &kaa_sdk_on_sync, SUPPORTED_SERVICES, SUPPORTED_SERVICES_COUNT);

    KAA_LOG_TRACE(kaa_context_->logger, KAA_ERR_NONE, "Kaa SDK started");
    return KAA_ERR_NONE;
}

kaa_error_t kaa_demo_init()
{
    printf("Initializing Kaa demo application...\n");
    if (kaa_init_security_stuff() == 0) {
        kaa_error_t error_code = kaa_sdk_init();
        if (error_code) {
            printf("Failed to init Kaa SDK. Error code : %d\n", error_code);
            return error_code;
        }
        kaatcp_init_parser();
    } else {
        printf("Failed to init security stuff...\n");
        return KAA_ERR_NOT_INITIALIZED;
    }
    return KAA_ERR_NONE;
}

static void add_log_record()
{
    kaa_user_log_record_t *log_record = kaa_logging_log_data_create();
    if (!log_record) {
        KAA_LOG_ERROR(kaa_context_->logger, KAA_ERR_NOT_INITIALIZED, "Failed to allocate log record");
        return;
    }

    log_record->level = (kaa_logging_level_t)(rand() % 6);
    log_record->tag = kaa_string_move_create(SAMPLE_LOG_TAG, NULL);
    log_record->message = kaa_string_move_create(SAMPLE_LOG_MESSAGE, NULL);

    kaa_error_t error_code = kaa_logging_add_record(kaa_context_->log_collector, log_record);
    if (error_code)
        KAA_LOG_ERROR(kaa_context_->logger, error_code, "Failed to add log record");

    log_record->destroy(log_record);
}

void kaa_demo_destroy()
{
    KAA_LOG_DEBUG(kaa_context_->logger, KAA_ERR_NONE, "Destroying Kaa driver...");
    kaa_demo_destroy_operation_servers();
    if (is_connected) {
        kaatcp_send_disconnect_message(KAATCP_DISCONNECT_NONE);
    }
    kaa_demo_close_socket();
    kaa_deinit(kaa_context_);
    if (kaa_public_key) {
        free(kaa_public_key);
        kaa_public_key_length = 0;
    }
}

int kaa_demo_event_loop()
{
    if (kaa_demo_receive_operation_server_list()) {
        KAA_LOG_ERROR(kaa_context_->logger, KAA_ERR_READ_FAILED, "Failed to receive server list. Aborting...");
        return -1;
    }
    while (kaa_demo_connect_to_next_operation_server());

    struct timeval timer_start = { 0, 0 };
    struct timeval timer_finish = { 0, 0 };
    gettimeofday(&timer_start, NULL); // starting timer

    fd_set read_set;
    while (!is_shutdown) {
        add_log_record();

        FD_ZERO(&read_set);
        FD_SET(kaa_client_socket, &read_set);

        struct timeval select_timeout = { PING_TIMEOUT, 0 };
        int select_result = select(kaa_client_socket + 1, &read_set, NULL, NULL, &select_timeout);
        if (select_result == 0) {
            KAA_LOG_DEBUG(kaa_context_->logger, KAA_ERR_NONE, "Select timeout occurred. Sending ping to server");
            kaatcp_send_ping_message();
            gettimeofday(&timer_start, NULL); // restarting timer
        } else if (select_result > 0) {
            if (FD_ISSET(kaa_client_socket, &read_set)) {
                KAA_LOG_DEBUG(kaa_context_->logger, KAA_ERR_NONE, "Processing IN event for the Kaa client socket %d", kaa_client_socket);
                int error = 0;
                socklen_t error_size = sizeof(int);
                if (getsockopt(kaa_client_socket, SOL_SOCKET, SO_ERROR, &error, &error_size)) {
                    KAA_LOG_ERROR(kaa_context_->logger, KAA_ERR_BAD_STATE, "Failed to get error code for socket %d. %s", kaa_client_socket, strerror(errno));
                    return -1;
                }
                if (error) {
                    KAA_LOG_ERROR(kaa_context_->logger, KAA_ERR_READ_FAILED, "Failed to read from the socket %d. %s", kaa_client_socket, strerror(error));

                    kaatcp_send_disconnect_message(KAATCP_DISCONNECT_NONE);
                    kaa_demo_close_socket();
                    kaa_demo_connect_to_next_operation_server();
                    gettimeofday(&timer_start, NULL); // restarting timer
                } else {
                    kaatcp_read_from_socket();
                }
            }
            gettimeofday(&timer_finish, NULL);
            if (timer_finish.tv_sec - timer_start.tv_sec >= PING_TIMEOUT) {
                KAA_LOG_DEBUG(kaa_context_->logger, KAA_ERR_NONE, "Timer timeout occurred. Sending ping to server");
                kaatcp_send_ping_message();
                gettimeofday(&timer_start, NULL); // restarting timer
            }
        } else {
            KAA_LOG_ERROR(kaa_context_->logger, KAA_ERR_BAD_STATE, "Failed to poll descriptors: %s", strerror(errno));
            return -1;
        }
    }
    return 0;
}

void signal_handler(int sig)
{
    void *stack[16];
    size_t size;

    size = backtrace(stack, 16);
    char **backtrace_syms = backtrace_symbols(stack, 16);

    printf("SIGSEGV");
    for (size_t i = 1; i < size - 1; ++i) {
        printf("%s\n", backtrace_syms[i]);
    }
    while (size--) {
        free(backtrace_syms[size]);
    }
    exit(1);
}

int main(/*int argc, char *argv[]*/)
{
    signal(SIGSEGV, signal_handler);

    kaa_error_t error_code = kaa_demo_init();
    if (error_code) {
        printf("Failed to initialize Kaa demo. Error code: %d\n", error_code);
        return error_code;
    }
    KAA_LOG_INFO(kaa_context_->logger, KAA_ERR_NONE, "Kaa demo started");
    int rval = kaa_demo_event_loop();
    kaa_demo_destroy();
    printf("Kaa demo stopped\n");
    return rval;
}

