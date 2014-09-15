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

#ifndef KAA_ENDPOINT_GEN_H_
#define KAA_ENDPOINT_GEN_H_

#ifdef __cplusplus
extern "C" {
#define CLOSE_EXTERN }
#else
#define CLOSE_EXTERN
#endif

#include "kaa_common_schema.h"
#include "kaa_list.h"


typedef struct kaa_event_class_family_version_info_t_ {
    char* name; 
    int32_t version; 

    serialize serialize;
    get_size  get_size;
    destruct  destruct;
} kaa_event_class_family_version_info_t;

kaa_event_class_family_version_info_t* kaa_create_event_class_family_version_info();


# ifndef KAA_ARRAY_EVENT_CLASS_FAMILY_VERSION_INFO_ARRAY_NULL_UNION_H_
# define KAA_ARRAY_EVENT_CLASS_FAMILY_VERSION_INFO_ARRAY_NULL_UNION_H_

# define KAA_ARRAY_EVENT_CLASS_FAMILY_VERSION_INFO_ARRAY_NULL_UNION_ARRAY_BRANCH 0
# define KAA_ARRAY_EVENT_CLASS_FAMILY_VERSION_INFO_ARRAY_NULL_UNION_NULL_BRANCH 1

kaa_union_t* kaa_create_array_event_class_family_version_info_array_null_union_array_branch();
kaa_union_t* kaa_create_array_event_class_family_version_info_array_null_union_null_branch();

# endif // KAA_array_event_class_family_version_info_array_null_union_H_


typedef struct kaa_endpoint_version_info_t_ {
    int32_t config_version; 
    int32_t profile_version; 
    int32_t system_nf_version; 
    int32_t user_nf_version; 
    kaa_union_t* event_family_versions; 
    int32_t log_schema_version; 

    serialize serialize;
    get_size  get_size;
    destruct  destruct;
} kaa_endpoint_version_info_t;

kaa_endpoint_version_info_t* kaa_create_endpoint_version_info();


typedef struct kaa_topic_state_t_ {
    char* topic_id; 
    int32_t seq_number; 

    serialize serialize;
    get_size  get_size;
    destruct  destruct;
} kaa_topic_state_t;

kaa_topic_state_t* kaa_create_topic_state();


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
    ENUM_SUBSCRIPTION_TYPE_VOLUNTARY,
} kaa_subscription_type_t;

#ifdef GENC_ENUM_DEFINE_ALIASES
#define MANDATORY ENUM_SUBSCRIPTION_TYPE_MANDATORY
#define VOLUNTARY ENUM_SUBSCRIPTION_TYPE_VOLUNTARY
# endif // GENC_ENUM_DEFINE_ALIASES

#ifdef GENC_ENUM_STRING_LITERALS
const char* KAA_SUBSCRIPTION_TYPE_SYMBOLS[2] = {
    "MANDATORY",
    "VOLUNTARY"};
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


typedef struct kaa_subscription_command_t_ {
    char* topic_id; 
    kaa_subscription_command_type_t command; 

    serialize serialize;
    get_size  get_size;
    destruct  destruct;
} kaa_subscription_command_t;

kaa_subscription_command_t* kaa_create_subscription_command();


typedef struct kaa_user_attach_request_t_ {
    char* user_external_id; 
    char* user_access_token; 

    serialize serialize;
    get_size  get_size;
    destruct  destruct;
} kaa_user_attach_request_t;

kaa_user_attach_request_t* kaa_create_user_attach_request();


typedef struct kaa_user_attach_response_t_ {
    kaa_sync_response_result_type_t result; 

    destruct  destruct;
} kaa_user_attach_response_t;

kaa_user_attach_response_t* kaa_deserialize_user_attach_response(avro_reader_t reader);


typedef struct kaa_user_attach_notification_t_ {
    char* user_external_id; 
    char* endpoint_access_token; 

    destruct  destruct;
} kaa_user_attach_notification_t;

kaa_user_attach_notification_t* kaa_deserialize_user_attach_notification(avro_reader_t reader);


typedef struct kaa_user_detach_notification_t_ {
    char* endpoint_access_token; 

    destruct  destruct;
} kaa_user_detach_notification_t;

kaa_user_detach_notification_t* kaa_deserialize_user_detach_notification(avro_reader_t reader);


typedef struct kaa_endpoint_attach_request_t_ {
    char* request_id; 
    char* endpoint_access_token; 

    serialize serialize;
    get_size  get_size;
    destruct  destruct;
} kaa_endpoint_attach_request_t;

kaa_endpoint_attach_request_t* kaa_create_endpoint_attach_request();


# ifndef KAA_STRING_NULL_UNION_H_
# define KAA_STRING_NULL_UNION_H_

# define KAA_STRING_NULL_UNION_STRING_BRANCH 0
# define KAA_STRING_NULL_UNION_NULL_BRANCH 1

kaa_union_t* kaa_create_string_null_union_string_branch();
kaa_union_t* kaa_create_string_null_union_null_branch();

kaa_union_t* kaa_deserialize_string_null_union(avro_reader_t reader);

# endif // KAA_string_null_union_H_


typedef struct kaa_endpoint_attach_response_t_ {
    char* request_id; 
    kaa_union_t* endpoint_key_hash; 
    kaa_sync_response_result_type_t result; 

    destruct  destruct;
} kaa_endpoint_attach_response_t;

kaa_endpoint_attach_response_t* kaa_deserialize_endpoint_attach_response(avro_reader_t reader);


typedef struct kaa_endpoint_detach_request_t_ {
    char* request_id; 
    char* endpoint_key_hash; 

    serialize serialize;
    get_size  get_size;
    destruct  destruct;
} kaa_endpoint_detach_request_t;

kaa_endpoint_detach_request_t* kaa_create_endpoint_detach_request();


typedef struct kaa_endpoint_detach_response_t_ {
    char* request_id; 
    kaa_sync_response_result_type_t result; 

    destruct  destruct;
} kaa_endpoint_detach_response_t;

kaa_endpoint_detach_response_t* kaa_deserialize_endpoint_detach_response(avro_reader_t reader);


typedef struct kaa_event_t_ {
    int32_t seq_num; 
    char* event_classfqn; 
    kaa_bytes_t* event_data; 
    kaa_union_t* source; 
    kaa_union_t* target; 

    serialize serialize;
    get_size  get_size;
    destruct  destruct;
} kaa_event_t;

kaa_event_t* kaa_create_event();
kaa_event_t* kaa_deserialize_event(avro_reader_t reader);


typedef struct kaa_event_listeners_request_t_ {
    char* request_id; 
    kaa_list_t* event_classfq_ns; 

    serialize serialize;
    get_size  get_size;
    destruct  destruct;
} kaa_event_listeners_request_t;

kaa_event_listeners_request_t* kaa_create_event_listeners_request();


# ifndef KAA_ARRAY_STRING_ARRAY_NULL_UNION_H_
# define KAA_ARRAY_STRING_ARRAY_NULL_UNION_H_

# define KAA_ARRAY_STRING_ARRAY_NULL_UNION_ARRAY_BRANCH 0
# define KAA_ARRAY_STRING_ARRAY_NULL_UNION_NULL_BRANCH 1

kaa_union_t* kaa_create_array_string_array_null_union_array_branch();
kaa_union_t* kaa_create_array_string_array_null_union_null_branch();

kaa_union_t* kaa_deserialize_array_string_array_null_union(avro_reader_t reader);

# endif // KAA_array_string_array_null_union_H_


typedef struct kaa_event_listeners_response_t_ {
    char* request_id; 
    kaa_union_t* listeners; 
    kaa_sync_response_result_type_t result; 

    destruct  destruct;
} kaa_event_listeners_response_t;

kaa_event_listeners_response_t* kaa_deserialize_event_listeners_response(avro_reader_t reader);


# ifndef KAA_INT_NULL_UNION_H_
# define KAA_INT_NULL_UNION_H_

# define KAA_INT_NULL_UNION_INT_BRANCH 0
# define KAA_INT_NULL_UNION_NULL_BRANCH 1

kaa_union_t* kaa_create_int_null_union_int_branch();
kaa_union_t* kaa_create_int_null_union_null_branch();

kaa_union_t* kaa_deserialize_int_null_union(avro_reader_t reader);

# endif // KAA_int_null_union_H_


typedef struct kaa_notification_t_ {
    char* topic_id; 
    kaa_notification_type_t type; 
    kaa_union_t* uid; 
    kaa_union_t* seq_number; 
    kaa_bytes_t* body; 

    destruct  destruct;
} kaa_notification_t;

kaa_notification_t* kaa_deserialize_notification(avro_reader_t reader);


typedef struct kaa_topic_t_ {
    char* id; 
    char* name; 
    kaa_subscription_type_t subscription_type; 

    destruct  destruct;
} kaa_topic_t;

kaa_topic_t* kaa_deserialize_topic(avro_reader_t reader);


typedef struct kaa_log_entry_t_ {
    kaa_bytes_t* data; 

    serialize serialize;
    get_size  get_size;
    destruct  destruct;
} kaa_log_entry_t;

kaa_log_entry_t* kaa_create_log_entry();


# ifndef KAA_BYTES_NULL_UNION_H_
# define KAA_BYTES_NULL_UNION_H_

# define KAA_BYTES_NULL_UNION_BYTES_BRANCH 0
# define KAA_BYTES_NULL_UNION_NULL_BRANCH 1

kaa_union_t* kaa_create_bytes_null_union_bytes_branch();
kaa_union_t* kaa_create_bytes_null_union_null_branch();

kaa_union_t* kaa_deserialize_bytes_null_union(avro_reader_t reader);

# endif // KAA_bytes_null_union_H_


typedef struct kaa_sync_request_meta_data_t_ {
    char* application_token; 
    kaa_bytes_t* endpoint_public_key_hash; 
    kaa_union_t* profile_hash; 
    int64_t timeout; 

    serialize serialize;
    get_size  get_size;
    destruct  destruct;
} kaa_sync_request_meta_data_t;

kaa_sync_request_meta_data_t* kaa_create_sync_request_meta_data();


typedef struct kaa_profile_sync_request_t_ {
    kaa_union_t* endpoint_public_key; 
    kaa_bytes_t* profile_body; 
    kaa_endpoint_version_info_t* version_info; 
    kaa_union_t* endpoint_access_token; 

    serialize serialize;
    get_size  get_size;
    destruct  destruct;
} kaa_profile_sync_request_t;

kaa_profile_sync_request_t* kaa_create_profile_sync_request();


typedef struct kaa_configuration_sync_request_t_ {
    int32_t app_state_seq_number; 
    kaa_union_t* configuration_hash; 

    serialize serialize;
    get_size  get_size;
    destruct  destruct;
} kaa_configuration_sync_request_t;

kaa_configuration_sync_request_t* kaa_create_configuration_sync_request();


# ifndef KAA_ARRAY_TOPIC_STATE_ARRAY_NULL_UNION_H_
# define KAA_ARRAY_TOPIC_STATE_ARRAY_NULL_UNION_H_

# define KAA_ARRAY_TOPIC_STATE_ARRAY_NULL_UNION_ARRAY_BRANCH 0
# define KAA_ARRAY_TOPIC_STATE_ARRAY_NULL_UNION_NULL_BRANCH 1

kaa_union_t* kaa_create_array_topic_state_array_null_union_array_branch();
kaa_union_t* kaa_create_array_topic_state_array_null_union_null_branch();

# endif // KAA_array_topic_state_array_null_union_H_


# ifndef KAA_ARRAY_SUBSCRIPTION_COMMAND_ARRAY_NULL_UNION_H_
# define KAA_ARRAY_SUBSCRIPTION_COMMAND_ARRAY_NULL_UNION_H_

# define KAA_ARRAY_SUBSCRIPTION_COMMAND_ARRAY_NULL_UNION_ARRAY_BRANCH 0
# define KAA_ARRAY_SUBSCRIPTION_COMMAND_ARRAY_NULL_UNION_NULL_BRANCH 1

kaa_union_t* kaa_create_array_subscription_command_array_null_union_array_branch();
kaa_union_t* kaa_create_array_subscription_command_array_null_union_null_branch();

# endif // KAA_array_subscription_command_array_null_union_H_


typedef struct kaa_notification_sync_request_t_ {
    int32_t app_state_seq_number; 
    kaa_union_t* topic_list_hash; 
    kaa_union_t* topic_states; 
    kaa_union_t* accepted_unicast_notifications; 
    kaa_union_t* subscription_commands; 

    serialize serialize;
    get_size  get_size;
    destruct  destruct;
} kaa_notification_sync_request_t;

kaa_notification_sync_request_t* kaa_create_notification_sync_request();


# ifndef KAA_RECORD_USER_ATTACH_REQUEST_NULL_UNION_H_
# define KAA_RECORD_USER_ATTACH_REQUEST_NULL_UNION_H_

# define KAA_RECORD_USER_ATTACH_REQUEST_NULL_UNION_USER_ATTACH_REQUEST_BRANCH 0
# define KAA_RECORD_USER_ATTACH_REQUEST_NULL_UNION_NULL_BRANCH 1

kaa_union_t* kaa_create_record_user_attach_request_null_union_user_attach_request_branch();
kaa_union_t* kaa_create_record_user_attach_request_null_union_null_branch();

# endif // KAA_record_user_attach_request_null_union_H_


# ifndef KAA_ARRAY_ENDPOINT_ATTACH_REQUEST_ARRAY_NULL_UNION_H_
# define KAA_ARRAY_ENDPOINT_ATTACH_REQUEST_ARRAY_NULL_UNION_H_

# define KAA_ARRAY_ENDPOINT_ATTACH_REQUEST_ARRAY_NULL_UNION_ARRAY_BRANCH 0
# define KAA_ARRAY_ENDPOINT_ATTACH_REQUEST_ARRAY_NULL_UNION_NULL_BRANCH 1

kaa_union_t* kaa_create_array_endpoint_attach_request_array_null_union_array_branch();
kaa_union_t* kaa_create_array_endpoint_attach_request_array_null_union_null_branch();

# endif // KAA_array_endpoint_attach_request_array_null_union_H_


# ifndef KAA_ARRAY_ENDPOINT_DETACH_REQUEST_ARRAY_NULL_UNION_H_
# define KAA_ARRAY_ENDPOINT_DETACH_REQUEST_ARRAY_NULL_UNION_H_

# define KAA_ARRAY_ENDPOINT_DETACH_REQUEST_ARRAY_NULL_UNION_ARRAY_BRANCH 0
# define KAA_ARRAY_ENDPOINT_DETACH_REQUEST_ARRAY_NULL_UNION_NULL_BRANCH 1

kaa_union_t* kaa_create_array_endpoint_detach_request_array_null_union_array_branch();
kaa_union_t* kaa_create_array_endpoint_detach_request_array_null_union_null_branch();

# endif // KAA_array_endpoint_detach_request_array_null_union_H_


typedef struct kaa_user_sync_request_t_ {
    kaa_union_t* user_attach_request; 
    kaa_union_t* endpoint_attach_requests; 
    kaa_union_t* endpoint_detach_requests; 

    serialize serialize;
    get_size  get_size;
    destruct  destruct;
} kaa_user_sync_request_t;

kaa_user_sync_request_t* kaa_create_user_sync_request();


# ifndef KAA_ARRAY_EVENT_LISTENERS_REQUEST_ARRAY_NULL_UNION_H_
# define KAA_ARRAY_EVENT_LISTENERS_REQUEST_ARRAY_NULL_UNION_H_

# define KAA_ARRAY_EVENT_LISTENERS_REQUEST_ARRAY_NULL_UNION_ARRAY_BRANCH 0
# define KAA_ARRAY_EVENT_LISTENERS_REQUEST_ARRAY_NULL_UNION_NULL_BRANCH 1

kaa_union_t* kaa_create_array_event_listeners_request_array_null_union_array_branch();
kaa_union_t* kaa_create_array_event_listeners_request_array_null_union_null_branch();

# endif // KAA_array_event_listeners_request_array_null_union_H_


# ifndef KAA_ARRAY_EVENT_ARRAY_NULL_UNION_H_
# define KAA_ARRAY_EVENT_ARRAY_NULL_UNION_H_

# define KAA_ARRAY_EVENT_ARRAY_NULL_UNION_ARRAY_BRANCH 0
# define KAA_ARRAY_EVENT_ARRAY_NULL_UNION_NULL_BRANCH 1

kaa_union_t* kaa_create_array_event_array_null_union_array_branch();
kaa_union_t* kaa_create_array_event_array_null_union_null_branch();

kaa_union_t* kaa_deserialize_array_event_array_null_union(avro_reader_t reader);

# endif // KAA_array_event_array_null_union_H_


typedef struct kaa_event_sync_request_t_ {
    kaa_union_t* event_listeners_requests; 
    kaa_union_t* events; 

    serialize serialize;
    get_size  get_size;
    destruct  destruct;
} kaa_event_sync_request_t;

kaa_event_sync_request_t* kaa_create_event_sync_request();


# ifndef KAA_ARRAY_LOG_ENTRY_ARRAY_NULL_UNION_H_
# define KAA_ARRAY_LOG_ENTRY_ARRAY_NULL_UNION_H_

# define KAA_ARRAY_LOG_ENTRY_ARRAY_NULL_UNION_ARRAY_BRANCH 0
# define KAA_ARRAY_LOG_ENTRY_ARRAY_NULL_UNION_NULL_BRANCH 1

kaa_union_t* kaa_create_array_log_entry_array_null_union_array_branch();
kaa_union_t* kaa_create_array_log_entry_array_null_union_null_branch();

# endif // KAA_array_log_entry_array_null_union_H_


typedef struct kaa_log_sync_request_t_ {
    kaa_union_t* request_id; 
    kaa_union_t* log_entries; 

    serialize serialize;
    get_size  get_size;
    destruct  destruct;
} kaa_log_sync_request_t;

kaa_log_sync_request_t* kaa_create_log_sync_request();


typedef struct kaa_profile_sync_response_t_ {
    kaa_sync_response_status_t response_status; 

    destruct  destruct;
} kaa_profile_sync_response_t;

kaa_profile_sync_response_t* kaa_deserialize_profile_sync_response(avro_reader_t reader);


typedef struct kaa_configuration_sync_response_t_ {
    int32_t app_state_seq_number; 
    kaa_sync_response_status_t response_status; 
    kaa_union_t* conf_schema_body; 
    kaa_union_t* conf_delta_body; 

    destruct  destruct;
} kaa_configuration_sync_response_t;

kaa_configuration_sync_response_t* kaa_deserialize_configuration_sync_response(avro_reader_t reader);


# ifndef KAA_ARRAY_NOTIFICATION_ARRAY_NULL_UNION_H_
# define KAA_ARRAY_NOTIFICATION_ARRAY_NULL_UNION_H_

# define KAA_ARRAY_NOTIFICATION_ARRAY_NULL_UNION_ARRAY_BRANCH 0
# define KAA_ARRAY_NOTIFICATION_ARRAY_NULL_UNION_NULL_BRANCH 1

kaa_union_t* kaa_deserialize_array_notification_array_null_union(avro_reader_t reader);

# endif // KAA_array_notification_array_null_union_H_


# ifndef KAA_ARRAY_TOPIC_ARRAY_NULL_UNION_H_
# define KAA_ARRAY_TOPIC_ARRAY_NULL_UNION_H_

# define KAA_ARRAY_TOPIC_ARRAY_NULL_UNION_ARRAY_BRANCH 0
# define KAA_ARRAY_TOPIC_ARRAY_NULL_UNION_NULL_BRANCH 1

kaa_union_t* kaa_deserialize_array_topic_array_null_union(avro_reader_t reader);

# endif // KAA_array_topic_array_null_union_H_


typedef struct kaa_notification_sync_response_t_ {
    int32_t app_state_seq_number; 
    kaa_sync_response_status_t response_status; 
    kaa_union_t* notifications; 
    kaa_union_t* available_topics; 

    destruct  destruct;
} kaa_notification_sync_response_t;

kaa_notification_sync_response_t* kaa_deserialize_notification_sync_response(avro_reader_t reader);


# ifndef KAA_RECORD_USER_ATTACH_RESPONSE_NULL_UNION_H_
# define KAA_RECORD_USER_ATTACH_RESPONSE_NULL_UNION_H_

# define KAA_RECORD_USER_ATTACH_RESPONSE_NULL_UNION_USER_ATTACH_RESPONSE_BRANCH 0
# define KAA_RECORD_USER_ATTACH_RESPONSE_NULL_UNION_NULL_BRANCH 1

kaa_union_t* kaa_deserialize_record_user_attach_response_null_union(avro_reader_t reader);

# endif // KAA_record_user_attach_response_null_union_H_


# ifndef KAA_RECORD_USER_ATTACH_NOTIFICATION_NULL_UNION_H_
# define KAA_RECORD_USER_ATTACH_NOTIFICATION_NULL_UNION_H_

# define KAA_RECORD_USER_ATTACH_NOTIFICATION_NULL_UNION_USER_ATTACH_NOTIFICATION_BRANCH 0
# define KAA_RECORD_USER_ATTACH_NOTIFICATION_NULL_UNION_NULL_BRANCH 1

kaa_union_t* kaa_deserialize_record_user_attach_notification_null_union(avro_reader_t reader);

# endif // KAA_record_user_attach_notification_null_union_H_


# ifndef KAA_RECORD_USER_DETACH_NOTIFICATION_NULL_UNION_H_
# define KAA_RECORD_USER_DETACH_NOTIFICATION_NULL_UNION_H_

# define KAA_RECORD_USER_DETACH_NOTIFICATION_NULL_UNION_USER_DETACH_NOTIFICATION_BRANCH 0
# define KAA_RECORD_USER_DETACH_NOTIFICATION_NULL_UNION_NULL_BRANCH 1

kaa_union_t* kaa_deserialize_record_user_detach_notification_null_union(avro_reader_t reader);

# endif // KAA_record_user_detach_notification_null_union_H_


# ifndef KAA_ARRAY_ENDPOINT_ATTACH_RESPONSE_ARRAY_NULL_UNION_H_
# define KAA_ARRAY_ENDPOINT_ATTACH_RESPONSE_ARRAY_NULL_UNION_H_

# define KAA_ARRAY_ENDPOINT_ATTACH_RESPONSE_ARRAY_NULL_UNION_ARRAY_BRANCH 0
# define KAA_ARRAY_ENDPOINT_ATTACH_RESPONSE_ARRAY_NULL_UNION_NULL_BRANCH 1

kaa_union_t* kaa_deserialize_array_endpoint_attach_response_array_null_union(avro_reader_t reader);

# endif // KAA_array_endpoint_attach_response_array_null_union_H_


# ifndef KAA_ARRAY_ENDPOINT_DETACH_RESPONSE_ARRAY_NULL_UNION_H_
# define KAA_ARRAY_ENDPOINT_DETACH_RESPONSE_ARRAY_NULL_UNION_H_

# define KAA_ARRAY_ENDPOINT_DETACH_RESPONSE_ARRAY_NULL_UNION_ARRAY_BRANCH 0
# define KAA_ARRAY_ENDPOINT_DETACH_RESPONSE_ARRAY_NULL_UNION_NULL_BRANCH 1

kaa_union_t* kaa_deserialize_array_endpoint_detach_response_array_null_union(avro_reader_t reader);

# endif // KAA_array_endpoint_detach_response_array_null_union_H_


typedef struct kaa_user_sync_response_t_ {
    kaa_union_t* user_attach_response; 
    kaa_union_t* user_attach_notification; 
    kaa_union_t* user_detach_notification; 
    kaa_union_t* endpoint_attach_responses; 
    kaa_union_t* endpoint_detach_responses; 

    destruct  destruct;
} kaa_user_sync_response_t;

kaa_user_sync_response_t* kaa_deserialize_user_sync_response(avro_reader_t reader);


# ifndef KAA_ARRAY_EVENT_LISTENERS_RESPONSE_ARRAY_NULL_UNION_H_
# define KAA_ARRAY_EVENT_LISTENERS_RESPONSE_ARRAY_NULL_UNION_H_

# define KAA_ARRAY_EVENT_LISTENERS_RESPONSE_ARRAY_NULL_UNION_ARRAY_BRANCH 0
# define KAA_ARRAY_EVENT_LISTENERS_RESPONSE_ARRAY_NULL_UNION_NULL_BRANCH 1

kaa_union_t* kaa_deserialize_array_event_listeners_response_array_null_union(avro_reader_t reader);

# endif // KAA_array_event_listeners_response_array_null_union_H_


typedef struct kaa_event_sync_response_t_ {
    kaa_union_t* event_listeners_responses; 
    kaa_union_t* events; 

    destruct  destruct;
} kaa_event_sync_response_t;

kaa_event_sync_response_t* kaa_deserialize_event_sync_response(avro_reader_t reader);


typedef struct kaa_log_sync_response_t_ {
    char* request_id; 
    kaa_sync_response_result_type_t result; 

    destruct  destruct;
} kaa_log_sync_response_t;

kaa_log_sync_response_t* kaa_deserialize_log_sync_response(avro_reader_t reader);


typedef struct kaa_redirect_sync_response_t_ {
    char* dns_name; 

    destruct  destruct;
} kaa_redirect_sync_response_t;

kaa_redirect_sync_response_t* kaa_deserialize_redirect_sync_response(avro_reader_t reader);


# ifndef KAA_RECORD_SYNC_REQUEST_META_DATA_NULL_UNION_H_
# define KAA_RECORD_SYNC_REQUEST_META_DATA_NULL_UNION_H_

# define KAA_RECORD_SYNC_REQUEST_META_DATA_NULL_UNION_SYNC_REQUEST_META_DATA_BRANCH 0
# define KAA_RECORD_SYNC_REQUEST_META_DATA_NULL_UNION_NULL_BRANCH 1

kaa_union_t* kaa_create_record_sync_request_meta_data_null_union_sync_request_meta_data_branch();
kaa_union_t* kaa_create_record_sync_request_meta_data_null_union_null_branch();

# endif // KAA_record_sync_request_meta_data_null_union_H_


# ifndef KAA_RECORD_PROFILE_SYNC_REQUEST_NULL_UNION_H_
# define KAA_RECORD_PROFILE_SYNC_REQUEST_NULL_UNION_H_

# define KAA_RECORD_PROFILE_SYNC_REQUEST_NULL_UNION_PROFILE_SYNC_REQUEST_BRANCH 0
# define KAA_RECORD_PROFILE_SYNC_REQUEST_NULL_UNION_NULL_BRANCH 1

kaa_union_t* kaa_create_record_profile_sync_request_null_union_profile_sync_request_branch();
kaa_union_t* kaa_create_record_profile_sync_request_null_union_null_branch();

# endif // KAA_record_profile_sync_request_null_union_H_


# ifndef KAA_RECORD_CONFIGURATION_SYNC_REQUEST_NULL_UNION_H_
# define KAA_RECORD_CONFIGURATION_SYNC_REQUEST_NULL_UNION_H_

# define KAA_RECORD_CONFIGURATION_SYNC_REQUEST_NULL_UNION_CONFIGURATION_SYNC_REQUEST_BRANCH 0
# define KAA_RECORD_CONFIGURATION_SYNC_REQUEST_NULL_UNION_NULL_BRANCH 1

kaa_union_t* kaa_create_record_configuration_sync_request_null_union_configuration_sync_request_branch();
kaa_union_t* kaa_create_record_configuration_sync_request_null_union_null_branch();

# endif // KAA_record_configuration_sync_request_null_union_H_


# ifndef KAA_RECORD_NOTIFICATION_SYNC_REQUEST_NULL_UNION_H_
# define KAA_RECORD_NOTIFICATION_SYNC_REQUEST_NULL_UNION_H_

# define KAA_RECORD_NOTIFICATION_SYNC_REQUEST_NULL_UNION_NOTIFICATION_SYNC_REQUEST_BRANCH 0
# define KAA_RECORD_NOTIFICATION_SYNC_REQUEST_NULL_UNION_NULL_BRANCH 1

kaa_union_t* kaa_create_record_notification_sync_request_null_union_notification_sync_request_branch();
kaa_union_t* kaa_create_record_notification_sync_request_null_union_null_branch();

# endif // KAA_record_notification_sync_request_null_union_H_


# ifndef KAA_RECORD_USER_SYNC_REQUEST_NULL_UNION_H_
# define KAA_RECORD_USER_SYNC_REQUEST_NULL_UNION_H_

# define KAA_RECORD_USER_SYNC_REQUEST_NULL_UNION_USER_SYNC_REQUEST_BRANCH 0
# define KAA_RECORD_USER_SYNC_REQUEST_NULL_UNION_NULL_BRANCH 1

kaa_union_t* kaa_create_record_user_sync_request_null_union_user_sync_request_branch();
kaa_union_t* kaa_create_record_user_sync_request_null_union_null_branch();

# endif // KAA_record_user_sync_request_null_union_H_


# ifndef KAA_RECORD_EVENT_SYNC_REQUEST_NULL_UNION_H_
# define KAA_RECORD_EVENT_SYNC_REQUEST_NULL_UNION_H_

# define KAA_RECORD_EVENT_SYNC_REQUEST_NULL_UNION_EVENT_SYNC_REQUEST_BRANCH 0
# define KAA_RECORD_EVENT_SYNC_REQUEST_NULL_UNION_NULL_BRANCH 1

kaa_union_t* kaa_create_record_event_sync_request_null_union_event_sync_request_branch();
kaa_union_t* kaa_create_record_event_sync_request_null_union_null_branch();

# endif // KAA_record_event_sync_request_null_union_H_


# ifndef KAA_RECORD_LOG_SYNC_REQUEST_NULL_UNION_H_
# define KAA_RECORD_LOG_SYNC_REQUEST_NULL_UNION_H_

# define KAA_RECORD_LOG_SYNC_REQUEST_NULL_UNION_LOG_SYNC_REQUEST_BRANCH 0
# define KAA_RECORD_LOG_SYNC_REQUEST_NULL_UNION_NULL_BRANCH 1

kaa_union_t* kaa_create_record_log_sync_request_null_union_log_sync_request_branch();
kaa_union_t* kaa_create_record_log_sync_request_null_union_null_branch();

# endif // KAA_record_log_sync_request_null_union_H_


typedef struct kaa_sync_request_t_ {
    kaa_union_t* request_id; 
    kaa_union_t* sync_request_meta_data; 
    kaa_union_t* profile_sync_request; 
    kaa_union_t* configuration_sync_request; 
    kaa_union_t* notification_sync_request; 
    kaa_union_t* user_sync_request; 
    kaa_union_t* event_sync_request; 
    kaa_union_t* log_sync_request; 

    serialize serialize;
    get_size  get_size;
    destruct  destruct;
} kaa_sync_request_t;

kaa_sync_request_t* kaa_create_sync_request();


# ifndef KAA_RECORD_PROFILE_SYNC_RESPONSE_NULL_UNION_H_
# define KAA_RECORD_PROFILE_SYNC_RESPONSE_NULL_UNION_H_

# define KAA_RECORD_PROFILE_SYNC_RESPONSE_NULL_UNION_PROFILE_SYNC_RESPONSE_BRANCH 0
# define KAA_RECORD_PROFILE_SYNC_RESPONSE_NULL_UNION_NULL_BRANCH 1

kaa_union_t* kaa_deserialize_record_profile_sync_response_null_union(avro_reader_t reader);

# endif // KAA_record_profile_sync_response_null_union_H_


# ifndef KAA_RECORD_CONFIGURATION_SYNC_RESPONSE_NULL_UNION_H_
# define KAA_RECORD_CONFIGURATION_SYNC_RESPONSE_NULL_UNION_H_

# define KAA_RECORD_CONFIGURATION_SYNC_RESPONSE_NULL_UNION_CONFIGURATION_SYNC_RESPONSE_BRANCH 0
# define KAA_RECORD_CONFIGURATION_SYNC_RESPONSE_NULL_UNION_NULL_BRANCH 1

kaa_union_t* kaa_deserialize_record_configuration_sync_response_null_union(avro_reader_t reader);

# endif // KAA_record_configuration_sync_response_null_union_H_


# ifndef KAA_RECORD_NOTIFICATION_SYNC_RESPONSE_NULL_UNION_H_
# define KAA_RECORD_NOTIFICATION_SYNC_RESPONSE_NULL_UNION_H_

# define KAA_RECORD_NOTIFICATION_SYNC_RESPONSE_NULL_UNION_NOTIFICATION_SYNC_RESPONSE_BRANCH 0
# define KAA_RECORD_NOTIFICATION_SYNC_RESPONSE_NULL_UNION_NULL_BRANCH 1

kaa_union_t* kaa_deserialize_record_notification_sync_response_null_union(avro_reader_t reader);

# endif // KAA_record_notification_sync_response_null_union_H_


# ifndef KAA_RECORD_USER_SYNC_RESPONSE_NULL_UNION_H_
# define KAA_RECORD_USER_SYNC_RESPONSE_NULL_UNION_H_

# define KAA_RECORD_USER_SYNC_RESPONSE_NULL_UNION_USER_SYNC_RESPONSE_BRANCH 0
# define KAA_RECORD_USER_SYNC_RESPONSE_NULL_UNION_NULL_BRANCH 1

kaa_union_t* kaa_deserialize_record_user_sync_response_null_union(avro_reader_t reader);

# endif // KAA_record_user_sync_response_null_union_H_


# ifndef KAA_RECORD_EVENT_SYNC_RESPONSE_NULL_UNION_H_
# define KAA_RECORD_EVENT_SYNC_RESPONSE_NULL_UNION_H_

# define KAA_RECORD_EVENT_SYNC_RESPONSE_NULL_UNION_EVENT_SYNC_RESPONSE_BRANCH 0
# define KAA_RECORD_EVENT_SYNC_RESPONSE_NULL_UNION_NULL_BRANCH 1

kaa_union_t* kaa_deserialize_record_event_sync_response_null_union(avro_reader_t reader);

# endif // KAA_record_event_sync_response_null_union_H_


# ifndef KAA_RECORD_REDIRECT_SYNC_RESPONSE_NULL_UNION_H_
# define KAA_RECORD_REDIRECT_SYNC_RESPONSE_NULL_UNION_H_

# define KAA_RECORD_REDIRECT_SYNC_RESPONSE_NULL_UNION_REDIRECT_SYNC_RESPONSE_BRANCH 0
# define KAA_RECORD_REDIRECT_SYNC_RESPONSE_NULL_UNION_NULL_BRANCH 1

kaa_union_t* kaa_deserialize_record_redirect_sync_response_null_union(avro_reader_t reader);

# endif // KAA_record_redirect_sync_response_null_union_H_


# ifndef KAA_RECORD_LOG_SYNC_RESPONSE_NULL_UNION_H_
# define KAA_RECORD_LOG_SYNC_RESPONSE_NULL_UNION_H_

# define KAA_RECORD_LOG_SYNC_RESPONSE_NULL_UNION_LOG_SYNC_RESPONSE_BRANCH 0
# define KAA_RECORD_LOG_SYNC_RESPONSE_NULL_UNION_NULL_BRANCH 1

kaa_union_t* kaa_deserialize_record_log_sync_response_null_union(avro_reader_t reader);

# endif // KAA_record_log_sync_response_null_union_H_


typedef struct kaa_sync_response_t_ {
    kaa_union_t* request_id; 
    kaa_sync_response_result_type_t status; 
    kaa_union_t* profile_sync_response; 
    kaa_union_t* configuration_sync_response; 
    kaa_union_t* notification_sync_response; 
    kaa_union_t* user_sync_response; 
    kaa_union_t* event_sync_response; 
    kaa_union_t* redirect_sync_response; 
    kaa_union_t* log_sync_response; 

    destruct  destruct;
} kaa_sync_response_t;

kaa_sync_response_t* kaa_deserialize_sync_response(avro_reader_t reader);


CLOSE_EXTERN
#endif