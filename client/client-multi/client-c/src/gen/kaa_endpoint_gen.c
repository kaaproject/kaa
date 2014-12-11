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

# include "kaa_endpoint_gen.h"

# include <stdio.h>
# include <string.h>

# include "avro_src/avro/io.h"
# include "avro_src/encoding.h"

# include "kaa_mem.h"

/*
 * AUTO-GENERATED CODE
 */



static void kaa_destroy_event_class_family_version_info(void* data)
{
    kaa_event_class_family_version_info_t* record = (kaa_event_class_family_version_info_t*)data;

    KAA_FREE(record->name);
}
static size_t kaa_get_size_event_class_family_version_info(void* data)
{
    size_t record_size = 0;
    kaa_event_class_family_version_info_t* record = (kaa_event_class_family_version_info_t*)data;

    record_size += kaa_get_size_string(record->name);
    record_size += size_long((int64_t)record->version);

    return record_size;
}

static void kaa_serialize_event_class_family_version_info(avro_writer_t writer, void* data)
{
    kaa_event_class_family_version_info_t* record = (kaa_event_class_family_version_info_t*)data;

    avro_binary_encoding.write_string(writer, record->name);
    avro_binary_encoding.write_int(writer, record->version);
}

kaa_event_class_family_version_info_t* kaa_create_event_class_family_version_info()
{
    kaa_event_class_family_version_info_t* record = KAA_MALLOC(kaa_event_class_family_version_info_t);
    record->serialize = kaa_serialize_event_class_family_version_info;
    record->get_size = kaa_get_size_event_class_family_version_info;
    record->destroy = kaa_destroy_event_class_family_version_info;
    return record;
}



# ifndef KAA_ARRAY_EVENT_CLASS_FAMILY_VERSION_INFO_NULL_UNION_C_
# define KAA_ARRAY_EVENT_CLASS_FAMILY_VERSION_INFO_NULL_UNION_C_
static void kaa_destroy_array_event_class_family_version_info_null_union(void *data)
{
    kaa_union_t *kaa_union = (kaa_union_t*)data;

    switch (kaa_union->type) {
    case KAA_ARRAY_EVENT_CLASS_FAMILY_VERSION_INFO_NULL_UNION_ARRAY_BRANCH:
    {
        kaa_list_destroy(kaa_union->data, kaa_destroy_event_class_family_version_info);
        break;
    }
    default:
        break;
    }
}

static size_t kaa_get_size_array_event_class_family_version_info_null_union(void *data)
{
    kaa_union_t *kaa_union = (kaa_union_t*)data;
    size_t union_len = size_long(kaa_union->type);

    switch (kaa_union->type) {
    case KAA_ARRAY_EVENT_CLASS_FAMILY_VERSION_INFO_NULL_UNION_ARRAY_BRANCH:
    {
            union_len += kaa_array_size(kaa_union->data, kaa_get_size_event_class_family_version_info);
            break;
    }
    default:
        break;
    }
    
    return union_len;
}

static void kaa_serialize_array_event_class_family_version_info_null_union(avro_writer_t writer, void *data)
{
    kaa_union_t *kaa_union = (kaa_union_t*)data;

    int64_t branch = kaa_union->type;
    avro_binary_encoding.write_long(writer, branch);

    switch (kaa_union->type) {
    case KAA_ARRAY_EVENT_CLASS_FAMILY_VERSION_INFO_NULL_UNION_ARRAY_BRANCH:
    {
            kaa_serialize_array(writer, kaa_union->data, kaa_serialize_event_class_family_version_info);
            break;
    }
    default:
        break;
    }
}
static kaa_union_t* kaa_create_array_event_class_family_version_info_null_union()
{
    kaa_union_t* kaa_union = KAA_MALLOC(kaa_union_t);
    kaa_union->data = NULL;
    kaa_union->serialize = kaa_serialize_array_event_class_family_version_info_null_union;
    kaa_union->get_size = kaa_get_size_array_event_class_family_version_info_null_union;
    kaa_union->destroy = kaa_destroy_array_event_class_family_version_info_null_union;

    return kaa_union; 
}

kaa_union_t* kaa_create_array_event_class_family_version_info_null_union_array_branch()
{
    kaa_union_t *kaa_union = kaa_create_array_event_class_family_version_info_null_union();
    kaa_union->type = KAA_ARRAY_EVENT_CLASS_FAMILY_VERSION_INFO_NULL_UNION_ARRAY_BRANCH;
    return kaa_union;
}

kaa_union_t* kaa_create_array_event_class_family_version_info_null_union_null_branch()
{
    kaa_union_t *kaa_union = kaa_create_array_event_class_family_version_info_null_union();
    kaa_union->type = KAA_ARRAY_EVENT_CLASS_FAMILY_VERSION_INFO_NULL_UNION_NULL_BRANCH;
    return kaa_union;
}
# endif // KAA_ARRAY_EVENT_CLASS_FAMILY_VERSION_INFO_NULL_UNION_C_



static void kaa_destroy_endpoint_version_info(void* data)
{
    kaa_endpoint_version_info_t* record = (kaa_endpoint_version_info_t*)data;

    record->event_family_versions->destroy(record->event_family_versions);
    KAA_FREE(record->event_family_versions);
}
static size_t kaa_get_size_endpoint_version_info(void* data)
{
    size_t record_size = 0;
    kaa_endpoint_version_info_t* record = (kaa_endpoint_version_info_t*)data;

    record_size += size_long((int64_t)record->config_version);
    record_size += size_long((int64_t)record->profile_version);
    record_size += size_long((int64_t)record->system_nf_version);
    record_size += size_long((int64_t)record->user_nf_version);
    record_size += record->event_family_versions->get_size(record->event_family_versions);
    record_size += size_long((int64_t)record->log_schema_version);

    return record_size;
}

static void kaa_serialize_endpoint_version_info(avro_writer_t writer, void* data)
{
    kaa_endpoint_version_info_t* record = (kaa_endpoint_version_info_t*)data;

    avro_binary_encoding.write_int(writer, record->config_version);
    avro_binary_encoding.write_int(writer, record->profile_version);
    avro_binary_encoding.write_int(writer, record->system_nf_version);
    avro_binary_encoding.write_int(writer, record->user_nf_version);
    record->event_family_versions->serialize(writer, record->event_family_versions);
    avro_binary_encoding.write_int(writer, record->log_schema_version);
}

kaa_endpoint_version_info_t* kaa_create_endpoint_version_info()
{
    kaa_endpoint_version_info_t* record = KAA_MALLOC(kaa_endpoint_version_info_t);
    record->serialize = kaa_serialize_endpoint_version_info;
    record->get_size = kaa_get_size_endpoint_version_info;
    record->destroy = kaa_destroy_endpoint_version_info;
    return record;
}



static void kaa_destroy_topic_state(void* data)
{
    kaa_topic_state_t* record = (kaa_topic_state_t*)data;

    KAA_FREE(record->topic_id);
}
static size_t kaa_get_size_topic_state(void* data)
{
    size_t record_size = 0;
    kaa_topic_state_t* record = (kaa_topic_state_t*)data;

    record_size += kaa_get_size_string(record->topic_id);
    record_size += size_long((int64_t)record->seq_number);

    return record_size;
}

static void kaa_serialize_topic_state(avro_writer_t writer, void* data)
{
    kaa_topic_state_t* record = (kaa_topic_state_t*)data;

    avro_binary_encoding.write_string(writer, record->topic_id);
    avro_binary_encoding.write_int(writer, record->seq_number);
}

kaa_topic_state_t* kaa_create_topic_state()
{
    kaa_topic_state_t* record = KAA_MALLOC(kaa_topic_state_t);
    record->serialize = kaa_serialize_topic_state;
    record->get_size = kaa_get_size_topic_state;
    record->destroy = kaa_destroy_topic_state;
    return record;
}



static void kaa_destroy_subscription_command(void* data)
{
    kaa_subscription_command_t* record = (kaa_subscription_command_t*)data;

    KAA_FREE(record->topic_id);
}
static size_t kaa_get_size_subscription_command(void* data)
{
    size_t record_size = 0;
    kaa_subscription_command_t* record = (kaa_subscription_command_t*)data;

    record_size += kaa_get_size_string(record->topic_id);
    record_size += size_long((int64_t)record->command);

    return record_size;
}

static void kaa_serialize_subscription_command(avro_writer_t writer, void* data)
{
    kaa_subscription_command_t* record = (kaa_subscription_command_t*)data;

    avro_binary_encoding.write_string(writer, record->topic_id);
    avro_binary_encoding.write_long(writer, record->command);
}

kaa_subscription_command_t* kaa_create_subscription_command()
{
    kaa_subscription_command_t* record = KAA_MALLOC(kaa_subscription_command_t);
    record->serialize = kaa_serialize_subscription_command;
    record->get_size = kaa_get_size_subscription_command;
    record->destroy = kaa_destroy_subscription_command;
    return record;
}



static void kaa_destroy_user_attach_request(void* data)
{
    kaa_user_attach_request_t* record = (kaa_user_attach_request_t*)data;

    KAA_FREE(record->user_external_id);
    KAA_FREE(record->user_access_token);
}
static size_t kaa_get_size_user_attach_request(void* data)
{
    size_t record_size = 0;
    kaa_user_attach_request_t* record = (kaa_user_attach_request_t*)data;

    record_size += kaa_get_size_string(record->user_external_id);
    record_size += kaa_get_size_string(record->user_access_token);

    return record_size;
}

static void kaa_serialize_user_attach_request(avro_writer_t writer, void* data)
{
    kaa_user_attach_request_t* record = (kaa_user_attach_request_t*)data;

    avro_binary_encoding.write_string(writer, record->user_external_id);
    avro_binary_encoding.write_string(writer, record->user_access_token);
}

kaa_user_attach_request_t* kaa_create_user_attach_request()
{
    kaa_user_attach_request_t* record = KAA_MALLOC(kaa_user_attach_request_t);
    record->serialize = kaa_serialize_user_attach_request;
    record->get_size = kaa_get_size_user_attach_request;
    record->destroy = kaa_destroy_user_attach_request;
    return record;
}




kaa_user_attach_response_t* kaa_deserialize_user_attach_response(avro_reader_t reader)
{
    kaa_user_attach_response_t* record = KAA_MALLOC(kaa_user_attach_response_t);
    record->destroy = kaa_destroy_null;
    
    int64_t result_value;
    avro_binary_encoding.read_long(reader, &result_value);
    record->result = result_value;
    
    return record;
}



static void kaa_destroy_user_attach_notification(void* data)
{
    kaa_user_attach_notification_t* record = (kaa_user_attach_notification_t*)data;

    KAA_FREE(record->user_external_id);
    KAA_FREE(record->endpoint_access_token);
}

kaa_user_attach_notification_t* kaa_deserialize_user_attach_notification(avro_reader_t reader)
{
    kaa_user_attach_notification_t* record = KAA_MALLOC(kaa_user_attach_notification_t);
    record->destroy = kaa_destroy_user_attach_notification;
    
        int64_t user_external_id_size;
    avro_binary_encoding.read_string(reader, &record->user_external_id, &user_external_id_size);
            int64_t endpoint_access_token_size;
    avro_binary_encoding.read_string(reader, &record->endpoint_access_token, &endpoint_access_token_size);
        
    return record;
}



static void kaa_destroy_user_detach_notification(void* data)
{
    kaa_user_detach_notification_t* record = (kaa_user_detach_notification_t*)data;

    KAA_FREE(record->endpoint_access_token);
}

kaa_user_detach_notification_t* kaa_deserialize_user_detach_notification(avro_reader_t reader)
{
    kaa_user_detach_notification_t* record = KAA_MALLOC(kaa_user_detach_notification_t);
    record->destroy = kaa_destroy_user_detach_notification;
    
        int64_t endpoint_access_token_size;
    avro_binary_encoding.read_string(reader, &record->endpoint_access_token, &endpoint_access_token_size);
        
    return record;
}



static void kaa_destroy_endpoint_attach_request(void* data)
{
    kaa_endpoint_attach_request_t* record = (kaa_endpoint_attach_request_t*)data;

    KAA_FREE(record->request_id);
    KAA_FREE(record->endpoint_access_token);
}
static size_t kaa_get_size_endpoint_attach_request(void* data)
{
    size_t record_size = 0;
    kaa_endpoint_attach_request_t* record = (kaa_endpoint_attach_request_t*)data;

    record_size += kaa_get_size_string(record->request_id);
    record_size += kaa_get_size_string(record->endpoint_access_token);

    return record_size;
}

static void kaa_serialize_endpoint_attach_request(avro_writer_t writer, void* data)
{
    kaa_endpoint_attach_request_t* record = (kaa_endpoint_attach_request_t*)data;

    avro_binary_encoding.write_string(writer, record->request_id);
    avro_binary_encoding.write_string(writer, record->endpoint_access_token);
}

kaa_endpoint_attach_request_t* kaa_create_endpoint_attach_request()
{
    kaa_endpoint_attach_request_t* record = KAA_MALLOC(kaa_endpoint_attach_request_t);
    record->serialize = kaa_serialize_endpoint_attach_request;
    record->get_size = kaa_get_size_endpoint_attach_request;
    record->destroy = kaa_destroy_endpoint_attach_request;
    return record;
}



# ifndef KAA_STRING_NULL_UNION_C_
# define KAA_STRING_NULL_UNION_C_
static void kaa_destroy_string_null_union(void *data)
{
    kaa_union_t *kaa_union = (kaa_union_t*)data;

    switch (kaa_union->type) {
    case KAA_STRING_NULL_UNION_STRING_BRANCH:
    {
        KAA_FREE(kaa_union->data);
        break;
    }
    default:
        break;
    }
}

static size_t kaa_get_size_string_null_union(void *data)
{
    kaa_union_t *kaa_union = (kaa_union_t*)data;
    size_t union_len = size_long(kaa_union->type);

    switch (kaa_union->type) {
    case KAA_STRING_NULL_UNION_STRING_BRANCH:
    {
        union_len += kaa_get_size_string(kaa_union->data);
        break;
    }
    default:
        break;
    }
    
    return union_len;
}

static void kaa_serialize_string_null_union(avro_writer_t writer, void *data)
{
    kaa_union_t *kaa_union = (kaa_union_t*)data;

    int64_t branch = kaa_union->type;
    avro_binary_encoding.write_long(writer, branch);

    switch (kaa_union->type) {
    case KAA_STRING_NULL_UNION_STRING_BRANCH:
    {
        avro_binary_encoding.write_string(writer, (char *)kaa_union->data);
        break;
    }
    default:
        break;
    }
}
static kaa_union_t* kaa_create_string_null_union()
{
    kaa_union_t* kaa_union = KAA_MALLOC(kaa_union_t);
    kaa_union->data = NULL;
    kaa_union->serialize = kaa_serialize_string_null_union;
    kaa_union->get_size = kaa_get_size_string_null_union;
    kaa_union->destroy = kaa_destroy_string_null_union;

    return kaa_union; 
}

kaa_union_t* kaa_create_string_null_union_string_branch()
{
    kaa_union_t *kaa_union = kaa_create_string_null_union();
    kaa_union->type = KAA_STRING_NULL_UNION_STRING_BRANCH;
    return kaa_union;
}

kaa_union_t* kaa_create_string_null_union_null_branch()
{
    kaa_union_t *kaa_union = kaa_create_string_null_union();
    kaa_union->type = KAA_STRING_NULL_UNION_NULL_BRANCH;
    return kaa_union;
}

kaa_union_t* kaa_deserialize_string_null_union(avro_reader_t reader)
{
    kaa_union_t *kaa_union = kaa_create_string_null_union();

    int64_t branch;
    avro_binary_encoding.read_long(reader, &branch);
    kaa_union->type = branch;

    switch (kaa_union->type) {
    case KAA_STRING_NULL_UNION_STRING_BRANCH:
    {
        int64_t data_size;
        avro_binary_encoding.read_string(reader, (char**)&kaa_union->data, &data_size);
        break;
    }
    default:
        break;
    }

    return kaa_union;
}
# endif // KAA_STRING_NULL_UNION_C_



static void kaa_destroy_endpoint_attach_response(void* data)
{
    kaa_endpoint_attach_response_t* record = (kaa_endpoint_attach_response_t*)data;

    KAA_FREE(record->request_id);
    record->endpoint_key_hash->destroy(record->endpoint_key_hash);
    KAA_FREE(record->endpoint_key_hash);
}

kaa_endpoint_attach_response_t* kaa_deserialize_endpoint_attach_response(avro_reader_t reader)
{
    kaa_endpoint_attach_response_t* record = KAA_MALLOC(kaa_endpoint_attach_response_t);
    record->destroy = kaa_destroy_endpoint_attach_response;
    
        int64_t request_id_size;
    avro_binary_encoding.read_string(reader, &record->request_id, &request_id_size);
        record->endpoint_key_hash = kaa_deserialize_string_null_union(reader);
    int64_t result_value;
    avro_binary_encoding.read_long(reader, &result_value);
    record->result = result_value;
    
    return record;
}



static void kaa_destroy_endpoint_detach_request(void* data)
{
    kaa_endpoint_detach_request_t* record = (kaa_endpoint_detach_request_t*)data;

    KAA_FREE(record->request_id);
    KAA_FREE(record->endpoint_key_hash);
}
static size_t kaa_get_size_endpoint_detach_request(void* data)
{
    size_t record_size = 0;
    kaa_endpoint_detach_request_t* record = (kaa_endpoint_detach_request_t*)data;

    record_size += kaa_get_size_string(record->request_id);
    record_size += kaa_get_size_string(record->endpoint_key_hash);

    return record_size;
}

static void kaa_serialize_endpoint_detach_request(avro_writer_t writer, void* data)
{
    kaa_endpoint_detach_request_t* record = (kaa_endpoint_detach_request_t*)data;

    avro_binary_encoding.write_string(writer, record->request_id);
    avro_binary_encoding.write_string(writer, record->endpoint_key_hash);
}

kaa_endpoint_detach_request_t* kaa_create_endpoint_detach_request()
{
    kaa_endpoint_detach_request_t* record = KAA_MALLOC(kaa_endpoint_detach_request_t);
    record->serialize = kaa_serialize_endpoint_detach_request;
    record->get_size = kaa_get_size_endpoint_detach_request;
    record->destroy = kaa_destroy_endpoint_detach_request;
    return record;
}



static void kaa_destroy_endpoint_detach_response(void* data)
{
    kaa_endpoint_detach_response_t* record = (kaa_endpoint_detach_response_t*)data;

    KAA_FREE(record->request_id);
}

kaa_endpoint_detach_response_t* kaa_deserialize_endpoint_detach_response(avro_reader_t reader)
{
    kaa_endpoint_detach_response_t* record = KAA_MALLOC(kaa_endpoint_detach_response_t);
    record->destroy = kaa_destroy_endpoint_detach_response;
    
        int64_t request_id_size;
    avro_binary_encoding.read_string(reader, &record->request_id, &request_id_size);
        int64_t result_value;
    avro_binary_encoding.read_long(reader, &result_value);
    record->result = result_value;
    
    return record;
}



static void kaa_destroy_event(void* data)
{
    kaa_event_t* record = (kaa_event_t*)data;

    KAA_FREE(record->event_class_fqn);
    kaa_destroy_bytes(record->event_data);
    KAA_FREE(record->event_data);
    record->source->destroy(record->source);
    KAA_FREE(record->source);
    record->target->destroy(record->target);
    KAA_FREE(record->target);
}
static size_t kaa_get_size_event(void* data)
{
    size_t record_size = 0;
    kaa_event_t* record = (kaa_event_t*)data;

    record_size += size_long((int64_t)record->seq_num);
    record_size += kaa_get_size_string(record->event_class_fqn);
    record_size += kaa_get_size_bytes(record->event_data);
    record_size += record->source->get_size(record->source);
    record_size += record->target->get_size(record->target);

    return record_size;
}

static void kaa_serialize_event(avro_writer_t writer, void* data)
{
    kaa_event_t* record = (kaa_event_t*)data;

    avro_binary_encoding.write_int(writer, record->seq_num);
    avro_binary_encoding.write_string(writer, record->event_class_fqn);
    kaa_serialize_bytes(writer, record->event_data);
    record->source->serialize(writer, record->source);
    record->target->serialize(writer, record->target);
}

kaa_event_t* kaa_create_event()
{
    kaa_event_t* record = KAA_MALLOC(kaa_event_t);
    record->serialize = kaa_serialize_event;
    record->get_size = kaa_get_size_event;
    record->destroy = kaa_destroy_event;
    return record;
}

kaa_event_t* kaa_deserialize_event(avro_reader_t reader)
{
    kaa_event_t* record = KAA_MALLOC(kaa_event_t);
    record->serialize = kaa_serialize_event;
    record->get_size = kaa_get_size_event;
    record->destroy = kaa_destroy_event;
    
    avro_binary_encoding.read_int(reader, &record->seq_num);
        int64_t event_class_fqn_size;
    avro_binary_encoding.read_string(reader, &record->event_class_fqn, &event_class_fqn_size);
                record->event_data = kaa_deserialize_bytes(reader); 
        record->source = kaa_deserialize_string_null_union(reader);
    record->target = kaa_deserialize_string_null_union(reader);
    
    return record;
}



static void kaa_destroy_event_listeners_request(void* data)
{
    kaa_event_listeners_request_t* record = (kaa_event_listeners_request_t*)data;

    KAA_FREE(record->request_id);
            kaa_list_destroy(record->event_class_fq_ns, NULL);
    }
static size_t kaa_get_size_event_listeners_request(void* data)
{
    size_t record_size = 0;
    kaa_event_listeners_request_t* record = (kaa_event_listeners_request_t*)data;

    record_size += kaa_get_size_string(record->request_id);
            record_size += kaa_array_size(record->event_class_fq_ns, kaa_get_size_string);
    
    return record_size;
}

static void kaa_serialize_event_listeners_request(avro_writer_t writer, void* data)
{
    kaa_event_listeners_request_t* record = (kaa_event_listeners_request_t*)data;

    avro_binary_encoding.write_string(writer, record->request_id);
                    kaa_serialize_array(writer, record->event_class_fq_ns, kaa_serialize_string);
    }

kaa_event_listeners_request_t* kaa_create_event_listeners_request()
{
    kaa_event_listeners_request_t* record = KAA_MALLOC(kaa_event_listeners_request_t);
    record->serialize = kaa_serialize_event_listeners_request;
    record->get_size = kaa_get_size_event_listeners_request;
    record->destroy = kaa_destroy_event_listeners_request;
    return record;
}



# ifndef KAA_ARRAY_STRING_NULL_UNION_C_
# define KAA_ARRAY_STRING_NULL_UNION_C_
static void kaa_destroy_array_string_null_union(void *data)
{
    kaa_union_t *kaa_union = (kaa_union_t*)data;

    switch (kaa_union->type) {
    case KAA_ARRAY_STRING_NULL_UNION_ARRAY_BRANCH:
    {
        kaa_list_destroy(kaa_union->data, NULL);
        break;
    }
    default:
        break;
    }
}

static size_t kaa_get_size_array_string_null_union(void *data)
{
    kaa_union_t *kaa_union = (kaa_union_t*)data;
    size_t union_len = size_long(kaa_union->type);

    switch (kaa_union->type) {
    case KAA_ARRAY_STRING_NULL_UNION_ARRAY_BRANCH:
    {
            //TODO: implement calculating size for array of primitives (bool, int, long, enum) 
            break;
    }
    default:
        break;
    }
    
    return union_len;
}

static void kaa_serialize_array_string_null_union(avro_writer_t writer, void *data)
{
    kaa_union_t *kaa_union = (kaa_union_t*)data;

    int64_t branch = kaa_union->type;
    avro_binary_encoding.write_long(writer, branch);

    switch (kaa_union->type) {
    case KAA_ARRAY_STRING_NULL_UNION_ARRAY_BRANCH:
    {
                    kaa_serialize_array(writer, kaa_union->data, kaa_serialize_string);
            break;
    }
    default:
        break;
    }
}
static kaa_union_t* kaa_create_array_string_null_union()
{
    kaa_union_t* kaa_union = KAA_MALLOC(kaa_union_t);
    kaa_union->data = NULL;
    kaa_union->serialize = kaa_serialize_array_string_null_union;
    kaa_union->get_size = kaa_get_size_array_string_null_union;
    kaa_union->destroy = kaa_destroy_array_string_null_union;

    return kaa_union; 
}

kaa_union_t* kaa_create_array_string_null_union_array_branch()
{
    kaa_union_t *kaa_union = kaa_create_array_string_null_union();
    kaa_union->type = KAA_ARRAY_STRING_NULL_UNION_ARRAY_BRANCH;
    return kaa_union;
}

kaa_union_t* kaa_create_array_string_null_union_null_branch()
{
    kaa_union_t *kaa_union = kaa_create_array_string_null_union();
    kaa_union->type = KAA_ARRAY_STRING_NULL_UNION_NULL_BRANCH;
    return kaa_union;
}

kaa_union_t* kaa_deserialize_array_string_null_union(avro_reader_t reader)
{
    kaa_union_t *kaa_union = kaa_create_array_string_null_union();

    int64_t branch;
    avro_binary_encoding.read_long(reader, &branch);
    kaa_union->type = branch;

    switch (kaa_union->type) {
    case KAA_ARRAY_STRING_NULL_UNION_ARRAY_BRANCH:
    {
                    kaa_union->data = kaa_deserialize_array(reader, (deserialize_fn)kaa_deserialize_string);
            break;
    }
    default:
        break;
    }

    return kaa_union;
}
# endif // KAA_ARRAY_STRING_NULL_UNION_C_



static void kaa_destroy_event_listeners_response(void* data)
{
    kaa_event_listeners_response_t* record = (kaa_event_listeners_response_t*)data;

    KAA_FREE(record->request_id);
    record->listeners->destroy(record->listeners);
    KAA_FREE(record->listeners);
}

kaa_event_listeners_response_t* kaa_deserialize_event_listeners_response(avro_reader_t reader)
{
    kaa_event_listeners_response_t* record = KAA_MALLOC(kaa_event_listeners_response_t);
    record->destroy = kaa_destroy_event_listeners_response;
    
        int64_t request_id_size;
    avro_binary_encoding.read_string(reader, &record->request_id, &request_id_size);
        record->listeners = kaa_deserialize_array_string_null_union(reader);
    int64_t result_value;
    avro_binary_encoding.read_long(reader, &result_value);
    record->result = result_value;
    
    return record;
}



static size_t kaa_get_size_event_sequence_number_request(void* data)
{
    size_t record_size = 0;
    kaa_event_sequence_number_request_t* record = (kaa_event_sequence_number_request_t*)data;


    return record_size;
}

static void kaa_serialize_event_sequence_number_request(avro_writer_t writer, void* data)
{
    kaa_event_sequence_number_request_t* record = (kaa_event_sequence_number_request_t*)data;

}

kaa_event_sequence_number_request_t* kaa_create_event_sequence_number_request()
{
    kaa_event_sequence_number_request_t* record = KAA_MALLOC(kaa_event_sequence_number_request_t);
    record->serialize = kaa_serialize_event_sequence_number_request;
    record->get_size = kaa_get_size_event_sequence_number_request;
    record->destroy = kaa_destroy_null;
    return record;
}




kaa_event_sequence_number_response_t* kaa_deserialize_event_sequence_number_response(avro_reader_t reader)
{
    kaa_event_sequence_number_response_t* record = KAA_MALLOC(kaa_event_sequence_number_response_t);
    record->destroy = kaa_destroy_null;
    
    avro_binary_encoding.read_int(reader, &record->seq_num);
    
    return record;
}



# ifndef KAA_INT_NULL_UNION_C_
# define KAA_INT_NULL_UNION_C_
static void kaa_destroy_int_null_union(void *data)
{
    kaa_union_t *kaa_union = (kaa_union_t*)data;

    switch (kaa_union->type) {
    case KAA_INT_NULL_UNION_INT_BRANCH:
    {
        KAA_FREE(kaa_union->data);
        break;
    }
    default:
        break;
    }
}

static size_t kaa_get_size_int_null_union(void *data)
{
    kaa_union_t *kaa_union = (kaa_union_t*)data;
    size_t union_len = size_long(kaa_union->type);

    switch (kaa_union->type) {
    case KAA_INT_NULL_UNION_INT_BRANCH:
    {
        union_len += size_long(*((int32_t *)kaa_union->data));
        break;
    }
    default:
        break;
    }
    
    return union_len;
}

static void kaa_serialize_int_null_union(avro_writer_t writer, void *data)
{
    kaa_union_t *kaa_union = (kaa_union_t*)data;

    int64_t branch = kaa_union->type;
    avro_binary_encoding.write_long(writer, branch);

    switch (kaa_union->type) {
    case KAA_INT_NULL_UNION_INT_BRANCH:
    {
        avro_binary_encoding.write_int(writer, *((int32_t *)kaa_union->data));
        break;
    }
    default:
        break;
    }
}
static kaa_union_t* kaa_create_int_null_union()
{
    kaa_union_t* kaa_union = KAA_MALLOC(kaa_union_t);
    kaa_union->data = NULL;
    kaa_union->serialize = kaa_serialize_int_null_union;
    kaa_union->get_size = kaa_get_size_int_null_union;
    kaa_union->destroy = kaa_destroy_int_null_union;

    return kaa_union; 
}

kaa_union_t* kaa_create_int_null_union_int_branch()
{
    kaa_union_t *kaa_union = kaa_create_int_null_union();
    kaa_union->type = KAA_INT_NULL_UNION_INT_BRANCH;
    return kaa_union;
}

kaa_union_t* kaa_create_int_null_union_null_branch()
{
    kaa_union_t *kaa_union = kaa_create_int_null_union();
    kaa_union->type = KAA_INT_NULL_UNION_NULL_BRANCH;
    return kaa_union;
}

kaa_union_t* kaa_deserialize_int_null_union(avro_reader_t reader)
{
    kaa_union_t *kaa_union = kaa_create_int_null_union();

    int64_t branch;
    avro_binary_encoding.read_long(reader, &branch);
    kaa_union->type = branch;

    switch (kaa_union->type) {
    case KAA_INT_NULL_UNION_INT_BRANCH:
    {
        kaa_union->data = kaa_deserialize_int(reader);
        break;
    }
    default:
        break;
    }

    return kaa_union;
}
# endif // KAA_INT_NULL_UNION_C_



static void kaa_destroy_notification(void* data)
{
    kaa_notification_t* record = (kaa_notification_t*)data;

    KAA_FREE(record->topic_id);
    record->uid->destroy(record->uid);
    KAA_FREE(record->uid);
    record->seq_number->destroy(record->seq_number);
    KAA_FREE(record->seq_number);
    kaa_destroy_bytes(record->body);
    KAA_FREE(record->body);
}

kaa_notification_t* kaa_deserialize_notification(avro_reader_t reader)
{
    kaa_notification_t* record = KAA_MALLOC(kaa_notification_t);
    record->destroy = kaa_destroy_notification;
    
        int64_t topic_id_size;
    avro_binary_encoding.read_string(reader, &record->topic_id, &topic_id_size);
        int64_t type_value;
    avro_binary_encoding.read_long(reader, &type_value);
    record->type = type_value;
    record->uid = kaa_deserialize_string_null_union(reader);
    record->seq_number = kaa_deserialize_int_null_union(reader);
            record->body = kaa_deserialize_bytes(reader); 
        
    return record;
}



static void kaa_destroy_topic(void* data)
{
    kaa_topic_t* record = (kaa_topic_t*)data;

    KAA_FREE(record->id);
    KAA_FREE(record->name);
}

kaa_topic_t* kaa_deserialize_topic(avro_reader_t reader)
{
    kaa_topic_t* record = KAA_MALLOC(kaa_topic_t);
    record->destroy = kaa_destroy_topic;
    
        int64_t id_size;
    avro_binary_encoding.read_string(reader, &record->id, &id_size);
            int64_t name_size;
    avro_binary_encoding.read_string(reader, &record->name, &name_size);
        int64_t subscription_type_value;
    avro_binary_encoding.read_long(reader, &subscription_type_value);
    record->subscription_type = subscription_type_value;
    
    return record;
}



static void kaa_destroy_log_entry(void* data)
{
    kaa_log_entry_t* record = (kaa_log_entry_t*)data;

    kaa_destroy_bytes(record->data);
    KAA_FREE(record->data);
}
static size_t kaa_get_size_log_entry(void* data)
{
    size_t record_size = 0;
    kaa_log_entry_t* record = (kaa_log_entry_t*)data;

    record_size += kaa_get_size_bytes(record->data);

    return record_size;
}

static void kaa_serialize_log_entry(avro_writer_t writer, void* data)
{
    kaa_log_entry_t* record = (kaa_log_entry_t*)data;

    kaa_serialize_bytes(writer, record->data);
}

kaa_log_entry_t* kaa_create_log_entry()
{
    kaa_log_entry_t* record = KAA_MALLOC(kaa_log_entry_t);
    record->serialize = kaa_serialize_log_entry;
    record->get_size = kaa_get_size_log_entry;
    record->destroy = kaa_destroy_log_entry;
    return record;
}



# ifndef KAA_BYTES_NULL_UNION_C_
# define KAA_BYTES_NULL_UNION_C_
static void kaa_destroy_bytes_null_union(void *data)
{
    kaa_union_t *kaa_union = (kaa_union_t*)data;

    switch (kaa_union->type) {
    case KAA_BYTES_NULL_UNION_BYTES_BRANCH:
    {
	    kaa_destroy_bytes(kaa_union->data);
	    KAA_FREE(kaa_union->data);
        break;
    }
    default:
        break;
    }
}

static size_t kaa_get_size_bytes_null_union(void *data)
{
    kaa_union_t *kaa_union = (kaa_union_t*)data;
    size_t union_len = size_long(kaa_union->type);

    switch (kaa_union->type) {
    case KAA_BYTES_NULL_UNION_BYTES_BRANCH:
    {
        union_len += kaa_get_size_bytes(kaa_union->data);
        break;
    }
    default:
        break;
    }
    
    return union_len;
}

static void kaa_serialize_bytes_null_union(avro_writer_t writer, void *data)
{
    kaa_union_t *kaa_union = (kaa_union_t*)data;

    int64_t branch = kaa_union->type;
    avro_binary_encoding.write_long(writer, branch);

    switch (kaa_union->type) {
    case KAA_BYTES_NULL_UNION_BYTES_BRANCH:
    {
        kaa_serialize_bytes(writer, kaa_union->data);
        break;
    }
    default:
        break;
    }
}
static kaa_union_t* kaa_create_bytes_null_union()
{
    kaa_union_t* kaa_union = KAA_MALLOC(kaa_union_t);
    kaa_union->data = NULL;
    kaa_union->serialize = kaa_serialize_bytes_null_union;
    kaa_union->get_size = kaa_get_size_bytes_null_union;
    kaa_union->destroy = kaa_destroy_bytes_null_union;

    return kaa_union; 
}

kaa_union_t* kaa_create_bytes_null_union_bytes_branch()
{
    kaa_union_t *kaa_union = kaa_create_bytes_null_union();
    kaa_union->type = KAA_BYTES_NULL_UNION_BYTES_BRANCH;
    return kaa_union;
}

kaa_union_t* kaa_create_bytes_null_union_null_branch()
{
    kaa_union_t *kaa_union = kaa_create_bytes_null_union();
    kaa_union->type = KAA_BYTES_NULL_UNION_NULL_BRANCH;
    return kaa_union;
}

kaa_union_t* kaa_deserialize_bytes_null_union(avro_reader_t reader)
{
    kaa_union_t *kaa_union = kaa_create_bytes_null_union();

    int64_t branch;
    avro_binary_encoding.read_long(reader, &branch);
    kaa_union->type = branch;

    switch (kaa_union->type) {
    case KAA_BYTES_NULL_UNION_BYTES_BRANCH:
    {
        kaa_union->data = kaa_deserialize_bytes(reader);
        break;
    }
    default:
        break;
    }

    return kaa_union;
}
# endif // KAA_BYTES_NULL_UNION_C_



static void kaa_destroy_sync_request_meta_data(void* data)
{
    kaa_sync_request_meta_data_t* record = (kaa_sync_request_meta_data_t*)data;

    KAA_FREE(record->application_token);
    kaa_destroy_bytes(record->endpoint_public_key_hash);
    KAA_FREE(record->endpoint_public_key_hash);
    record->profile_hash->destroy(record->profile_hash);
    KAA_FREE(record->profile_hash);
}
static size_t kaa_get_size_sync_request_meta_data(void* data)
{
    size_t record_size = 0;
    kaa_sync_request_meta_data_t* record = (kaa_sync_request_meta_data_t*)data;

    record_size += kaa_get_size_string(record->application_token);
    record_size += kaa_get_size_bytes(record->endpoint_public_key_hash);
    record_size += record->profile_hash->get_size(record->profile_hash);
    record_size += size_long((int64_t)record->timeout);

    return record_size;
}

static void kaa_serialize_sync_request_meta_data(avro_writer_t writer, void* data)
{
    kaa_sync_request_meta_data_t* record = (kaa_sync_request_meta_data_t*)data;

    avro_binary_encoding.write_string(writer, record->application_token);
    kaa_serialize_bytes(writer, record->endpoint_public_key_hash);
    record->profile_hash->serialize(writer, record->profile_hash);
    avro_binary_encoding.write_long(writer, record->timeout);
}

kaa_sync_request_meta_data_t* kaa_create_sync_request_meta_data()
{
    kaa_sync_request_meta_data_t* record = KAA_MALLOC(kaa_sync_request_meta_data_t);
    record->serialize = kaa_serialize_sync_request_meta_data;
    record->get_size = kaa_get_size_sync_request_meta_data;
    record->destroy = kaa_destroy_sync_request_meta_data;
    return record;
}



static void kaa_destroy_profile_sync_request(void* data)
{
    kaa_profile_sync_request_t* record = (kaa_profile_sync_request_t*)data;

    record->endpoint_public_key->destroy(record->endpoint_public_key);
    KAA_FREE(record->endpoint_public_key);
    kaa_destroy_bytes(record->profile_body);
    KAA_FREE(record->profile_body);
    record->version_info->destroy(record->version_info);
    KAA_FREE(record->version_info);
    record->endpoint_access_token->destroy(record->endpoint_access_token);
    KAA_FREE(record->endpoint_access_token);
}
static size_t kaa_get_size_profile_sync_request(void* data)
{
    size_t record_size = 0;
    kaa_profile_sync_request_t* record = (kaa_profile_sync_request_t*)data;

    record_size += record->endpoint_public_key->get_size(record->endpoint_public_key);
    record_size += kaa_get_size_bytes(record->profile_body);
    record_size += record->version_info->get_size(record->version_info);
    record_size += record->endpoint_access_token->get_size(record->endpoint_access_token);

    return record_size;
}

static void kaa_serialize_profile_sync_request(avro_writer_t writer, void* data)
{
    kaa_profile_sync_request_t* record = (kaa_profile_sync_request_t*)data;

    record->endpoint_public_key->serialize(writer, record->endpoint_public_key);
    kaa_serialize_bytes(writer, record->profile_body);
    record->version_info->serialize(writer, record->version_info);
    record->endpoint_access_token->serialize(writer, record->endpoint_access_token);
}

kaa_profile_sync_request_t* kaa_create_profile_sync_request()
{
    kaa_profile_sync_request_t* record = KAA_MALLOC(kaa_profile_sync_request_t);
    record->serialize = kaa_serialize_profile_sync_request;
    record->get_size = kaa_get_size_profile_sync_request;
    record->destroy = kaa_destroy_profile_sync_request;
    return record;
}



static void kaa_destroy_configuration_sync_request(void* data)
{
    kaa_configuration_sync_request_t* record = (kaa_configuration_sync_request_t*)data;

    record->configuration_hash->destroy(record->configuration_hash);
    KAA_FREE(record->configuration_hash);
}
static size_t kaa_get_size_configuration_sync_request(void* data)
{
    size_t record_size = 0;
    kaa_configuration_sync_request_t* record = (kaa_configuration_sync_request_t*)data;

    record_size += size_long((int64_t)record->app_state_seq_number);
    record_size += record->configuration_hash->get_size(record->configuration_hash);

    return record_size;
}

static void kaa_serialize_configuration_sync_request(avro_writer_t writer, void* data)
{
    kaa_configuration_sync_request_t* record = (kaa_configuration_sync_request_t*)data;

    avro_binary_encoding.write_int(writer, record->app_state_seq_number);
    record->configuration_hash->serialize(writer, record->configuration_hash);
}

kaa_configuration_sync_request_t* kaa_create_configuration_sync_request()
{
    kaa_configuration_sync_request_t* record = KAA_MALLOC(kaa_configuration_sync_request_t);
    record->serialize = kaa_serialize_configuration_sync_request;
    record->get_size = kaa_get_size_configuration_sync_request;
    record->destroy = kaa_destroy_configuration_sync_request;
    return record;
}



# ifndef KAA_ARRAY_TOPIC_STATE_NULL_UNION_C_
# define KAA_ARRAY_TOPIC_STATE_NULL_UNION_C_
static void kaa_destroy_array_topic_state_null_union(void *data)
{
    kaa_union_t *kaa_union = (kaa_union_t*)data;

    switch (kaa_union->type) {
    case KAA_ARRAY_TOPIC_STATE_NULL_UNION_ARRAY_BRANCH:
    {
        kaa_list_destroy(kaa_union->data, kaa_destroy_topic_state);
        break;
    }
    default:
        break;
    }
}

static size_t kaa_get_size_array_topic_state_null_union(void *data)
{
    kaa_union_t *kaa_union = (kaa_union_t*)data;
    size_t union_len = size_long(kaa_union->type);

    switch (kaa_union->type) {
    case KAA_ARRAY_TOPIC_STATE_NULL_UNION_ARRAY_BRANCH:
    {
            union_len += kaa_array_size(kaa_union->data, kaa_get_size_topic_state);
            break;
    }
    default:
        break;
    }
    
    return union_len;
}

static void kaa_serialize_array_topic_state_null_union(avro_writer_t writer, void *data)
{
    kaa_union_t *kaa_union = (kaa_union_t*)data;

    int64_t branch = kaa_union->type;
    avro_binary_encoding.write_long(writer, branch);

    switch (kaa_union->type) {
    case KAA_ARRAY_TOPIC_STATE_NULL_UNION_ARRAY_BRANCH:
    {
            kaa_serialize_array(writer, kaa_union->data, kaa_serialize_topic_state);
            break;
    }
    default:
        break;
    }
}
static kaa_union_t* kaa_create_array_topic_state_null_union()
{
    kaa_union_t* kaa_union = KAA_MALLOC(kaa_union_t);
    kaa_union->data = NULL;
    kaa_union->serialize = kaa_serialize_array_topic_state_null_union;
    kaa_union->get_size = kaa_get_size_array_topic_state_null_union;
    kaa_union->destroy = kaa_destroy_array_topic_state_null_union;

    return kaa_union; 
}

kaa_union_t* kaa_create_array_topic_state_null_union_array_branch()
{
    kaa_union_t *kaa_union = kaa_create_array_topic_state_null_union();
    kaa_union->type = KAA_ARRAY_TOPIC_STATE_NULL_UNION_ARRAY_BRANCH;
    return kaa_union;
}

kaa_union_t* kaa_create_array_topic_state_null_union_null_branch()
{
    kaa_union_t *kaa_union = kaa_create_array_topic_state_null_union();
    kaa_union->type = KAA_ARRAY_TOPIC_STATE_NULL_UNION_NULL_BRANCH;
    return kaa_union;
}
# endif // KAA_ARRAY_TOPIC_STATE_NULL_UNION_C_



# ifndef KAA_ARRAY_SUBSCRIPTION_COMMAND_NULL_UNION_C_
# define KAA_ARRAY_SUBSCRIPTION_COMMAND_NULL_UNION_C_
static void kaa_destroy_array_subscription_command_null_union(void *data)
{
    kaa_union_t *kaa_union = (kaa_union_t*)data;

    switch (kaa_union->type) {
    case KAA_ARRAY_SUBSCRIPTION_COMMAND_NULL_UNION_ARRAY_BRANCH:
    {
        kaa_list_destroy(kaa_union->data, kaa_destroy_subscription_command);
        break;
    }
    default:
        break;
    }
}

static size_t kaa_get_size_array_subscription_command_null_union(void *data)
{
    kaa_union_t *kaa_union = (kaa_union_t*)data;
    size_t union_len = size_long(kaa_union->type);

    switch (kaa_union->type) {
    case KAA_ARRAY_SUBSCRIPTION_COMMAND_NULL_UNION_ARRAY_BRANCH:
    {
            union_len += kaa_array_size(kaa_union->data, kaa_get_size_subscription_command);
            break;
    }
    default:
        break;
    }
    
    return union_len;
}

static void kaa_serialize_array_subscription_command_null_union(avro_writer_t writer, void *data)
{
    kaa_union_t *kaa_union = (kaa_union_t*)data;

    int64_t branch = kaa_union->type;
    avro_binary_encoding.write_long(writer, branch);

    switch (kaa_union->type) {
    case KAA_ARRAY_SUBSCRIPTION_COMMAND_NULL_UNION_ARRAY_BRANCH:
    {
            kaa_serialize_array(writer, kaa_union->data, kaa_serialize_subscription_command);
            break;
    }
    default:
        break;
    }
}
static kaa_union_t* kaa_create_array_subscription_command_null_union()
{
    kaa_union_t* kaa_union = KAA_MALLOC(kaa_union_t);
    kaa_union->data = NULL;
    kaa_union->serialize = kaa_serialize_array_subscription_command_null_union;
    kaa_union->get_size = kaa_get_size_array_subscription_command_null_union;
    kaa_union->destroy = kaa_destroy_array_subscription_command_null_union;

    return kaa_union; 
}

kaa_union_t* kaa_create_array_subscription_command_null_union_array_branch()
{
    kaa_union_t *kaa_union = kaa_create_array_subscription_command_null_union();
    kaa_union->type = KAA_ARRAY_SUBSCRIPTION_COMMAND_NULL_UNION_ARRAY_BRANCH;
    return kaa_union;
}

kaa_union_t* kaa_create_array_subscription_command_null_union_null_branch()
{
    kaa_union_t *kaa_union = kaa_create_array_subscription_command_null_union();
    kaa_union->type = KAA_ARRAY_SUBSCRIPTION_COMMAND_NULL_UNION_NULL_BRANCH;
    return kaa_union;
}
# endif // KAA_ARRAY_SUBSCRIPTION_COMMAND_NULL_UNION_C_



static void kaa_destroy_notification_sync_request(void* data)
{
    kaa_notification_sync_request_t* record = (kaa_notification_sync_request_t*)data;

    record->topic_list_hash->destroy(record->topic_list_hash);
    KAA_FREE(record->topic_list_hash);
    record->topic_states->destroy(record->topic_states);
    KAA_FREE(record->topic_states);
    record->accepted_unicast_notifications->destroy(record->accepted_unicast_notifications);
    KAA_FREE(record->accepted_unicast_notifications);
    record->subscription_commands->destroy(record->subscription_commands);
    KAA_FREE(record->subscription_commands);
}
static size_t kaa_get_size_notification_sync_request(void* data)
{
    size_t record_size = 0;
    kaa_notification_sync_request_t* record = (kaa_notification_sync_request_t*)data;

    record_size += size_long((int64_t)record->app_state_seq_number);
    record_size += record->topic_list_hash->get_size(record->topic_list_hash);
    record_size += record->topic_states->get_size(record->topic_states);
    record_size += record->accepted_unicast_notifications->get_size(record->accepted_unicast_notifications);
    record_size += record->subscription_commands->get_size(record->subscription_commands);

    return record_size;
}

static void kaa_serialize_notification_sync_request(avro_writer_t writer, void* data)
{
    kaa_notification_sync_request_t* record = (kaa_notification_sync_request_t*)data;

    avro_binary_encoding.write_int(writer, record->app_state_seq_number);
    record->topic_list_hash->serialize(writer, record->topic_list_hash);
    record->topic_states->serialize(writer, record->topic_states);
    record->accepted_unicast_notifications->serialize(writer, record->accepted_unicast_notifications);
    record->subscription_commands->serialize(writer, record->subscription_commands);
}

kaa_notification_sync_request_t* kaa_create_notification_sync_request()
{
    kaa_notification_sync_request_t* record = KAA_MALLOC(kaa_notification_sync_request_t);
    record->serialize = kaa_serialize_notification_sync_request;
    record->get_size = kaa_get_size_notification_sync_request;
    record->destroy = kaa_destroy_notification_sync_request;
    return record;
}



# ifndef KAA_RECORD_USER_ATTACH_REQUEST_NULL_UNION_C_
# define KAA_RECORD_USER_ATTACH_REQUEST_NULL_UNION_C_
static void kaa_destroy_record_user_attach_request_null_union(void *data)
{
    kaa_union_t *kaa_union = (kaa_union_t*)data;

    switch (kaa_union->type) {
    case KAA_RECORD_USER_ATTACH_REQUEST_NULL_UNION_USER_ATTACH_REQUEST_BRANCH:
    {
        kaa_user_attach_request_t* record = (kaa_user_attach_request_t*)kaa_union->data;
        record->destroy(record);
        KAA_FREE(kaa_union->data);
        break;
    }
    default:
        break;
    }
}

static size_t kaa_get_size_record_user_attach_request_null_union(void *data)
{
    kaa_union_t *kaa_union = (kaa_union_t*)data;
    size_t union_len = size_long(kaa_union->type);

    switch (kaa_union->type) {
    case KAA_RECORD_USER_ATTACH_REQUEST_NULL_UNION_USER_ATTACH_REQUEST_BRANCH:
    {
        kaa_user_attach_request_t* record = (kaa_user_attach_request_t*)kaa_union->data;
        union_len += record->get_size(record);
        break;
    }
    default:
        break;
    }
    
    return union_len;
}

static void kaa_serialize_record_user_attach_request_null_union(avro_writer_t writer, void *data)
{
    kaa_union_t *kaa_union = (kaa_union_t*)data;

    int64_t branch = kaa_union->type;
    avro_binary_encoding.write_long(writer, branch);

    switch (kaa_union->type) {
    case KAA_RECORD_USER_ATTACH_REQUEST_NULL_UNION_USER_ATTACH_REQUEST_BRANCH:
    {
        kaa_user_attach_request_t* record = (kaa_user_attach_request_t*)kaa_union->data;
        record->serialize(writer, record);
        break;
    }
    default:
        break;
    }
}
static kaa_union_t* kaa_create_record_user_attach_request_null_union()
{
    kaa_union_t* kaa_union = KAA_MALLOC(kaa_union_t);
    kaa_union->data = NULL;
    kaa_union->serialize = kaa_serialize_record_user_attach_request_null_union;
    kaa_union->get_size = kaa_get_size_record_user_attach_request_null_union;
    kaa_union->destroy = kaa_destroy_record_user_attach_request_null_union;

    return kaa_union; 
}

kaa_union_t* kaa_create_record_user_attach_request_null_union_user_attach_request_branch()
{
    kaa_union_t *kaa_union = kaa_create_record_user_attach_request_null_union();
    kaa_union->type = KAA_RECORD_USER_ATTACH_REQUEST_NULL_UNION_USER_ATTACH_REQUEST_BRANCH;
    return kaa_union;
}

kaa_union_t* kaa_create_record_user_attach_request_null_union_null_branch()
{
    kaa_union_t *kaa_union = kaa_create_record_user_attach_request_null_union();
    kaa_union->type = KAA_RECORD_USER_ATTACH_REQUEST_NULL_UNION_NULL_BRANCH;
    return kaa_union;
}
# endif // KAA_RECORD_USER_ATTACH_REQUEST_NULL_UNION_C_



# ifndef KAA_ARRAY_ENDPOINT_ATTACH_REQUEST_NULL_UNION_C_
# define KAA_ARRAY_ENDPOINT_ATTACH_REQUEST_NULL_UNION_C_
static void kaa_destroy_array_endpoint_attach_request_null_union(void *data)
{
    kaa_union_t *kaa_union = (kaa_union_t*)data;

    switch (kaa_union->type) {
    case KAA_ARRAY_ENDPOINT_ATTACH_REQUEST_NULL_UNION_ARRAY_BRANCH:
    {
        kaa_list_destroy(kaa_union->data, kaa_destroy_endpoint_attach_request);
        break;
    }
    default:
        break;
    }
}

static size_t kaa_get_size_array_endpoint_attach_request_null_union(void *data)
{
    kaa_union_t *kaa_union = (kaa_union_t*)data;
    size_t union_len = size_long(kaa_union->type);

    switch (kaa_union->type) {
    case KAA_ARRAY_ENDPOINT_ATTACH_REQUEST_NULL_UNION_ARRAY_BRANCH:
    {
            union_len += kaa_array_size(kaa_union->data, kaa_get_size_endpoint_attach_request);
            break;
    }
    default:
        break;
    }
    
    return union_len;
}

static void kaa_serialize_array_endpoint_attach_request_null_union(avro_writer_t writer, void *data)
{
    kaa_union_t *kaa_union = (kaa_union_t*)data;

    int64_t branch = kaa_union->type;
    avro_binary_encoding.write_long(writer, branch);

    switch (kaa_union->type) {
    case KAA_ARRAY_ENDPOINT_ATTACH_REQUEST_NULL_UNION_ARRAY_BRANCH:
    {
            kaa_serialize_array(writer, kaa_union->data, kaa_serialize_endpoint_attach_request);
            break;
    }
    default:
        break;
    }
}
static kaa_union_t* kaa_create_array_endpoint_attach_request_null_union()
{
    kaa_union_t* kaa_union = KAA_MALLOC(kaa_union_t);
    kaa_union->data = NULL;
    kaa_union->serialize = kaa_serialize_array_endpoint_attach_request_null_union;
    kaa_union->get_size = kaa_get_size_array_endpoint_attach_request_null_union;
    kaa_union->destroy = kaa_destroy_array_endpoint_attach_request_null_union;

    return kaa_union; 
}

kaa_union_t* kaa_create_array_endpoint_attach_request_null_union_array_branch()
{
    kaa_union_t *kaa_union = kaa_create_array_endpoint_attach_request_null_union();
    kaa_union->type = KAA_ARRAY_ENDPOINT_ATTACH_REQUEST_NULL_UNION_ARRAY_BRANCH;
    return kaa_union;
}

kaa_union_t* kaa_create_array_endpoint_attach_request_null_union_null_branch()
{
    kaa_union_t *kaa_union = kaa_create_array_endpoint_attach_request_null_union();
    kaa_union->type = KAA_ARRAY_ENDPOINT_ATTACH_REQUEST_NULL_UNION_NULL_BRANCH;
    return kaa_union;
}
# endif // KAA_ARRAY_ENDPOINT_ATTACH_REQUEST_NULL_UNION_C_



# ifndef KAA_ARRAY_ENDPOINT_DETACH_REQUEST_NULL_UNION_C_
# define KAA_ARRAY_ENDPOINT_DETACH_REQUEST_NULL_UNION_C_
static void kaa_destroy_array_endpoint_detach_request_null_union(void *data)
{
    kaa_union_t *kaa_union = (kaa_union_t*)data;

    switch (kaa_union->type) {
    case KAA_ARRAY_ENDPOINT_DETACH_REQUEST_NULL_UNION_ARRAY_BRANCH:
    {
        kaa_list_destroy(kaa_union->data, kaa_destroy_endpoint_detach_request);
        break;
    }
    default:
        break;
    }
}

static size_t kaa_get_size_array_endpoint_detach_request_null_union(void *data)
{
    kaa_union_t *kaa_union = (kaa_union_t*)data;
    size_t union_len = size_long(kaa_union->type);

    switch (kaa_union->type) {
    case KAA_ARRAY_ENDPOINT_DETACH_REQUEST_NULL_UNION_ARRAY_BRANCH:
    {
            union_len += kaa_array_size(kaa_union->data, kaa_get_size_endpoint_detach_request);
            break;
    }
    default:
        break;
    }
    
    return union_len;
}

static void kaa_serialize_array_endpoint_detach_request_null_union(avro_writer_t writer, void *data)
{
    kaa_union_t *kaa_union = (kaa_union_t*)data;

    int64_t branch = kaa_union->type;
    avro_binary_encoding.write_long(writer, branch);

    switch (kaa_union->type) {
    case KAA_ARRAY_ENDPOINT_DETACH_REQUEST_NULL_UNION_ARRAY_BRANCH:
    {
            kaa_serialize_array(writer, kaa_union->data, kaa_serialize_endpoint_detach_request);
            break;
    }
    default:
        break;
    }
}
static kaa_union_t* kaa_create_array_endpoint_detach_request_null_union()
{
    kaa_union_t* kaa_union = KAA_MALLOC(kaa_union_t);
    kaa_union->data = NULL;
    kaa_union->serialize = kaa_serialize_array_endpoint_detach_request_null_union;
    kaa_union->get_size = kaa_get_size_array_endpoint_detach_request_null_union;
    kaa_union->destroy = kaa_destroy_array_endpoint_detach_request_null_union;

    return kaa_union; 
}

kaa_union_t* kaa_create_array_endpoint_detach_request_null_union_array_branch()
{
    kaa_union_t *kaa_union = kaa_create_array_endpoint_detach_request_null_union();
    kaa_union->type = KAA_ARRAY_ENDPOINT_DETACH_REQUEST_NULL_UNION_ARRAY_BRANCH;
    return kaa_union;
}

kaa_union_t* kaa_create_array_endpoint_detach_request_null_union_null_branch()
{
    kaa_union_t *kaa_union = kaa_create_array_endpoint_detach_request_null_union();
    kaa_union->type = KAA_ARRAY_ENDPOINT_DETACH_REQUEST_NULL_UNION_NULL_BRANCH;
    return kaa_union;
}
# endif // KAA_ARRAY_ENDPOINT_DETACH_REQUEST_NULL_UNION_C_



static void kaa_destroy_user_sync_request(void* data)
{
    kaa_user_sync_request_t* record = (kaa_user_sync_request_t*)data;

    record->user_attach_request->destroy(record->user_attach_request);
    KAA_FREE(record->user_attach_request);
    record->endpoint_attach_requests->destroy(record->endpoint_attach_requests);
    KAA_FREE(record->endpoint_attach_requests);
    record->endpoint_detach_requests->destroy(record->endpoint_detach_requests);
    KAA_FREE(record->endpoint_detach_requests);
}
static size_t kaa_get_size_user_sync_request(void* data)
{
    size_t record_size = 0;
    kaa_user_sync_request_t* record = (kaa_user_sync_request_t*)data;

    record_size += record->user_attach_request->get_size(record->user_attach_request);
    record_size += record->endpoint_attach_requests->get_size(record->endpoint_attach_requests);
    record_size += record->endpoint_detach_requests->get_size(record->endpoint_detach_requests);

    return record_size;
}

static void kaa_serialize_user_sync_request(avro_writer_t writer, void* data)
{
    kaa_user_sync_request_t* record = (kaa_user_sync_request_t*)data;

    record->user_attach_request->serialize(writer, record->user_attach_request);
    record->endpoint_attach_requests->serialize(writer, record->endpoint_attach_requests);
    record->endpoint_detach_requests->serialize(writer, record->endpoint_detach_requests);
}

kaa_user_sync_request_t* kaa_create_user_sync_request()
{
    kaa_user_sync_request_t* record = KAA_MALLOC(kaa_user_sync_request_t);
    record->serialize = kaa_serialize_user_sync_request;
    record->get_size = kaa_get_size_user_sync_request;
    record->destroy = kaa_destroy_user_sync_request;
    return record;
}



# ifndef KAA_RECORD_EVENT_SEQUENCE_NUMBER_REQUEST_NULL_UNION_C_
# define KAA_RECORD_EVENT_SEQUENCE_NUMBER_REQUEST_NULL_UNION_C_
static void kaa_destroy_record_event_sequence_number_request_null_union(void *data)
{
    kaa_union_t *kaa_union = (kaa_union_t*)data;

    switch (kaa_union->type) {
    case KAA_RECORD_EVENT_SEQUENCE_NUMBER_REQUEST_NULL_UNION_EVENT_SEQUENCE_NUMBER_REQUEST_BRANCH:
    {
        KAA_FREE(kaa_union->data);
        break;
    }
    default:
        break;
    }
}

static size_t kaa_get_size_record_event_sequence_number_request_null_union(void *data)
{
    kaa_union_t *kaa_union = (kaa_union_t*)data;
    size_t union_len = size_long(kaa_union->type);

    switch (kaa_union->type) {
    case KAA_RECORD_EVENT_SEQUENCE_NUMBER_REQUEST_NULL_UNION_EVENT_SEQUENCE_NUMBER_REQUEST_BRANCH:
    {
        kaa_event_sequence_number_request_t* record = (kaa_event_sequence_number_request_t*)kaa_union->data;
        union_len += record->get_size(record);
        break;
    }
    default:
        break;
    }
    
    return union_len;
}

static void kaa_serialize_record_event_sequence_number_request_null_union(avro_writer_t writer, void *data)
{
    kaa_union_t *kaa_union = (kaa_union_t*)data;

    int64_t branch = kaa_union->type;
    avro_binary_encoding.write_long(writer, branch);

    switch (kaa_union->type) {
    case KAA_RECORD_EVENT_SEQUENCE_NUMBER_REQUEST_NULL_UNION_EVENT_SEQUENCE_NUMBER_REQUEST_BRANCH:
    {
        kaa_event_sequence_number_request_t* record = (kaa_event_sequence_number_request_t*)kaa_union->data;
        record->serialize(writer, record);
        break;
    }
    default:
        break;
    }
}
static kaa_union_t* kaa_create_record_event_sequence_number_request_null_union()
{
    kaa_union_t* kaa_union = KAA_MALLOC(kaa_union_t);
    kaa_union->data = NULL;
    kaa_union->serialize = kaa_serialize_record_event_sequence_number_request_null_union;
    kaa_union->get_size = kaa_get_size_record_event_sequence_number_request_null_union;
    kaa_union->destroy = kaa_destroy_record_event_sequence_number_request_null_union;

    return kaa_union; 
}

kaa_union_t* kaa_create_record_event_sequence_number_request_null_union_event_sequence_number_request_branch()
{
    kaa_union_t *kaa_union = kaa_create_record_event_sequence_number_request_null_union();
    kaa_union->type = KAA_RECORD_EVENT_SEQUENCE_NUMBER_REQUEST_NULL_UNION_EVENT_SEQUENCE_NUMBER_REQUEST_BRANCH;
    return kaa_union;
}

kaa_union_t* kaa_create_record_event_sequence_number_request_null_union_null_branch()
{
    kaa_union_t *kaa_union = kaa_create_record_event_sequence_number_request_null_union();
    kaa_union->type = KAA_RECORD_EVENT_SEQUENCE_NUMBER_REQUEST_NULL_UNION_NULL_BRANCH;
    return kaa_union;
}
# endif // KAA_RECORD_EVENT_SEQUENCE_NUMBER_REQUEST_NULL_UNION_C_



# ifndef KAA_ARRAY_EVENT_LISTENERS_REQUEST_NULL_UNION_C_
# define KAA_ARRAY_EVENT_LISTENERS_REQUEST_NULL_UNION_C_
static void kaa_destroy_array_event_listeners_request_null_union(void *data)
{
    kaa_union_t *kaa_union = (kaa_union_t*)data;

    switch (kaa_union->type) {
    case KAA_ARRAY_EVENT_LISTENERS_REQUEST_NULL_UNION_ARRAY_BRANCH:
    {
        kaa_list_destroy(kaa_union->data, kaa_destroy_event_listeners_request);
        break;
    }
    default:
        break;
    }
}

static size_t kaa_get_size_array_event_listeners_request_null_union(void *data)
{
    kaa_union_t *kaa_union = (kaa_union_t*)data;
    size_t union_len = size_long(kaa_union->type);

    switch (kaa_union->type) {
    case KAA_ARRAY_EVENT_LISTENERS_REQUEST_NULL_UNION_ARRAY_BRANCH:
    {
            union_len += kaa_array_size(kaa_union->data, kaa_get_size_event_listeners_request);
            break;
    }
    default:
        break;
    }
    
    return union_len;
}

static void kaa_serialize_array_event_listeners_request_null_union(avro_writer_t writer, void *data)
{
    kaa_union_t *kaa_union = (kaa_union_t*)data;

    int64_t branch = kaa_union->type;
    avro_binary_encoding.write_long(writer, branch);

    switch (kaa_union->type) {
    case KAA_ARRAY_EVENT_LISTENERS_REQUEST_NULL_UNION_ARRAY_BRANCH:
    {
            kaa_serialize_array(writer, kaa_union->data, kaa_serialize_event_listeners_request);
            break;
    }
    default:
        break;
    }
}
static kaa_union_t* kaa_create_array_event_listeners_request_null_union()
{
    kaa_union_t* kaa_union = KAA_MALLOC(kaa_union_t);
    kaa_union->data = NULL;
    kaa_union->serialize = kaa_serialize_array_event_listeners_request_null_union;
    kaa_union->get_size = kaa_get_size_array_event_listeners_request_null_union;
    kaa_union->destroy = kaa_destroy_array_event_listeners_request_null_union;

    return kaa_union; 
}

kaa_union_t* kaa_create_array_event_listeners_request_null_union_array_branch()
{
    kaa_union_t *kaa_union = kaa_create_array_event_listeners_request_null_union();
    kaa_union->type = KAA_ARRAY_EVENT_LISTENERS_REQUEST_NULL_UNION_ARRAY_BRANCH;
    return kaa_union;
}

kaa_union_t* kaa_create_array_event_listeners_request_null_union_null_branch()
{
    kaa_union_t *kaa_union = kaa_create_array_event_listeners_request_null_union();
    kaa_union->type = KAA_ARRAY_EVENT_LISTENERS_REQUEST_NULL_UNION_NULL_BRANCH;
    return kaa_union;
}
# endif // KAA_ARRAY_EVENT_LISTENERS_REQUEST_NULL_UNION_C_



# ifndef KAA_ARRAY_EVENT_NULL_UNION_C_
# define KAA_ARRAY_EVENT_NULL_UNION_C_
static void kaa_destroy_array_event_null_union(void *data)
{
    kaa_union_t *kaa_union = (kaa_union_t*)data;

    switch (kaa_union->type) {
    case KAA_ARRAY_EVENT_NULL_UNION_ARRAY_BRANCH:
    {
        kaa_list_destroy(kaa_union->data, kaa_destroy_event);
        break;
    }
    default:
        break;
    }
}

static size_t kaa_get_size_array_event_null_union(void *data)
{
    kaa_union_t *kaa_union = (kaa_union_t*)data;
    size_t union_len = size_long(kaa_union->type);

    switch (kaa_union->type) {
    case KAA_ARRAY_EVENT_NULL_UNION_ARRAY_BRANCH:
    {
            union_len += kaa_array_size(kaa_union->data, kaa_get_size_event);
            break;
    }
    default:
        break;
    }
    
    return union_len;
}

static void kaa_serialize_array_event_null_union(avro_writer_t writer, void *data)
{
    kaa_union_t *kaa_union = (kaa_union_t*)data;

    int64_t branch = kaa_union->type;
    avro_binary_encoding.write_long(writer, branch);

    switch (kaa_union->type) {
    case KAA_ARRAY_EVENT_NULL_UNION_ARRAY_BRANCH:
    {
            kaa_serialize_array(writer, kaa_union->data, kaa_serialize_event);
            break;
    }
    default:
        break;
    }
}
static kaa_union_t* kaa_create_array_event_null_union()
{
    kaa_union_t* kaa_union = KAA_MALLOC(kaa_union_t);
    kaa_union->data = NULL;
    kaa_union->serialize = kaa_serialize_array_event_null_union;
    kaa_union->get_size = kaa_get_size_array_event_null_union;
    kaa_union->destroy = kaa_destroy_array_event_null_union;

    return kaa_union; 
}

kaa_union_t* kaa_create_array_event_null_union_array_branch()
{
    kaa_union_t *kaa_union = kaa_create_array_event_null_union();
    kaa_union->type = KAA_ARRAY_EVENT_NULL_UNION_ARRAY_BRANCH;
    return kaa_union;
}

kaa_union_t* kaa_create_array_event_null_union_null_branch()
{
    kaa_union_t *kaa_union = kaa_create_array_event_null_union();
    kaa_union->type = KAA_ARRAY_EVENT_NULL_UNION_NULL_BRANCH;
    return kaa_union;
}

kaa_union_t* kaa_deserialize_array_event_null_union(avro_reader_t reader)
{
    kaa_union_t *kaa_union = kaa_create_array_event_null_union();

    int64_t branch;
    avro_binary_encoding.read_long(reader, &branch);
    kaa_union->type = branch;

    switch (kaa_union->type) {
    case KAA_ARRAY_EVENT_NULL_UNION_ARRAY_BRANCH:
    {
            kaa_union->data = kaa_deserialize_array(reader, (deserialize_fn)kaa_deserialize_event);
            break;
    }
    default:
        break;
    }

    return kaa_union;
}
# endif // KAA_ARRAY_EVENT_NULL_UNION_C_



static void kaa_destroy_event_sync_request(void* data)
{
    kaa_event_sync_request_t* record = (kaa_event_sync_request_t*)data;

    record->event_sequence_number_request->destroy(record->event_sequence_number_request);
    KAA_FREE(record->event_sequence_number_request);
    record->event_listeners_requests->destroy(record->event_listeners_requests);
    KAA_FREE(record->event_listeners_requests);
    record->events->destroy(record->events);
    KAA_FREE(record->events);
}
static size_t kaa_get_size_event_sync_request(void* data)
{
    size_t record_size = 0;
    kaa_event_sync_request_t* record = (kaa_event_sync_request_t*)data;

    record_size += record->event_sequence_number_request->get_size(record->event_sequence_number_request);
    record_size += record->event_listeners_requests->get_size(record->event_listeners_requests);
    record_size += record->events->get_size(record->events);

    return record_size;
}

static void kaa_serialize_event_sync_request(avro_writer_t writer, void* data)
{
    kaa_event_sync_request_t* record = (kaa_event_sync_request_t*)data;

    record->event_sequence_number_request->serialize(writer, record->event_sequence_number_request);
    record->event_listeners_requests->serialize(writer, record->event_listeners_requests);
    record->events->serialize(writer, record->events);
}

kaa_event_sync_request_t* kaa_create_event_sync_request()
{
    kaa_event_sync_request_t* record = KAA_MALLOC(kaa_event_sync_request_t);
    record->serialize = kaa_serialize_event_sync_request;
    record->get_size = kaa_get_size_event_sync_request;
    record->destroy = kaa_destroy_event_sync_request;
    return record;
}



# ifndef KAA_ARRAY_LOG_ENTRY_NULL_UNION_C_
# define KAA_ARRAY_LOG_ENTRY_NULL_UNION_C_
static void kaa_destroy_array_log_entry_null_union(void *data)
{
    kaa_union_t *kaa_union = (kaa_union_t*)data;

    switch (kaa_union->type) {
    case KAA_ARRAY_LOG_ENTRY_NULL_UNION_ARRAY_BRANCH:
    {
        kaa_list_destroy(kaa_union->data, kaa_destroy_log_entry);
        break;
    }
    default:
        break;
    }
}

static size_t kaa_get_size_array_log_entry_null_union(void *data)
{
    kaa_union_t *kaa_union = (kaa_union_t*)data;
    size_t union_len = size_long(kaa_union->type);

    switch (kaa_union->type) {
    case KAA_ARRAY_LOG_ENTRY_NULL_UNION_ARRAY_BRANCH:
    {
            union_len += kaa_array_size(kaa_union->data, kaa_get_size_log_entry);
            break;
    }
    default:
        break;
    }
    
    return union_len;
}

static void kaa_serialize_array_log_entry_null_union(avro_writer_t writer, void *data)
{
    kaa_union_t *kaa_union = (kaa_union_t*)data;

    int64_t branch = kaa_union->type;
    avro_binary_encoding.write_long(writer, branch);

    switch (kaa_union->type) {
    case KAA_ARRAY_LOG_ENTRY_NULL_UNION_ARRAY_BRANCH:
    {
            kaa_serialize_array(writer, kaa_union->data, kaa_serialize_log_entry);
            break;
    }
    default:
        break;
    }
}
static kaa_union_t* kaa_create_array_log_entry_null_union()
{
    kaa_union_t* kaa_union = KAA_MALLOC(kaa_union_t);
    kaa_union->data = NULL;
    kaa_union->serialize = kaa_serialize_array_log_entry_null_union;
    kaa_union->get_size = kaa_get_size_array_log_entry_null_union;
    kaa_union->destroy = kaa_destroy_array_log_entry_null_union;

    return kaa_union; 
}

kaa_union_t* kaa_create_array_log_entry_null_union_array_branch()
{
    kaa_union_t *kaa_union = kaa_create_array_log_entry_null_union();
    kaa_union->type = KAA_ARRAY_LOG_ENTRY_NULL_UNION_ARRAY_BRANCH;
    return kaa_union;
}

kaa_union_t* kaa_create_array_log_entry_null_union_null_branch()
{
    kaa_union_t *kaa_union = kaa_create_array_log_entry_null_union();
    kaa_union->type = KAA_ARRAY_LOG_ENTRY_NULL_UNION_NULL_BRANCH;
    return kaa_union;
}
# endif // KAA_ARRAY_LOG_ENTRY_NULL_UNION_C_



static void kaa_destroy_log_sync_request(void* data)
{
    kaa_log_sync_request_t* record = (kaa_log_sync_request_t*)data;

    record->request_id->destroy(record->request_id);
    KAA_FREE(record->request_id);
    record->log_entries->destroy(record->log_entries);
    KAA_FREE(record->log_entries);
}
static size_t kaa_get_size_log_sync_request(void* data)
{
    size_t record_size = 0;
    kaa_log_sync_request_t* record = (kaa_log_sync_request_t*)data;

    record_size += record->request_id->get_size(record->request_id);
    record_size += record->log_entries->get_size(record->log_entries);

    return record_size;
}

static void kaa_serialize_log_sync_request(avro_writer_t writer, void* data)
{
    kaa_log_sync_request_t* record = (kaa_log_sync_request_t*)data;

    record->request_id->serialize(writer, record->request_id);
    record->log_entries->serialize(writer, record->log_entries);
}

kaa_log_sync_request_t* kaa_create_log_sync_request()
{
    kaa_log_sync_request_t* record = KAA_MALLOC(kaa_log_sync_request_t);
    record->serialize = kaa_serialize_log_sync_request;
    record->get_size = kaa_get_size_log_sync_request;
    record->destroy = kaa_destroy_log_sync_request;
    return record;
}




kaa_profile_sync_response_t* kaa_deserialize_profile_sync_response(avro_reader_t reader)
{
    kaa_profile_sync_response_t* record = KAA_MALLOC(kaa_profile_sync_response_t);
    record->destroy = kaa_destroy_null;
    
    int64_t response_status_value;
    avro_binary_encoding.read_long(reader, &response_status_value);
    record->response_status = response_status_value;
    
    return record;
}



static void kaa_destroy_configuration_sync_response(void* data)
{
    kaa_configuration_sync_response_t* record = (kaa_configuration_sync_response_t*)data;

    record->conf_schema_body->destroy(record->conf_schema_body);
    KAA_FREE(record->conf_schema_body);
    record->conf_delta_body->destroy(record->conf_delta_body);
    KAA_FREE(record->conf_delta_body);
}

kaa_configuration_sync_response_t* kaa_deserialize_configuration_sync_response(avro_reader_t reader)
{
    kaa_configuration_sync_response_t* record = KAA_MALLOC(kaa_configuration_sync_response_t);
    record->destroy = kaa_destroy_configuration_sync_response;
    
    avro_binary_encoding.read_int(reader, &record->app_state_seq_number);
    int64_t response_status_value;
    avro_binary_encoding.read_long(reader, &response_status_value);
    record->response_status = response_status_value;
    record->conf_schema_body = kaa_deserialize_bytes_null_union(reader);
    record->conf_delta_body = kaa_deserialize_bytes_null_union(reader);
    
    return record;
}



# ifndef KAA_ARRAY_NOTIFICATION_NULL_UNION_C_
# define KAA_ARRAY_NOTIFICATION_NULL_UNION_C_
static void kaa_destroy_array_notification_null_union(void *data)
{
    kaa_union_t *kaa_union = (kaa_union_t*)data;

    switch (kaa_union->type) {
    case KAA_ARRAY_NOTIFICATION_NULL_UNION_ARRAY_BRANCH:
    {
        kaa_list_destroy(kaa_union->data, kaa_destroy_notification);
        break;
    }
    default:
        break;
    }
}
static kaa_union_t* kaa_create_array_notification_null_union()
{
    kaa_union_t* kaa_union = KAA_MALLOC(kaa_union_t);
    kaa_union->data = NULL;
    kaa_union->serialize = NULL;
    kaa_union->get_size = NULL;
    kaa_union->destroy = kaa_destroy_array_notification_null_union;

    return kaa_union; 
}

kaa_union_t* kaa_deserialize_array_notification_null_union(avro_reader_t reader)
{
    kaa_union_t *kaa_union = kaa_create_array_notification_null_union();

    int64_t branch;
    avro_binary_encoding.read_long(reader, &branch);
    kaa_union->type = branch;

    switch (kaa_union->type) {
    case KAA_ARRAY_NOTIFICATION_NULL_UNION_ARRAY_BRANCH:
    {
            kaa_union->data = kaa_deserialize_array(reader, (deserialize_fn)kaa_deserialize_notification);
            break;
    }
    default:
        break;
    }

    return kaa_union;
}
# endif // KAA_ARRAY_NOTIFICATION_NULL_UNION_C_



# ifndef KAA_ARRAY_TOPIC_NULL_UNION_C_
# define KAA_ARRAY_TOPIC_NULL_UNION_C_
static void kaa_destroy_array_topic_null_union(void *data)
{
    kaa_union_t *kaa_union = (kaa_union_t*)data;

    switch (kaa_union->type) {
    case KAA_ARRAY_TOPIC_NULL_UNION_ARRAY_BRANCH:
    {
        kaa_list_destroy(kaa_union->data, kaa_destroy_topic);
        break;
    }
    default:
        break;
    }
}
static kaa_union_t* kaa_create_array_topic_null_union()
{
    kaa_union_t* kaa_union = KAA_MALLOC(kaa_union_t);
    kaa_union->data = NULL;
    kaa_union->serialize = NULL;
    kaa_union->get_size = NULL;
    kaa_union->destroy = kaa_destroy_array_topic_null_union;

    return kaa_union; 
}

kaa_union_t* kaa_deserialize_array_topic_null_union(avro_reader_t reader)
{
    kaa_union_t *kaa_union = kaa_create_array_topic_null_union();

    int64_t branch;
    avro_binary_encoding.read_long(reader, &branch);
    kaa_union->type = branch;

    switch (kaa_union->type) {
    case KAA_ARRAY_TOPIC_NULL_UNION_ARRAY_BRANCH:
    {
            kaa_union->data = kaa_deserialize_array(reader, (deserialize_fn)kaa_deserialize_topic);
            break;
    }
    default:
        break;
    }

    return kaa_union;
}
# endif // KAA_ARRAY_TOPIC_NULL_UNION_C_



static void kaa_destroy_notification_sync_response(void* data)
{
    kaa_notification_sync_response_t* record = (kaa_notification_sync_response_t*)data;

    record->notifications->destroy(record->notifications);
    KAA_FREE(record->notifications);
    record->available_topics->destroy(record->available_topics);
    KAA_FREE(record->available_topics);
}

kaa_notification_sync_response_t* kaa_deserialize_notification_sync_response(avro_reader_t reader)
{
    kaa_notification_sync_response_t* record = KAA_MALLOC(kaa_notification_sync_response_t);
    record->destroy = kaa_destroy_notification_sync_response;
    
    avro_binary_encoding.read_int(reader, &record->app_state_seq_number);
    int64_t response_status_value;
    avro_binary_encoding.read_long(reader, &response_status_value);
    record->response_status = response_status_value;
    record->notifications = kaa_deserialize_array_notification_null_union(reader);
    record->available_topics = kaa_deserialize_array_topic_null_union(reader);
    
    return record;
}



# ifndef KAA_RECORD_USER_ATTACH_RESPONSE_NULL_UNION_C_
# define KAA_RECORD_USER_ATTACH_RESPONSE_NULL_UNION_C_
static void kaa_destroy_record_user_attach_response_null_union(void *data)
{
    kaa_union_t *kaa_union = (kaa_union_t*)data;

    switch (kaa_union->type) {
    case KAA_RECORD_USER_ATTACH_RESPONSE_NULL_UNION_USER_ATTACH_RESPONSE_BRANCH:
    {
        KAA_FREE(kaa_union->data);
        break;
    }
    default:
        break;
    }
}
static kaa_union_t* kaa_create_record_user_attach_response_null_union()
{
    kaa_union_t* kaa_union = KAA_MALLOC(kaa_union_t);
    kaa_union->data = NULL;
    kaa_union->serialize = NULL;
    kaa_union->get_size = NULL;
    kaa_union->destroy = kaa_destroy_record_user_attach_response_null_union;

    return kaa_union; 
}

kaa_union_t* kaa_deserialize_record_user_attach_response_null_union(avro_reader_t reader)
{
    kaa_union_t *kaa_union = kaa_create_record_user_attach_response_null_union();

    int64_t branch;
    avro_binary_encoding.read_long(reader, &branch);
    kaa_union->type = branch;

    switch (kaa_union->type) {
    case KAA_RECORD_USER_ATTACH_RESPONSE_NULL_UNION_USER_ATTACH_RESPONSE_BRANCH:
    {
        kaa_union->data = kaa_deserialize_user_attach_response(reader);
        break;
    }
    default:
        break;
    }

    return kaa_union;
}
# endif // KAA_RECORD_USER_ATTACH_RESPONSE_NULL_UNION_C_



# ifndef KAA_RECORD_USER_ATTACH_NOTIFICATION_NULL_UNION_C_
# define KAA_RECORD_USER_ATTACH_NOTIFICATION_NULL_UNION_C_
static void kaa_destroy_record_user_attach_notification_null_union(void *data)
{
    kaa_union_t *kaa_union = (kaa_union_t*)data;

    switch (kaa_union->type) {
    case KAA_RECORD_USER_ATTACH_NOTIFICATION_NULL_UNION_USER_ATTACH_NOTIFICATION_BRANCH:
    {
        kaa_user_attach_notification_t* record = (kaa_user_attach_notification_t*)kaa_union->data;
        record->destroy(record);
        KAA_FREE(kaa_union->data);
        break;
    }
    default:
        break;
    }
}
static kaa_union_t* kaa_create_record_user_attach_notification_null_union()
{
    kaa_union_t* kaa_union = KAA_MALLOC(kaa_union_t);
    kaa_union->data = NULL;
    kaa_union->serialize = NULL;
    kaa_union->get_size = NULL;
    kaa_union->destroy = kaa_destroy_record_user_attach_notification_null_union;

    return kaa_union; 
}

kaa_union_t* kaa_deserialize_record_user_attach_notification_null_union(avro_reader_t reader)
{
    kaa_union_t *kaa_union = kaa_create_record_user_attach_notification_null_union();

    int64_t branch;
    avro_binary_encoding.read_long(reader, &branch);
    kaa_union->type = branch;

    switch (kaa_union->type) {
    case KAA_RECORD_USER_ATTACH_NOTIFICATION_NULL_UNION_USER_ATTACH_NOTIFICATION_BRANCH:
    {
        kaa_union->data = kaa_deserialize_user_attach_notification(reader);
        break;
    }
    default:
        break;
    }

    return kaa_union;
}
# endif // KAA_RECORD_USER_ATTACH_NOTIFICATION_NULL_UNION_C_



# ifndef KAA_RECORD_USER_DETACH_NOTIFICATION_NULL_UNION_C_
# define KAA_RECORD_USER_DETACH_NOTIFICATION_NULL_UNION_C_
static void kaa_destroy_record_user_detach_notification_null_union(void *data)
{
    kaa_union_t *kaa_union = (kaa_union_t*)data;

    switch (kaa_union->type) {
    case KAA_RECORD_USER_DETACH_NOTIFICATION_NULL_UNION_USER_DETACH_NOTIFICATION_BRANCH:
    {
        kaa_user_detach_notification_t* record = (kaa_user_detach_notification_t*)kaa_union->data;
        record->destroy(record);
        KAA_FREE(kaa_union->data);
        break;
    }
    default:
        break;
    }
}
static kaa_union_t* kaa_create_record_user_detach_notification_null_union()
{
    kaa_union_t* kaa_union = KAA_MALLOC(kaa_union_t);
    kaa_union->data = NULL;
    kaa_union->serialize = NULL;
    kaa_union->get_size = NULL;
    kaa_union->destroy = kaa_destroy_record_user_detach_notification_null_union;

    return kaa_union; 
}

kaa_union_t* kaa_deserialize_record_user_detach_notification_null_union(avro_reader_t reader)
{
    kaa_union_t *kaa_union = kaa_create_record_user_detach_notification_null_union();

    int64_t branch;
    avro_binary_encoding.read_long(reader, &branch);
    kaa_union->type = branch;

    switch (kaa_union->type) {
    case KAA_RECORD_USER_DETACH_NOTIFICATION_NULL_UNION_USER_DETACH_NOTIFICATION_BRANCH:
    {
        kaa_union->data = kaa_deserialize_user_detach_notification(reader);
        break;
    }
    default:
        break;
    }

    return kaa_union;
}
# endif // KAA_RECORD_USER_DETACH_NOTIFICATION_NULL_UNION_C_



# ifndef KAA_ARRAY_ENDPOINT_ATTACH_RESPONSE_NULL_UNION_C_
# define KAA_ARRAY_ENDPOINT_ATTACH_RESPONSE_NULL_UNION_C_
static void kaa_destroy_array_endpoint_attach_response_null_union(void *data)
{
    kaa_union_t *kaa_union = (kaa_union_t*)data;

    switch (kaa_union->type) {
    case KAA_ARRAY_ENDPOINT_ATTACH_RESPONSE_NULL_UNION_ARRAY_BRANCH:
    {
        kaa_list_destroy(kaa_union->data, kaa_destroy_endpoint_attach_response);
        break;
    }
    default:
        break;
    }
}
static kaa_union_t* kaa_create_array_endpoint_attach_response_null_union()
{
    kaa_union_t* kaa_union = KAA_MALLOC(kaa_union_t);
    kaa_union->data = NULL;
    kaa_union->serialize = NULL;
    kaa_union->get_size = NULL;
    kaa_union->destroy = kaa_destroy_array_endpoint_attach_response_null_union;

    return kaa_union; 
}

kaa_union_t* kaa_deserialize_array_endpoint_attach_response_null_union(avro_reader_t reader)
{
    kaa_union_t *kaa_union = kaa_create_array_endpoint_attach_response_null_union();

    int64_t branch;
    avro_binary_encoding.read_long(reader, &branch);
    kaa_union->type = branch;

    switch (kaa_union->type) {
    case KAA_ARRAY_ENDPOINT_ATTACH_RESPONSE_NULL_UNION_ARRAY_BRANCH:
    {
            kaa_union->data = kaa_deserialize_array(reader, (deserialize_fn)kaa_deserialize_endpoint_attach_response);
            break;
    }
    default:
        break;
    }

    return kaa_union;
}
# endif // KAA_ARRAY_ENDPOINT_ATTACH_RESPONSE_NULL_UNION_C_



# ifndef KAA_ARRAY_ENDPOINT_DETACH_RESPONSE_NULL_UNION_C_
# define KAA_ARRAY_ENDPOINT_DETACH_RESPONSE_NULL_UNION_C_
static void kaa_destroy_array_endpoint_detach_response_null_union(void *data)
{
    kaa_union_t *kaa_union = (kaa_union_t*)data;

    switch (kaa_union->type) {
    case KAA_ARRAY_ENDPOINT_DETACH_RESPONSE_NULL_UNION_ARRAY_BRANCH:
    {
        kaa_list_destroy(kaa_union->data, kaa_destroy_endpoint_detach_response);
        break;
    }
    default:
        break;
    }
}
static kaa_union_t* kaa_create_array_endpoint_detach_response_null_union()
{
    kaa_union_t* kaa_union = KAA_MALLOC(kaa_union_t);
    kaa_union->data = NULL;
    kaa_union->serialize = NULL;
    kaa_union->get_size = NULL;
    kaa_union->destroy = kaa_destroy_array_endpoint_detach_response_null_union;

    return kaa_union; 
}

kaa_union_t* kaa_deserialize_array_endpoint_detach_response_null_union(avro_reader_t reader)
{
    kaa_union_t *kaa_union = kaa_create_array_endpoint_detach_response_null_union();

    int64_t branch;
    avro_binary_encoding.read_long(reader, &branch);
    kaa_union->type = branch;

    switch (kaa_union->type) {
    case KAA_ARRAY_ENDPOINT_DETACH_RESPONSE_NULL_UNION_ARRAY_BRANCH:
    {
            kaa_union->data = kaa_deserialize_array(reader, (deserialize_fn)kaa_deserialize_endpoint_detach_response);
            break;
    }
    default:
        break;
    }

    return kaa_union;
}
# endif // KAA_ARRAY_ENDPOINT_DETACH_RESPONSE_NULL_UNION_C_



static void kaa_destroy_user_sync_response(void* data)
{
    kaa_user_sync_response_t* record = (kaa_user_sync_response_t*)data;

    record->user_attach_response->destroy(record->user_attach_response);
    KAA_FREE(record->user_attach_response);
    record->user_attach_notification->destroy(record->user_attach_notification);
    KAA_FREE(record->user_attach_notification);
    record->user_detach_notification->destroy(record->user_detach_notification);
    KAA_FREE(record->user_detach_notification);
    record->endpoint_attach_responses->destroy(record->endpoint_attach_responses);
    KAA_FREE(record->endpoint_attach_responses);
    record->endpoint_detach_responses->destroy(record->endpoint_detach_responses);
    KAA_FREE(record->endpoint_detach_responses);
}

kaa_user_sync_response_t* kaa_deserialize_user_sync_response(avro_reader_t reader)
{
    kaa_user_sync_response_t* record = KAA_MALLOC(kaa_user_sync_response_t);
    record->destroy = kaa_destroy_user_sync_response;
    
    record->user_attach_response = kaa_deserialize_record_user_attach_response_null_union(reader);
    record->user_attach_notification = kaa_deserialize_record_user_attach_notification_null_union(reader);
    record->user_detach_notification = kaa_deserialize_record_user_detach_notification_null_union(reader);
    record->endpoint_attach_responses = kaa_deserialize_array_endpoint_attach_response_null_union(reader);
    record->endpoint_detach_responses = kaa_deserialize_array_endpoint_detach_response_null_union(reader);
    
    return record;
}



# ifndef KAA_RECORD_EVENT_SEQUENCE_NUMBER_RESPONSE_NULL_UNION_C_
# define KAA_RECORD_EVENT_SEQUENCE_NUMBER_RESPONSE_NULL_UNION_C_
static void kaa_destroy_record_event_sequence_number_response_null_union(void *data)
{
    kaa_union_t *kaa_union = (kaa_union_t*)data;

    switch (kaa_union->type) {
    case KAA_RECORD_EVENT_SEQUENCE_NUMBER_RESPONSE_NULL_UNION_EVENT_SEQUENCE_NUMBER_RESPONSE_BRANCH:
    {
        KAA_FREE(kaa_union->data);
        break;
    }
    default:
        break;
    }
}
static kaa_union_t* kaa_create_record_event_sequence_number_response_null_union()
{
    kaa_union_t* kaa_union = KAA_MALLOC(kaa_union_t);
    kaa_union->data = NULL;
    kaa_union->serialize = NULL;
    kaa_union->get_size = NULL;
    kaa_union->destroy = kaa_destroy_record_event_sequence_number_response_null_union;

    return kaa_union; 
}

kaa_union_t* kaa_deserialize_record_event_sequence_number_response_null_union(avro_reader_t reader)
{
    kaa_union_t *kaa_union = kaa_create_record_event_sequence_number_response_null_union();

    int64_t branch;
    avro_binary_encoding.read_long(reader, &branch);
    kaa_union->type = branch;

    switch (kaa_union->type) {
    case KAA_RECORD_EVENT_SEQUENCE_NUMBER_RESPONSE_NULL_UNION_EVENT_SEQUENCE_NUMBER_RESPONSE_BRANCH:
    {
        kaa_union->data = kaa_deserialize_event_sequence_number_response(reader);
        break;
    }
    default:
        break;
    }

    return kaa_union;
}
# endif // KAA_RECORD_EVENT_SEQUENCE_NUMBER_RESPONSE_NULL_UNION_C_



# ifndef KAA_ARRAY_EVENT_LISTENERS_RESPONSE_NULL_UNION_C_
# define KAA_ARRAY_EVENT_LISTENERS_RESPONSE_NULL_UNION_C_
static void kaa_destroy_array_event_listeners_response_null_union(void *data)
{
    kaa_union_t *kaa_union = (kaa_union_t*)data;

    switch (kaa_union->type) {
    case KAA_ARRAY_EVENT_LISTENERS_RESPONSE_NULL_UNION_ARRAY_BRANCH:
    {
        kaa_list_destroy(kaa_union->data, kaa_destroy_event_listeners_response);
        break;
    }
    default:
        break;
    }
}
static kaa_union_t* kaa_create_array_event_listeners_response_null_union()
{
    kaa_union_t* kaa_union = KAA_MALLOC(kaa_union_t);
    kaa_union->data = NULL;
    kaa_union->serialize = NULL;
    kaa_union->get_size = NULL;
    kaa_union->destroy = kaa_destroy_array_event_listeners_response_null_union;

    return kaa_union; 
}

kaa_union_t* kaa_deserialize_array_event_listeners_response_null_union(avro_reader_t reader)
{
    kaa_union_t *kaa_union = kaa_create_array_event_listeners_response_null_union();

    int64_t branch;
    avro_binary_encoding.read_long(reader, &branch);
    kaa_union->type = branch;

    switch (kaa_union->type) {
    case KAA_ARRAY_EVENT_LISTENERS_RESPONSE_NULL_UNION_ARRAY_BRANCH:
    {
            kaa_union->data = kaa_deserialize_array(reader, (deserialize_fn)kaa_deserialize_event_listeners_response);
            break;
    }
    default:
        break;
    }

    return kaa_union;
}
# endif // KAA_ARRAY_EVENT_LISTENERS_RESPONSE_NULL_UNION_C_



static void kaa_destroy_event_sync_response(void* data)
{
    kaa_event_sync_response_t* record = (kaa_event_sync_response_t*)data;

    record->event_sequence_number_response->destroy(record->event_sequence_number_response);
    KAA_FREE(record->event_sequence_number_response);
    record->event_listeners_responses->destroy(record->event_listeners_responses);
    KAA_FREE(record->event_listeners_responses);
    record->events->destroy(record->events);
    KAA_FREE(record->events);
}

kaa_event_sync_response_t* kaa_deserialize_event_sync_response(avro_reader_t reader)
{
    kaa_event_sync_response_t* record = KAA_MALLOC(kaa_event_sync_response_t);
    record->destroy = kaa_destroy_event_sync_response;
    
    record->event_sequence_number_response = kaa_deserialize_record_event_sequence_number_response_null_union(reader);
    record->event_listeners_responses = kaa_deserialize_array_event_listeners_response_null_union(reader);
    record->events = kaa_deserialize_array_event_null_union(reader);
    
    return record;
}



static void kaa_destroy_log_sync_response(void* data)
{
    kaa_log_sync_response_t* record = (kaa_log_sync_response_t*)data;

    KAA_FREE(record->request_id);
}

kaa_log_sync_response_t* kaa_deserialize_log_sync_response(avro_reader_t reader)
{
    kaa_log_sync_response_t* record = KAA_MALLOC(kaa_log_sync_response_t);
    record->destroy = kaa_destroy_log_sync_response;
    
        int64_t request_id_size;
    avro_binary_encoding.read_string(reader, &record->request_id, &request_id_size);
        int64_t result_value;
    avro_binary_encoding.read_long(reader, &result_value);
    record->result = result_value;
    
    return record;
}



static void kaa_destroy_redirect_sync_response(void* data)
{
    kaa_redirect_sync_response_t* record = (kaa_redirect_sync_response_t*)data;

    KAA_FREE(record->dns_name);
}

kaa_redirect_sync_response_t* kaa_deserialize_redirect_sync_response(avro_reader_t reader)
{
    kaa_redirect_sync_response_t* record = KAA_MALLOC(kaa_redirect_sync_response_t);
    record->destroy = kaa_destroy_redirect_sync_response;
    
        int64_t dns_name_size;
    avro_binary_encoding.read_string(reader, &record->dns_name, &dns_name_size);
        
    return record;
}



# ifndef KAA_RECORD_SYNC_REQUEST_META_DATA_NULL_UNION_C_
# define KAA_RECORD_SYNC_REQUEST_META_DATA_NULL_UNION_C_
static void kaa_destroy_record_sync_request_meta_data_null_union(void *data)
{
    kaa_union_t *kaa_union = (kaa_union_t*)data;

    switch (kaa_union->type) {
    case KAA_RECORD_SYNC_REQUEST_META_DATA_NULL_UNION_SYNC_REQUEST_META_DATA_BRANCH:
    {
        kaa_sync_request_meta_data_t* record = (kaa_sync_request_meta_data_t*)kaa_union->data;
        record->destroy(record);
        KAA_FREE(kaa_union->data);
        break;
    }
    default:
        break;
    }
}

static size_t kaa_get_size_record_sync_request_meta_data_null_union(void *data)
{
    kaa_union_t *kaa_union = (kaa_union_t*)data;
    size_t union_len = size_long(kaa_union->type);

    switch (kaa_union->type) {
    case KAA_RECORD_SYNC_REQUEST_META_DATA_NULL_UNION_SYNC_REQUEST_META_DATA_BRANCH:
    {
        kaa_sync_request_meta_data_t* record = (kaa_sync_request_meta_data_t*)kaa_union->data;
        union_len += record->get_size(record);
        break;
    }
    default:
        break;
    }
    
    return union_len;
}

static void kaa_serialize_record_sync_request_meta_data_null_union(avro_writer_t writer, void *data)
{
    kaa_union_t *kaa_union = (kaa_union_t*)data;

    int64_t branch = kaa_union->type;
    avro_binary_encoding.write_long(writer, branch);

    switch (kaa_union->type) {
    case KAA_RECORD_SYNC_REQUEST_META_DATA_NULL_UNION_SYNC_REQUEST_META_DATA_BRANCH:
    {
        kaa_sync_request_meta_data_t* record = (kaa_sync_request_meta_data_t*)kaa_union->data;
        record->serialize(writer, record);
        break;
    }
    default:
        break;
    }
}
static kaa_union_t* kaa_create_record_sync_request_meta_data_null_union()
{
    kaa_union_t* kaa_union = KAA_MALLOC(kaa_union_t);
    kaa_union->data = NULL;
    kaa_union->serialize = kaa_serialize_record_sync_request_meta_data_null_union;
    kaa_union->get_size = kaa_get_size_record_sync_request_meta_data_null_union;
    kaa_union->destroy = kaa_destroy_record_sync_request_meta_data_null_union;

    return kaa_union; 
}

kaa_union_t* kaa_create_record_sync_request_meta_data_null_union_sync_request_meta_data_branch()
{
    kaa_union_t *kaa_union = kaa_create_record_sync_request_meta_data_null_union();
    kaa_union->type = KAA_RECORD_SYNC_REQUEST_META_DATA_NULL_UNION_SYNC_REQUEST_META_DATA_BRANCH;
    return kaa_union;
}

kaa_union_t* kaa_create_record_sync_request_meta_data_null_union_null_branch()
{
    kaa_union_t *kaa_union = kaa_create_record_sync_request_meta_data_null_union();
    kaa_union->type = KAA_RECORD_SYNC_REQUEST_META_DATA_NULL_UNION_NULL_BRANCH;
    return kaa_union;
}
# endif // KAA_RECORD_SYNC_REQUEST_META_DATA_NULL_UNION_C_



# ifndef KAA_RECORD_PROFILE_SYNC_REQUEST_NULL_UNION_C_
# define KAA_RECORD_PROFILE_SYNC_REQUEST_NULL_UNION_C_
static void kaa_destroy_record_profile_sync_request_null_union(void *data)
{
    kaa_union_t *kaa_union = (kaa_union_t*)data;

    switch (kaa_union->type) {
    case KAA_RECORD_PROFILE_SYNC_REQUEST_NULL_UNION_PROFILE_SYNC_REQUEST_BRANCH:
    {
        kaa_profile_sync_request_t* record = (kaa_profile_sync_request_t*)kaa_union->data;
        record->destroy(record);
        KAA_FREE(kaa_union->data);
        break;
    }
    default:
        break;
    }
}

static size_t kaa_get_size_record_profile_sync_request_null_union(void *data)
{
    kaa_union_t *kaa_union = (kaa_union_t*)data;
    size_t union_len = size_long(kaa_union->type);

    switch (kaa_union->type) {
    case KAA_RECORD_PROFILE_SYNC_REQUEST_NULL_UNION_PROFILE_SYNC_REQUEST_BRANCH:
    {
        kaa_profile_sync_request_t* record = (kaa_profile_sync_request_t*)kaa_union->data;
        union_len += record->get_size(record);
        break;
    }
    default:
        break;
    }
    
    return union_len;
}

static void kaa_serialize_record_profile_sync_request_null_union(avro_writer_t writer, void *data)
{
    kaa_union_t *kaa_union = (kaa_union_t*)data;

    int64_t branch = kaa_union->type;
    avro_binary_encoding.write_long(writer, branch);

    switch (kaa_union->type) {
    case KAA_RECORD_PROFILE_SYNC_REQUEST_NULL_UNION_PROFILE_SYNC_REQUEST_BRANCH:
    {
        kaa_profile_sync_request_t* record = (kaa_profile_sync_request_t*)kaa_union->data;
        record->serialize(writer, record);
        break;
    }
    default:
        break;
    }
}
static kaa_union_t* kaa_create_record_profile_sync_request_null_union()
{
    kaa_union_t* kaa_union = KAA_MALLOC(kaa_union_t);
    kaa_union->data = NULL;
    kaa_union->serialize = kaa_serialize_record_profile_sync_request_null_union;
    kaa_union->get_size = kaa_get_size_record_profile_sync_request_null_union;
    kaa_union->destroy = kaa_destroy_record_profile_sync_request_null_union;

    return kaa_union; 
}

kaa_union_t* kaa_create_record_profile_sync_request_null_union_profile_sync_request_branch()
{
    kaa_union_t *kaa_union = kaa_create_record_profile_sync_request_null_union();
    kaa_union->type = KAA_RECORD_PROFILE_SYNC_REQUEST_NULL_UNION_PROFILE_SYNC_REQUEST_BRANCH;
    return kaa_union;
}

kaa_union_t* kaa_create_record_profile_sync_request_null_union_null_branch()
{
    kaa_union_t *kaa_union = kaa_create_record_profile_sync_request_null_union();
    kaa_union->type = KAA_RECORD_PROFILE_SYNC_REQUEST_NULL_UNION_NULL_BRANCH;
    return kaa_union;
}
# endif // KAA_RECORD_PROFILE_SYNC_REQUEST_NULL_UNION_C_



# ifndef KAA_RECORD_CONFIGURATION_SYNC_REQUEST_NULL_UNION_C_
# define KAA_RECORD_CONFIGURATION_SYNC_REQUEST_NULL_UNION_C_
static void kaa_destroy_record_configuration_sync_request_null_union(void *data)
{
    kaa_union_t *kaa_union = (kaa_union_t*)data;

    switch (kaa_union->type) {
    case KAA_RECORD_CONFIGURATION_SYNC_REQUEST_NULL_UNION_CONFIGURATION_SYNC_REQUEST_BRANCH:
    {
        kaa_configuration_sync_request_t* record = (kaa_configuration_sync_request_t*)kaa_union->data;
        record->destroy(record);
        KAA_FREE(kaa_union->data);
        break;
    }
    default:
        break;
    }
}

static size_t kaa_get_size_record_configuration_sync_request_null_union(void *data)
{
    kaa_union_t *kaa_union = (kaa_union_t*)data;
    size_t union_len = size_long(kaa_union->type);

    switch (kaa_union->type) {
    case KAA_RECORD_CONFIGURATION_SYNC_REQUEST_NULL_UNION_CONFIGURATION_SYNC_REQUEST_BRANCH:
    {
        kaa_configuration_sync_request_t* record = (kaa_configuration_sync_request_t*)kaa_union->data;
        union_len += record->get_size(record);
        break;
    }
    default:
        break;
    }
    
    return union_len;
}

static void kaa_serialize_record_configuration_sync_request_null_union(avro_writer_t writer, void *data)
{
    kaa_union_t *kaa_union = (kaa_union_t*)data;

    int64_t branch = kaa_union->type;
    avro_binary_encoding.write_long(writer, branch);

    switch (kaa_union->type) {
    case KAA_RECORD_CONFIGURATION_SYNC_REQUEST_NULL_UNION_CONFIGURATION_SYNC_REQUEST_BRANCH:
    {
        kaa_configuration_sync_request_t* record = (kaa_configuration_sync_request_t*)kaa_union->data;
        record->serialize(writer, record);
        break;
    }
    default:
        break;
    }
}
static kaa_union_t* kaa_create_record_configuration_sync_request_null_union()
{
    kaa_union_t* kaa_union = KAA_MALLOC(kaa_union_t);
    kaa_union->data = NULL;
    kaa_union->serialize = kaa_serialize_record_configuration_sync_request_null_union;
    kaa_union->get_size = kaa_get_size_record_configuration_sync_request_null_union;
    kaa_union->destroy = kaa_destroy_record_configuration_sync_request_null_union;

    return kaa_union; 
}

kaa_union_t* kaa_create_record_configuration_sync_request_null_union_configuration_sync_request_branch()
{
    kaa_union_t *kaa_union = kaa_create_record_configuration_sync_request_null_union();
    kaa_union->type = KAA_RECORD_CONFIGURATION_SYNC_REQUEST_NULL_UNION_CONFIGURATION_SYNC_REQUEST_BRANCH;
    return kaa_union;
}

kaa_union_t* kaa_create_record_configuration_sync_request_null_union_null_branch()
{
    kaa_union_t *kaa_union = kaa_create_record_configuration_sync_request_null_union();
    kaa_union->type = KAA_RECORD_CONFIGURATION_SYNC_REQUEST_NULL_UNION_NULL_BRANCH;
    return kaa_union;
}
# endif // KAA_RECORD_CONFIGURATION_SYNC_REQUEST_NULL_UNION_C_



# ifndef KAA_RECORD_NOTIFICATION_SYNC_REQUEST_NULL_UNION_C_
# define KAA_RECORD_NOTIFICATION_SYNC_REQUEST_NULL_UNION_C_
static void kaa_destroy_record_notification_sync_request_null_union(void *data)
{
    kaa_union_t *kaa_union = (kaa_union_t*)data;

    switch (kaa_union->type) {
    case KAA_RECORD_NOTIFICATION_SYNC_REQUEST_NULL_UNION_NOTIFICATION_SYNC_REQUEST_BRANCH:
    {
        kaa_notification_sync_request_t* record = (kaa_notification_sync_request_t*)kaa_union->data;
        record->destroy(record);
        KAA_FREE(kaa_union->data);
        break;
    }
    default:
        break;
    }
}

static size_t kaa_get_size_record_notification_sync_request_null_union(void *data)
{
    kaa_union_t *kaa_union = (kaa_union_t*)data;
    size_t union_len = size_long(kaa_union->type);

    switch (kaa_union->type) {
    case KAA_RECORD_NOTIFICATION_SYNC_REQUEST_NULL_UNION_NOTIFICATION_SYNC_REQUEST_BRANCH:
    {
        kaa_notification_sync_request_t* record = (kaa_notification_sync_request_t*)kaa_union->data;
        union_len += record->get_size(record);
        break;
    }
    default:
        break;
    }
    
    return union_len;
}

static void kaa_serialize_record_notification_sync_request_null_union(avro_writer_t writer, void *data)
{
    kaa_union_t *kaa_union = (kaa_union_t*)data;

    int64_t branch = kaa_union->type;
    avro_binary_encoding.write_long(writer, branch);

    switch (kaa_union->type) {
    case KAA_RECORD_NOTIFICATION_SYNC_REQUEST_NULL_UNION_NOTIFICATION_SYNC_REQUEST_BRANCH:
    {
        kaa_notification_sync_request_t* record = (kaa_notification_sync_request_t*)kaa_union->data;
        record->serialize(writer, record);
        break;
    }
    default:
        break;
    }
}
static kaa_union_t* kaa_create_record_notification_sync_request_null_union()
{
    kaa_union_t* kaa_union = KAA_MALLOC(kaa_union_t);
    kaa_union->data = NULL;
    kaa_union->serialize = kaa_serialize_record_notification_sync_request_null_union;
    kaa_union->get_size = kaa_get_size_record_notification_sync_request_null_union;
    kaa_union->destroy = kaa_destroy_record_notification_sync_request_null_union;

    return kaa_union; 
}

kaa_union_t* kaa_create_record_notification_sync_request_null_union_notification_sync_request_branch()
{
    kaa_union_t *kaa_union = kaa_create_record_notification_sync_request_null_union();
    kaa_union->type = KAA_RECORD_NOTIFICATION_SYNC_REQUEST_NULL_UNION_NOTIFICATION_SYNC_REQUEST_BRANCH;
    return kaa_union;
}

kaa_union_t* kaa_create_record_notification_sync_request_null_union_null_branch()
{
    kaa_union_t *kaa_union = kaa_create_record_notification_sync_request_null_union();
    kaa_union->type = KAA_RECORD_NOTIFICATION_SYNC_REQUEST_NULL_UNION_NULL_BRANCH;
    return kaa_union;
}
# endif // KAA_RECORD_NOTIFICATION_SYNC_REQUEST_NULL_UNION_C_



# ifndef KAA_RECORD_USER_SYNC_REQUEST_NULL_UNION_C_
# define KAA_RECORD_USER_SYNC_REQUEST_NULL_UNION_C_
static void kaa_destroy_record_user_sync_request_null_union(void *data)
{
    kaa_union_t *kaa_union = (kaa_union_t*)data;

    switch (kaa_union->type) {
    case KAA_RECORD_USER_SYNC_REQUEST_NULL_UNION_USER_SYNC_REQUEST_BRANCH:
    {
        kaa_user_sync_request_t* record = (kaa_user_sync_request_t*)kaa_union->data;
        record->destroy(record);
        KAA_FREE(kaa_union->data);
        break;
    }
    default:
        break;
    }
}

static size_t kaa_get_size_record_user_sync_request_null_union(void *data)
{
    kaa_union_t *kaa_union = (kaa_union_t*)data;
    size_t union_len = size_long(kaa_union->type);

    switch (kaa_union->type) {
    case KAA_RECORD_USER_SYNC_REQUEST_NULL_UNION_USER_SYNC_REQUEST_BRANCH:
    {
        kaa_user_sync_request_t* record = (kaa_user_sync_request_t*)kaa_union->data;
        union_len += record->get_size(record);
        break;
    }
    default:
        break;
    }
    
    return union_len;
}

static void kaa_serialize_record_user_sync_request_null_union(avro_writer_t writer, void *data)
{
    kaa_union_t *kaa_union = (kaa_union_t*)data;

    int64_t branch = kaa_union->type;
    avro_binary_encoding.write_long(writer, branch);

    switch (kaa_union->type) {
    case KAA_RECORD_USER_SYNC_REQUEST_NULL_UNION_USER_SYNC_REQUEST_BRANCH:
    {
        kaa_user_sync_request_t* record = (kaa_user_sync_request_t*)kaa_union->data;
        record->serialize(writer, record);
        break;
    }
    default:
        break;
    }
}
static kaa_union_t* kaa_create_record_user_sync_request_null_union()
{
    kaa_union_t* kaa_union = KAA_MALLOC(kaa_union_t);
    kaa_union->data = NULL;
    kaa_union->serialize = kaa_serialize_record_user_sync_request_null_union;
    kaa_union->get_size = kaa_get_size_record_user_sync_request_null_union;
    kaa_union->destroy = kaa_destroy_record_user_sync_request_null_union;

    return kaa_union; 
}

kaa_union_t* kaa_create_record_user_sync_request_null_union_user_sync_request_branch()
{
    kaa_union_t *kaa_union = kaa_create_record_user_sync_request_null_union();
    kaa_union->type = KAA_RECORD_USER_SYNC_REQUEST_NULL_UNION_USER_SYNC_REQUEST_BRANCH;
    return kaa_union;
}

kaa_union_t* kaa_create_record_user_sync_request_null_union_null_branch()
{
    kaa_union_t *kaa_union = kaa_create_record_user_sync_request_null_union();
    kaa_union->type = KAA_RECORD_USER_SYNC_REQUEST_NULL_UNION_NULL_BRANCH;
    return kaa_union;
}
# endif // KAA_RECORD_USER_SYNC_REQUEST_NULL_UNION_C_



# ifndef KAA_RECORD_EVENT_SYNC_REQUEST_NULL_UNION_C_
# define KAA_RECORD_EVENT_SYNC_REQUEST_NULL_UNION_C_
static void kaa_destroy_record_event_sync_request_null_union(void *data)
{
    kaa_union_t *kaa_union = (kaa_union_t*)data;

    switch (kaa_union->type) {
    case KAA_RECORD_EVENT_SYNC_REQUEST_NULL_UNION_EVENT_SYNC_REQUEST_BRANCH:
    {
        kaa_event_sync_request_t* record = (kaa_event_sync_request_t*)kaa_union->data;
        record->destroy(record);
        KAA_FREE(kaa_union->data);
        break;
    }
    default:
        break;
    }
}

static size_t kaa_get_size_record_event_sync_request_null_union(void *data)
{
    kaa_union_t *kaa_union = (kaa_union_t*)data;
    size_t union_len = size_long(kaa_union->type);

    switch (kaa_union->type) {
    case KAA_RECORD_EVENT_SYNC_REQUEST_NULL_UNION_EVENT_SYNC_REQUEST_BRANCH:
    {
        kaa_event_sync_request_t* record = (kaa_event_sync_request_t*)kaa_union->data;
        union_len += record->get_size(record);
        break;
    }
    default:
        break;
    }
    
    return union_len;
}

static void kaa_serialize_record_event_sync_request_null_union(avro_writer_t writer, void *data)
{
    kaa_union_t *kaa_union = (kaa_union_t*)data;

    int64_t branch = kaa_union->type;
    avro_binary_encoding.write_long(writer, branch);

    switch (kaa_union->type) {
    case KAA_RECORD_EVENT_SYNC_REQUEST_NULL_UNION_EVENT_SYNC_REQUEST_BRANCH:
    {
        kaa_event_sync_request_t* record = (kaa_event_sync_request_t*)kaa_union->data;
        record->serialize(writer, record);
        break;
    }
    default:
        break;
    }
}
static kaa_union_t* kaa_create_record_event_sync_request_null_union()
{
    kaa_union_t* kaa_union = KAA_MALLOC(kaa_union_t);
    kaa_union->data = NULL;
    kaa_union->serialize = kaa_serialize_record_event_sync_request_null_union;
    kaa_union->get_size = kaa_get_size_record_event_sync_request_null_union;
    kaa_union->destroy = kaa_destroy_record_event_sync_request_null_union;

    return kaa_union; 
}

kaa_union_t* kaa_create_record_event_sync_request_null_union_event_sync_request_branch()
{
    kaa_union_t *kaa_union = kaa_create_record_event_sync_request_null_union();
    kaa_union->type = KAA_RECORD_EVENT_SYNC_REQUEST_NULL_UNION_EVENT_SYNC_REQUEST_BRANCH;
    return kaa_union;
}

kaa_union_t* kaa_create_record_event_sync_request_null_union_null_branch()
{
    kaa_union_t *kaa_union = kaa_create_record_event_sync_request_null_union();
    kaa_union->type = KAA_RECORD_EVENT_SYNC_REQUEST_NULL_UNION_NULL_BRANCH;
    return kaa_union;
}
# endif // KAA_RECORD_EVENT_SYNC_REQUEST_NULL_UNION_C_



# ifndef KAA_RECORD_LOG_SYNC_REQUEST_NULL_UNION_C_
# define KAA_RECORD_LOG_SYNC_REQUEST_NULL_UNION_C_
static void kaa_destroy_record_log_sync_request_null_union(void *data)
{
    kaa_union_t *kaa_union = (kaa_union_t*)data;

    switch (kaa_union->type) {
    case KAA_RECORD_LOG_SYNC_REQUEST_NULL_UNION_LOG_SYNC_REQUEST_BRANCH:
    {
        kaa_log_sync_request_t* record = (kaa_log_sync_request_t*)kaa_union->data;
        record->destroy(record);
        KAA_FREE(kaa_union->data);
        break;
    }
    default:
        break;
    }
}

static size_t kaa_get_size_record_log_sync_request_null_union(void *data)
{
    kaa_union_t *kaa_union = (kaa_union_t*)data;
    size_t union_len = size_long(kaa_union->type);

    switch (kaa_union->type) {
    case KAA_RECORD_LOG_SYNC_REQUEST_NULL_UNION_LOG_SYNC_REQUEST_BRANCH:
    {
        kaa_log_sync_request_t* record = (kaa_log_sync_request_t*)kaa_union->data;
        union_len += record->get_size(record);
        break;
    }
    default:
        break;
    }
    
    return union_len;
}

static void kaa_serialize_record_log_sync_request_null_union(avro_writer_t writer, void *data)
{
    kaa_union_t *kaa_union = (kaa_union_t*)data;

    int64_t branch = kaa_union->type;
    avro_binary_encoding.write_long(writer, branch);

    switch (kaa_union->type) {
    case KAA_RECORD_LOG_SYNC_REQUEST_NULL_UNION_LOG_SYNC_REQUEST_BRANCH:
    {
        kaa_log_sync_request_t* record = (kaa_log_sync_request_t*)kaa_union->data;
        record->serialize(writer, record);
        break;
    }
    default:
        break;
    }
}
static kaa_union_t* kaa_create_record_log_sync_request_null_union()
{
    kaa_union_t* kaa_union = KAA_MALLOC(kaa_union_t);
    kaa_union->data = NULL;
    kaa_union->serialize = kaa_serialize_record_log_sync_request_null_union;
    kaa_union->get_size = kaa_get_size_record_log_sync_request_null_union;
    kaa_union->destroy = kaa_destroy_record_log_sync_request_null_union;

    return kaa_union; 
}

kaa_union_t* kaa_create_record_log_sync_request_null_union_log_sync_request_branch()
{
    kaa_union_t *kaa_union = kaa_create_record_log_sync_request_null_union();
    kaa_union->type = KAA_RECORD_LOG_SYNC_REQUEST_NULL_UNION_LOG_SYNC_REQUEST_BRANCH;
    return kaa_union;
}

kaa_union_t* kaa_create_record_log_sync_request_null_union_null_branch()
{
    kaa_union_t *kaa_union = kaa_create_record_log_sync_request_null_union();
    kaa_union->type = KAA_RECORD_LOG_SYNC_REQUEST_NULL_UNION_NULL_BRANCH;
    return kaa_union;
}
# endif // KAA_RECORD_LOG_SYNC_REQUEST_NULL_UNION_C_



static void kaa_destroy_sync_request(void* data)
{
    kaa_sync_request_t* record = (kaa_sync_request_t*)data;

    record->request_id->destroy(record->request_id);
    KAA_FREE(record->request_id);
    record->sync_request_meta_data->destroy(record->sync_request_meta_data);
    KAA_FREE(record->sync_request_meta_data);
    record->profile_sync_request->destroy(record->profile_sync_request);
    KAA_FREE(record->profile_sync_request);
    record->configuration_sync_request->destroy(record->configuration_sync_request);
    KAA_FREE(record->configuration_sync_request);
    record->notification_sync_request->destroy(record->notification_sync_request);
    KAA_FREE(record->notification_sync_request);
    record->user_sync_request->destroy(record->user_sync_request);
    KAA_FREE(record->user_sync_request);
    record->event_sync_request->destroy(record->event_sync_request);
    KAA_FREE(record->event_sync_request);
    record->log_sync_request->destroy(record->log_sync_request);
    KAA_FREE(record->log_sync_request);
}
static size_t kaa_get_size_sync_request(void* data)
{
    size_t record_size = 0;
    kaa_sync_request_t* record = (kaa_sync_request_t*)data;

    record_size += record->request_id->get_size(record->request_id);
    record_size += record->sync_request_meta_data->get_size(record->sync_request_meta_data);
    record_size += record->profile_sync_request->get_size(record->profile_sync_request);
    record_size += record->configuration_sync_request->get_size(record->configuration_sync_request);
    record_size += record->notification_sync_request->get_size(record->notification_sync_request);
    record_size += record->user_sync_request->get_size(record->user_sync_request);
    record_size += record->event_sync_request->get_size(record->event_sync_request);
    record_size += record->log_sync_request->get_size(record->log_sync_request);

    return record_size;
}

static void kaa_serialize_sync_request(avro_writer_t writer, void* data)
{
    kaa_sync_request_t* record = (kaa_sync_request_t*)data;

    record->request_id->serialize(writer, record->request_id);
    record->sync_request_meta_data->serialize(writer, record->sync_request_meta_data);
    record->profile_sync_request->serialize(writer, record->profile_sync_request);
    record->configuration_sync_request->serialize(writer, record->configuration_sync_request);
    record->notification_sync_request->serialize(writer, record->notification_sync_request);
    record->user_sync_request->serialize(writer, record->user_sync_request);
    record->event_sync_request->serialize(writer, record->event_sync_request);
    record->log_sync_request->serialize(writer, record->log_sync_request);
}

kaa_sync_request_t* kaa_create_sync_request()
{
    kaa_sync_request_t* record = KAA_MALLOC(kaa_sync_request_t);
    record->serialize = kaa_serialize_sync_request;
    record->get_size = kaa_get_size_sync_request;
    record->destroy = kaa_destroy_sync_request;
    return record;
}



# ifndef KAA_RECORD_PROFILE_SYNC_RESPONSE_NULL_UNION_C_
# define KAA_RECORD_PROFILE_SYNC_RESPONSE_NULL_UNION_C_
static void kaa_destroy_record_profile_sync_response_null_union(void *data)
{
    kaa_union_t *kaa_union = (kaa_union_t*)data;

    switch (kaa_union->type) {
    case KAA_RECORD_PROFILE_SYNC_RESPONSE_NULL_UNION_PROFILE_SYNC_RESPONSE_BRANCH:
    {
        KAA_FREE(kaa_union->data);
        break;
    }
    default:
        break;
    }
}
static kaa_union_t* kaa_create_record_profile_sync_response_null_union()
{
    kaa_union_t* kaa_union = KAA_MALLOC(kaa_union_t);
    kaa_union->data = NULL;
    kaa_union->serialize = NULL;
    kaa_union->get_size = NULL;
    kaa_union->destroy = kaa_destroy_record_profile_sync_response_null_union;

    return kaa_union; 
}

kaa_union_t* kaa_deserialize_record_profile_sync_response_null_union(avro_reader_t reader)
{
    kaa_union_t *kaa_union = kaa_create_record_profile_sync_response_null_union();

    int64_t branch;
    avro_binary_encoding.read_long(reader, &branch);
    kaa_union->type = branch;

    switch (kaa_union->type) {
    case KAA_RECORD_PROFILE_SYNC_RESPONSE_NULL_UNION_PROFILE_SYNC_RESPONSE_BRANCH:
    {
        kaa_union->data = kaa_deserialize_profile_sync_response(reader);
        break;
    }
    default:
        break;
    }

    return kaa_union;
}
# endif // KAA_RECORD_PROFILE_SYNC_RESPONSE_NULL_UNION_C_



# ifndef KAA_RECORD_CONFIGURATION_SYNC_RESPONSE_NULL_UNION_C_
# define KAA_RECORD_CONFIGURATION_SYNC_RESPONSE_NULL_UNION_C_
static void kaa_destroy_record_configuration_sync_response_null_union(void *data)
{
    kaa_union_t *kaa_union = (kaa_union_t*)data;

    switch (kaa_union->type) {
    case KAA_RECORD_CONFIGURATION_SYNC_RESPONSE_NULL_UNION_CONFIGURATION_SYNC_RESPONSE_BRANCH:
    {
        kaa_configuration_sync_response_t* record = (kaa_configuration_sync_response_t*)kaa_union->data;
        record->destroy(record);
        KAA_FREE(kaa_union->data);
        break;
    }
    default:
        break;
    }
}
static kaa_union_t* kaa_create_record_configuration_sync_response_null_union()
{
    kaa_union_t* kaa_union = KAA_MALLOC(kaa_union_t);
    kaa_union->data = NULL;
    kaa_union->serialize = NULL;
    kaa_union->get_size = NULL;
    kaa_union->destroy = kaa_destroy_record_configuration_sync_response_null_union;

    return kaa_union; 
}

kaa_union_t* kaa_deserialize_record_configuration_sync_response_null_union(avro_reader_t reader)
{
    kaa_union_t *kaa_union = kaa_create_record_configuration_sync_response_null_union();

    int64_t branch;
    avro_binary_encoding.read_long(reader, &branch);
    kaa_union->type = branch;

    switch (kaa_union->type) {
    case KAA_RECORD_CONFIGURATION_SYNC_RESPONSE_NULL_UNION_CONFIGURATION_SYNC_RESPONSE_BRANCH:
    {
        kaa_union->data = kaa_deserialize_configuration_sync_response(reader);
        break;
    }
    default:
        break;
    }

    return kaa_union;
}
# endif // KAA_RECORD_CONFIGURATION_SYNC_RESPONSE_NULL_UNION_C_



# ifndef KAA_RECORD_NOTIFICATION_SYNC_RESPONSE_NULL_UNION_C_
# define KAA_RECORD_NOTIFICATION_SYNC_RESPONSE_NULL_UNION_C_
static void kaa_destroy_record_notification_sync_response_null_union(void *data)
{
    kaa_union_t *kaa_union = (kaa_union_t*)data;

    switch (kaa_union->type) {
    case KAA_RECORD_NOTIFICATION_SYNC_RESPONSE_NULL_UNION_NOTIFICATION_SYNC_RESPONSE_BRANCH:
    {
        kaa_notification_sync_response_t* record = (kaa_notification_sync_response_t*)kaa_union->data;
        record->destroy(record);
        KAA_FREE(kaa_union->data);
        break;
    }
    default:
        break;
    }
}
static kaa_union_t* kaa_create_record_notification_sync_response_null_union()
{
    kaa_union_t* kaa_union = KAA_MALLOC(kaa_union_t);
    kaa_union->data = NULL;
    kaa_union->serialize = NULL;
    kaa_union->get_size = NULL;
    kaa_union->destroy = kaa_destroy_record_notification_sync_response_null_union;

    return kaa_union; 
}

kaa_union_t* kaa_deserialize_record_notification_sync_response_null_union(avro_reader_t reader)
{
    kaa_union_t *kaa_union = kaa_create_record_notification_sync_response_null_union();

    int64_t branch;
    avro_binary_encoding.read_long(reader, &branch);
    kaa_union->type = branch;

    switch (kaa_union->type) {
    case KAA_RECORD_NOTIFICATION_SYNC_RESPONSE_NULL_UNION_NOTIFICATION_SYNC_RESPONSE_BRANCH:
    {
        kaa_union->data = kaa_deserialize_notification_sync_response(reader);
        break;
    }
    default:
        break;
    }

    return kaa_union;
}
# endif // KAA_RECORD_NOTIFICATION_SYNC_RESPONSE_NULL_UNION_C_



# ifndef KAA_RECORD_USER_SYNC_RESPONSE_NULL_UNION_C_
# define KAA_RECORD_USER_SYNC_RESPONSE_NULL_UNION_C_
static void kaa_destroy_record_user_sync_response_null_union(void *data)
{
    kaa_union_t *kaa_union = (kaa_union_t*)data;

    switch (kaa_union->type) {
    case KAA_RECORD_USER_SYNC_RESPONSE_NULL_UNION_USER_SYNC_RESPONSE_BRANCH:
    {
        kaa_user_sync_response_t* record = (kaa_user_sync_response_t*)kaa_union->data;
        record->destroy(record);
        KAA_FREE(kaa_union->data);
        break;
    }
    default:
        break;
    }
}
static kaa_union_t* kaa_create_record_user_sync_response_null_union()
{
    kaa_union_t* kaa_union = KAA_MALLOC(kaa_union_t);
    kaa_union->data = NULL;
    kaa_union->serialize = NULL;
    kaa_union->get_size = NULL;
    kaa_union->destroy = kaa_destroy_record_user_sync_response_null_union;

    return kaa_union; 
}

kaa_union_t* kaa_deserialize_record_user_sync_response_null_union(avro_reader_t reader)
{
    kaa_union_t *kaa_union = kaa_create_record_user_sync_response_null_union();

    int64_t branch;
    avro_binary_encoding.read_long(reader, &branch);
    kaa_union->type = branch;

    switch (kaa_union->type) {
    case KAA_RECORD_USER_SYNC_RESPONSE_NULL_UNION_USER_SYNC_RESPONSE_BRANCH:
    {
        kaa_union->data = kaa_deserialize_user_sync_response(reader);
        break;
    }
    default:
        break;
    }

    return kaa_union;
}
# endif // KAA_RECORD_USER_SYNC_RESPONSE_NULL_UNION_C_



# ifndef KAA_RECORD_EVENT_SYNC_RESPONSE_NULL_UNION_C_
# define KAA_RECORD_EVENT_SYNC_RESPONSE_NULL_UNION_C_
static void kaa_destroy_record_event_sync_response_null_union(void *data)
{
    kaa_union_t *kaa_union = (kaa_union_t*)data;

    switch (kaa_union->type) {
    case KAA_RECORD_EVENT_SYNC_RESPONSE_NULL_UNION_EVENT_SYNC_RESPONSE_BRANCH:
    {
        kaa_event_sync_response_t* record = (kaa_event_sync_response_t*)kaa_union->data;
        record->destroy(record);
        KAA_FREE(kaa_union->data);
        break;
    }
    default:
        break;
    }
}
static kaa_union_t* kaa_create_record_event_sync_response_null_union()
{
    kaa_union_t* kaa_union = KAA_MALLOC(kaa_union_t);
    kaa_union->data = NULL;
    kaa_union->serialize = NULL;
    kaa_union->get_size = NULL;
    kaa_union->destroy = kaa_destroy_record_event_sync_response_null_union;

    return kaa_union; 
}

kaa_union_t* kaa_deserialize_record_event_sync_response_null_union(avro_reader_t reader)
{
    kaa_union_t *kaa_union = kaa_create_record_event_sync_response_null_union();

    int64_t branch;
    avro_binary_encoding.read_long(reader, &branch);
    kaa_union->type = branch;

    switch (kaa_union->type) {
    case KAA_RECORD_EVENT_SYNC_RESPONSE_NULL_UNION_EVENT_SYNC_RESPONSE_BRANCH:
    {
        kaa_union->data = kaa_deserialize_event_sync_response(reader);
        break;
    }
    default:
        break;
    }

    return kaa_union;
}
# endif // KAA_RECORD_EVENT_SYNC_RESPONSE_NULL_UNION_C_



# ifndef KAA_RECORD_REDIRECT_SYNC_RESPONSE_NULL_UNION_C_
# define KAA_RECORD_REDIRECT_SYNC_RESPONSE_NULL_UNION_C_
static void kaa_destroy_record_redirect_sync_response_null_union(void *data)
{
    kaa_union_t *kaa_union = (kaa_union_t*)data;

    switch (kaa_union->type) {
    case KAA_RECORD_REDIRECT_SYNC_RESPONSE_NULL_UNION_REDIRECT_SYNC_RESPONSE_BRANCH:
    {
        kaa_redirect_sync_response_t* record = (kaa_redirect_sync_response_t*)kaa_union->data;
        record->destroy(record);
        KAA_FREE(kaa_union->data);
        break;
    }
    default:
        break;
    }
}
static kaa_union_t* kaa_create_record_redirect_sync_response_null_union()
{
    kaa_union_t* kaa_union = KAA_MALLOC(kaa_union_t);
    kaa_union->data = NULL;
    kaa_union->serialize = NULL;
    kaa_union->get_size = NULL;
    kaa_union->destroy = kaa_destroy_record_redirect_sync_response_null_union;

    return kaa_union; 
}

kaa_union_t* kaa_deserialize_record_redirect_sync_response_null_union(avro_reader_t reader)
{
    kaa_union_t *kaa_union = kaa_create_record_redirect_sync_response_null_union();

    int64_t branch;
    avro_binary_encoding.read_long(reader, &branch);
    kaa_union->type = branch;

    switch (kaa_union->type) {
    case KAA_RECORD_REDIRECT_SYNC_RESPONSE_NULL_UNION_REDIRECT_SYNC_RESPONSE_BRANCH:
    {
        kaa_union->data = kaa_deserialize_redirect_sync_response(reader);
        break;
    }
    default:
        break;
    }

    return kaa_union;
}
# endif // KAA_RECORD_REDIRECT_SYNC_RESPONSE_NULL_UNION_C_



# ifndef KAA_RECORD_LOG_SYNC_RESPONSE_NULL_UNION_C_
# define KAA_RECORD_LOG_SYNC_RESPONSE_NULL_UNION_C_
static void kaa_destroy_record_log_sync_response_null_union(void *data)
{
    kaa_union_t *kaa_union = (kaa_union_t*)data;

    switch (kaa_union->type) {
    case KAA_RECORD_LOG_SYNC_RESPONSE_NULL_UNION_LOG_SYNC_RESPONSE_BRANCH:
    {
        kaa_log_sync_response_t* record = (kaa_log_sync_response_t*)kaa_union->data;
        record->destroy(record);
        KAA_FREE(kaa_union->data);
        break;
    }
    default:
        break;
    }
}
static kaa_union_t* kaa_create_record_log_sync_response_null_union()
{
    kaa_union_t* kaa_union = KAA_MALLOC(kaa_union_t);
    kaa_union->data = NULL;
    kaa_union->serialize = NULL;
    kaa_union->get_size = NULL;
    kaa_union->destroy = kaa_destroy_record_log_sync_response_null_union;

    return kaa_union; 
}

kaa_union_t* kaa_deserialize_record_log_sync_response_null_union(avro_reader_t reader)
{
    kaa_union_t *kaa_union = kaa_create_record_log_sync_response_null_union();

    int64_t branch;
    avro_binary_encoding.read_long(reader, &branch);
    kaa_union->type = branch;

    switch (kaa_union->type) {
    case KAA_RECORD_LOG_SYNC_RESPONSE_NULL_UNION_LOG_SYNC_RESPONSE_BRANCH:
    {
        kaa_union->data = kaa_deserialize_log_sync_response(reader);
        break;
    }
    default:
        break;
    }

    return kaa_union;
}
# endif // KAA_RECORD_LOG_SYNC_RESPONSE_NULL_UNION_C_



static void kaa_destroy_sync_response(void* data)
{
    kaa_sync_response_t* record = (kaa_sync_response_t*)data;

    record->request_id->destroy(record->request_id);
    KAA_FREE(record->request_id);
    record->profile_sync_response->destroy(record->profile_sync_response);
    KAA_FREE(record->profile_sync_response);
    record->configuration_sync_response->destroy(record->configuration_sync_response);
    KAA_FREE(record->configuration_sync_response);
    record->notification_sync_response->destroy(record->notification_sync_response);
    KAA_FREE(record->notification_sync_response);
    record->user_sync_response->destroy(record->user_sync_response);
    KAA_FREE(record->user_sync_response);
    record->event_sync_response->destroy(record->event_sync_response);
    KAA_FREE(record->event_sync_response);
    record->redirect_sync_response->destroy(record->redirect_sync_response);
    KAA_FREE(record->redirect_sync_response);
    record->log_sync_response->destroy(record->log_sync_response);
    KAA_FREE(record->log_sync_response);
}

kaa_sync_response_t* kaa_deserialize_sync_response(avro_reader_t reader)
{
    kaa_sync_response_t* record = KAA_MALLOC(kaa_sync_response_t);
    record->destroy = kaa_destroy_sync_response;
    
    record->request_id = kaa_deserialize_int_null_union(reader);
    int64_t status_value;
    avro_binary_encoding.read_long(reader, &status_value);
    record->status = status_value;
    record->profile_sync_response = kaa_deserialize_record_profile_sync_response_null_union(reader);
    record->configuration_sync_response = kaa_deserialize_record_configuration_sync_response_null_union(reader);
    record->notification_sync_response = kaa_deserialize_record_notification_sync_response_null_union(reader);
    record->user_sync_response = kaa_deserialize_record_user_sync_response_null_union(reader);
    record->event_sync_response = kaa_deserialize_record_event_sync_response_null_union(reader);
    record->redirect_sync_response = kaa_deserialize_record_redirect_sync_response_null_union(reader);
    record->log_sync_response = kaa_deserialize_record_log_sync_response_null_union(reader);
    
    return record;
}



static void kaa_destroy_topic_subscription_info(void* data)
{
    kaa_topic_subscription_info_t* record = (kaa_topic_subscription_info_t*)data;

    record->topic_info->destroy(record->topic_info);
    KAA_FREE(record->topic_info);
}

kaa_topic_subscription_info_t* kaa_deserialize_topic_subscription_info(avro_reader_t reader)
{
    kaa_topic_subscription_info_t* record = KAA_MALLOC(kaa_topic_subscription_info_t);
    record->destroy = kaa_destroy_topic_subscription_info;
    
    record->topic_info = kaa_deserialize_topic(reader);
    avro_binary_encoding.read_int(reader, &record->seq_number);
    
    return record;
}

