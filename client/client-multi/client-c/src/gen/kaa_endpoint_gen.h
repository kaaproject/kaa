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

# ifndef KAA_ENDPOINT_GEN_H_
# define KAA_ENDPOINT_GEN_H_

# include "kaa_common_schema.h"
# include "collections/kaa_list.h"

# ifdef __cplusplus
extern "C" {
# endif


typedef struct {
    kaa_string_t* name;
    int32_t version;

    serialize_fn serialize;
    get_size_fn  get_size;
    destroy_fn   destroy;
} kaa_event_class_family_version_info_t;

kaa_event_class_family_version_info_t* kaa_event_class_family_version_info_create();


# ifndef KAA_UNION_ARRAY_EVENT_CLASS_FAMILY_VERSION_INFO_OR_NULL_H_
# define KAA_UNION_ARRAY_EVENT_CLASS_FAMILY_VERSION_INFO_OR_NULL_H_

# define KAA_UNION_ARRAY_EVENT_CLASS_FAMILY_VERSION_INFO_OR_NULL_BRANCH_0 0
# define KAA_UNION_ARRAY_EVENT_CLASS_FAMILY_VERSION_INFO_OR_NULL_BRANCH_1 1

kaa_union_t* kaa_union_array_event_class_family_version_info_or_null_branch_0_create();
kaa_union_t* kaa_union_array_event_class_family_version_info_or_null_branch_1_create();

# endif // KAA_UNION_ARRAY_EVENT_CLASS_FAMILY_VERSION_INFO_OR_NULL_H_


typedef struct {
    int32_t config_version;
    int32_t profile_version;
    int32_t system_nf_version;
    int32_t user_nf_version;
    kaa_union_t* event_family_versions;
    int32_t log_schema_version;

    serialize_fn serialize;
    get_size_fn  get_size;
    destroy_fn   destroy;
} kaa_endpoint_version_info_t;

kaa_endpoint_version_info_t* kaa_endpoint_version_info_create();


typedef struct {
    kaa_string_t* topic_id;
    int32_t seq_number;

    serialize_fn serialize;
    get_size_fn  get_size;
    destroy_fn   destroy;
} kaa_topic_state_t;

kaa_topic_state_t* kaa_topic_state_create();



typedef enum kaa_sync_response_status_t_ {
    ENUM_SYNC_RESPONSE_STATUS_NO_DELTA,
    ENUM_SYNC_RESPONSE_STATUS_DELTA,
    ENUM_SYNC_RESPONSE_STATUS_RESYNC,
} kaa_sync_response_status_t;

#ifdef GENC_ENUM_DEFINE_ALIASES
#define NO_DELTA ENUM_SYNC_RESPONSE_STATUS_NO_DELTA
#define DELTA ENUM_SYNC_RESPONSE_STATUS_DELTA
#define RESYNC ENUM_SYNC_RESPONSE_STATUS_RESYNC
# endif // GENC_ENUM_DEFINE_ALIASES

#ifdef GENC_ENUM_STRING_LITERALS
const char* KAA_SYNC_RESPONSE_STATUS_SYMBOLS[3] = {
    "NO_DELTA",
    "DELTA",
    "RESYNC"};
# endif // GENC_ENUM_STRING_LITERALS



typedef enum kaa_notification_type_t_ {
    ENUM_NOTIFICATION_TYPE_SYSTEM,
    ENUM_NOTIFICATION_TYPE_CUSTOM,
} kaa_notification_type_t;

#ifdef GENC_ENUM_DEFINE_ALIASES
#define SYSTEM ENUM_NOTIFICATION_TYPE_SYSTEM
#define CUSTOM ENUM_NOTIFICATION_TYPE_CUSTOM
# endif // GENC_ENUM_DEFINE_ALIASES

#ifdef GENC_ENUM_STRING_LITERALS
const char* KAA_NOTIFICATION_TYPE_SYMBOLS[2] = {
    "SYSTEM",
    "CUSTOM"};
# endif // GENC_ENUM_STRING_LITERALS



typedef enum kaa_subscription_type_t_ {
    ENUM_SUBSCRIPTION_TYPE_MANDATORY,
    ENUM_SUBSCRIPTION_TYPE_OPTIONAL,
} kaa_subscription_type_t;

#ifdef GENC_ENUM_DEFINE_ALIASES
#define MANDATORY ENUM_SUBSCRIPTION_TYPE_MANDATORY
#define OPTIONAL ENUM_SUBSCRIPTION_TYPE_OPTIONAL
# endif // GENC_ENUM_DEFINE_ALIASES

#ifdef GENC_ENUM_STRING_LITERALS
const char* KAA_SUBSCRIPTION_TYPE_SYMBOLS[2] = {
    "MANDATORY",
    "OPTIONAL"};
# endif // GENC_ENUM_STRING_LITERALS



typedef enum kaa_subscription_command_type_t_ {
    ENUM_SUBSCRIPTION_COMMAND_TYPE_ADD,
    ENUM_SUBSCRIPTION_COMMAND_TYPE_REMOVE,
} kaa_subscription_command_type_t;

#ifdef GENC_ENUM_DEFINE_ALIASES
#define ADD ENUM_SUBSCRIPTION_COMMAND_TYPE_ADD
#define REMOVE ENUM_SUBSCRIPTION_COMMAND_TYPE_REMOVE
# endif // GENC_ENUM_DEFINE_ALIASES

#ifdef GENC_ENUM_STRING_LITERALS
const char* KAA_SUBSCRIPTION_COMMAND_TYPE_SYMBOLS[2] = {
    "ADD",
    "REMOVE"};
# endif // GENC_ENUM_STRING_LITERALS



typedef enum kaa_sync_response_result_type_t_ {
    ENUM_SYNC_RESPONSE_RESULT_TYPE_SUCCESS,
    ENUM_SYNC_RESPONSE_RESULT_TYPE_FAILURE,
    ENUM_SYNC_RESPONSE_RESULT_TYPE_PROFILE_RESYNC,
    ENUM_SYNC_RESPONSE_RESULT_TYPE_REDIRECT,
} kaa_sync_response_result_type_t;

#ifdef GENC_ENUM_DEFINE_ALIASES
#define SUCCESS ENUM_SYNC_RESPONSE_RESULT_TYPE_SUCCESS
#define FAILURE ENUM_SYNC_RESPONSE_RESULT_TYPE_FAILURE
#define PROFILE_RESYNC ENUM_SYNC_RESPONSE_RESULT_TYPE_PROFILE_RESYNC
#define REDIRECT ENUM_SYNC_RESPONSE_RESULT_TYPE_REDIRECT
# endif // GENC_ENUM_DEFINE_ALIASES

#ifdef GENC_ENUM_STRING_LITERALS
const char* KAA_SYNC_RESPONSE_RESULT_TYPE_SYMBOLS[4] = {
    "SUCCESS",
    "FAILURE",
    "PROFILE_RESYNC",
    "REDIRECT"};
# endif // GENC_ENUM_STRING_LITERALS


typedef struct {
    kaa_string_t* topic_id;
    kaa_subscription_command_type_t command;

    serialize_fn serialize;
    get_size_fn  get_size;
    destroy_fn   destroy;
} kaa_subscription_command_t;

kaa_subscription_command_t* kaa_subscription_command_create();


typedef struct {
    kaa_string_t* user_external_id;
    kaa_string_t* user_access_token;

    serialize_fn serialize;
    get_size_fn  get_size;
    destroy_fn   destroy;
} kaa_user_attach_request_t;

kaa_user_attach_request_t* kaa_user_attach_request_create();


typedef struct {
    kaa_sync_response_result_type_t result;

    destroy_fn   destroy;
} kaa_user_attach_response_t;

kaa_user_attach_response_t* kaa_user_attach_response_deserialize(avro_reader_t reader);


typedef struct {
    kaa_string_t* user_external_id;
    kaa_string_t* endpoint_access_token;

    destroy_fn   destroy;
} kaa_user_attach_notification_t;

kaa_user_attach_notification_t* kaa_user_attach_notification_deserialize(avro_reader_t reader);


typedef struct {
    kaa_string_t* endpoint_access_token;

    destroy_fn   destroy;
} kaa_user_detach_notification_t;

kaa_user_detach_notification_t* kaa_user_detach_notification_deserialize(avro_reader_t reader);


typedef struct {
    kaa_string_t* request_id;
    kaa_string_t* endpoint_access_token;

    serialize_fn serialize;
    get_size_fn  get_size;
    destroy_fn   destroy;
} kaa_endpoint_attach_request_t;

kaa_endpoint_attach_request_t* kaa_endpoint_attach_request_create();


# ifndef KAA_UNION_STRING_OR_NULL_H_
# define KAA_UNION_STRING_OR_NULL_H_

# define KAA_UNION_STRING_OR_NULL_BRANCH_0 0
# define KAA_UNION_STRING_OR_NULL_BRANCH_1 1

kaa_union_t* kaa_union_string_or_null_branch_0_create();
kaa_union_t* kaa_union_string_or_null_branch_1_create();

kaa_union_t* kaa_union_string_or_null_deserialize(avro_reader_t reader);

# endif // KAA_UNION_STRING_OR_NULL_H_


typedef struct {
    kaa_string_t* request_id;
    kaa_union_t* endpoint_key_hash;
    kaa_sync_response_result_type_t result;

    destroy_fn   destroy;
} kaa_endpoint_attach_response_t;

kaa_endpoint_attach_response_t* kaa_endpoint_attach_response_deserialize(avro_reader_t reader);


typedef struct {
    kaa_string_t* request_id;
    kaa_string_t* endpoint_key_hash;

    serialize_fn serialize;
    get_size_fn  get_size;
    destroy_fn   destroy;
} kaa_endpoint_detach_request_t;

kaa_endpoint_detach_request_t* kaa_endpoint_detach_request_create();


typedef struct {
    kaa_string_t* request_id;
    kaa_sync_response_result_type_t result;

    destroy_fn   destroy;
} kaa_endpoint_detach_response_t;

kaa_endpoint_detach_response_t* kaa_endpoint_detach_response_deserialize(avro_reader_t reader);


typedef struct {
    int32_t seq_num;
    kaa_string_t* event_class_fqn;
    kaa_bytes_t* event_data;
    kaa_union_t* source;
    kaa_union_t* target;

    serialize_fn serialize;
    get_size_fn  get_size;
    destroy_fn   destroy;
} kaa_event_t;

kaa_event_t* kaa_event_create();
kaa_event_t* kaa_event_deserialize(avro_reader_t reader);


typedef struct {
    kaa_string_t* request_id;
    kaa_list_t* event_class_fq_ns;

    serialize_fn serialize;
    get_size_fn  get_size;
    destroy_fn   destroy;
} kaa_event_listeners_request_t;

kaa_event_listeners_request_t* kaa_event_listeners_request_create();


# ifndef KAA_UNION_ARRAY_STRING_OR_NULL_H_
# define KAA_UNION_ARRAY_STRING_OR_NULL_H_

# define KAA_UNION_ARRAY_STRING_OR_NULL_BRANCH_0 0
# define KAA_UNION_ARRAY_STRING_OR_NULL_BRANCH_1 1

kaa_union_t* kaa_union_array_string_or_null_branch_0_create();
kaa_union_t* kaa_union_array_string_or_null_branch_1_create();

kaa_union_t* kaa_union_array_string_or_null_deserialize(avro_reader_t reader);

# endif // KAA_UNION_ARRAY_STRING_OR_NULL_H_


typedef struct {
    kaa_string_t* request_id;
    kaa_union_t* listeners;
    kaa_sync_response_result_type_t result;

    destroy_fn   destroy;
} kaa_event_listeners_response_t;

kaa_event_listeners_response_t* kaa_event_listeners_response_deserialize(avro_reader_t reader);


typedef struct {

    destroy_fn   destroy;
} kaa_event_sequence_number_request_t;

kaa_event_sequence_number_request_t* kaa_event_sequence_number_request_create();


typedef struct {
    int32_t seq_num;

    destroy_fn   destroy;
} kaa_event_sequence_number_response_t;

kaa_event_sequence_number_response_t* kaa_event_sequence_number_response_deserialize(avro_reader_t reader);


# ifndef KAA_UNION_INT_OR_NULL_H_
# define KAA_UNION_INT_OR_NULL_H_

# define KAA_UNION_INT_OR_NULL_BRANCH_0 0
# define KAA_UNION_INT_OR_NULL_BRANCH_1 1

kaa_union_t* kaa_union_int_or_null_branch_0_create();
kaa_union_t* kaa_union_int_or_null_branch_1_create();

kaa_union_t* kaa_union_int_or_null_deserialize(avro_reader_t reader);

# endif // KAA_UNION_INT_OR_NULL_H_


typedef struct {
    kaa_string_t* topic_id;
    kaa_notification_type_t type;
    kaa_union_t* uid;
    kaa_union_t* seq_number;
    kaa_bytes_t* body;

    destroy_fn   destroy;
} kaa_notification_t;

kaa_notification_t* kaa_notification_deserialize(avro_reader_t reader);


typedef struct {
    kaa_string_t* id;
    kaa_string_t* name;
    kaa_subscription_type_t subscription_type;

    destroy_fn   destroy;
} kaa_topic_t;

kaa_topic_t* kaa_topic_deserialize(avro_reader_t reader);


typedef struct {
    kaa_bytes_t* data;

    serialize_fn serialize;
    get_size_fn  get_size;
    destroy_fn   destroy;
} kaa_log_entry_t;

kaa_log_entry_t* kaa_log_entry_create();


# ifndef KAA_UNION_BYTES_OR_NULL_H_
# define KAA_UNION_BYTES_OR_NULL_H_

# define KAA_UNION_BYTES_OR_NULL_BRANCH_0 0
# define KAA_UNION_BYTES_OR_NULL_BRANCH_1 1

kaa_union_t* kaa_union_bytes_or_null_branch_0_create();
kaa_union_t* kaa_union_bytes_or_null_branch_1_create();

kaa_union_t* kaa_union_bytes_or_null_deserialize(avro_reader_t reader);

# endif // KAA_UNION_BYTES_OR_NULL_H_


typedef struct {
    kaa_string_t* application_token;
    kaa_bytes_t* endpoint_public_key_hash;
    kaa_union_t* profile_hash;
    int64_t timeout;

    serialize_fn serialize;
    get_size_fn  get_size;
    destroy_fn   destroy;
} kaa_sync_request_meta_data_t;

kaa_sync_request_meta_data_t* kaa_sync_request_meta_data_create();


typedef struct {
    kaa_union_t* endpoint_public_key;
    kaa_bytes_t* profile_body;
    kaa_endpoint_version_info_t* version_info;
    kaa_union_t* endpoint_access_token;

    serialize_fn serialize;
    get_size_fn  get_size;
    destroy_fn   destroy;
} kaa_profile_sync_request_t;

kaa_profile_sync_request_t* kaa_profile_sync_request_create();


typedef struct {
    int32_t app_state_seq_number;
    kaa_union_t* configuration_hash;

    serialize_fn serialize;
    get_size_fn  get_size;
    destroy_fn   destroy;
} kaa_configuration_sync_request_t;

kaa_configuration_sync_request_t* kaa_configuration_sync_request_create();


# ifndef KAA_UNION_ARRAY_TOPIC_STATE_OR_NULL_H_
# define KAA_UNION_ARRAY_TOPIC_STATE_OR_NULL_H_

# define KAA_UNION_ARRAY_TOPIC_STATE_OR_NULL_BRANCH_0 0
# define KAA_UNION_ARRAY_TOPIC_STATE_OR_NULL_BRANCH_1 1

kaa_union_t* kaa_union_array_topic_state_or_null_branch_0_create();
kaa_union_t* kaa_union_array_topic_state_or_null_branch_1_create();

# endif // KAA_UNION_ARRAY_TOPIC_STATE_OR_NULL_H_


# ifndef KAA_UNION_ARRAY_SUBSCRIPTION_COMMAND_OR_NULL_H_
# define KAA_UNION_ARRAY_SUBSCRIPTION_COMMAND_OR_NULL_H_

# define KAA_UNION_ARRAY_SUBSCRIPTION_COMMAND_OR_NULL_BRANCH_0 0
# define KAA_UNION_ARRAY_SUBSCRIPTION_COMMAND_OR_NULL_BRANCH_1 1

kaa_union_t* kaa_union_array_subscription_command_or_null_branch_0_create();
kaa_union_t* kaa_union_array_subscription_command_or_null_branch_1_create();

# endif // KAA_UNION_ARRAY_SUBSCRIPTION_COMMAND_OR_NULL_H_


typedef struct {
    int32_t app_state_seq_number;
    kaa_union_t* topic_list_hash;
    kaa_union_t* topic_states;
    kaa_union_t* accepted_unicast_notifications;
    kaa_union_t* subscription_commands;

    serialize_fn serialize;
    get_size_fn  get_size;
    destroy_fn   destroy;
} kaa_notification_sync_request_t;

kaa_notification_sync_request_t* kaa_notification_sync_request_create();


# ifndef KAA_UNION_USER_ATTACH_REQUEST_OR_NULL_H_
# define KAA_UNION_USER_ATTACH_REQUEST_OR_NULL_H_

# define KAA_UNION_USER_ATTACH_REQUEST_OR_NULL_BRANCH_0 0
# define KAA_UNION_USER_ATTACH_REQUEST_OR_NULL_BRANCH_1 1

kaa_union_t* kaa_union_user_attach_request_or_null_branch_0_create();
kaa_union_t* kaa_union_user_attach_request_or_null_branch_1_create();

# endif // KAA_UNION_USER_ATTACH_REQUEST_OR_NULL_H_


# ifndef KAA_UNION_ARRAY_ENDPOINT_ATTACH_REQUEST_OR_NULL_H_
# define KAA_UNION_ARRAY_ENDPOINT_ATTACH_REQUEST_OR_NULL_H_

# define KAA_UNION_ARRAY_ENDPOINT_ATTACH_REQUEST_OR_NULL_BRANCH_0 0
# define KAA_UNION_ARRAY_ENDPOINT_ATTACH_REQUEST_OR_NULL_BRANCH_1 1

kaa_union_t* kaa_union_array_endpoint_attach_request_or_null_branch_0_create();
kaa_union_t* kaa_union_array_endpoint_attach_request_or_null_branch_1_create();

# endif // KAA_UNION_ARRAY_ENDPOINT_ATTACH_REQUEST_OR_NULL_H_


# ifndef KAA_UNION_ARRAY_ENDPOINT_DETACH_REQUEST_OR_NULL_H_
# define KAA_UNION_ARRAY_ENDPOINT_DETACH_REQUEST_OR_NULL_H_

# define KAA_UNION_ARRAY_ENDPOINT_DETACH_REQUEST_OR_NULL_BRANCH_0 0
# define KAA_UNION_ARRAY_ENDPOINT_DETACH_REQUEST_OR_NULL_BRANCH_1 1

kaa_union_t* kaa_union_array_endpoint_detach_request_or_null_branch_0_create();
kaa_union_t* kaa_union_array_endpoint_detach_request_or_null_branch_1_create();

# endif // KAA_UNION_ARRAY_ENDPOINT_DETACH_REQUEST_OR_NULL_H_


typedef struct {
    kaa_union_t* user_attach_request;
    kaa_union_t* endpoint_attach_requests;
    kaa_union_t* endpoint_detach_requests;

    serialize_fn serialize;
    get_size_fn  get_size;
    destroy_fn   destroy;
} kaa_user_sync_request_t;

kaa_user_sync_request_t* kaa_user_sync_request_create();


# ifndef KAA_UNION_EVENT_SEQUENCE_NUMBER_REQUEST_OR_NULL_H_
# define KAA_UNION_EVENT_SEQUENCE_NUMBER_REQUEST_OR_NULL_H_

# define KAA_UNION_EVENT_SEQUENCE_NUMBER_REQUEST_OR_NULL_BRANCH_0 0
# define KAA_UNION_EVENT_SEQUENCE_NUMBER_REQUEST_OR_NULL_BRANCH_1 1

kaa_union_t* kaa_union_event_sequence_number_request_or_null_branch_0_create();
kaa_union_t* kaa_union_event_sequence_number_request_or_null_branch_1_create();

# endif // KAA_UNION_EVENT_SEQUENCE_NUMBER_REQUEST_OR_NULL_H_


# ifndef KAA_UNION_ARRAY_EVENT_LISTENERS_REQUEST_OR_NULL_H_
# define KAA_UNION_ARRAY_EVENT_LISTENERS_REQUEST_OR_NULL_H_

# define KAA_UNION_ARRAY_EVENT_LISTENERS_REQUEST_OR_NULL_BRANCH_0 0
# define KAA_UNION_ARRAY_EVENT_LISTENERS_REQUEST_OR_NULL_BRANCH_1 1

kaa_union_t* kaa_union_array_event_listeners_request_or_null_branch_0_create();
kaa_union_t* kaa_union_array_event_listeners_request_or_null_branch_1_create();

# endif // KAA_UNION_ARRAY_EVENT_LISTENERS_REQUEST_OR_NULL_H_


# ifndef KAA_UNION_ARRAY_EVENT_OR_NULL_H_
# define KAA_UNION_ARRAY_EVENT_OR_NULL_H_

# define KAA_UNION_ARRAY_EVENT_OR_NULL_BRANCH_0 0
# define KAA_UNION_ARRAY_EVENT_OR_NULL_BRANCH_1 1

kaa_union_t* kaa_union_array_event_or_null_branch_0_create();
kaa_union_t* kaa_union_array_event_or_null_branch_1_create();

kaa_union_t* kaa_union_array_event_or_null_deserialize(avro_reader_t reader);

# endif // KAA_UNION_ARRAY_EVENT_OR_NULL_H_


typedef struct {
    kaa_union_t* event_sequence_number_request;
    kaa_union_t* event_listeners_requests;
    kaa_union_t* events;

    serialize_fn serialize;
    get_size_fn  get_size;
    destroy_fn   destroy;
} kaa_event_sync_request_t;

kaa_event_sync_request_t* kaa_event_sync_request_create();


# ifndef KAA_UNION_ARRAY_LOG_ENTRY_OR_NULL_H_
# define KAA_UNION_ARRAY_LOG_ENTRY_OR_NULL_H_

# define KAA_UNION_ARRAY_LOG_ENTRY_OR_NULL_BRANCH_0 0
# define KAA_UNION_ARRAY_LOG_ENTRY_OR_NULL_BRANCH_1 1

kaa_union_t* kaa_union_array_log_entry_or_null_branch_0_create();
kaa_union_t* kaa_union_array_log_entry_or_null_branch_1_create();

# endif // KAA_UNION_ARRAY_LOG_ENTRY_OR_NULL_H_


typedef struct {
    kaa_union_t* request_id;
    kaa_union_t* log_entries;

    serialize_fn serialize;
    get_size_fn  get_size;
    destroy_fn   destroy;
} kaa_log_sync_request_t;

kaa_log_sync_request_t* kaa_log_sync_request_create();


typedef struct {
    kaa_sync_response_status_t response_status;

    destroy_fn   destroy;
} kaa_profile_sync_response_t;

kaa_profile_sync_response_t* kaa_profile_sync_response_deserialize(avro_reader_t reader);


typedef struct {
    int32_t app_state_seq_number;
    kaa_sync_response_status_t response_status;
    kaa_union_t* conf_schema_body;
    kaa_union_t* conf_delta_body;

    destroy_fn   destroy;
} kaa_configuration_sync_response_t;

kaa_configuration_sync_response_t* kaa_configuration_sync_response_deserialize(avro_reader_t reader);


# ifndef KAA_UNION_ARRAY_NOTIFICATION_OR_NULL_H_
# define KAA_UNION_ARRAY_NOTIFICATION_OR_NULL_H_

# define KAA_UNION_ARRAY_NOTIFICATION_OR_NULL_BRANCH_0 0
# define KAA_UNION_ARRAY_NOTIFICATION_OR_NULL_BRANCH_1 1

kaa_union_t* kaa_union_array_notification_or_null_deserialize(avro_reader_t reader);

# endif // KAA_UNION_ARRAY_NOTIFICATION_OR_NULL_H_


# ifndef KAA_UNION_ARRAY_TOPIC_OR_NULL_H_
# define KAA_UNION_ARRAY_TOPIC_OR_NULL_H_

# define KAA_UNION_ARRAY_TOPIC_OR_NULL_BRANCH_0 0
# define KAA_UNION_ARRAY_TOPIC_OR_NULL_BRANCH_1 1

kaa_union_t* kaa_union_array_topic_or_null_deserialize(avro_reader_t reader);

# endif // KAA_UNION_ARRAY_TOPIC_OR_NULL_H_


typedef struct {
    int32_t app_state_seq_number;
    kaa_sync_response_status_t response_status;
    kaa_union_t* notifications;
    kaa_union_t* available_topics;

    destroy_fn   destroy;
} kaa_notification_sync_response_t;

kaa_notification_sync_response_t* kaa_notification_sync_response_deserialize(avro_reader_t reader);


# ifndef KAA_UNION_USER_ATTACH_RESPONSE_OR_NULL_H_
# define KAA_UNION_USER_ATTACH_RESPONSE_OR_NULL_H_

# define KAA_UNION_USER_ATTACH_RESPONSE_OR_NULL_BRANCH_0 0
# define KAA_UNION_USER_ATTACH_RESPONSE_OR_NULL_BRANCH_1 1

kaa_union_t* kaa_union_user_attach_response_or_null_deserialize(avro_reader_t reader);

# endif // KAA_UNION_USER_ATTACH_RESPONSE_OR_NULL_H_


# ifndef KAA_UNION_USER_ATTACH_NOTIFICATION_OR_NULL_H_
# define KAA_UNION_USER_ATTACH_NOTIFICATION_OR_NULL_H_

# define KAA_UNION_USER_ATTACH_NOTIFICATION_OR_NULL_BRANCH_0 0
# define KAA_UNION_USER_ATTACH_NOTIFICATION_OR_NULL_BRANCH_1 1

kaa_union_t* kaa_union_user_attach_notification_or_null_deserialize(avro_reader_t reader);

# endif // KAA_UNION_USER_ATTACH_NOTIFICATION_OR_NULL_H_


# ifndef KAA_UNION_USER_DETACH_NOTIFICATION_OR_NULL_H_
# define KAA_UNION_USER_DETACH_NOTIFICATION_OR_NULL_H_

# define KAA_UNION_USER_DETACH_NOTIFICATION_OR_NULL_BRANCH_0 0
# define KAA_UNION_USER_DETACH_NOTIFICATION_OR_NULL_BRANCH_1 1

kaa_union_t* kaa_union_user_detach_notification_or_null_deserialize(avro_reader_t reader);

# endif // KAA_UNION_USER_DETACH_NOTIFICATION_OR_NULL_H_


# ifndef KAA_UNION_ARRAY_ENDPOINT_ATTACH_RESPONSE_OR_NULL_H_
# define KAA_UNION_ARRAY_ENDPOINT_ATTACH_RESPONSE_OR_NULL_H_

# define KAA_UNION_ARRAY_ENDPOINT_ATTACH_RESPONSE_OR_NULL_BRANCH_0 0
# define KAA_UNION_ARRAY_ENDPOINT_ATTACH_RESPONSE_OR_NULL_BRANCH_1 1

kaa_union_t* kaa_union_array_endpoint_attach_response_or_null_deserialize(avro_reader_t reader);

# endif // KAA_UNION_ARRAY_ENDPOINT_ATTACH_RESPONSE_OR_NULL_H_


# ifndef KAA_UNION_ARRAY_ENDPOINT_DETACH_RESPONSE_OR_NULL_H_
# define KAA_UNION_ARRAY_ENDPOINT_DETACH_RESPONSE_OR_NULL_H_

# define KAA_UNION_ARRAY_ENDPOINT_DETACH_RESPONSE_OR_NULL_BRANCH_0 0
# define KAA_UNION_ARRAY_ENDPOINT_DETACH_RESPONSE_OR_NULL_BRANCH_1 1

kaa_union_t* kaa_union_array_endpoint_detach_response_or_null_deserialize(avro_reader_t reader);

# endif // KAA_UNION_ARRAY_ENDPOINT_DETACH_RESPONSE_OR_NULL_H_


typedef struct {
    kaa_union_t* user_attach_response;
    kaa_union_t* user_attach_notification;
    kaa_union_t* user_detach_notification;
    kaa_union_t* endpoint_attach_responses;
    kaa_union_t* endpoint_detach_responses;

    destroy_fn   destroy;
} kaa_user_sync_response_t;

kaa_user_sync_response_t* kaa_user_sync_response_deserialize(avro_reader_t reader);


# ifndef KAA_UNION_EVENT_SEQUENCE_NUMBER_RESPONSE_OR_NULL_H_
# define KAA_UNION_EVENT_SEQUENCE_NUMBER_RESPONSE_OR_NULL_H_

# define KAA_UNION_EVENT_SEQUENCE_NUMBER_RESPONSE_OR_NULL_BRANCH_0 0
# define KAA_UNION_EVENT_SEQUENCE_NUMBER_RESPONSE_OR_NULL_BRANCH_1 1

kaa_union_t* kaa_union_event_sequence_number_response_or_null_deserialize(avro_reader_t reader);

# endif // KAA_UNION_EVENT_SEQUENCE_NUMBER_RESPONSE_OR_NULL_H_


# ifndef KAA_UNION_ARRAY_EVENT_LISTENERS_RESPONSE_OR_NULL_H_
# define KAA_UNION_ARRAY_EVENT_LISTENERS_RESPONSE_OR_NULL_H_

# define KAA_UNION_ARRAY_EVENT_LISTENERS_RESPONSE_OR_NULL_BRANCH_0 0
# define KAA_UNION_ARRAY_EVENT_LISTENERS_RESPONSE_OR_NULL_BRANCH_1 1

kaa_union_t* kaa_union_array_event_listeners_response_or_null_deserialize(avro_reader_t reader);

# endif // KAA_UNION_ARRAY_EVENT_LISTENERS_RESPONSE_OR_NULL_H_


typedef struct {
    kaa_union_t* event_sequence_number_response;
    kaa_union_t* event_listeners_responses;
    kaa_union_t* events;

    destroy_fn   destroy;
} kaa_event_sync_response_t;

kaa_event_sync_response_t* kaa_event_sync_response_deserialize(avro_reader_t reader);


typedef struct {
    kaa_string_t* request_id;
    kaa_sync_response_result_type_t result;

    destroy_fn   destroy;
} kaa_log_sync_response_t;

kaa_log_sync_response_t* kaa_log_sync_response_deserialize(avro_reader_t reader);


typedef struct {
    kaa_string_t* dns_name;

    destroy_fn   destroy;
} kaa_redirect_sync_response_t;

kaa_redirect_sync_response_t* kaa_redirect_sync_response_deserialize(avro_reader_t reader);


# ifndef KAA_UNION_SYNC_REQUEST_META_DATA_OR_NULL_H_
# define KAA_UNION_SYNC_REQUEST_META_DATA_OR_NULL_H_

# define KAA_UNION_SYNC_REQUEST_META_DATA_OR_NULL_BRANCH_0 0
# define KAA_UNION_SYNC_REQUEST_META_DATA_OR_NULL_BRANCH_1 1

kaa_union_t* kaa_union_sync_request_meta_data_or_null_branch_0_create();
kaa_union_t* kaa_union_sync_request_meta_data_or_null_branch_1_create();

# endif // KAA_UNION_SYNC_REQUEST_META_DATA_OR_NULL_H_


# ifndef KAA_UNION_PROFILE_SYNC_REQUEST_OR_NULL_H_
# define KAA_UNION_PROFILE_SYNC_REQUEST_OR_NULL_H_

# define KAA_UNION_PROFILE_SYNC_REQUEST_OR_NULL_BRANCH_0 0
# define KAA_UNION_PROFILE_SYNC_REQUEST_OR_NULL_BRANCH_1 1

kaa_union_t* kaa_union_profile_sync_request_or_null_branch_0_create();
kaa_union_t* kaa_union_profile_sync_request_or_null_branch_1_create();

# endif // KAA_UNION_PROFILE_SYNC_REQUEST_OR_NULL_H_


# ifndef KAA_UNION_CONFIGURATION_SYNC_REQUEST_OR_NULL_H_
# define KAA_UNION_CONFIGURATION_SYNC_REQUEST_OR_NULL_H_

# define KAA_UNION_CONFIGURATION_SYNC_REQUEST_OR_NULL_BRANCH_0 0
# define KAA_UNION_CONFIGURATION_SYNC_REQUEST_OR_NULL_BRANCH_1 1

kaa_union_t* kaa_union_configuration_sync_request_or_null_branch_0_create();
kaa_union_t* kaa_union_configuration_sync_request_or_null_branch_1_create();

# endif // KAA_UNION_CONFIGURATION_SYNC_REQUEST_OR_NULL_H_


# ifndef KAA_UNION_NOTIFICATION_SYNC_REQUEST_OR_NULL_H_
# define KAA_UNION_NOTIFICATION_SYNC_REQUEST_OR_NULL_H_

# define KAA_UNION_NOTIFICATION_SYNC_REQUEST_OR_NULL_BRANCH_0 0
# define KAA_UNION_NOTIFICATION_SYNC_REQUEST_OR_NULL_BRANCH_1 1

kaa_union_t* kaa_union_notification_sync_request_or_null_branch_0_create();
kaa_union_t* kaa_union_notification_sync_request_or_null_branch_1_create();

# endif // KAA_UNION_NOTIFICATION_SYNC_REQUEST_OR_NULL_H_


# ifndef KAA_UNION_USER_SYNC_REQUEST_OR_NULL_H_
# define KAA_UNION_USER_SYNC_REQUEST_OR_NULL_H_

# define KAA_UNION_USER_SYNC_REQUEST_OR_NULL_BRANCH_0 0
# define KAA_UNION_USER_SYNC_REQUEST_OR_NULL_BRANCH_1 1

kaa_union_t* kaa_union_user_sync_request_or_null_branch_0_create();
kaa_union_t* kaa_union_user_sync_request_or_null_branch_1_create();

# endif // KAA_UNION_USER_SYNC_REQUEST_OR_NULL_H_


# ifndef KAA_UNION_EVENT_SYNC_REQUEST_OR_NULL_H_
# define KAA_UNION_EVENT_SYNC_REQUEST_OR_NULL_H_

# define KAA_UNION_EVENT_SYNC_REQUEST_OR_NULL_BRANCH_0 0
# define KAA_UNION_EVENT_SYNC_REQUEST_OR_NULL_BRANCH_1 1

kaa_union_t* kaa_union_event_sync_request_or_null_branch_0_create();
kaa_union_t* kaa_union_event_sync_request_or_null_branch_1_create();

# endif // KAA_UNION_EVENT_SYNC_REQUEST_OR_NULL_H_


# ifndef KAA_UNION_LOG_SYNC_REQUEST_OR_NULL_H_
# define KAA_UNION_LOG_SYNC_REQUEST_OR_NULL_H_

# define KAA_UNION_LOG_SYNC_REQUEST_OR_NULL_BRANCH_0 0
# define KAA_UNION_LOG_SYNC_REQUEST_OR_NULL_BRANCH_1 1

kaa_union_t* kaa_union_log_sync_request_or_null_branch_0_create();
kaa_union_t* kaa_union_log_sync_request_or_null_branch_1_create();

# endif // KAA_UNION_LOG_SYNC_REQUEST_OR_NULL_H_


typedef struct {
    kaa_union_t* request_id;
    kaa_union_t* sync_request_meta_data;
    kaa_union_t* profile_sync_request;
    kaa_union_t* configuration_sync_request;
    kaa_union_t* notification_sync_request;
    kaa_union_t* user_sync_request;
    kaa_union_t* event_sync_request;
    kaa_union_t* log_sync_request;

    serialize_fn serialize;
    get_size_fn  get_size;
    destroy_fn   destroy;
} kaa_sync_request_t;

kaa_sync_request_t* kaa_sync_request_create();


# ifndef KAA_UNION_PROFILE_SYNC_RESPONSE_OR_NULL_H_
# define KAA_UNION_PROFILE_SYNC_RESPONSE_OR_NULL_H_

# define KAA_UNION_PROFILE_SYNC_RESPONSE_OR_NULL_BRANCH_0 0
# define KAA_UNION_PROFILE_SYNC_RESPONSE_OR_NULL_BRANCH_1 1

kaa_union_t* kaa_union_profile_sync_response_or_null_deserialize(avro_reader_t reader);

# endif // KAA_UNION_PROFILE_SYNC_RESPONSE_OR_NULL_H_


# ifndef KAA_UNION_CONFIGURATION_SYNC_RESPONSE_OR_NULL_H_
# define KAA_UNION_CONFIGURATION_SYNC_RESPONSE_OR_NULL_H_

# define KAA_UNION_CONFIGURATION_SYNC_RESPONSE_OR_NULL_BRANCH_0 0
# define KAA_UNION_CONFIGURATION_SYNC_RESPONSE_OR_NULL_BRANCH_1 1

kaa_union_t* kaa_union_configuration_sync_response_or_null_deserialize(avro_reader_t reader);

# endif // KAA_UNION_CONFIGURATION_SYNC_RESPONSE_OR_NULL_H_


# ifndef KAA_UNION_NOTIFICATION_SYNC_RESPONSE_OR_NULL_H_
# define KAA_UNION_NOTIFICATION_SYNC_RESPONSE_OR_NULL_H_

# define KAA_UNION_NOTIFICATION_SYNC_RESPONSE_OR_NULL_BRANCH_0 0
# define KAA_UNION_NOTIFICATION_SYNC_RESPONSE_OR_NULL_BRANCH_1 1

kaa_union_t* kaa_union_notification_sync_response_or_null_deserialize(avro_reader_t reader);

# endif // KAA_UNION_NOTIFICATION_SYNC_RESPONSE_OR_NULL_H_


# ifndef KAA_UNION_USER_SYNC_RESPONSE_OR_NULL_H_
# define KAA_UNION_USER_SYNC_RESPONSE_OR_NULL_H_

# define KAA_UNION_USER_SYNC_RESPONSE_OR_NULL_BRANCH_0 0
# define KAA_UNION_USER_SYNC_RESPONSE_OR_NULL_BRANCH_1 1

kaa_union_t* kaa_union_user_sync_response_or_null_deserialize(avro_reader_t reader);

# endif // KAA_UNION_USER_SYNC_RESPONSE_OR_NULL_H_


# ifndef KAA_UNION_EVENT_SYNC_RESPONSE_OR_NULL_H_
# define KAA_UNION_EVENT_SYNC_RESPONSE_OR_NULL_H_

# define KAA_UNION_EVENT_SYNC_RESPONSE_OR_NULL_BRANCH_0 0
# define KAA_UNION_EVENT_SYNC_RESPONSE_OR_NULL_BRANCH_1 1

kaa_union_t* kaa_union_event_sync_response_or_null_deserialize(avro_reader_t reader);

# endif // KAA_UNION_EVENT_SYNC_RESPONSE_OR_NULL_H_


# ifndef KAA_UNION_REDIRECT_SYNC_RESPONSE_OR_NULL_H_
# define KAA_UNION_REDIRECT_SYNC_RESPONSE_OR_NULL_H_

# define KAA_UNION_REDIRECT_SYNC_RESPONSE_OR_NULL_BRANCH_0 0
# define KAA_UNION_REDIRECT_SYNC_RESPONSE_OR_NULL_BRANCH_1 1

kaa_union_t* kaa_union_redirect_sync_response_or_null_deserialize(avro_reader_t reader);

# endif // KAA_UNION_REDIRECT_SYNC_RESPONSE_OR_NULL_H_


# ifndef KAA_UNION_LOG_SYNC_RESPONSE_OR_NULL_H_
# define KAA_UNION_LOG_SYNC_RESPONSE_OR_NULL_H_

# define KAA_UNION_LOG_SYNC_RESPONSE_OR_NULL_BRANCH_0 0
# define KAA_UNION_LOG_SYNC_RESPONSE_OR_NULL_BRANCH_1 1

kaa_union_t* kaa_union_log_sync_response_or_null_deserialize(avro_reader_t reader);

# endif // KAA_UNION_LOG_SYNC_RESPONSE_OR_NULL_H_


typedef struct {
    kaa_union_t* request_id;
    kaa_sync_response_result_type_t status;
    kaa_union_t* profile_sync_response;
    kaa_union_t* configuration_sync_response;
    kaa_union_t* notification_sync_response;
    kaa_union_t* user_sync_response;
    kaa_union_t* event_sync_response;
    kaa_union_t* redirect_sync_response;
    kaa_union_t* log_sync_response;

    destroy_fn   destroy;
} kaa_sync_response_t;

kaa_sync_response_t* kaa_sync_response_deserialize(avro_reader_t reader);


typedef struct {
    kaa_topic_t* topic_info;
    int32_t seq_number;

    destroy_fn   destroy;
} kaa_topic_subscription_info_t;

kaa_topic_subscription_info_t* kaa_topic_subscription_info_deserialize(avro_reader_t reader);

#ifdef __cplusplus
}      /* extern "C" */
#endif
#endif
