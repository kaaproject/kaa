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


static void kaa_event_class_family_version_info_destroy(void* data)
{
    if (data) {
        kaa_event_class_family_version_info_t* record = (kaa_event_class_family_version_info_t*)data;

        kaa_string_destroy(record->name);
        kaa_data_destroy(record);
    }
}

static void kaa_event_class_family_version_info_serialize(avro_writer_t writer, void* data)
{
    if (data) {
        kaa_event_class_family_version_info_t* record = (kaa_event_class_family_version_info_t*)data;

        kaa_string_serialize(writer, record->name);
            avro_binary_encoding.write_int(writer, record->version);
    }
}

static size_t kaa_event_class_family_version_info_get_size(void* data)
{
    if (data) {
        size_t record_size = 0;
        kaa_event_class_family_version_info_t* record = (kaa_event_class_family_version_info_t*)data;

        record_size += kaa_string_get_size(record->name);
        record_size += kaa_long_get_size((int64_t)record->version);

        return record_size;
    }

    return 0;
}

kaa_event_class_family_version_info_t* kaa_event_class_family_version_info_create()
{
    kaa_event_class_family_version_info_t* record = 
            (kaa_event_class_family_version_info_t*)KAA_CALLOC(1, sizeof(kaa_event_class_family_version_info_t));

    if (record) {
        record->serialize = kaa_event_class_family_version_info_serialize;
        record->get_size = kaa_event_class_family_version_info_get_size;
        record->destroy = kaa_event_class_family_version_info_destroy;
    }

    return record;
}


# ifndef KAA_UNION_ARRAY_EVENT_CLASS_FAMILY_VERSION_INFO_OR_NULL_C_
# define KAA_UNION_ARRAY_EVENT_CLASS_FAMILY_VERSION_INFO_OR_NULL_C_
static void kaa_union_array_event_class_family_version_info_or_null_destroy(void *data)
{
    if (data) {
        kaa_union_t *kaa_union = (kaa_union_t*)data;

        switch (kaa_union->type) {
        case KAA_UNION_ARRAY_EVENT_CLASS_FAMILY_VERSION_INFO_OR_NULL_BRANCH_0:
        {
            if (kaa_union->data) {
                            kaa_list_destroy(kaa_union->data, kaa_event_class_family_version_info_destroy);
                        }
            break;
        }
        default:
            break;
        }

        kaa_data_destroy(kaa_union);
    }
}

static size_t kaa_union_array_event_class_family_version_info_or_null_get_size(void *data)
{
    if (data) {
        kaa_union_t *kaa_union = (kaa_union_t*)data;
        size_t union_size = kaa_long_get_size(kaa_union->type);

        switch (kaa_union->type) {
        case KAA_UNION_ARRAY_EVENT_CLASS_FAMILY_VERSION_INFO_OR_NULL_BRANCH_0:
        {
            if (kaa_union->data) {
                            union_size += kaa_array_get_size(kaa_union->data, kaa_event_class_family_version_info_get_size);
                        }
            break;
        }
        default:
            break;
        }

        return union_size;
    }

    return 0;
}

static void kaa_union_array_event_class_family_version_info_or_null_serialize(avro_writer_t writer, void *data)
{
    if (data) {
        kaa_union_t *kaa_union = (kaa_union_t*)data;
        avro_binary_encoding.write_long(writer, kaa_union->type);

        switch (kaa_union->type) {
        case KAA_UNION_ARRAY_EVENT_CLASS_FAMILY_VERSION_INFO_OR_NULL_BRANCH_0:
        {
            if (kaa_union->data) {
                            kaa_array_serialize(writer, kaa_union->data, kaa_event_class_family_version_info_serialize);
                        }
            break;
        }
        default:
            break;
        }
    }
}
static kaa_union_t* kaa_union_array_event_class_family_version_info_or_null_create()
{
    kaa_union_t* kaa_union = KAA_CALLOC(1, sizeof(kaa_union_t));

    if (kaa_union) {
        kaa_union->serialize = kaa_union_array_event_class_family_version_info_or_null_serialize;
        kaa_union->get_size = kaa_union_array_event_class_family_version_info_or_null_get_size;
        kaa_union->destroy = kaa_union_array_event_class_family_version_info_or_null_destroy;
    }

    return kaa_union; 
}

kaa_union_t* kaa_union_array_event_class_family_version_info_or_null_branch_0_create()
{
    kaa_union_t *kaa_union = kaa_union_array_event_class_family_version_info_or_null_create();
    if (kaa_union) {
        kaa_union->type = KAA_UNION_ARRAY_EVENT_CLASS_FAMILY_VERSION_INFO_OR_NULL_BRANCH_0;
    }
    return kaa_union;
}

kaa_union_t* kaa_union_array_event_class_family_version_info_or_null_branch_1_create()
{
    kaa_union_t *kaa_union = kaa_union_array_event_class_family_version_info_or_null_create();
    if (kaa_union) {
        kaa_union->type = KAA_UNION_ARRAY_EVENT_CLASS_FAMILY_VERSION_INFO_OR_NULL_BRANCH_1;
    }
    return kaa_union;
}
# endif // KAA_UNION_ARRAY_EVENT_CLASS_FAMILY_VERSION_INFO_OR_NULL_C_


static void kaa_endpoint_version_info_destroy(void* data)
{
    if (data) {
        kaa_endpoint_version_info_t* record = (kaa_endpoint_version_info_t*)data;

        if (record->event_family_versions && record->event_family_versions->destroy) {
            record->event_family_versions->destroy(record->event_family_versions);
        }
        kaa_data_destroy(record);
    }
}

static void kaa_endpoint_version_info_serialize(avro_writer_t writer, void* data)
{
    if (data) {
        kaa_endpoint_version_info_t* record = (kaa_endpoint_version_info_t*)data;

            avro_binary_encoding.write_int(writer, record->config_version);
            avro_binary_encoding.write_int(writer, record->profile_version);
            avro_binary_encoding.write_int(writer, record->system_nf_version);
            avro_binary_encoding.write_int(writer, record->user_nf_version);
        record->event_family_versions->serialize(writer, record->event_family_versions);
            avro_binary_encoding.write_int(writer, record->log_schema_version);
    }
}

static size_t kaa_endpoint_version_info_get_size(void* data)
{
    if (data) {
        size_t record_size = 0;
        kaa_endpoint_version_info_t* record = (kaa_endpoint_version_info_t*)data;

        record_size += kaa_long_get_size((int64_t)record->config_version);
        record_size += kaa_long_get_size((int64_t)record->profile_version);
        record_size += kaa_long_get_size((int64_t)record->system_nf_version);
        record_size += kaa_long_get_size((int64_t)record->user_nf_version);
        record_size += record->event_family_versions->get_size(record->event_family_versions);
        record_size += kaa_long_get_size((int64_t)record->log_schema_version);

        return record_size;
    }

    return 0;
}

kaa_endpoint_version_info_t* kaa_endpoint_version_info_create()
{
    kaa_endpoint_version_info_t* record = 
            (kaa_endpoint_version_info_t*)KAA_CALLOC(1, sizeof(kaa_endpoint_version_info_t));

    if (record) {
        record->serialize = kaa_endpoint_version_info_serialize;
        record->get_size = kaa_endpoint_version_info_get_size;
        record->destroy = kaa_endpoint_version_info_destroy;
    }

    return record;
}


static void kaa_topic_state_destroy(void* data)
{
    if (data) {
        kaa_topic_state_t* record = (kaa_topic_state_t*)data;

        kaa_string_destroy(record->topic_id);
        kaa_data_destroy(record);
    }
}

static void kaa_topic_state_serialize(avro_writer_t writer, void* data)
{
    if (data) {
        kaa_topic_state_t* record = (kaa_topic_state_t*)data;

        kaa_string_serialize(writer, record->topic_id);
            avro_binary_encoding.write_int(writer, record->seq_number);
    }
}

static size_t kaa_topic_state_get_size(void* data)
{
    if (data) {
        size_t record_size = 0;
        kaa_topic_state_t* record = (kaa_topic_state_t*)data;

        record_size += kaa_string_get_size(record->topic_id);
        record_size += kaa_long_get_size((int64_t)record->seq_number);

        return record_size;
    }

    return 0;
}

kaa_topic_state_t* kaa_topic_state_create()
{
    kaa_topic_state_t* record = 
            (kaa_topic_state_t*)KAA_CALLOC(1, sizeof(kaa_topic_state_t));

    if (record) {
        record->serialize = kaa_topic_state_serialize;
        record->get_size = kaa_topic_state_get_size;
        record->destroy = kaa_topic_state_destroy;
    }

    return record;
}


static void kaa_subscription_command_destroy(void* data)
{
    if (data) {
        kaa_subscription_command_t* record = (kaa_subscription_command_t*)data;

        kaa_string_destroy(record->topic_id);
        kaa_data_destroy(record);
    }
}

static void kaa_subscription_command_serialize(avro_writer_t writer, void* data)
{
    if (data) {
        kaa_subscription_command_t* record = (kaa_subscription_command_t*)data;

        kaa_string_serialize(writer, record->topic_id);
                avro_binary_encoding.write_long(writer, record->command);
    }
}

static size_t kaa_subscription_command_get_size(void* data)
{
    if (data) {
        size_t record_size = 0;
        kaa_subscription_command_t* record = (kaa_subscription_command_t*)data;

        record_size += kaa_string_get_size(record->topic_id);
        record_size += kaa_long_get_size((int64_t)record->command);

        return record_size;
    }

    return 0;
}

kaa_subscription_command_t* kaa_subscription_command_create()
{
    kaa_subscription_command_t* record = 
            (kaa_subscription_command_t*)KAA_CALLOC(1, sizeof(kaa_subscription_command_t));

    if (record) {
        record->serialize = kaa_subscription_command_serialize;
        record->get_size = kaa_subscription_command_get_size;
        record->destroy = kaa_subscription_command_destroy;
    }

    return record;
}


static void kaa_user_attach_request_destroy(void* data)
{
    if (data) {
        kaa_user_attach_request_t* record = (kaa_user_attach_request_t*)data;

        kaa_string_destroy(record->user_external_id);
        kaa_string_destroy(record->user_access_token);
        kaa_data_destroy(record);
    }
}

static void kaa_user_attach_request_serialize(avro_writer_t writer, void* data)
{
    if (data) {
        kaa_user_attach_request_t* record = (kaa_user_attach_request_t*)data;

        kaa_string_serialize(writer, record->user_external_id);
        kaa_string_serialize(writer, record->user_access_token);
    }
}

static size_t kaa_user_attach_request_get_size(void* data)
{
    if (data) {
        size_t record_size = 0;
        kaa_user_attach_request_t* record = (kaa_user_attach_request_t*)data;

        record_size += kaa_string_get_size(record->user_external_id);
        record_size += kaa_string_get_size(record->user_access_token);

        return record_size;
    }

    return 0;
}

kaa_user_attach_request_t* kaa_user_attach_request_create()
{
    kaa_user_attach_request_t* record = 
            (kaa_user_attach_request_t*)KAA_CALLOC(1, sizeof(kaa_user_attach_request_t));

    if (record) {
        record->serialize = kaa_user_attach_request_serialize;
        record->get_size = kaa_user_attach_request_get_size;
        record->destroy = kaa_user_attach_request_destroy;
    }

    return record;
}




kaa_user_attach_response_t* kaa_user_attach_response_deserialize(avro_reader_t reader)
{
    kaa_user_attach_response_t* record = 
            (kaa_user_attach_response_t*)KAA_MALLOC(sizeof(kaa_user_attach_response_t));

    if (record) {
        record->destroy = kaa_data_destroy;

        int64_t result_value;
        avro_binary_encoding.read_long(reader, &result_value);
        record->result = result_value;
    }

    return record;
}


static void kaa_user_attach_notification_destroy(void* data)
{
    if (data) {
        kaa_user_attach_notification_t* record = (kaa_user_attach_notification_t*)data;

        kaa_string_destroy(record->user_external_id);
        kaa_string_destroy(record->endpoint_access_token);
        kaa_data_destroy(record);
    }
}


kaa_user_attach_notification_t* kaa_user_attach_notification_deserialize(avro_reader_t reader)
{
    kaa_user_attach_notification_t* record = 
            (kaa_user_attach_notification_t*)KAA_MALLOC(sizeof(kaa_user_attach_notification_t));

    if (record) {
        record->destroy = kaa_user_attach_notification_destroy;

        record->user_external_id = kaa_string_deserialize(reader);
        record->endpoint_access_token = kaa_string_deserialize(reader);
    }

    return record;
}


static void kaa_user_detach_notification_destroy(void* data)
{
    if (data) {
        kaa_user_detach_notification_t* record = (kaa_user_detach_notification_t*)data;

        kaa_string_destroy(record->endpoint_access_token);
        kaa_data_destroy(record);
    }
}


kaa_user_detach_notification_t* kaa_user_detach_notification_deserialize(avro_reader_t reader)
{
    kaa_user_detach_notification_t* record = 
            (kaa_user_detach_notification_t*)KAA_MALLOC(sizeof(kaa_user_detach_notification_t));

    if (record) {
        record->destroy = kaa_user_detach_notification_destroy;

        record->endpoint_access_token = kaa_string_deserialize(reader);
    }

    return record;
}


static void kaa_endpoint_attach_request_destroy(void* data)
{
    if (data) {
        kaa_endpoint_attach_request_t* record = (kaa_endpoint_attach_request_t*)data;

        kaa_string_destroy(record->request_id);
        kaa_string_destroy(record->endpoint_access_token);
        kaa_data_destroy(record);
    }
}

static void kaa_endpoint_attach_request_serialize(avro_writer_t writer, void* data)
{
    if (data) {
        kaa_endpoint_attach_request_t* record = (kaa_endpoint_attach_request_t*)data;

        kaa_string_serialize(writer, record->request_id);
        kaa_string_serialize(writer, record->endpoint_access_token);
    }
}

static size_t kaa_endpoint_attach_request_get_size(void* data)
{
    if (data) {
        size_t record_size = 0;
        kaa_endpoint_attach_request_t* record = (kaa_endpoint_attach_request_t*)data;

        record_size += kaa_string_get_size(record->request_id);
        record_size += kaa_string_get_size(record->endpoint_access_token);

        return record_size;
    }

    return 0;
}

kaa_endpoint_attach_request_t* kaa_endpoint_attach_request_create()
{
    kaa_endpoint_attach_request_t* record = 
            (kaa_endpoint_attach_request_t*)KAA_CALLOC(1, sizeof(kaa_endpoint_attach_request_t));

    if (record) {
        record->serialize = kaa_endpoint_attach_request_serialize;
        record->get_size = kaa_endpoint_attach_request_get_size;
        record->destroy = kaa_endpoint_attach_request_destroy;
    }

    return record;
}


# ifndef KAA_UNION_STRING_OR_NULL_C_
# define KAA_UNION_STRING_OR_NULL_C_
static void kaa_union_string_or_null_destroy(void *data)
{
    if (data) {
        kaa_union_t *kaa_union = (kaa_union_t*)data;

        switch (kaa_union->type) {
        case KAA_UNION_STRING_OR_NULL_BRANCH_0:
        {
            if (kaa_union->data) {
                kaa_string_destroy(kaa_union->data);
            }
            break;
        }
        default:
            break;
        }

        kaa_data_destroy(kaa_union);
    }
}

static size_t kaa_union_string_or_null_get_size(void *data)
{
    if (data) {
        kaa_union_t *kaa_union = (kaa_union_t*)data;
        size_t union_size = kaa_long_get_size(kaa_union->type);

        switch (kaa_union->type) {
        case KAA_UNION_STRING_OR_NULL_BRANCH_0:
        {
            if (kaa_union->data) {
                union_size += kaa_string_get_size(kaa_union->data);
            }
            break;
        }
        default:
            break;
        }

        return union_size;
    }

    return 0;
}

static void kaa_union_string_or_null_serialize(avro_writer_t writer, void *data)
{
    if (data) {
        kaa_union_t *kaa_union = (kaa_union_t*)data;
        avro_binary_encoding.write_long(writer, kaa_union->type);

        switch (kaa_union->type) {
        case KAA_UNION_STRING_OR_NULL_BRANCH_0:
        {
            if (kaa_union->data) {
                avro_binary_encoding.write_string(writer, (char *)kaa_union->data);
            }
            break;
        }
        default:
            break;
        }
    }
}
static kaa_union_t* kaa_union_string_or_null_create()
{
    kaa_union_t* kaa_union = KAA_CALLOC(1, sizeof(kaa_union_t));

    if (kaa_union) {
        kaa_union->serialize = kaa_union_string_or_null_serialize;
        kaa_union->get_size = kaa_union_string_or_null_get_size;
        kaa_union->destroy = kaa_union_string_or_null_destroy;
    }

    return kaa_union; 
}

kaa_union_t* kaa_union_string_or_null_branch_0_create()
{
    kaa_union_t *kaa_union = kaa_union_string_or_null_create();
    if (kaa_union) {
        kaa_union->type = KAA_UNION_STRING_OR_NULL_BRANCH_0;
    }
    return kaa_union;
}

kaa_union_t* kaa_union_string_or_null_branch_1_create()
{
    kaa_union_t *kaa_union = kaa_union_string_or_null_create();
    if (kaa_union) {
        kaa_union->type = KAA_UNION_STRING_OR_NULL_BRANCH_1;
    }
    return kaa_union;
}

kaa_union_t* kaa_union_string_or_null_deserialize(avro_reader_t reader)
{
    kaa_union_t *kaa_union = kaa_union_string_or_null_create();

    if (kaa_union) {
        int64_t branch;
        avro_binary_encoding.read_long(reader, &branch);
        kaa_union->type = branch;

        switch (kaa_union->type) {
        case KAA_UNION_STRING_OR_NULL_BRANCH_0:
        {
            kaa_union->data = kaa_string_deserialize(reader);
            break;
        }
        default:
            break;
        }
    }

    return kaa_union;
}
# endif // KAA_UNION_STRING_OR_NULL_C_


static void kaa_endpoint_attach_response_destroy(void* data)
{
    if (data) {
        kaa_endpoint_attach_response_t* record = (kaa_endpoint_attach_response_t*)data;

        kaa_string_destroy(record->request_id);
        if (record->endpoint_key_hash && record->endpoint_key_hash->destroy) {
            record->endpoint_key_hash->destroy(record->endpoint_key_hash);
        }
        kaa_data_destroy(record);
    }
}


kaa_endpoint_attach_response_t* kaa_endpoint_attach_response_deserialize(avro_reader_t reader)
{
    kaa_endpoint_attach_response_t* record = 
            (kaa_endpoint_attach_response_t*)KAA_MALLOC(sizeof(kaa_endpoint_attach_response_t));

    if (record) {
        record->destroy = kaa_endpoint_attach_response_destroy;

        record->request_id = kaa_string_deserialize(reader);
        record->endpoint_key_hash = kaa_union_string_or_null_deserialize(reader);
        int64_t result_value;
        avro_binary_encoding.read_long(reader, &result_value);
        record->result = result_value;
    }

    return record;
}


static void kaa_endpoint_detach_request_destroy(void* data)
{
    if (data) {
        kaa_endpoint_detach_request_t* record = (kaa_endpoint_detach_request_t*)data;

        kaa_string_destroy(record->request_id);
        kaa_string_destroy(record->endpoint_key_hash);
        kaa_data_destroy(record);
    }
}

static void kaa_endpoint_detach_request_serialize(avro_writer_t writer, void* data)
{
    if (data) {
        kaa_endpoint_detach_request_t* record = (kaa_endpoint_detach_request_t*)data;

        kaa_string_serialize(writer, record->request_id);
        kaa_string_serialize(writer, record->endpoint_key_hash);
    }
}

static size_t kaa_endpoint_detach_request_get_size(void* data)
{
    if (data) {
        size_t record_size = 0;
        kaa_endpoint_detach_request_t* record = (kaa_endpoint_detach_request_t*)data;

        record_size += kaa_string_get_size(record->request_id);
        record_size += kaa_string_get_size(record->endpoint_key_hash);

        return record_size;
    }

    return 0;
}

kaa_endpoint_detach_request_t* kaa_endpoint_detach_request_create()
{
    kaa_endpoint_detach_request_t* record = 
            (kaa_endpoint_detach_request_t*)KAA_CALLOC(1, sizeof(kaa_endpoint_detach_request_t));

    if (record) {
        record->serialize = kaa_endpoint_detach_request_serialize;
        record->get_size = kaa_endpoint_detach_request_get_size;
        record->destroy = kaa_endpoint_detach_request_destroy;
    }

    return record;
}


static void kaa_endpoint_detach_response_destroy(void* data)
{
    if (data) {
        kaa_endpoint_detach_response_t* record = (kaa_endpoint_detach_response_t*)data;

        kaa_string_destroy(record->request_id);
        kaa_data_destroy(record);
    }
}


kaa_endpoint_detach_response_t* kaa_endpoint_detach_response_deserialize(avro_reader_t reader)
{
    kaa_endpoint_detach_response_t* record = 
            (kaa_endpoint_detach_response_t*)KAA_MALLOC(sizeof(kaa_endpoint_detach_response_t));

    if (record) {
        record->destroy = kaa_endpoint_detach_response_destroy;

        record->request_id = kaa_string_deserialize(reader);
        int64_t result_value;
        avro_binary_encoding.read_long(reader, &result_value);
        record->result = result_value;
    }

    return record;
}


static void kaa_event_destroy(void* data)
{
    if (data) {
        kaa_event_t* record = (kaa_event_t*)data;

        kaa_string_destroy(record->event_class_fqn);
        kaa_bytes_destroy(record->event_data);
        if (record->source && record->source->destroy) {
            record->source->destroy(record->source);
        }
        if (record->target && record->target->destroy) {
            record->target->destroy(record->target);
        }
        kaa_data_destroy(record);
    }
}

static void kaa_event_serialize(avro_writer_t writer, void* data)
{
    if (data) {
        kaa_event_t* record = (kaa_event_t*)data;

            avro_binary_encoding.write_int(writer, record->seq_num);
        kaa_string_serialize(writer, record->event_class_fqn);
        kaa_bytes_serialize(writer, record->event_data);
        record->source->serialize(writer, record->source);
        record->target->serialize(writer, record->target);
    }
}

static size_t kaa_event_get_size(void* data)
{
    if (data) {
        size_t record_size = 0;
        kaa_event_t* record = (kaa_event_t*)data;

        record_size += kaa_long_get_size((int64_t)record->seq_num);
        record_size += kaa_string_get_size(record->event_class_fqn);
        record_size += kaa_bytes_get_size(record->event_data);
        record_size += record->source->get_size(record->source);
        record_size += record->target->get_size(record->target);

        return record_size;
    }

    return 0;
}

kaa_event_t* kaa_event_create()
{
    kaa_event_t* record = 
            (kaa_event_t*)KAA_CALLOC(1, sizeof(kaa_event_t));

    if (record) {
        record->serialize = kaa_event_serialize;
        record->get_size = kaa_event_get_size;
        record->destroy = kaa_event_destroy;
    }

    return record;
}

kaa_event_t* kaa_event_deserialize(avro_reader_t reader)
{
    kaa_event_t* record = 
            (kaa_event_t*)KAA_MALLOC(sizeof(kaa_event_t));

    if (record) {
        record->serialize = kaa_event_serialize;
        record->get_size = kaa_event_get_size;
        record->destroy = kaa_event_destroy;

        avro_binary_encoding.read_int(reader, &record->seq_num);
        record->event_class_fqn = kaa_string_deserialize(reader);
        record->event_data = kaa_bytes_deserialize(reader);
        record->source = kaa_union_string_or_null_deserialize(reader);
        record->target = kaa_union_string_or_null_deserialize(reader);
    }

    return record;
}


static void kaa_event_listeners_request_destroy(void* data)
{
    if (data) {
        kaa_event_listeners_request_t* record = (kaa_event_listeners_request_t*)data;

        kaa_string_destroy(record->request_id);
            kaa_list_destroy(record->event_class_fq_ns, kaa_string_destroy);
            kaa_data_destroy(record);
    }
}

static void kaa_event_listeners_request_serialize(avro_writer_t writer, void* data)
{
    if (data) {
        kaa_event_listeners_request_t* record = (kaa_event_listeners_request_t*)data;

        kaa_string_serialize(writer, record->request_id);
            kaa_array_serialize(writer, record->event_class_fq_ns, kaa_string_serialize);
        }
}

static size_t kaa_event_listeners_request_get_size(void* data)
{
    if (data) {
        size_t record_size = 0;
        kaa_event_listeners_request_t* record = (kaa_event_listeners_request_t*)data;

        record_size += kaa_string_get_size(record->request_id);
            record_size += kaa_array_get_size(record->event_class_fq_ns, kaa_string_get_size);
    
        return record_size;
    }

    return 0;
}

kaa_event_listeners_request_t* kaa_event_listeners_request_create()
{
    kaa_event_listeners_request_t* record = 
            (kaa_event_listeners_request_t*)KAA_CALLOC(1, sizeof(kaa_event_listeners_request_t));

    if (record) {
        record->serialize = kaa_event_listeners_request_serialize;
        record->get_size = kaa_event_listeners_request_get_size;
        record->destroy = kaa_event_listeners_request_destroy;
    }

    return record;
}


# ifndef KAA_UNION_ARRAY_STRING_OR_NULL_C_
# define KAA_UNION_ARRAY_STRING_OR_NULL_C_
static void kaa_union_array_string_or_null_destroy(void *data)
{
    if (data) {
        kaa_union_t *kaa_union = (kaa_union_t*)data;

        switch (kaa_union->type) {
        case KAA_UNION_ARRAY_STRING_OR_NULL_BRANCH_0:
        {
            if (kaa_union->data) {
                    kaa_list_destroy(kaa_union->data, kaa_string_destroy);
                }
            break;
        }
        default:
            break;
        }

        kaa_data_destroy(kaa_union);
    }
}

static size_t kaa_union_array_string_or_null_get_size(void *data)
{
    if (data) {
        kaa_union_t *kaa_union = (kaa_union_t*)data;
        size_t union_size = kaa_long_get_size(kaa_union->type);

        switch (kaa_union->type) {
        case KAA_UNION_ARRAY_STRING_OR_NULL_BRANCH_0:
        {
            if (kaa_union->data) {
                    union_size += kaa_array_get_size(kaa_union->data, kaa_string_get_size);
                }
            break;
        }
        default:
            break;
        }

        return union_size;
    }

    return 0;
}

static void kaa_union_array_string_or_null_serialize(avro_writer_t writer, void *data)
{
    if (data) {
        kaa_union_t *kaa_union = (kaa_union_t*)data;
        avro_binary_encoding.write_long(writer, kaa_union->type);

        switch (kaa_union->type) {
        case KAA_UNION_ARRAY_STRING_OR_NULL_BRANCH_0:
        {
            if (kaa_union->data) {
                    kaa_array_serialize(writer, kaa_union->data, kaa_string_serialize);
                }
            break;
        }
        default:
            break;
        }
    }
}
static kaa_union_t* kaa_union_array_string_or_null_create()
{
    kaa_union_t* kaa_union = KAA_CALLOC(1, sizeof(kaa_union_t));

    if (kaa_union) {
        kaa_union->serialize = kaa_union_array_string_or_null_serialize;
        kaa_union->get_size = kaa_union_array_string_or_null_get_size;
        kaa_union->destroy = kaa_union_array_string_or_null_destroy;
    }

    return kaa_union; 
}

kaa_union_t* kaa_union_array_string_or_null_branch_0_create()
{
    kaa_union_t *kaa_union = kaa_union_array_string_or_null_create();
    if (kaa_union) {
        kaa_union->type = KAA_UNION_ARRAY_STRING_OR_NULL_BRANCH_0;
    }
    return kaa_union;
}

kaa_union_t* kaa_union_array_string_or_null_branch_1_create()
{
    kaa_union_t *kaa_union = kaa_union_array_string_or_null_create();
    if (kaa_union) {
        kaa_union->type = KAA_UNION_ARRAY_STRING_OR_NULL_BRANCH_1;
    }
    return kaa_union;
}

kaa_union_t* kaa_union_array_string_or_null_deserialize(avro_reader_t reader)
{
    kaa_union_t *kaa_union = kaa_union_array_string_or_null_create();

    if (kaa_union) {
        int64_t branch;
        avro_binary_encoding.read_long(reader, &branch);
        kaa_union->type = branch;

        switch (kaa_union->type) {
        case KAA_UNION_ARRAY_STRING_OR_NULL_BRANCH_0:
        {
                kaa_union->data = kaa_array_deserialize(reader, (deserialize_fn)kaa_string_deserialize);
                break;
        }
        default:
            break;
        }
    }

    return kaa_union;
}
# endif // KAA_UNION_ARRAY_STRING_OR_NULL_C_


static void kaa_event_listeners_response_destroy(void* data)
{
    if (data) {
        kaa_event_listeners_response_t* record = (kaa_event_listeners_response_t*)data;

        kaa_string_destroy(record->request_id);
        if (record->listeners && record->listeners->destroy) {
            record->listeners->destroy(record->listeners);
        }
        kaa_data_destroy(record);
    }
}


kaa_event_listeners_response_t* kaa_event_listeners_response_deserialize(avro_reader_t reader)
{
    kaa_event_listeners_response_t* record = 
            (kaa_event_listeners_response_t*)KAA_MALLOC(sizeof(kaa_event_listeners_response_t));

    if (record) {
        record->destroy = kaa_event_listeners_response_destroy;

        record->request_id = kaa_string_deserialize(reader);
        record->listeners = kaa_union_array_string_or_null_deserialize(reader);
        int64_t result_value;
        avro_binary_encoding.read_long(reader, &result_value);
        record->result = result_value;
    }

    return record;
}




kaa_event_sequence_number_request_t* kaa_event_sequence_number_request_create()
{
    kaa_event_sequence_number_request_t* record = 
            (kaa_event_sequence_number_request_t*)KAA_CALLOC(1, sizeof(kaa_event_sequence_number_request_t));

    if (record) {
        record->destroy = kaa_data_destroy;
    }

    return record;
}




kaa_event_sequence_number_response_t* kaa_event_sequence_number_response_deserialize(avro_reader_t reader)
{
    kaa_event_sequence_number_response_t* record = 
            (kaa_event_sequence_number_response_t*)KAA_MALLOC(sizeof(kaa_event_sequence_number_response_t));

    if (record) {
        record->destroy = kaa_data_destroy;

        avro_binary_encoding.read_int(reader, &record->seq_num);
    }

    return record;
}


# ifndef KAA_UNION_INT_OR_NULL_C_
# define KAA_UNION_INT_OR_NULL_C_
static void kaa_union_int_or_null_destroy(void *data)
{
    if (data) {
        kaa_union_t *kaa_union = (kaa_union_t*)data;

        switch (kaa_union->type) {
        case KAA_UNION_INT_OR_NULL_BRANCH_0:
        {
            if (kaa_union->data) {
                kaa_data_destroy(kaa_union->data);
            }
            break;
        }
        default:
            break;
        }

        kaa_data_destroy(kaa_union);
    }
}

static size_t kaa_union_int_or_null_get_size(void *data)
{
    if (data) {
        kaa_union_t *kaa_union = (kaa_union_t*)data;
        size_t union_size = kaa_long_get_size(kaa_union->type);

        switch (kaa_union->type) {
        case KAA_UNION_INT_OR_NULL_BRANCH_0:
        {
            if (kaa_union->data) {
                    int64_t primitive_value = *((int32_t *)kaa_union->data);
                union_size += kaa_long_get_size(primitive_value);
            }
            break;
        }
        default:
            break;
        }

        return union_size;
    }

    return 0;
}

static void kaa_union_int_or_null_serialize(avro_writer_t writer, void *data)
{
    if (data) {
        kaa_union_t *kaa_union = (kaa_union_t*)data;
        avro_binary_encoding.write_long(writer, kaa_union->type);

        switch (kaa_union->type) {
        case KAA_UNION_INT_OR_NULL_BRANCH_0:
        {
            if (kaa_union->data) {
                    avro_binary_encoding.write_int(writer, *((int32_t *)kaa_union->data));
            }
            break;
        }
        default:
            break;
        }
    }
}
static kaa_union_t* kaa_union_int_or_null_create()
{
    kaa_union_t* kaa_union = KAA_CALLOC(1, sizeof(kaa_union_t));

    if (kaa_union) {
        kaa_union->serialize = kaa_union_int_or_null_serialize;
        kaa_union->get_size = kaa_union_int_or_null_get_size;
        kaa_union->destroy = kaa_union_int_or_null_destroy;
    }

    return kaa_union; 
}

kaa_union_t* kaa_union_int_or_null_branch_0_create()
{
    kaa_union_t *kaa_union = kaa_union_int_or_null_create();
    if (kaa_union) {
        kaa_union->type = KAA_UNION_INT_OR_NULL_BRANCH_0;
    }
    return kaa_union;
}

kaa_union_t* kaa_union_int_or_null_branch_1_create()
{
    kaa_union_t *kaa_union = kaa_union_int_or_null_create();
    if (kaa_union) {
        kaa_union->type = KAA_UNION_INT_OR_NULL_BRANCH_1;
    }
    return kaa_union;
}

kaa_union_t* kaa_union_int_or_null_deserialize(avro_reader_t reader)
{
    kaa_union_t *kaa_union = kaa_union_int_or_null_create();

    if (kaa_union) {
        int64_t branch;
        avro_binary_encoding.read_long(reader, &branch);
        kaa_union->type = branch;

        switch (kaa_union->type) {
        case KAA_UNION_INT_OR_NULL_BRANCH_0:
        {
                kaa_union->data = kaa_int_deserialize(reader);
            break;
        }
        default:
            break;
        }
    }

    return kaa_union;
}
# endif // KAA_UNION_INT_OR_NULL_C_


static void kaa_notification_destroy(void* data)
{
    if (data) {
        kaa_notification_t* record = (kaa_notification_t*)data;

        kaa_string_destroy(record->topic_id);
        if (record->uid && record->uid->destroy) {
            record->uid->destroy(record->uid);
        }
        if (record->seq_number && record->seq_number->destroy) {
            record->seq_number->destroy(record->seq_number);
        }
        kaa_bytes_destroy(record->body);
        kaa_data_destroy(record);
    }
}


kaa_notification_t* kaa_notification_deserialize(avro_reader_t reader)
{
    kaa_notification_t* record = 
            (kaa_notification_t*)KAA_MALLOC(sizeof(kaa_notification_t));

    if (record) {
        record->destroy = kaa_notification_destroy;

        record->topic_id = kaa_string_deserialize(reader);
        int64_t type_value;
        avro_binary_encoding.read_long(reader, &type_value);
        record->type = type_value;
        record->uid = kaa_union_string_or_null_deserialize(reader);
        record->seq_number = kaa_union_int_or_null_deserialize(reader);
        record->body = kaa_bytes_deserialize(reader);
    }

    return record;
}


static void kaa_topic_destroy(void* data)
{
    if (data) {
        kaa_topic_t* record = (kaa_topic_t*)data;

        kaa_string_destroy(record->id);
        kaa_string_destroy(record->name);
        kaa_data_destroy(record);
    }
}


kaa_topic_t* kaa_topic_deserialize(avro_reader_t reader)
{
    kaa_topic_t* record = 
            (kaa_topic_t*)KAA_MALLOC(sizeof(kaa_topic_t));

    if (record) {
        record->destroy = kaa_topic_destroy;

        record->id = kaa_string_deserialize(reader);
        record->name = kaa_string_deserialize(reader);
        int64_t subscription_type_value;
        avro_binary_encoding.read_long(reader, &subscription_type_value);
        record->subscription_type = subscription_type_value;
    }

    return record;
}


static void kaa_log_entry_destroy(void* data)
{
    if (data) {
        kaa_log_entry_t* record = (kaa_log_entry_t*)data;

        kaa_bytes_destroy(record->data);
        kaa_data_destroy(record);
    }
}

static void kaa_log_entry_serialize(avro_writer_t writer, void* data)
{
    if (data) {
        kaa_log_entry_t* record = (kaa_log_entry_t*)data;

        kaa_bytes_serialize(writer, record->data);
    }
}

static size_t kaa_log_entry_get_size(void* data)
{
    if (data) {
        size_t record_size = 0;
        kaa_log_entry_t* record = (kaa_log_entry_t*)data;

        record_size += kaa_bytes_get_size(record->data);

        return record_size;
    }

    return 0;
}

kaa_log_entry_t* kaa_log_entry_create()
{
    kaa_log_entry_t* record = 
            (kaa_log_entry_t*)KAA_CALLOC(1, sizeof(kaa_log_entry_t));

    if (record) {
        record->serialize = kaa_log_entry_serialize;
        record->get_size = kaa_log_entry_get_size;
        record->destroy = kaa_log_entry_destroy;
    }

    return record;
}


# ifndef KAA_UNION_BYTES_OR_NULL_C_
# define KAA_UNION_BYTES_OR_NULL_C_
static void kaa_union_bytes_or_null_destroy(void *data)
{
    if (data) {
        kaa_union_t *kaa_union = (kaa_union_t*)data;

        switch (kaa_union->type) {
        case KAA_UNION_BYTES_OR_NULL_BRANCH_0:
        {
            if (kaa_union->data) {
                kaa_bytes_destroy(kaa_union->data);
            }
            break;
        }
        default:
            break;
        }

        kaa_data_destroy(kaa_union);
    }
}

static size_t kaa_union_bytes_or_null_get_size(void *data)
{
    if (data) {
        kaa_union_t *kaa_union = (kaa_union_t*)data;
        size_t union_size = kaa_long_get_size(kaa_union->type);

        switch (kaa_union->type) {
        case KAA_UNION_BYTES_OR_NULL_BRANCH_0:
        {
            if (kaa_union->data) {
                union_size += kaa_bytes_get_size(kaa_union->data);
            }
            break;
        }
        default:
            break;
        }

        return union_size;
    }

    return 0;
}

static void kaa_union_bytes_or_null_serialize(avro_writer_t writer, void *data)
{
    if (data) {
        kaa_union_t *kaa_union = (kaa_union_t*)data;
        avro_binary_encoding.write_long(writer, kaa_union->type);

        switch (kaa_union->type) {
        case KAA_UNION_BYTES_OR_NULL_BRANCH_0:
        {
            if (kaa_union->data) {
                kaa_bytes_serialize(writer, kaa_union->data);
            }
            break;
        }
        default:
            break;
        }
    }
}
static kaa_union_t* kaa_union_bytes_or_null_create()
{
    kaa_union_t* kaa_union = KAA_CALLOC(1, sizeof(kaa_union_t));

    if (kaa_union) {
        kaa_union->serialize = kaa_union_bytes_or_null_serialize;
        kaa_union->get_size = kaa_union_bytes_or_null_get_size;
        kaa_union->destroy = kaa_union_bytes_or_null_destroy;
    }

    return kaa_union; 
}

kaa_union_t* kaa_union_bytes_or_null_branch_0_create()
{
    kaa_union_t *kaa_union = kaa_union_bytes_or_null_create();
    if (kaa_union) {
        kaa_union->type = KAA_UNION_BYTES_OR_NULL_BRANCH_0;
    }
    return kaa_union;
}

kaa_union_t* kaa_union_bytes_or_null_branch_1_create()
{
    kaa_union_t *kaa_union = kaa_union_bytes_or_null_create();
    if (kaa_union) {
        kaa_union->type = KAA_UNION_BYTES_OR_NULL_BRANCH_1;
    }
    return kaa_union;
}

kaa_union_t* kaa_union_bytes_or_null_deserialize(avro_reader_t reader)
{
    kaa_union_t *kaa_union = kaa_union_bytes_or_null_create();

    if (kaa_union) {
        int64_t branch;
        avro_binary_encoding.read_long(reader, &branch);
        kaa_union->type = branch;

        switch (kaa_union->type) {
        case KAA_UNION_BYTES_OR_NULL_BRANCH_0:
        {
            kaa_union->data = kaa_bytes_deserialize(reader);
            break;
        }
        default:
            break;
        }
    }

    return kaa_union;
}
# endif // KAA_UNION_BYTES_OR_NULL_C_


static void kaa_sync_request_meta_data_destroy(void* data)
{
    if (data) {
        kaa_sync_request_meta_data_t* record = (kaa_sync_request_meta_data_t*)data;

        kaa_string_destroy(record->application_token);
        kaa_bytes_destroy(record->endpoint_public_key_hash);
        if (record->profile_hash && record->profile_hash->destroy) {
            record->profile_hash->destroy(record->profile_hash);
        }
        kaa_data_destroy(record);
    }
}

static void kaa_sync_request_meta_data_serialize(avro_writer_t writer, void* data)
{
    if (data) {
        kaa_sync_request_meta_data_t* record = (kaa_sync_request_meta_data_t*)data;

        kaa_string_serialize(writer, record->application_token);
        kaa_bytes_serialize(writer, record->endpoint_public_key_hash);
        record->profile_hash->serialize(writer, record->profile_hash);
            avro_binary_encoding.write_long(writer, record->timeout);
    }
}

static size_t kaa_sync_request_meta_data_get_size(void* data)
{
    if (data) {
        size_t record_size = 0;
        kaa_sync_request_meta_data_t* record = (kaa_sync_request_meta_data_t*)data;

        record_size += kaa_string_get_size(record->application_token);
        record_size += kaa_bytes_get_size(record->endpoint_public_key_hash);
        record_size += record->profile_hash->get_size(record->profile_hash);
        record_size += kaa_long_get_size((int64_t)record->timeout);

        return record_size;
    }

    return 0;
}

kaa_sync_request_meta_data_t* kaa_sync_request_meta_data_create()
{
    kaa_sync_request_meta_data_t* record = 
            (kaa_sync_request_meta_data_t*)KAA_CALLOC(1, sizeof(kaa_sync_request_meta_data_t));

    if (record) {
        record->serialize = kaa_sync_request_meta_data_serialize;
        record->get_size = kaa_sync_request_meta_data_get_size;
        record->destroy = kaa_sync_request_meta_data_destroy;
    }

    return record;
}


static void kaa_profile_sync_request_destroy(void* data)
{
    if (data) {
        kaa_profile_sync_request_t* record = (kaa_profile_sync_request_t*)data;

        if (record->endpoint_public_key && record->endpoint_public_key->destroy) {
            record->endpoint_public_key->destroy(record->endpoint_public_key);
        }
        kaa_bytes_destroy(record->profile_body);
        if (record->version_info && record->version_info->destroy) {
            record->version_info->destroy(record->version_info);
        }
        if (record->endpoint_access_token && record->endpoint_access_token->destroy) {
            record->endpoint_access_token->destroy(record->endpoint_access_token);
        }
        kaa_data_destroy(record);
    }
}

static void kaa_profile_sync_request_serialize(avro_writer_t writer, void* data)
{
    if (data) {
        kaa_profile_sync_request_t* record = (kaa_profile_sync_request_t*)data;

        record->endpoint_public_key->serialize(writer, record->endpoint_public_key);
        kaa_bytes_serialize(writer, record->profile_body);
        record->version_info->serialize(writer, record->version_info);
        record->endpoint_access_token->serialize(writer, record->endpoint_access_token);
    }
}

static size_t kaa_profile_sync_request_get_size(void* data)
{
    if (data) {
        size_t record_size = 0;
        kaa_profile_sync_request_t* record = (kaa_profile_sync_request_t*)data;

        record_size += record->endpoint_public_key->get_size(record->endpoint_public_key);
        record_size += kaa_bytes_get_size(record->profile_body);
        record_size += record->version_info->get_size(record->version_info);
        record_size += record->endpoint_access_token->get_size(record->endpoint_access_token);

        return record_size;
    }

    return 0;
}

kaa_profile_sync_request_t* kaa_profile_sync_request_create()
{
    kaa_profile_sync_request_t* record = 
            (kaa_profile_sync_request_t*)KAA_CALLOC(1, sizeof(kaa_profile_sync_request_t));

    if (record) {
        record->serialize = kaa_profile_sync_request_serialize;
        record->get_size = kaa_profile_sync_request_get_size;
        record->destroy = kaa_profile_sync_request_destroy;
    }

    return record;
}


static void kaa_configuration_sync_request_destroy(void* data)
{
    if (data) {
        kaa_configuration_sync_request_t* record = (kaa_configuration_sync_request_t*)data;

        if (record->configuration_hash && record->configuration_hash->destroy) {
            record->configuration_hash->destroy(record->configuration_hash);
        }
        kaa_data_destroy(record);
    }
}

static void kaa_configuration_sync_request_serialize(avro_writer_t writer, void* data)
{
    if (data) {
        kaa_configuration_sync_request_t* record = (kaa_configuration_sync_request_t*)data;

            avro_binary_encoding.write_int(writer, record->app_state_seq_number);
        record->configuration_hash->serialize(writer, record->configuration_hash);
    }
}

static size_t kaa_configuration_sync_request_get_size(void* data)
{
    if (data) {
        size_t record_size = 0;
        kaa_configuration_sync_request_t* record = (kaa_configuration_sync_request_t*)data;

        record_size += kaa_long_get_size((int64_t)record->app_state_seq_number);
        record_size += record->configuration_hash->get_size(record->configuration_hash);

        return record_size;
    }

    return 0;
}

kaa_configuration_sync_request_t* kaa_configuration_sync_request_create()
{
    kaa_configuration_sync_request_t* record = 
            (kaa_configuration_sync_request_t*)KAA_CALLOC(1, sizeof(kaa_configuration_sync_request_t));

    if (record) {
        record->serialize = kaa_configuration_sync_request_serialize;
        record->get_size = kaa_configuration_sync_request_get_size;
        record->destroy = kaa_configuration_sync_request_destroy;
    }

    return record;
}


# ifndef KAA_UNION_ARRAY_TOPIC_STATE_OR_NULL_C_
# define KAA_UNION_ARRAY_TOPIC_STATE_OR_NULL_C_
static void kaa_union_array_topic_state_or_null_destroy(void *data)
{
    if (data) {
        kaa_union_t *kaa_union = (kaa_union_t*)data;

        switch (kaa_union->type) {
        case KAA_UNION_ARRAY_TOPIC_STATE_OR_NULL_BRANCH_0:
        {
            if (kaa_union->data) {
                            kaa_list_destroy(kaa_union->data, kaa_topic_state_destroy);
                        }
            break;
        }
        default:
            break;
        }

        kaa_data_destroy(kaa_union);
    }
}

static size_t kaa_union_array_topic_state_or_null_get_size(void *data)
{
    if (data) {
        kaa_union_t *kaa_union = (kaa_union_t*)data;
        size_t union_size = kaa_long_get_size(kaa_union->type);

        switch (kaa_union->type) {
        case KAA_UNION_ARRAY_TOPIC_STATE_OR_NULL_BRANCH_0:
        {
            if (kaa_union->data) {
                            union_size += kaa_array_get_size(kaa_union->data, kaa_topic_state_get_size);
                        }
            break;
        }
        default:
            break;
        }

        return union_size;
    }

    return 0;
}

static void kaa_union_array_topic_state_or_null_serialize(avro_writer_t writer, void *data)
{
    if (data) {
        kaa_union_t *kaa_union = (kaa_union_t*)data;
        avro_binary_encoding.write_long(writer, kaa_union->type);

        switch (kaa_union->type) {
        case KAA_UNION_ARRAY_TOPIC_STATE_OR_NULL_BRANCH_0:
        {
            if (kaa_union->data) {
                            kaa_array_serialize(writer, kaa_union->data, kaa_topic_state_serialize);
                        }
            break;
        }
        default:
            break;
        }
    }
}
static kaa_union_t* kaa_union_array_topic_state_or_null_create()
{
    kaa_union_t* kaa_union = KAA_CALLOC(1, sizeof(kaa_union_t));

    if (kaa_union) {
        kaa_union->serialize = kaa_union_array_topic_state_or_null_serialize;
        kaa_union->get_size = kaa_union_array_topic_state_or_null_get_size;
        kaa_union->destroy = kaa_union_array_topic_state_or_null_destroy;
    }

    return kaa_union; 
}

kaa_union_t* kaa_union_array_topic_state_or_null_branch_0_create()
{
    kaa_union_t *kaa_union = kaa_union_array_topic_state_or_null_create();
    if (kaa_union) {
        kaa_union->type = KAA_UNION_ARRAY_TOPIC_STATE_OR_NULL_BRANCH_0;
    }
    return kaa_union;
}

kaa_union_t* kaa_union_array_topic_state_or_null_branch_1_create()
{
    kaa_union_t *kaa_union = kaa_union_array_topic_state_or_null_create();
    if (kaa_union) {
        kaa_union->type = KAA_UNION_ARRAY_TOPIC_STATE_OR_NULL_BRANCH_1;
    }
    return kaa_union;
}
# endif // KAA_UNION_ARRAY_TOPIC_STATE_OR_NULL_C_


# ifndef KAA_UNION_ARRAY_SUBSCRIPTION_COMMAND_OR_NULL_C_
# define KAA_UNION_ARRAY_SUBSCRIPTION_COMMAND_OR_NULL_C_
static void kaa_union_array_subscription_command_or_null_destroy(void *data)
{
    if (data) {
        kaa_union_t *kaa_union = (kaa_union_t*)data;

        switch (kaa_union->type) {
        case KAA_UNION_ARRAY_SUBSCRIPTION_COMMAND_OR_NULL_BRANCH_0:
        {
            if (kaa_union->data) {
                            kaa_list_destroy(kaa_union->data, kaa_subscription_command_destroy);
                        }
            break;
        }
        default:
            break;
        }

        kaa_data_destroy(kaa_union);
    }
}

static size_t kaa_union_array_subscription_command_or_null_get_size(void *data)
{
    if (data) {
        kaa_union_t *kaa_union = (kaa_union_t*)data;
        size_t union_size = kaa_long_get_size(kaa_union->type);

        switch (kaa_union->type) {
        case KAA_UNION_ARRAY_SUBSCRIPTION_COMMAND_OR_NULL_BRANCH_0:
        {
            if (kaa_union->data) {
                            union_size += kaa_array_get_size(kaa_union->data, kaa_subscription_command_get_size);
                        }
            break;
        }
        default:
            break;
        }

        return union_size;
    }

    return 0;
}

static void kaa_union_array_subscription_command_or_null_serialize(avro_writer_t writer, void *data)
{
    if (data) {
        kaa_union_t *kaa_union = (kaa_union_t*)data;
        avro_binary_encoding.write_long(writer, kaa_union->type);

        switch (kaa_union->type) {
        case KAA_UNION_ARRAY_SUBSCRIPTION_COMMAND_OR_NULL_BRANCH_0:
        {
            if (kaa_union->data) {
                            kaa_array_serialize(writer, kaa_union->data, kaa_subscription_command_serialize);
                        }
            break;
        }
        default:
            break;
        }
    }
}
static kaa_union_t* kaa_union_array_subscription_command_or_null_create()
{
    kaa_union_t* kaa_union = KAA_CALLOC(1, sizeof(kaa_union_t));

    if (kaa_union) {
        kaa_union->serialize = kaa_union_array_subscription_command_or_null_serialize;
        kaa_union->get_size = kaa_union_array_subscription_command_or_null_get_size;
        kaa_union->destroy = kaa_union_array_subscription_command_or_null_destroy;
    }

    return kaa_union; 
}

kaa_union_t* kaa_union_array_subscription_command_or_null_branch_0_create()
{
    kaa_union_t *kaa_union = kaa_union_array_subscription_command_or_null_create();
    if (kaa_union) {
        kaa_union->type = KAA_UNION_ARRAY_SUBSCRIPTION_COMMAND_OR_NULL_BRANCH_0;
    }
    return kaa_union;
}

kaa_union_t* kaa_union_array_subscription_command_or_null_branch_1_create()
{
    kaa_union_t *kaa_union = kaa_union_array_subscription_command_or_null_create();
    if (kaa_union) {
        kaa_union->type = KAA_UNION_ARRAY_SUBSCRIPTION_COMMAND_OR_NULL_BRANCH_1;
    }
    return kaa_union;
}
# endif // KAA_UNION_ARRAY_SUBSCRIPTION_COMMAND_OR_NULL_C_


static void kaa_notification_sync_request_destroy(void* data)
{
    if (data) {
        kaa_notification_sync_request_t* record = (kaa_notification_sync_request_t*)data;

        if (record->topic_list_hash && record->topic_list_hash->destroy) {
            record->topic_list_hash->destroy(record->topic_list_hash);
        }
        if (record->topic_states && record->topic_states->destroy) {
            record->topic_states->destroy(record->topic_states);
        }
        if (record->accepted_unicast_notifications && record->accepted_unicast_notifications->destroy) {
            record->accepted_unicast_notifications->destroy(record->accepted_unicast_notifications);
        }
        if (record->subscription_commands && record->subscription_commands->destroy) {
            record->subscription_commands->destroy(record->subscription_commands);
        }
        kaa_data_destroy(record);
    }
}

static void kaa_notification_sync_request_serialize(avro_writer_t writer, void* data)
{
    if (data) {
        kaa_notification_sync_request_t* record = (kaa_notification_sync_request_t*)data;

            avro_binary_encoding.write_int(writer, record->app_state_seq_number);
        record->topic_list_hash->serialize(writer, record->topic_list_hash);
        record->topic_states->serialize(writer, record->topic_states);
        record->accepted_unicast_notifications->serialize(writer, record->accepted_unicast_notifications);
        record->subscription_commands->serialize(writer, record->subscription_commands);
    }
}

static size_t kaa_notification_sync_request_get_size(void* data)
{
    if (data) {
        size_t record_size = 0;
        kaa_notification_sync_request_t* record = (kaa_notification_sync_request_t*)data;

        record_size += kaa_long_get_size((int64_t)record->app_state_seq_number);
        record_size += record->topic_list_hash->get_size(record->topic_list_hash);
        record_size += record->topic_states->get_size(record->topic_states);
        record_size += record->accepted_unicast_notifications->get_size(record->accepted_unicast_notifications);
        record_size += record->subscription_commands->get_size(record->subscription_commands);

        return record_size;
    }

    return 0;
}

kaa_notification_sync_request_t* kaa_notification_sync_request_create()
{
    kaa_notification_sync_request_t* record = 
            (kaa_notification_sync_request_t*)KAA_CALLOC(1, sizeof(kaa_notification_sync_request_t));

    if (record) {
        record->serialize = kaa_notification_sync_request_serialize;
        record->get_size = kaa_notification_sync_request_get_size;
        record->destroy = kaa_notification_sync_request_destroy;
    }

    return record;
}


# ifndef KAA_UNION_USER_ATTACH_REQUEST_OR_NULL_C_
# define KAA_UNION_USER_ATTACH_REQUEST_OR_NULL_C_
static void kaa_union_user_attach_request_or_null_destroy(void *data)
{
    if (data) {
        kaa_union_t *kaa_union = (kaa_union_t*)data;

        switch (kaa_union->type) {
        case KAA_UNION_USER_ATTACH_REQUEST_OR_NULL_BRANCH_0:
        {
            if (kaa_union->data) {
                kaa_user_attach_request_t* record = (kaa_user_attach_request_t*)kaa_union->data;
                record->destroy(record);
            }
            break;
        }
        default:
            break;
        }

        kaa_data_destroy(kaa_union);
    }
}

static size_t kaa_union_user_attach_request_or_null_get_size(void *data)
{
    if (data) {
        kaa_union_t *kaa_union = (kaa_union_t*)data;
        size_t union_size = kaa_long_get_size(kaa_union->type);

        switch (kaa_union->type) {
        case KAA_UNION_USER_ATTACH_REQUEST_OR_NULL_BRANCH_0:
        {
            if (kaa_union->data) {
                kaa_user_attach_request_t* record = (kaa_user_attach_request_t*)kaa_union->data;
                union_size += record->get_size(record);
            }
            break;
        }
        default:
            break;
        }

        return union_size;
    }

    return 0;
}

static void kaa_union_user_attach_request_or_null_serialize(avro_writer_t writer, void *data)
{
    if (data) {
        kaa_union_t *kaa_union = (kaa_union_t*)data;
        avro_binary_encoding.write_long(writer, kaa_union->type);

        switch (kaa_union->type) {
        case KAA_UNION_USER_ATTACH_REQUEST_OR_NULL_BRANCH_0:
        {
            if (kaa_union->data) {
                kaa_user_attach_request_t* record = (kaa_user_attach_request_t*)kaa_union->data;
                record->serialize(writer, record);
            }
            break;
        }
        default:
            break;
        }
    }
}
static kaa_union_t* kaa_union_user_attach_request_or_null_create()
{
    kaa_union_t* kaa_union = KAA_CALLOC(1, sizeof(kaa_union_t));

    if (kaa_union) {
        kaa_union->serialize = kaa_union_user_attach_request_or_null_serialize;
        kaa_union->get_size = kaa_union_user_attach_request_or_null_get_size;
        kaa_union->destroy = kaa_union_user_attach_request_or_null_destroy;
    }

    return kaa_union; 
}

kaa_union_t* kaa_union_user_attach_request_or_null_branch_0_create()
{
    kaa_union_t *kaa_union = kaa_union_user_attach_request_or_null_create();
    if (kaa_union) {
        kaa_union->type = KAA_UNION_USER_ATTACH_REQUEST_OR_NULL_BRANCH_0;
    }
    return kaa_union;
}

kaa_union_t* kaa_union_user_attach_request_or_null_branch_1_create()
{
    kaa_union_t *kaa_union = kaa_union_user_attach_request_or_null_create();
    if (kaa_union) {
        kaa_union->type = KAA_UNION_USER_ATTACH_REQUEST_OR_NULL_BRANCH_1;
    }
    return kaa_union;
}
# endif // KAA_UNION_USER_ATTACH_REQUEST_OR_NULL_C_


# ifndef KAA_UNION_ARRAY_ENDPOINT_ATTACH_REQUEST_OR_NULL_C_
# define KAA_UNION_ARRAY_ENDPOINT_ATTACH_REQUEST_OR_NULL_C_
static void kaa_union_array_endpoint_attach_request_or_null_destroy(void *data)
{
    if (data) {
        kaa_union_t *kaa_union = (kaa_union_t*)data;

        switch (kaa_union->type) {
        case KAA_UNION_ARRAY_ENDPOINT_ATTACH_REQUEST_OR_NULL_BRANCH_0:
        {
            if (kaa_union->data) {
                            kaa_list_destroy(kaa_union->data, kaa_endpoint_attach_request_destroy);
                        }
            break;
        }
        default:
            break;
        }

        kaa_data_destroy(kaa_union);
    }
}

static size_t kaa_union_array_endpoint_attach_request_or_null_get_size(void *data)
{
    if (data) {
        kaa_union_t *kaa_union = (kaa_union_t*)data;
        size_t union_size = kaa_long_get_size(kaa_union->type);

        switch (kaa_union->type) {
        case KAA_UNION_ARRAY_ENDPOINT_ATTACH_REQUEST_OR_NULL_BRANCH_0:
        {
            if (kaa_union->data) {
                            union_size += kaa_array_get_size(kaa_union->data, kaa_endpoint_attach_request_get_size);
                        }
            break;
        }
        default:
            break;
        }

        return union_size;
    }

    return 0;
}

static void kaa_union_array_endpoint_attach_request_or_null_serialize(avro_writer_t writer, void *data)
{
    if (data) {
        kaa_union_t *kaa_union = (kaa_union_t*)data;
        avro_binary_encoding.write_long(writer, kaa_union->type);

        switch (kaa_union->type) {
        case KAA_UNION_ARRAY_ENDPOINT_ATTACH_REQUEST_OR_NULL_BRANCH_0:
        {
            if (kaa_union->data) {
                            kaa_array_serialize(writer, kaa_union->data, kaa_endpoint_attach_request_serialize);
                        }
            break;
        }
        default:
            break;
        }
    }
}
static kaa_union_t* kaa_union_array_endpoint_attach_request_or_null_create()
{
    kaa_union_t* kaa_union = KAA_CALLOC(1, sizeof(kaa_union_t));

    if (kaa_union) {
        kaa_union->serialize = kaa_union_array_endpoint_attach_request_or_null_serialize;
        kaa_union->get_size = kaa_union_array_endpoint_attach_request_or_null_get_size;
        kaa_union->destroy = kaa_union_array_endpoint_attach_request_or_null_destroy;
    }

    return kaa_union; 
}

kaa_union_t* kaa_union_array_endpoint_attach_request_or_null_branch_0_create()
{
    kaa_union_t *kaa_union = kaa_union_array_endpoint_attach_request_or_null_create();
    if (kaa_union) {
        kaa_union->type = KAA_UNION_ARRAY_ENDPOINT_ATTACH_REQUEST_OR_NULL_BRANCH_0;
    }
    return kaa_union;
}

kaa_union_t* kaa_union_array_endpoint_attach_request_or_null_branch_1_create()
{
    kaa_union_t *kaa_union = kaa_union_array_endpoint_attach_request_or_null_create();
    if (kaa_union) {
        kaa_union->type = KAA_UNION_ARRAY_ENDPOINT_ATTACH_REQUEST_OR_NULL_BRANCH_1;
    }
    return kaa_union;
}
# endif // KAA_UNION_ARRAY_ENDPOINT_ATTACH_REQUEST_OR_NULL_C_


# ifndef KAA_UNION_ARRAY_ENDPOINT_DETACH_REQUEST_OR_NULL_C_
# define KAA_UNION_ARRAY_ENDPOINT_DETACH_REQUEST_OR_NULL_C_
static void kaa_union_array_endpoint_detach_request_or_null_destroy(void *data)
{
    if (data) {
        kaa_union_t *kaa_union = (kaa_union_t*)data;

        switch (kaa_union->type) {
        case KAA_UNION_ARRAY_ENDPOINT_DETACH_REQUEST_OR_NULL_BRANCH_0:
        {
            if (kaa_union->data) {
                            kaa_list_destroy(kaa_union->data, kaa_endpoint_detach_request_destroy);
                        }
            break;
        }
        default:
            break;
        }

        kaa_data_destroy(kaa_union);
    }
}

static size_t kaa_union_array_endpoint_detach_request_or_null_get_size(void *data)
{
    if (data) {
        kaa_union_t *kaa_union = (kaa_union_t*)data;
        size_t union_size = kaa_long_get_size(kaa_union->type);

        switch (kaa_union->type) {
        case KAA_UNION_ARRAY_ENDPOINT_DETACH_REQUEST_OR_NULL_BRANCH_0:
        {
            if (kaa_union->data) {
                            union_size += kaa_array_get_size(kaa_union->data, kaa_endpoint_detach_request_get_size);
                        }
            break;
        }
        default:
            break;
        }

        return union_size;
    }

    return 0;
}

static void kaa_union_array_endpoint_detach_request_or_null_serialize(avro_writer_t writer, void *data)
{
    if (data) {
        kaa_union_t *kaa_union = (kaa_union_t*)data;
        avro_binary_encoding.write_long(writer, kaa_union->type);

        switch (kaa_union->type) {
        case KAA_UNION_ARRAY_ENDPOINT_DETACH_REQUEST_OR_NULL_BRANCH_0:
        {
            if (kaa_union->data) {
                            kaa_array_serialize(writer, kaa_union->data, kaa_endpoint_detach_request_serialize);
                        }
            break;
        }
        default:
            break;
        }
    }
}
static kaa_union_t* kaa_union_array_endpoint_detach_request_or_null_create()
{
    kaa_union_t* kaa_union = KAA_CALLOC(1, sizeof(kaa_union_t));

    if (kaa_union) {
        kaa_union->serialize = kaa_union_array_endpoint_detach_request_or_null_serialize;
        kaa_union->get_size = kaa_union_array_endpoint_detach_request_or_null_get_size;
        kaa_union->destroy = kaa_union_array_endpoint_detach_request_or_null_destroy;
    }

    return kaa_union; 
}

kaa_union_t* kaa_union_array_endpoint_detach_request_or_null_branch_0_create()
{
    kaa_union_t *kaa_union = kaa_union_array_endpoint_detach_request_or_null_create();
    if (kaa_union) {
        kaa_union->type = KAA_UNION_ARRAY_ENDPOINT_DETACH_REQUEST_OR_NULL_BRANCH_0;
    }
    return kaa_union;
}

kaa_union_t* kaa_union_array_endpoint_detach_request_or_null_branch_1_create()
{
    kaa_union_t *kaa_union = kaa_union_array_endpoint_detach_request_or_null_create();
    if (kaa_union) {
        kaa_union->type = KAA_UNION_ARRAY_ENDPOINT_DETACH_REQUEST_OR_NULL_BRANCH_1;
    }
    return kaa_union;
}
# endif // KAA_UNION_ARRAY_ENDPOINT_DETACH_REQUEST_OR_NULL_C_


static void kaa_user_sync_request_destroy(void* data)
{
    if (data) {
        kaa_user_sync_request_t* record = (kaa_user_sync_request_t*)data;

        if (record->user_attach_request && record->user_attach_request->destroy) {
            record->user_attach_request->destroy(record->user_attach_request);
        }
        if (record->endpoint_attach_requests && record->endpoint_attach_requests->destroy) {
            record->endpoint_attach_requests->destroy(record->endpoint_attach_requests);
        }
        if (record->endpoint_detach_requests && record->endpoint_detach_requests->destroy) {
            record->endpoint_detach_requests->destroy(record->endpoint_detach_requests);
        }
        kaa_data_destroy(record);
    }
}

static void kaa_user_sync_request_serialize(avro_writer_t writer, void* data)
{
    if (data) {
        kaa_user_sync_request_t* record = (kaa_user_sync_request_t*)data;

        record->user_attach_request->serialize(writer, record->user_attach_request);
        record->endpoint_attach_requests->serialize(writer, record->endpoint_attach_requests);
        record->endpoint_detach_requests->serialize(writer, record->endpoint_detach_requests);
    }
}

static size_t kaa_user_sync_request_get_size(void* data)
{
    if (data) {
        size_t record_size = 0;
        kaa_user_sync_request_t* record = (kaa_user_sync_request_t*)data;

        record_size += record->user_attach_request->get_size(record->user_attach_request);
        record_size += record->endpoint_attach_requests->get_size(record->endpoint_attach_requests);
        record_size += record->endpoint_detach_requests->get_size(record->endpoint_detach_requests);

        return record_size;
    }

    return 0;
}

kaa_user_sync_request_t* kaa_user_sync_request_create()
{
    kaa_user_sync_request_t* record = 
            (kaa_user_sync_request_t*)KAA_CALLOC(1, sizeof(kaa_user_sync_request_t));

    if (record) {
        record->serialize = kaa_user_sync_request_serialize;
        record->get_size = kaa_user_sync_request_get_size;
        record->destroy = kaa_user_sync_request_destroy;
    }

    return record;
}


# ifndef KAA_UNION_EVENT_SEQUENCE_NUMBER_REQUEST_OR_NULL_C_
# define KAA_UNION_EVENT_SEQUENCE_NUMBER_REQUEST_OR_NULL_C_
static void kaa_union_event_sequence_number_request_or_null_destroy(void *data)
{
    if (data) {
        kaa_union_t *kaa_union = (kaa_union_t*)data;

        switch (kaa_union->type) {
        case KAA_UNION_EVENT_SEQUENCE_NUMBER_REQUEST_OR_NULL_BRANCH_0:
        {
            if (kaa_union->data) {
                kaa_event_sequence_number_request_t* record = (kaa_event_sequence_number_request_t*)kaa_union->data;
                record->destroy(record);
            }
            break;
        }
        default:
            break;
        }

        kaa_data_destroy(kaa_union);
    }
}

static size_t kaa_union_event_sequence_number_request_or_null_get_size(void *data)
{
    if (data) {
        kaa_union_t *kaa_union = (kaa_union_t*)data;
        size_t union_size = kaa_long_get_size(kaa_union->type);

        switch (kaa_union->type) {
        default:
            break;
        }

        return union_size;
    }

    return 0;
}

static void kaa_union_event_sequence_number_request_or_null_serialize(avro_writer_t writer, void *data)
{
    if (data) {
        kaa_union_t *kaa_union = (kaa_union_t*)data;
        avro_binary_encoding.write_long(writer, kaa_union->type);

        switch (kaa_union->type) {
        default:
            break;
        }
    }
}
static kaa_union_t* kaa_union_event_sequence_number_request_or_null_create()
{
    kaa_union_t* kaa_union = KAA_CALLOC(1, sizeof(kaa_union_t));

    if (kaa_union) {
        kaa_union->serialize = kaa_union_event_sequence_number_request_or_null_serialize;
        kaa_union->get_size = kaa_union_event_sequence_number_request_or_null_get_size;
        kaa_union->destroy = kaa_union_event_sequence_number_request_or_null_destroy;
    }

    return kaa_union; 
}

kaa_union_t* kaa_union_event_sequence_number_request_or_null_branch_0_create()
{
    kaa_union_t *kaa_union = kaa_union_event_sequence_number_request_or_null_create();
    if (kaa_union) {
        kaa_union->type = KAA_UNION_EVENT_SEQUENCE_NUMBER_REQUEST_OR_NULL_BRANCH_0;
    }
    return kaa_union;
}

kaa_union_t* kaa_union_event_sequence_number_request_or_null_branch_1_create()
{
    kaa_union_t *kaa_union = kaa_union_event_sequence_number_request_or_null_create();
    if (kaa_union) {
        kaa_union->type = KAA_UNION_EVENT_SEQUENCE_NUMBER_REQUEST_OR_NULL_BRANCH_1;
    }
    return kaa_union;
}
# endif // KAA_UNION_EVENT_SEQUENCE_NUMBER_REQUEST_OR_NULL_C_


# ifndef KAA_UNION_ARRAY_EVENT_LISTENERS_REQUEST_OR_NULL_C_
# define KAA_UNION_ARRAY_EVENT_LISTENERS_REQUEST_OR_NULL_C_
static void kaa_union_array_event_listeners_request_or_null_destroy(void *data)
{
    if (data) {
        kaa_union_t *kaa_union = (kaa_union_t*)data;

        switch (kaa_union->type) {
        case KAA_UNION_ARRAY_EVENT_LISTENERS_REQUEST_OR_NULL_BRANCH_0:
        {
            if (kaa_union->data) {
                            kaa_list_destroy(kaa_union->data, kaa_event_listeners_request_destroy);
                        }
            break;
        }
        default:
            break;
        }

        kaa_data_destroy(kaa_union);
    }
}

static size_t kaa_union_array_event_listeners_request_or_null_get_size(void *data)
{
    if (data) {
        kaa_union_t *kaa_union = (kaa_union_t*)data;
        size_t union_size = kaa_long_get_size(kaa_union->type);

        switch (kaa_union->type) {
        case KAA_UNION_ARRAY_EVENT_LISTENERS_REQUEST_OR_NULL_BRANCH_0:
        {
            if (kaa_union->data) {
                            union_size += kaa_array_get_size(kaa_union->data, kaa_event_listeners_request_get_size);
                        }
            break;
        }
        default:
            break;
        }

        return union_size;
    }

    return 0;
}

static void kaa_union_array_event_listeners_request_or_null_serialize(avro_writer_t writer, void *data)
{
    if (data) {
        kaa_union_t *kaa_union = (kaa_union_t*)data;
        avro_binary_encoding.write_long(writer, kaa_union->type);

        switch (kaa_union->type) {
        case KAA_UNION_ARRAY_EVENT_LISTENERS_REQUEST_OR_NULL_BRANCH_0:
        {
            if (kaa_union->data) {
                            kaa_array_serialize(writer, kaa_union->data, kaa_event_listeners_request_serialize);
                        }
            break;
        }
        default:
            break;
        }
    }
}
static kaa_union_t* kaa_union_array_event_listeners_request_or_null_create()
{
    kaa_union_t* kaa_union = KAA_CALLOC(1, sizeof(kaa_union_t));

    if (kaa_union) {
        kaa_union->serialize = kaa_union_array_event_listeners_request_or_null_serialize;
        kaa_union->get_size = kaa_union_array_event_listeners_request_or_null_get_size;
        kaa_union->destroy = kaa_union_array_event_listeners_request_or_null_destroy;
    }

    return kaa_union; 
}

kaa_union_t* kaa_union_array_event_listeners_request_or_null_branch_0_create()
{
    kaa_union_t *kaa_union = kaa_union_array_event_listeners_request_or_null_create();
    if (kaa_union) {
        kaa_union->type = KAA_UNION_ARRAY_EVENT_LISTENERS_REQUEST_OR_NULL_BRANCH_0;
    }
    return kaa_union;
}

kaa_union_t* kaa_union_array_event_listeners_request_or_null_branch_1_create()
{
    kaa_union_t *kaa_union = kaa_union_array_event_listeners_request_or_null_create();
    if (kaa_union) {
        kaa_union->type = KAA_UNION_ARRAY_EVENT_LISTENERS_REQUEST_OR_NULL_BRANCH_1;
    }
    return kaa_union;
}
# endif // KAA_UNION_ARRAY_EVENT_LISTENERS_REQUEST_OR_NULL_C_


# ifndef KAA_UNION_ARRAY_EVENT_OR_NULL_C_
# define KAA_UNION_ARRAY_EVENT_OR_NULL_C_
static void kaa_union_array_event_or_null_destroy(void *data)
{
    if (data) {
        kaa_union_t *kaa_union = (kaa_union_t*)data;

        switch (kaa_union->type) {
        case KAA_UNION_ARRAY_EVENT_OR_NULL_BRANCH_0:
        {
            if (kaa_union->data) {
                            kaa_list_destroy(kaa_union->data, kaa_event_destroy);
                        }
            break;
        }
        default:
            break;
        }

        kaa_data_destroy(kaa_union);
    }
}

static size_t kaa_union_array_event_or_null_get_size(void *data)
{
    if (data) {
        kaa_union_t *kaa_union = (kaa_union_t*)data;
        size_t union_size = kaa_long_get_size(kaa_union->type);

        switch (kaa_union->type) {
        case KAA_UNION_ARRAY_EVENT_OR_NULL_BRANCH_0:
        {
            if (kaa_union->data) {
                            union_size += kaa_array_get_size(kaa_union->data, kaa_event_get_size);
                        }
            break;
        }
        default:
            break;
        }

        return union_size;
    }

    return 0;
}

static void kaa_union_array_event_or_null_serialize(avro_writer_t writer, void *data)
{
    if (data) {
        kaa_union_t *kaa_union = (kaa_union_t*)data;
        avro_binary_encoding.write_long(writer, kaa_union->type);

        switch (kaa_union->type) {
        case KAA_UNION_ARRAY_EVENT_OR_NULL_BRANCH_0:
        {
            if (kaa_union->data) {
                            kaa_array_serialize(writer, kaa_union->data, kaa_event_serialize);
                        }
            break;
        }
        default:
            break;
        }
    }
}
static kaa_union_t* kaa_union_array_event_or_null_create()
{
    kaa_union_t* kaa_union = KAA_CALLOC(1, sizeof(kaa_union_t));

    if (kaa_union) {
        kaa_union->serialize = kaa_union_array_event_or_null_serialize;
        kaa_union->get_size = kaa_union_array_event_or_null_get_size;
        kaa_union->destroy = kaa_union_array_event_or_null_destroy;
    }

    return kaa_union; 
}

kaa_union_t* kaa_union_array_event_or_null_branch_0_create()
{
    kaa_union_t *kaa_union = kaa_union_array_event_or_null_create();
    if (kaa_union) {
        kaa_union->type = KAA_UNION_ARRAY_EVENT_OR_NULL_BRANCH_0;
    }
    return kaa_union;
}

kaa_union_t* kaa_union_array_event_or_null_branch_1_create()
{
    kaa_union_t *kaa_union = kaa_union_array_event_or_null_create();
    if (kaa_union) {
        kaa_union->type = KAA_UNION_ARRAY_EVENT_OR_NULL_BRANCH_1;
    }
    return kaa_union;
}

kaa_union_t* kaa_union_array_event_or_null_deserialize(avro_reader_t reader)
{
    kaa_union_t *kaa_union = kaa_union_array_event_or_null_create();

    if (kaa_union) {
        int64_t branch;
        avro_binary_encoding.read_long(reader, &branch);
        kaa_union->type = branch;

        switch (kaa_union->type) {
        case KAA_UNION_ARRAY_EVENT_OR_NULL_BRANCH_0:
        {
                kaa_union->data = kaa_array_deserialize(reader, (deserialize_fn)kaa_event_deserialize);
                break;
        }
        default:
            break;
        }
    }

    return kaa_union;
}
# endif // KAA_UNION_ARRAY_EVENT_OR_NULL_C_


static void kaa_event_sync_request_destroy(void* data)
{
    if (data) {
        kaa_event_sync_request_t* record = (kaa_event_sync_request_t*)data;

        if (record->event_sequence_number_request && record->event_sequence_number_request->destroy) {
            record->event_sequence_number_request->destroy(record->event_sequence_number_request);
        }
        if (record->event_listeners_requests && record->event_listeners_requests->destroy) {
            record->event_listeners_requests->destroy(record->event_listeners_requests);
        }
        if (record->events && record->events->destroy) {
            record->events->destroy(record->events);
        }
        kaa_data_destroy(record);
    }
}

static void kaa_event_sync_request_serialize(avro_writer_t writer, void* data)
{
    if (data) {
        kaa_event_sync_request_t* record = (kaa_event_sync_request_t*)data;

        record->event_sequence_number_request->serialize(writer, record->event_sequence_number_request);
        record->event_listeners_requests->serialize(writer, record->event_listeners_requests);
        record->events->serialize(writer, record->events);
    }
}

static size_t kaa_event_sync_request_get_size(void* data)
{
    if (data) {
        size_t record_size = 0;
        kaa_event_sync_request_t* record = (kaa_event_sync_request_t*)data;

        record_size += record->event_sequence_number_request->get_size(record->event_sequence_number_request);
        record_size += record->event_listeners_requests->get_size(record->event_listeners_requests);
        record_size += record->events->get_size(record->events);

        return record_size;
    }

    return 0;
}

kaa_event_sync_request_t* kaa_event_sync_request_create()
{
    kaa_event_sync_request_t* record = 
            (kaa_event_sync_request_t*)KAA_CALLOC(1, sizeof(kaa_event_sync_request_t));

    if (record) {
        record->serialize = kaa_event_sync_request_serialize;
        record->get_size = kaa_event_sync_request_get_size;
        record->destroy = kaa_event_sync_request_destroy;
    }

    return record;
}


# ifndef KAA_UNION_ARRAY_LOG_ENTRY_OR_NULL_C_
# define KAA_UNION_ARRAY_LOG_ENTRY_OR_NULL_C_
static void kaa_union_array_log_entry_or_null_destroy(void *data)
{
    if (data) {
        kaa_union_t *kaa_union = (kaa_union_t*)data;

        switch (kaa_union->type) {
        case KAA_UNION_ARRAY_LOG_ENTRY_OR_NULL_BRANCH_0:
        {
            if (kaa_union->data) {
                            kaa_list_destroy(kaa_union->data, kaa_log_entry_destroy);
                        }
            break;
        }
        default:
            break;
        }

        kaa_data_destroy(kaa_union);
    }
}

static size_t kaa_union_array_log_entry_or_null_get_size(void *data)
{
    if (data) {
        kaa_union_t *kaa_union = (kaa_union_t*)data;
        size_t union_size = kaa_long_get_size(kaa_union->type);

        switch (kaa_union->type) {
        case KAA_UNION_ARRAY_LOG_ENTRY_OR_NULL_BRANCH_0:
        {
            if (kaa_union->data) {
                            union_size += kaa_array_get_size(kaa_union->data, kaa_log_entry_get_size);
                        }
            break;
        }
        default:
            break;
        }

        return union_size;
    }

    return 0;
}

static void kaa_union_array_log_entry_or_null_serialize(avro_writer_t writer, void *data)
{
    if (data) {
        kaa_union_t *kaa_union = (kaa_union_t*)data;
        avro_binary_encoding.write_long(writer, kaa_union->type);

        switch (kaa_union->type) {
        case KAA_UNION_ARRAY_LOG_ENTRY_OR_NULL_BRANCH_0:
        {
            if (kaa_union->data) {
                            kaa_array_serialize(writer, kaa_union->data, kaa_log_entry_serialize);
                        }
            break;
        }
        default:
            break;
        }
    }
}
static kaa_union_t* kaa_union_array_log_entry_or_null_create()
{
    kaa_union_t* kaa_union = KAA_CALLOC(1, sizeof(kaa_union_t));

    if (kaa_union) {
        kaa_union->serialize = kaa_union_array_log_entry_or_null_serialize;
        kaa_union->get_size = kaa_union_array_log_entry_or_null_get_size;
        kaa_union->destroy = kaa_union_array_log_entry_or_null_destroy;
    }

    return kaa_union; 
}

kaa_union_t* kaa_union_array_log_entry_or_null_branch_0_create()
{
    kaa_union_t *kaa_union = kaa_union_array_log_entry_or_null_create();
    if (kaa_union) {
        kaa_union->type = KAA_UNION_ARRAY_LOG_ENTRY_OR_NULL_BRANCH_0;
    }
    return kaa_union;
}

kaa_union_t* kaa_union_array_log_entry_or_null_branch_1_create()
{
    kaa_union_t *kaa_union = kaa_union_array_log_entry_or_null_create();
    if (kaa_union) {
        kaa_union->type = KAA_UNION_ARRAY_LOG_ENTRY_OR_NULL_BRANCH_1;
    }
    return kaa_union;
}
# endif // KAA_UNION_ARRAY_LOG_ENTRY_OR_NULL_C_


static void kaa_log_sync_request_destroy(void* data)
{
    if (data) {
        kaa_log_sync_request_t* record = (kaa_log_sync_request_t*)data;

        if (record->request_id && record->request_id->destroy) {
            record->request_id->destroy(record->request_id);
        }
        if (record->log_entries && record->log_entries->destroy) {
            record->log_entries->destroy(record->log_entries);
        }
        kaa_data_destroy(record);
    }
}

static void kaa_log_sync_request_serialize(avro_writer_t writer, void* data)
{
    if (data) {
        kaa_log_sync_request_t* record = (kaa_log_sync_request_t*)data;

        record->request_id->serialize(writer, record->request_id);
        record->log_entries->serialize(writer, record->log_entries);
    }
}

static size_t kaa_log_sync_request_get_size(void* data)
{
    if (data) {
        size_t record_size = 0;
        kaa_log_sync_request_t* record = (kaa_log_sync_request_t*)data;

        record_size += record->request_id->get_size(record->request_id);
        record_size += record->log_entries->get_size(record->log_entries);

        return record_size;
    }

    return 0;
}

kaa_log_sync_request_t* kaa_log_sync_request_create()
{
    kaa_log_sync_request_t* record = 
            (kaa_log_sync_request_t*)KAA_CALLOC(1, sizeof(kaa_log_sync_request_t));

    if (record) {
        record->serialize = kaa_log_sync_request_serialize;
        record->get_size = kaa_log_sync_request_get_size;
        record->destroy = kaa_log_sync_request_destroy;
    }

    return record;
}




kaa_profile_sync_response_t* kaa_profile_sync_response_deserialize(avro_reader_t reader)
{
    kaa_profile_sync_response_t* record = 
            (kaa_profile_sync_response_t*)KAA_MALLOC(sizeof(kaa_profile_sync_response_t));

    if (record) {
        record->destroy = kaa_data_destroy;

        int64_t response_status_value;
        avro_binary_encoding.read_long(reader, &response_status_value);
        record->response_status = response_status_value;
    }

    return record;
}


static void kaa_configuration_sync_response_destroy(void* data)
{
    if (data) {
        kaa_configuration_sync_response_t* record = (kaa_configuration_sync_response_t*)data;

        if (record->conf_schema_body && record->conf_schema_body->destroy) {
            record->conf_schema_body->destroy(record->conf_schema_body);
        }
        if (record->conf_delta_body && record->conf_delta_body->destroy) {
            record->conf_delta_body->destroy(record->conf_delta_body);
        }
        kaa_data_destroy(record);
    }
}


kaa_configuration_sync_response_t* kaa_configuration_sync_response_deserialize(avro_reader_t reader)
{
    kaa_configuration_sync_response_t* record = 
            (kaa_configuration_sync_response_t*)KAA_MALLOC(sizeof(kaa_configuration_sync_response_t));

    if (record) {
        record->destroy = kaa_configuration_sync_response_destroy;

        avro_binary_encoding.read_int(reader, &record->app_state_seq_number);
        int64_t response_status_value;
        avro_binary_encoding.read_long(reader, &response_status_value);
        record->response_status = response_status_value;
        record->conf_schema_body = kaa_union_bytes_or_null_deserialize(reader);
        record->conf_delta_body = kaa_union_bytes_or_null_deserialize(reader);
    }

    return record;
}


# ifndef KAA_UNION_ARRAY_NOTIFICATION_OR_NULL_C_
# define KAA_UNION_ARRAY_NOTIFICATION_OR_NULL_C_
static void kaa_union_array_notification_or_null_destroy(void *data)
{
    if (data) {
        kaa_union_t *kaa_union = (kaa_union_t*)data;

        switch (kaa_union->type) {
        case KAA_UNION_ARRAY_NOTIFICATION_OR_NULL_BRANCH_0:
        {
            if (kaa_union->data) {
                            kaa_list_destroy(kaa_union->data, kaa_notification_destroy);
                        }
            break;
        }
        default:
            break;
        }

        kaa_data_destroy(kaa_union);
    }
}
static kaa_union_t* kaa_union_array_notification_or_null_create()
{
    kaa_union_t* kaa_union = KAA_CALLOC(1, sizeof(kaa_union_t));

    if (kaa_union) {
        kaa_union->destroy = kaa_union_array_notification_or_null_destroy;
    }

    return kaa_union; 
}

kaa_union_t* kaa_union_array_notification_or_null_deserialize(avro_reader_t reader)
{
    kaa_union_t *kaa_union = kaa_union_array_notification_or_null_create();

    if (kaa_union) {
        int64_t branch;
        avro_binary_encoding.read_long(reader, &branch);
        kaa_union->type = branch;

        switch (kaa_union->type) {
        case KAA_UNION_ARRAY_NOTIFICATION_OR_NULL_BRANCH_0:
        {
                kaa_union->data = kaa_array_deserialize(reader, (deserialize_fn)kaa_notification_deserialize);
                break;
        }
        default:
            break;
        }
    }

    return kaa_union;
}
# endif // KAA_UNION_ARRAY_NOTIFICATION_OR_NULL_C_


# ifndef KAA_UNION_ARRAY_TOPIC_OR_NULL_C_
# define KAA_UNION_ARRAY_TOPIC_OR_NULL_C_
static void kaa_union_array_topic_or_null_destroy(void *data)
{
    if (data) {
        kaa_union_t *kaa_union = (kaa_union_t*)data;

        switch (kaa_union->type) {
        case KAA_UNION_ARRAY_TOPIC_OR_NULL_BRANCH_0:
        {
            if (kaa_union->data) {
                            kaa_list_destroy(kaa_union->data, kaa_topic_destroy);
                        }
            break;
        }
        default:
            break;
        }

        kaa_data_destroy(kaa_union);
    }
}
static kaa_union_t* kaa_union_array_topic_or_null_create()
{
    kaa_union_t* kaa_union = KAA_CALLOC(1, sizeof(kaa_union_t));

    if (kaa_union) {
        kaa_union->destroy = kaa_union_array_topic_or_null_destroy;
    }

    return kaa_union; 
}

kaa_union_t* kaa_union_array_topic_or_null_deserialize(avro_reader_t reader)
{
    kaa_union_t *kaa_union = kaa_union_array_topic_or_null_create();

    if (kaa_union) {
        int64_t branch;
        avro_binary_encoding.read_long(reader, &branch);
        kaa_union->type = branch;

        switch (kaa_union->type) {
        case KAA_UNION_ARRAY_TOPIC_OR_NULL_BRANCH_0:
        {
                kaa_union->data = kaa_array_deserialize(reader, (deserialize_fn)kaa_topic_deserialize);
                break;
        }
        default:
            break;
        }
    }

    return kaa_union;
}
# endif // KAA_UNION_ARRAY_TOPIC_OR_NULL_C_


static void kaa_notification_sync_response_destroy(void* data)
{
    if (data) {
        kaa_notification_sync_response_t* record = (kaa_notification_sync_response_t*)data;

        if (record->notifications && record->notifications->destroy) {
            record->notifications->destroy(record->notifications);
        }
        if (record->available_topics && record->available_topics->destroy) {
            record->available_topics->destroy(record->available_topics);
        }
        kaa_data_destroy(record);
    }
}


kaa_notification_sync_response_t* kaa_notification_sync_response_deserialize(avro_reader_t reader)
{
    kaa_notification_sync_response_t* record = 
            (kaa_notification_sync_response_t*)KAA_MALLOC(sizeof(kaa_notification_sync_response_t));

    if (record) {
        record->destroy = kaa_notification_sync_response_destroy;

        avro_binary_encoding.read_int(reader, &record->app_state_seq_number);
        int64_t response_status_value;
        avro_binary_encoding.read_long(reader, &response_status_value);
        record->response_status = response_status_value;
        record->notifications = kaa_union_array_notification_or_null_deserialize(reader);
        record->available_topics = kaa_union_array_topic_or_null_deserialize(reader);
    }

    return record;
}


# ifndef KAA_UNION_USER_ATTACH_RESPONSE_OR_NULL_C_
# define KAA_UNION_USER_ATTACH_RESPONSE_OR_NULL_C_
static void kaa_union_user_attach_response_or_null_destroy(void *data)
{
    if (data) {
        kaa_union_t *kaa_union = (kaa_union_t*)data;

        switch (kaa_union->type) {
        case KAA_UNION_USER_ATTACH_RESPONSE_OR_NULL_BRANCH_0:
        {
            if (kaa_union->data) {
                kaa_user_attach_response_t* record = (kaa_user_attach_response_t*)kaa_union->data;
                record->destroy(record);
            }
            break;
        }
        default:
            break;
        }

        kaa_data_destroy(kaa_union);
    }
}
static kaa_union_t* kaa_union_user_attach_response_or_null_create()
{
    kaa_union_t* kaa_union = KAA_CALLOC(1, sizeof(kaa_union_t));

    if (kaa_union) {
        kaa_union->destroy = kaa_union_user_attach_response_or_null_destroy;
    }

    return kaa_union; 
}

kaa_union_t* kaa_union_user_attach_response_or_null_deserialize(avro_reader_t reader)
{
    kaa_union_t *kaa_union = kaa_union_user_attach_response_or_null_create();

    if (kaa_union) {
        int64_t branch;
        avro_binary_encoding.read_long(reader, &branch);
        kaa_union->type = branch;

        switch (kaa_union->type) {
        case KAA_UNION_USER_ATTACH_RESPONSE_OR_NULL_BRANCH_0:
        {
            kaa_union->data = kaa_user_attach_response_deserialize(reader);
            break;
        }
        default:
            break;
        }
    }

    return kaa_union;
}
# endif // KAA_UNION_USER_ATTACH_RESPONSE_OR_NULL_C_


# ifndef KAA_UNION_USER_ATTACH_NOTIFICATION_OR_NULL_C_
# define KAA_UNION_USER_ATTACH_NOTIFICATION_OR_NULL_C_
static void kaa_union_user_attach_notification_or_null_destroy(void *data)
{
    if (data) {
        kaa_union_t *kaa_union = (kaa_union_t*)data;

        switch (kaa_union->type) {
        case KAA_UNION_USER_ATTACH_NOTIFICATION_OR_NULL_BRANCH_0:
        {
            if (kaa_union->data) {
                kaa_user_attach_notification_t* record = (kaa_user_attach_notification_t*)kaa_union->data;
                record->destroy(record);
            }
            break;
        }
        default:
            break;
        }

        kaa_data_destroy(kaa_union);
    }
}
static kaa_union_t* kaa_union_user_attach_notification_or_null_create()
{
    kaa_union_t* kaa_union = KAA_CALLOC(1, sizeof(kaa_union_t));

    if (kaa_union) {
        kaa_union->destroy = kaa_union_user_attach_notification_or_null_destroy;
    }

    return kaa_union; 
}

kaa_union_t* kaa_union_user_attach_notification_or_null_deserialize(avro_reader_t reader)
{
    kaa_union_t *kaa_union = kaa_union_user_attach_notification_or_null_create();

    if (kaa_union) {
        int64_t branch;
        avro_binary_encoding.read_long(reader, &branch);
        kaa_union->type = branch;

        switch (kaa_union->type) {
        case KAA_UNION_USER_ATTACH_NOTIFICATION_OR_NULL_BRANCH_0:
        {
            kaa_union->data = kaa_user_attach_notification_deserialize(reader);
            break;
        }
        default:
            break;
        }
    }

    return kaa_union;
}
# endif // KAA_UNION_USER_ATTACH_NOTIFICATION_OR_NULL_C_


# ifndef KAA_UNION_USER_DETACH_NOTIFICATION_OR_NULL_C_
# define KAA_UNION_USER_DETACH_NOTIFICATION_OR_NULL_C_
static void kaa_union_user_detach_notification_or_null_destroy(void *data)
{
    if (data) {
        kaa_union_t *kaa_union = (kaa_union_t*)data;

        switch (kaa_union->type) {
        case KAA_UNION_USER_DETACH_NOTIFICATION_OR_NULL_BRANCH_0:
        {
            if (kaa_union->data) {
                kaa_user_detach_notification_t* record = (kaa_user_detach_notification_t*)kaa_union->data;
                record->destroy(record);
            }
            break;
        }
        default:
            break;
        }

        kaa_data_destroy(kaa_union);
    }
}
static kaa_union_t* kaa_union_user_detach_notification_or_null_create()
{
    kaa_union_t* kaa_union = KAA_CALLOC(1, sizeof(kaa_union_t));

    if (kaa_union) {
        kaa_union->destroy = kaa_union_user_detach_notification_or_null_destroy;
    }

    return kaa_union; 
}

kaa_union_t* kaa_union_user_detach_notification_or_null_deserialize(avro_reader_t reader)
{
    kaa_union_t *kaa_union = kaa_union_user_detach_notification_or_null_create();

    if (kaa_union) {
        int64_t branch;
        avro_binary_encoding.read_long(reader, &branch);
        kaa_union->type = branch;

        switch (kaa_union->type) {
        case KAA_UNION_USER_DETACH_NOTIFICATION_OR_NULL_BRANCH_0:
        {
            kaa_union->data = kaa_user_detach_notification_deserialize(reader);
            break;
        }
        default:
            break;
        }
    }

    return kaa_union;
}
# endif // KAA_UNION_USER_DETACH_NOTIFICATION_OR_NULL_C_


# ifndef KAA_UNION_ARRAY_ENDPOINT_ATTACH_RESPONSE_OR_NULL_C_
# define KAA_UNION_ARRAY_ENDPOINT_ATTACH_RESPONSE_OR_NULL_C_
static void kaa_union_array_endpoint_attach_response_or_null_destroy(void *data)
{
    if (data) {
        kaa_union_t *kaa_union = (kaa_union_t*)data;

        switch (kaa_union->type) {
        case KAA_UNION_ARRAY_ENDPOINT_ATTACH_RESPONSE_OR_NULL_BRANCH_0:
        {
            if (kaa_union->data) {
                            kaa_list_destroy(kaa_union->data, kaa_endpoint_attach_response_destroy);
                        }
            break;
        }
        default:
            break;
        }

        kaa_data_destroy(kaa_union);
    }
}
static kaa_union_t* kaa_union_array_endpoint_attach_response_or_null_create()
{
    kaa_union_t* kaa_union = KAA_CALLOC(1, sizeof(kaa_union_t));

    if (kaa_union) {
        kaa_union->destroy = kaa_union_array_endpoint_attach_response_or_null_destroy;
    }

    return kaa_union; 
}

kaa_union_t* kaa_union_array_endpoint_attach_response_or_null_deserialize(avro_reader_t reader)
{
    kaa_union_t *kaa_union = kaa_union_array_endpoint_attach_response_or_null_create();

    if (kaa_union) {
        int64_t branch;
        avro_binary_encoding.read_long(reader, &branch);
        kaa_union->type = branch;

        switch (kaa_union->type) {
        case KAA_UNION_ARRAY_ENDPOINT_ATTACH_RESPONSE_OR_NULL_BRANCH_0:
        {
                kaa_union->data = kaa_array_deserialize(reader, (deserialize_fn)kaa_endpoint_attach_response_deserialize);
                break;
        }
        default:
            break;
        }
    }

    return kaa_union;
}
# endif // KAA_UNION_ARRAY_ENDPOINT_ATTACH_RESPONSE_OR_NULL_C_


# ifndef KAA_UNION_ARRAY_ENDPOINT_DETACH_RESPONSE_OR_NULL_C_
# define KAA_UNION_ARRAY_ENDPOINT_DETACH_RESPONSE_OR_NULL_C_
static void kaa_union_array_endpoint_detach_response_or_null_destroy(void *data)
{
    if (data) {
        kaa_union_t *kaa_union = (kaa_union_t*)data;

        switch (kaa_union->type) {
        case KAA_UNION_ARRAY_ENDPOINT_DETACH_RESPONSE_OR_NULL_BRANCH_0:
        {
            if (kaa_union->data) {
                            kaa_list_destroy(kaa_union->data, kaa_endpoint_detach_response_destroy);
                        }
            break;
        }
        default:
            break;
        }

        kaa_data_destroy(kaa_union);
    }
}
static kaa_union_t* kaa_union_array_endpoint_detach_response_or_null_create()
{
    kaa_union_t* kaa_union = KAA_CALLOC(1, sizeof(kaa_union_t));

    if (kaa_union) {
        kaa_union->destroy = kaa_union_array_endpoint_detach_response_or_null_destroy;
    }

    return kaa_union; 
}

kaa_union_t* kaa_union_array_endpoint_detach_response_or_null_deserialize(avro_reader_t reader)
{
    kaa_union_t *kaa_union = kaa_union_array_endpoint_detach_response_or_null_create();

    if (kaa_union) {
        int64_t branch;
        avro_binary_encoding.read_long(reader, &branch);
        kaa_union->type = branch;

        switch (kaa_union->type) {
        case KAA_UNION_ARRAY_ENDPOINT_DETACH_RESPONSE_OR_NULL_BRANCH_0:
        {
                kaa_union->data = kaa_array_deserialize(reader, (deserialize_fn)kaa_endpoint_detach_response_deserialize);
                break;
        }
        default:
            break;
        }
    }

    return kaa_union;
}
# endif // KAA_UNION_ARRAY_ENDPOINT_DETACH_RESPONSE_OR_NULL_C_


static void kaa_user_sync_response_destroy(void* data)
{
    if (data) {
        kaa_user_sync_response_t* record = (kaa_user_sync_response_t*)data;

        if (record->user_attach_response && record->user_attach_response->destroy) {
            record->user_attach_response->destroy(record->user_attach_response);
        }
        if (record->user_attach_notification && record->user_attach_notification->destroy) {
            record->user_attach_notification->destroy(record->user_attach_notification);
        }
        if (record->user_detach_notification && record->user_detach_notification->destroy) {
            record->user_detach_notification->destroy(record->user_detach_notification);
        }
        if (record->endpoint_attach_responses && record->endpoint_attach_responses->destroy) {
            record->endpoint_attach_responses->destroy(record->endpoint_attach_responses);
        }
        if (record->endpoint_detach_responses && record->endpoint_detach_responses->destroy) {
            record->endpoint_detach_responses->destroy(record->endpoint_detach_responses);
        }
        kaa_data_destroy(record);
    }
}


kaa_user_sync_response_t* kaa_user_sync_response_deserialize(avro_reader_t reader)
{
    kaa_user_sync_response_t* record = 
            (kaa_user_sync_response_t*)KAA_MALLOC(sizeof(kaa_user_sync_response_t));

    if (record) {
        record->destroy = kaa_user_sync_response_destroy;

        record->user_attach_response = kaa_union_user_attach_response_or_null_deserialize(reader);
        record->user_attach_notification = kaa_union_user_attach_notification_or_null_deserialize(reader);
        record->user_detach_notification = kaa_union_user_detach_notification_or_null_deserialize(reader);
        record->endpoint_attach_responses = kaa_union_array_endpoint_attach_response_or_null_deserialize(reader);
        record->endpoint_detach_responses = kaa_union_array_endpoint_detach_response_or_null_deserialize(reader);
    }

    return record;
}


# ifndef KAA_UNION_EVENT_SEQUENCE_NUMBER_RESPONSE_OR_NULL_C_
# define KAA_UNION_EVENT_SEQUENCE_NUMBER_RESPONSE_OR_NULL_C_
static void kaa_union_event_sequence_number_response_or_null_destroy(void *data)
{
    if (data) {
        kaa_union_t *kaa_union = (kaa_union_t*)data;

        switch (kaa_union->type) {
        case KAA_UNION_EVENT_SEQUENCE_NUMBER_RESPONSE_OR_NULL_BRANCH_0:
        {
            if (kaa_union->data) {
                kaa_event_sequence_number_response_t* record = (kaa_event_sequence_number_response_t*)kaa_union->data;
                record->destroy(record);
            }
            break;
        }
        default:
            break;
        }

        kaa_data_destroy(kaa_union);
    }
}
static kaa_union_t* kaa_union_event_sequence_number_response_or_null_create()
{
    kaa_union_t* kaa_union = KAA_CALLOC(1, sizeof(kaa_union_t));

    if (kaa_union) {
        kaa_union->destroy = kaa_union_event_sequence_number_response_or_null_destroy;
    }

    return kaa_union; 
}

kaa_union_t* kaa_union_event_sequence_number_response_or_null_deserialize(avro_reader_t reader)
{
    kaa_union_t *kaa_union = kaa_union_event_sequence_number_response_or_null_create();

    if (kaa_union) {
        int64_t branch;
        avro_binary_encoding.read_long(reader, &branch);
        kaa_union->type = branch;

        switch (kaa_union->type) {
        case KAA_UNION_EVENT_SEQUENCE_NUMBER_RESPONSE_OR_NULL_BRANCH_0:
        {
            kaa_union->data = kaa_event_sequence_number_response_deserialize(reader);
            break;
        }
        default:
            break;
        }
    }

    return kaa_union;
}
# endif // KAA_UNION_EVENT_SEQUENCE_NUMBER_RESPONSE_OR_NULL_C_


# ifndef KAA_UNION_ARRAY_EVENT_LISTENERS_RESPONSE_OR_NULL_C_
# define KAA_UNION_ARRAY_EVENT_LISTENERS_RESPONSE_OR_NULL_C_
static void kaa_union_array_event_listeners_response_or_null_destroy(void *data)
{
    if (data) {
        kaa_union_t *kaa_union = (kaa_union_t*)data;

        switch (kaa_union->type) {
        case KAA_UNION_ARRAY_EVENT_LISTENERS_RESPONSE_OR_NULL_BRANCH_0:
        {
            if (kaa_union->data) {
                            kaa_list_destroy(kaa_union->data, kaa_event_listeners_response_destroy);
                        }
            break;
        }
        default:
            break;
        }

        kaa_data_destroy(kaa_union);
    }
}
static kaa_union_t* kaa_union_array_event_listeners_response_or_null_create()
{
    kaa_union_t* kaa_union = KAA_CALLOC(1, sizeof(kaa_union_t));

    if (kaa_union) {
        kaa_union->destroy = kaa_union_array_event_listeners_response_or_null_destroy;
    }

    return kaa_union; 
}

kaa_union_t* kaa_union_array_event_listeners_response_or_null_deserialize(avro_reader_t reader)
{
    kaa_union_t *kaa_union = kaa_union_array_event_listeners_response_or_null_create();

    if (kaa_union) {
        int64_t branch;
        avro_binary_encoding.read_long(reader, &branch);
        kaa_union->type = branch;

        switch (kaa_union->type) {
        case KAA_UNION_ARRAY_EVENT_LISTENERS_RESPONSE_OR_NULL_BRANCH_0:
        {
                kaa_union->data = kaa_array_deserialize(reader, (deserialize_fn)kaa_event_listeners_response_deserialize);
                break;
        }
        default:
            break;
        }
    }

    return kaa_union;
}
# endif // KAA_UNION_ARRAY_EVENT_LISTENERS_RESPONSE_OR_NULL_C_


static void kaa_event_sync_response_destroy(void* data)
{
    if (data) {
        kaa_event_sync_response_t* record = (kaa_event_sync_response_t*)data;

        if (record->event_sequence_number_response && record->event_sequence_number_response->destroy) {
            record->event_sequence_number_response->destroy(record->event_sequence_number_response);
        }
        if (record->event_listeners_responses && record->event_listeners_responses->destroy) {
            record->event_listeners_responses->destroy(record->event_listeners_responses);
        }
        if (record->events && record->events->destroy) {
            record->events->destroy(record->events);
        }
        kaa_data_destroy(record);
    }
}


kaa_event_sync_response_t* kaa_event_sync_response_deserialize(avro_reader_t reader)
{
    kaa_event_sync_response_t* record = 
            (kaa_event_sync_response_t*)KAA_MALLOC(sizeof(kaa_event_sync_response_t));

    if (record) {
        record->destroy = kaa_event_sync_response_destroy;

        record->event_sequence_number_response = kaa_union_event_sequence_number_response_or_null_deserialize(reader);
        record->event_listeners_responses = kaa_union_array_event_listeners_response_or_null_deserialize(reader);
        record->events = kaa_union_array_event_or_null_deserialize(reader);
    }

    return record;
}


static void kaa_log_sync_response_destroy(void* data)
{
    if (data) {
        kaa_log_sync_response_t* record = (kaa_log_sync_response_t*)data;

        kaa_string_destroy(record->request_id);
        kaa_data_destroy(record);
    }
}


kaa_log_sync_response_t* kaa_log_sync_response_deserialize(avro_reader_t reader)
{
    kaa_log_sync_response_t* record = 
            (kaa_log_sync_response_t*)KAA_MALLOC(sizeof(kaa_log_sync_response_t));

    if (record) {
        record->destroy = kaa_log_sync_response_destroy;

        record->request_id = kaa_string_deserialize(reader);
        int64_t result_value;
        avro_binary_encoding.read_long(reader, &result_value);
        record->result = result_value;
    }

    return record;
}


static void kaa_redirect_sync_response_destroy(void* data)
{
    if (data) {
        kaa_redirect_sync_response_t* record = (kaa_redirect_sync_response_t*)data;

        kaa_string_destroy(record->dns_name);
        kaa_data_destroy(record);
    }
}


kaa_redirect_sync_response_t* kaa_redirect_sync_response_deserialize(avro_reader_t reader)
{
    kaa_redirect_sync_response_t* record = 
            (kaa_redirect_sync_response_t*)KAA_MALLOC(sizeof(kaa_redirect_sync_response_t));

    if (record) {
        record->destroy = kaa_redirect_sync_response_destroy;

        record->dns_name = kaa_string_deserialize(reader);
    }

    return record;
}


# ifndef KAA_UNION_SYNC_REQUEST_META_DATA_OR_NULL_C_
# define KAA_UNION_SYNC_REQUEST_META_DATA_OR_NULL_C_
static void kaa_union_sync_request_meta_data_or_null_destroy(void *data)
{
    if (data) {
        kaa_union_t *kaa_union = (kaa_union_t*)data;

        switch (kaa_union->type) {
        case KAA_UNION_SYNC_REQUEST_META_DATA_OR_NULL_BRANCH_0:
        {
            if (kaa_union->data) {
                kaa_sync_request_meta_data_t* record = (kaa_sync_request_meta_data_t*)kaa_union->data;
                record->destroy(record);
            }
            break;
        }
        default:
            break;
        }

        kaa_data_destroy(kaa_union);
    }
}

static size_t kaa_union_sync_request_meta_data_or_null_get_size(void *data)
{
    if (data) {
        kaa_union_t *kaa_union = (kaa_union_t*)data;
        size_t union_size = kaa_long_get_size(kaa_union->type);

        switch (kaa_union->type) {
        case KAA_UNION_SYNC_REQUEST_META_DATA_OR_NULL_BRANCH_0:
        {
            if (kaa_union->data) {
                kaa_sync_request_meta_data_t* record = (kaa_sync_request_meta_data_t*)kaa_union->data;
                union_size += record->get_size(record);
            }
            break;
        }
        default:
            break;
        }

        return union_size;
    }

    return 0;
}

static void kaa_union_sync_request_meta_data_or_null_serialize(avro_writer_t writer, void *data)
{
    if (data) {
        kaa_union_t *kaa_union = (kaa_union_t*)data;
        avro_binary_encoding.write_long(writer, kaa_union->type);

        switch (kaa_union->type) {
        case KAA_UNION_SYNC_REQUEST_META_DATA_OR_NULL_BRANCH_0:
        {
            if (kaa_union->data) {
                kaa_sync_request_meta_data_t* record = (kaa_sync_request_meta_data_t*)kaa_union->data;
                record->serialize(writer, record);
            }
            break;
        }
        default:
            break;
        }
    }
}
static kaa_union_t* kaa_union_sync_request_meta_data_or_null_create()
{
    kaa_union_t* kaa_union = KAA_CALLOC(1, sizeof(kaa_union_t));

    if (kaa_union) {
        kaa_union->serialize = kaa_union_sync_request_meta_data_or_null_serialize;
        kaa_union->get_size = kaa_union_sync_request_meta_data_or_null_get_size;
        kaa_union->destroy = kaa_union_sync_request_meta_data_or_null_destroy;
    }

    return kaa_union; 
}

kaa_union_t* kaa_union_sync_request_meta_data_or_null_branch_0_create()
{
    kaa_union_t *kaa_union = kaa_union_sync_request_meta_data_or_null_create();
    if (kaa_union) {
        kaa_union->type = KAA_UNION_SYNC_REQUEST_META_DATA_OR_NULL_BRANCH_0;
    }
    return kaa_union;
}

kaa_union_t* kaa_union_sync_request_meta_data_or_null_branch_1_create()
{
    kaa_union_t *kaa_union = kaa_union_sync_request_meta_data_or_null_create();
    if (kaa_union) {
        kaa_union->type = KAA_UNION_SYNC_REQUEST_META_DATA_OR_NULL_BRANCH_1;
    }
    return kaa_union;
}
# endif // KAA_UNION_SYNC_REQUEST_META_DATA_OR_NULL_C_


# ifndef KAA_UNION_PROFILE_SYNC_REQUEST_OR_NULL_C_
# define KAA_UNION_PROFILE_SYNC_REQUEST_OR_NULL_C_
static void kaa_union_profile_sync_request_or_null_destroy(void *data)
{
    if (data) {
        kaa_union_t *kaa_union = (kaa_union_t*)data;

        switch (kaa_union->type) {
        case KAA_UNION_PROFILE_SYNC_REQUEST_OR_NULL_BRANCH_0:
        {
            if (kaa_union->data) {
                kaa_profile_sync_request_t* record = (kaa_profile_sync_request_t*)kaa_union->data;
                record->destroy(record);
            }
            break;
        }
        default:
            break;
        }

        kaa_data_destroy(kaa_union);
    }
}

static size_t kaa_union_profile_sync_request_or_null_get_size(void *data)
{
    if (data) {
        kaa_union_t *kaa_union = (kaa_union_t*)data;
        size_t union_size = kaa_long_get_size(kaa_union->type);

        switch (kaa_union->type) {
        case KAA_UNION_PROFILE_SYNC_REQUEST_OR_NULL_BRANCH_0:
        {
            if (kaa_union->data) {
                kaa_profile_sync_request_t* record = (kaa_profile_sync_request_t*)kaa_union->data;
                union_size += record->get_size(record);
            }
            break;
        }
        default:
            break;
        }

        return union_size;
    }

    return 0;
}

static void kaa_union_profile_sync_request_or_null_serialize(avro_writer_t writer, void *data)
{
    if (data) {
        kaa_union_t *kaa_union = (kaa_union_t*)data;
        avro_binary_encoding.write_long(writer, kaa_union->type);

        switch (kaa_union->type) {
        case KAA_UNION_PROFILE_SYNC_REQUEST_OR_NULL_BRANCH_0:
        {
            if (kaa_union->data) {
                kaa_profile_sync_request_t* record = (kaa_profile_sync_request_t*)kaa_union->data;
                record->serialize(writer, record);
            }
            break;
        }
        default:
            break;
        }
    }
}
static kaa_union_t* kaa_union_profile_sync_request_or_null_create()
{
    kaa_union_t* kaa_union = KAA_CALLOC(1, sizeof(kaa_union_t));

    if (kaa_union) {
        kaa_union->serialize = kaa_union_profile_sync_request_or_null_serialize;
        kaa_union->get_size = kaa_union_profile_sync_request_or_null_get_size;
        kaa_union->destroy = kaa_union_profile_sync_request_or_null_destroy;
    }

    return kaa_union; 
}

kaa_union_t* kaa_union_profile_sync_request_or_null_branch_0_create()
{
    kaa_union_t *kaa_union = kaa_union_profile_sync_request_or_null_create();
    if (kaa_union) {
        kaa_union->type = KAA_UNION_PROFILE_SYNC_REQUEST_OR_NULL_BRANCH_0;
    }
    return kaa_union;
}

kaa_union_t* kaa_union_profile_sync_request_or_null_branch_1_create()
{
    kaa_union_t *kaa_union = kaa_union_profile_sync_request_or_null_create();
    if (kaa_union) {
        kaa_union->type = KAA_UNION_PROFILE_SYNC_REQUEST_OR_NULL_BRANCH_1;
    }
    return kaa_union;
}
# endif // KAA_UNION_PROFILE_SYNC_REQUEST_OR_NULL_C_


# ifndef KAA_UNION_CONFIGURATION_SYNC_REQUEST_OR_NULL_C_
# define KAA_UNION_CONFIGURATION_SYNC_REQUEST_OR_NULL_C_
static void kaa_union_configuration_sync_request_or_null_destroy(void *data)
{
    if (data) {
        kaa_union_t *kaa_union = (kaa_union_t*)data;

        switch (kaa_union->type) {
        case KAA_UNION_CONFIGURATION_SYNC_REQUEST_OR_NULL_BRANCH_0:
        {
            if (kaa_union->data) {
                kaa_configuration_sync_request_t* record = (kaa_configuration_sync_request_t*)kaa_union->data;
                record->destroy(record);
            }
            break;
        }
        default:
            break;
        }

        kaa_data_destroy(kaa_union);
    }
}

static size_t kaa_union_configuration_sync_request_or_null_get_size(void *data)
{
    if (data) {
        kaa_union_t *kaa_union = (kaa_union_t*)data;
        size_t union_size = kaa_long_get_size(kaa_union->type);

        switch (kaa_union->type) {
        case KAA_UNION_CONFIGURATION_SYNC_REQUEST_OR_NULL_BRANCH_0:
        {
            if (kaa_union->data) {
                kaa_configuration_sync_request_t* record = (kaa_configuration_sync_request_t*)kaa_union->data;
                union_size += record->get_size(record);
            }
            break;
        }
        default:
            break;
        }

        return union_size;
    }

    return 0;
}

static void kaa_union_configuration_sync_request_or_null_serialize(avro_writer_t writer, void *data)
{
    if (data) {
        kaa_union_t *kaa_union = (kaa_union_t*)data;
        avro_binary_encoding.write_long(writer, kaa_union->type);

        switch (kaa_union->type) {
        case KAA_UNION_CONFIGURATION_SYNC_REQUEST_OR_NULL_BRANCH_0:
        {
            if (kaa_union->data) {
                kaa_configuration_sync_request_t* record = (kaa_configuration_sync_request_t*)kaa_union->data;
                record->serialize(writer, record);
            }
            break;
        }
        default:
            break;
        }
    }
}
static kaa_union_t* kaa_union_configuration_sync_request_or_null_create()
{
    kaa_union_t* kaa_union = KAA_CALLOC(1, sizeof(kaa_union_t));

    if (kaa_union) {
        kaa_union->serialize = kaa_union_configuration_sync_request_or_null_serialize;
        kaa_union->get_size = kaa_union_configuration_sync_request_or_null_get_size;
        kaa_union->destroy = kaa_union_configuration_sync_request_or_null_destroy;
    }

    return kaa_union; 
}

kaa_union_t* kaa_union_configuration_sync_request_or_null_branch_0_create()
{
    kaa_union_t *kaa_union = kaa_union_configuration_sync_request_or_null_create();
    if (kaa_union) {
        kaa_union->type = KAA_UNION_CONFIGURATION_SYNC_REQUEST_OR_NULL_BRANCH_0;
    }
    return kaa_union;
}

kaa_union_t* kaa_union_configuration_sync_request_or_null_branch_1_create()
{
    kaa_union_t *kaa_union = kaa_union_configuration_sync_request_or_null_create();
    if (kaa_union) {
        kaa_union->type = KAA_UNION_CONFIGURATION_SYNC_REQUEST_OR_NULL_BRANCH_1;
    }
    return kaa_union;
}
# endif // KAA_UNION_CONFIGURATION_SYNC_REQUEST_OR_NULL_C_


# ifndef KAA_UNION_NOTIFICATION_SYNC_REQUEST_OR_NULL_C_
# define KAA_UNION_NOTIFICATION_SYNC_REQUEST_OR_NULL_C_
static void kaa_union_notification_sync_request_or_null_destroy(void *data)
{
    if (data) {
        kaa_union_t *kaa_union = (kaa_union_t*)data;

        switch (kaa_union->type) {
        case KAA_UNION_NOTIFICATION_SYNC_REQUEST_OR_NULL_BRANCH_0:
        {
            if (kaa_union->data) {
                kaa_notification_sync_request_t* record = (kaa_notification_sync_request_t*)kaa_union->data;
                record->destroy(record);
            }
            break;
        }
        default:
            break;
        }

        kaa_data_destroy(kaa_union);
    }
}

static size_t kaa_union_notification_sync_request_or_null_get_size(void *data)
{
    if (data) {
        kaa_union_t *kaa_union = (kaa_union_t*)data;
        size_t union_size = kaa_long_get_size(kaa_union->type);

        switch (kaa_union->type) {
        case KAA_UNION_NOTIFICATION_SYNC_REQUEST_OR_NULL_BRANCH_0:
        {
            if (kaa_union->data) {
                kaa_notification_sync_request_t* record = (kaa_notification_sync_request_t*)kaa_union->data;
                union_size += record->get_size(record);
            }
            break;
        }
        default:
            break;
        }

        return union_size;
    }

    return 0;
}

static void kaa_union_notification_sync_request_or_null_serialize(avro_writer_t writer, void *data)
{
    if (data) {
        kaa_union_t *kaa_union = (kaa_union_t*)data;
        avro_binary_encoding.write_long(writer, kaa_union->type);

        switch (kaa_union->type) {
        case KAA_UNION_NOTIFICATION_SYNC_REQUEST_OR_NULL_BRANCH_0:
        {
            if (kaa_union->data) {
                kaa_notification_sync_request_t* record = (kaa_notification_sync_request_t*)kaa_union->data;
                record->serialize(writer, record);
            }
            break;
        }
        default:
            break;
        }
    }
}
static kaa_union_t* kaa_union_notification_sync_request_or_null_create()
{
    kaa_union_t* kaa_union = KAA_CALLOC(1, sizeof(kaa_union_t));

    if (kaa_union) {
        kaa_union->serialize = kaa_union_notification_sync_request_or_null_serialize;
        kaa_union->get_size = kaa_union_notification_sync_request_or_null_get_size;
        kaa_union->destroy = kaa_union_notification_sync_request_or_null_destroy;
    }

    return kaa_union; 
}

kaa_union_t* kaa_union_notification_sync_request_or_null_branch_0_create()
{
    kaa_union_t *kaa_union = kaa_union_notification_sync_request_or_null_create();
    if (kaa_union) {
        kaa_union->type = KAA_UNION_NOTIFICATION_SYNC_REQUEST_OR_NULL_BRANCH_0;
    }
    return kaa_union;
}

kaa_union_t* kaa_union_notification_sync_request_or_null_branch_1_create()
{
    kaa_union_t *kaa_union = kaa_union_notification_sync_request_or_null_create();
    if (kaa_union) {
        kaa_union->type = KAA_UNION_NOTIFICATION_SYNC_REQUEST_OR_NULL_BRANCH_1;
    }
    return kaa_union;
}
# endif // KAA_UNION_NOTIFICATION_SYNC_REQUEST_OR_NULL_C_


# ifndef KAA_UNION_USER_SYNC_REQUEST_OR_NULL_C_
# define KAA_UNION_USER_SYNC_REQUEST_OR_NULL_C_
static void kaa_union_user_sync_request_or_null_destroy(void *data)
{
    if (data) {
        kaa_union_t *kaa_union = (kaa_union_t*)data;

        switch (kaa_union->type) {
        case KAA_UNION_USER_SYNC_REQUEST_OR_NULL_BRANCH_0:
        {
            if (kaa_union->data) {
                kaa_user_sync_request_t* record = (kaa_user_sync_request_t*)kaa_union->data;
                record->destroy(record);
            }
            break;
        }
        default:
            break;
        }

        kaa_data_destroy(kaa_union);
    }
}

static size_t kaa_union_user_sync_request_or_null_get_size(void *data)
{
    if (data) {
        kaa_union_t *kaa_union = (kaa_union_t*)data;
        size_t union_size = kaa_long_get_size(kaa_union->type);

        switch (kaa_union->type) {
        case KAA_UNION_USER_SYNC_REQUEST_OR_NULL_BRANCH_0:
        {
            if (kaa_union->data) {
                kaa_user_sync_request_t* record = (kaa_user_sync_request_t*)kaa_union->data;
                union_size += record->get_size(record);
            }
            break;
        }
        default:
            break;
        }

        return union_size;
    }

    return 0;
}

static void kaa_union_user_sync_request_or_null_serialize(avro_writer_t writer, void *data)
{
    if (data) {
        kaa_union_t *kaa_union = (kaa_union_t*)data;
        avro_binary_encoding.write_long(writer, kaa_union->type);

        switch (kaa_union->type) {
        case KAA_UNION_USER_SYNC_REQUEST_OR_NULL_BRANCH_0:
        {
            if (kaa_union->data) {
                kaa_user_sync_request_t* record = (kaa_user_sync_request_t*)kaa_union->data;
                record->serialize(writer, record);
            }
            break;
        }
        default:
            break;
        }
    }
}
static kaa_union_t* kaa_union_user_sync_request_or_null_create()
{
    kaa_union_t* kaa_union = KAA_CALLOC(1, sizeof(kaa_union_t));

    if (kaa_union) {
        kaa_union->serialize = kaa_union_user_sync_request_or_null_serialize;
        kaa_union->get_size = kaa_union_user_sync_request_or_null_get_size;
        kaa_union->destroy = kaa_union_user_sync_request_or_null_destroy;
    }

    return kaa_union; 
}

kaa_union_t* kaa_union_user_sync_request_or_null_branch_0_create()
{
    kaa_union_t *kaa_union = kaa_union_user_sync_request_or_null_create();
    if (kaa_union) {
        kaa_union->type = KAA_UNION_USER_SYNC_REQUEST_OR_NULL_BRANCH_0;
    }
    return kaa_union;
}

kaa_union_t* kaa_union_user_sync_request_or_null_branch_1_create()
{
    kaa_union_t *kaa_union = kaa_union_user_sync_request_or_null_create();
    if (kaa_union) {
        kaa_union->type = KAA_UNION_USER_SYNC_REQUEST_OR_NULL_BRANCH_1;
    }
    return kaa_union;
}
# endif // KAA_UNION_USER_SYNC_REQUEST_OR_NULL_C_


# ifndef KAA_UNION_EVENT_SYNC_REQUEST_OR_NULL_C_
# define KAA_UNION_EVENT_SYNC_REQUEST_OR_NULL_C_
static void kaa_union_event_sync_request_or_null_destroy(void *data)
{
    if (data) {
        kaa_union_t *kaa_union = (kaa_union_t*)data;

        switch (kaa_union->type) {
        case KAA_UNION_EVENT_SYNC_REQUEST_OR_NULL_BRANCH_0:
        {
            if (kaa_union->data) {
                kaa_event_sync_request_t* record = (kaa_event_sync_request_t*)kaa_union->data;
                record->destroy(record);
            }
            break;
        }
        default:
            break;
        }

        kaa_data_destroy(kaa_union);
    }
}

static size_t kaa_union_event_sync_request_or_null_get_size(void *data)
{
    if (data) {
        kaa_union_t *kaa_union = (kaa_union_t*)data;
        size_t union_size = kaa_long_get_size(kaa_union->type);

        switch (kaa_union->type) {
        case KAA_UNION_EVENT_SYNC_REQUEST_OR_NULL_BRANCH_0:
        {
            if (kaa_union->data) {
                kaa_event_sync_request_t* record = (kaa_event_sync_request_t*)kaa_union->data;
                union_size += record->get_size(record);
            }
            break;
        }
        default:
            break;
        }

        return union_size;
    }

    return 0;
}

static void kaa_union_event_sync_request_or_null_serialize(avro_writer_t writer, void *data)
{
    if (data) {
        kaa_union_t *kaa_union = (kaa_union_t*)data;
        avro_binary_encoding.write_long(writer, kaa_union->type);

        switch (kaa_union->type) {
        case KAA_UNION_EVENT_SYNC_REQUEST_OR_NULL_BRANCH_0:
        {
            if (kaa_union->data) {
                kaa_event_sync_request_t* record = (kaa_event_sync_request_t*)kaa_union->data;
                record->serialize(writer, record);
            }
            break;
        }
        default:
            break;
        }
    }
}
static kaa_union_t* kaa_union_event_sync_request_or_null_create()
{
    kaa_union_t* kaa_union = KAA_CALLOC(1, sizeof(kaa_union_t));

    if (kaa_union) {
        kaa_union->serialize = kaa_union_event_sync_request_or_null_serialize;
        kaa_union->get_size = kaa_union_event_sync_request_or_null_get_size;
        kaa_union->destroy = kaa_union_event_sync_request_or_null_destroy;
    }

    return kaa_union; 
}

kaa_union_t* kaa_union_event_sync_request_or_null_branch_0_create()
{
    kaa_union_t *kaa_union = kaa_union_event_sync_request_or_null_create();
    if (kaa_union) {
        kaa_union->type = KAA_UNION_EVENT_SYNC_REQUEST_OR_NULL_BRANCH_0;
    }
    return kaa_union;
}

kaa_union_t* kaa_union_event_sync_request_or_null_branch_1_create()
{
    kaa_union_t *kaa_union = kaa_union_event_sync_request_or_null_create();
    if (kaa_union) {
        kaa_union->type = KAA_UNION_EVENT_SYNC_REQUEST_OR_NULL_BRANCH_1;
    }
    return kaa_union;
}
# endif // KAA_UNION_EVENT_SYNC_REQUEST_OR_NULL_C_


# ifndef KAA_UNION_LOG_SYNC_REQUEST_OR_NULL_C_
# define KAA_UNION_LOG_SYNC_REQUEST_OR_NULL_C_
static void kaa_union_log_sync_request_or_null_destroy(void *data)
{
    if (data) {
        kaa_union_t *kaa_union = (kaa_union_t*)data;

        switch (kaa_union->type) {
        case KAA_UNION_LOG_SYNC_REQUEST_OR_NULL_BRANCH_0:
        {
            if (kaa_union->data) {
                kaa_log_sync_request_t* record = (kaa_log_sync_request_t*)kaa_union->data;
                record->destroy(record);
            }
            break;
        }
        default:
            break;
        }

        kaa_data_destroy(kaa_union);
    }
}

static size_t kaa_union_log_sync_request_or_null_get_size(void *data)
{
    if (data) {
        kaa_union_t *kaa_union = (kaa_union_t*)data;
        size_t union_size = kaa_long_get_size(kaa_union->type);

        switch (kaa_union->type) {
        case KAA_UNION_LOG_SYNC_REQUEST_OR_NULL_BRANCH_0:
        {
            if (kaa_union->data) {
                kaa_log_sync_request_t* record = (kaa_log_sync_request_t*)kaa_union->data;
                union_size += record->get_size(record);
            }
            break;
        }
        default:
            break;
        }

        return union_size;
    }

    return 0;
}

static void kaa_union_log_sync_request_or_null_serialize(avro_writer_t writer, void *data)
{
    if (data) {
        kaa_union_t *kaa_union = (kaa_union_t*)data;
        avro_binary_encoding.write_long(writer, kaa_union->type);

        switch (kaa_union->type) {
        case KAA_UNION_LOG_SYNC_REQUEST_OR_NULL_BRANCH_0:
        {
            if (kaa_union->data) {
                kaa_log_sync_request_t* record = (kaa_log_sync_request_t*)kaa_union->data;
                record->serialize(writer, record);
            }
            break;
        }
        default:
            break;
        }
    }
}
static kaa_union_t* kaa_union_log_sync_request_or_null_create()
{
    kaa_union_t* kaa_union = KAA_CALLOC(1, sizeof(kaa_union_t));

    if (kaa_union) {
        kaa_union->serialize = kaa_union_log_sync_request_or_null_serialize;
        kaa_union->get_size = kaa_union_log_sync_request_or_null_get_size;
        kaa_union->destroy = kaa_union_log_sync_request_or_null_destroy;
    }

    return kaa_union; 
}

kaa_union_t* kaa_union_log_sync_request_or_null_branch_0_create()
{
    kaa_union_t *kaa_union = kaa_union_log_sync_request_or_null_create();
    if (kaa_union) {
        kaa_union->type = KAA_UNION_LOG_SYNC_REQUEST_OR_NULL_BRANCH_0;
    }
    return kaa_union;
}

kaa_union_t* kaa_union_log_sync_request_or_null_branch_1_create()
{
    kaa_union_t *kaa_union = kaa_union_log_sync_request_or_null_create();
    if (kaa_union) {
        kaa_union->type = KAA_UNION_LOG_SYNC_REQUEST_OR_NULL_BRANCH_1;
    }
    return kaa_union;
}
# endif // KAA_UNION_LOG_SYNC_REQUEST_OR_NULL_C_


static void kaa_sync_request_destroy(void* data)
{
    if (data) {
        kaa_sync_request_t* record = (kaa_sync_request_t*)data;

        if (record->request_id && record->request_id->destroy) {
            record->request_id->destroy(record->request_id);
        }
        if (record->sync_request_meta_data && record->sync_request_meta_data->destroy) {
            record->sync_request_meta_data->destroy(record->sync_request_meta_data);
        }
        if (record->profile_sync_request && record->profile_sync_request->destroy) {
            record->profile_sync_request->destroy(record->profile_sync_request);
        }
        if (record->configuration_sync_request && record->configuration_sync_request->destroy) {
            record->configuration_sync_request->destroy(record->configuration_sync_request);
        }
        if (record->notification_sync_request && record->notification_sync_request->destroy) {
            record->notification_sync_request->destroy(record->notification_sync_request);
        }
        if (record->user_sync_request && record->user_sync_request->destroy) {
            record->user_sync_request->destroy(record->user_sync_request);
        }
        if (record->event_sync_request && record->event_sync_request->destroy) {
            record->event_sync_request->destroy(record->event_sync_request);
        }
        if (record->log_sync_request && record->log_sync_request->destroy) {
            record->log_sync_request->destroy(record->log_sync_request);
        }
        kaa_data_destroy(record);
    }
}

static void kaa_sync_request_serialize(avro_writer_t writer, void* data)
{
    if (data) {
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
}

static size_t kaa_sync_request_get_size(void* data)
{
    if (data) {
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

    return 0;
}

kaa_sync_request_t* kaa_sync_request_create()
{
    kaa_sync_request_t* record = 
            (kaa_sync_request_t*)KAA_CALLOC(1, sizeof(kaa_sync_request_t));

    if (record) {
        record->serialize = kaa_sync_request_serialize;
        record->get_size = kaa_sync_request_get_size;
        record->destroy = kaa_sync_request_destroy;
    }

    return record;
}


# ifndef KAA_UNION_PROFILE_SYNC_RESPONSE_OR_NULL_C_
# define KAA_UNION_PROFILE_SYNC_RESPONSE_OR_NULL_C_
static void kaa_union_profile_sync_response_or_null_destroy(void *data)
{
    if (data) {
        kaa_union_t *kaa_union = (kaa_union_t*)data;

        switch (kaa_union->type) {
        case KAA_UNION_PROFILE_SYNC_RESPONSE_OR_NULL_BRANCH_0:
        {
            if (kaa_union->data) {
                kaa_profile_sync_response_t* record = (kaa_profile_sync_response_t*)kaa_union->data;
                record->destroy(record);
            }
            break;
        }
        default:
            break;
        }

        kaa_data_destroy(kaa_union);
    }
}
static kaa_union_t* kaa_union_profile_sync_response_or_null_create()
{
    kaa_union_t* kaa_union = KAA_CALLOC(1, sizeof(kaa_union_t));

    if (kaa_union) {
        kaa_union->destroy = kaa_union_profile_sync_response_or_null_destroy;
    }

    return kaa_union; 
}

kaa_union_t* kaa_union_profile_sync_response_or_null_deserialize(avro_reader_t reader)
{
    kaa_union_t *kaa_union = kaa_union_profile_sync_response_or_null_create();

    if (kaa_union) {
        int64_t branch;
        avro_binary_encoding.read_long(reader, &branch);
        kaa_union->type = branch;

        switch (kaa_union->type) {
        case KAA_UNION_PROFILE_SYNC_RESPONSE_OR_NULL_BRANCH_0:
        {
            kaa_union->data = kaa_profile_sync_response_deserialize(reader);
            break;
        }
        default:
            break;
        }
    }

    return kaa_union;
}
# endif // KAA_UNION_PROFILE_SYNC_RESPONSE_OR_NULL_C_


# ifndef KAA_UNION_CONFIGURATION_SYNC_RESPONSE_OR_NULL_C_
# define KAA_UNION_CONFIGURATION_SYNC_RESPONSE_OR_NULL_C_
static void kaa_union_configuration_sync_response_or_null_destroy(void *data)
{
    if (data) {
        kaa_union_t *kaa_union = (kaa_union_t*)data;

        switch (kaa_union->type) {
        case KAA_UNION_CONFIGURATION_SYNC_RESPONSE_OR_NULL_BRANCH_0:
        {
            if (kaa_union->data) {
                kaa_configuration_sync_response_t* record = (kaa_configuration_sync_response_t*)kaa_union->data;
                record->destroy(record);
            }
            break;
        }
        default:
            break;
        }

        kaa_data_destroy(kaa_union);
    }
}
static kaa_union_t* kaa_union_configuration_sync_response_or_null_create()
{
    kaa_union_t* kaa_union = KAA_CALLOC(1, sizeof(kaa_union_t));

    if (kaa_union) {
        kaa_union->destroy = kaa_union_configuration_sync_response_or_null_destroy;
    }

    return kaa_union; 
}

kaa_union_t* kaa_union_configuration_sync_response_or_null_deserialize(avro_reader_t reader)
{
    kaa_union_t *kaa_union = kaa_union_configuration_sync_response_or_null_create();

    if (kaa_union) {
        int64_t branch;
        avro_binary_encoding.read_long(reader, &branch);
        kaa_union->type = branch;

        switch (kaa_union->type) {
        case KAA_UNION_CONFIGURATION_SYNC_RESPONSE_OR_NULL_BRANCH_0:
        {
            kaa_union->data = kaa_configuration_sync_response_deserialize(reader);
            break;
        }
        default:
            break;
        }
    }

    return kaa_union;
}
# endif // KAA_UNION_CONFIGURATION_SYNC_RESPONSE_OR_NULL_C_


# ifndef KAA_UNION_NOTIFICATION_SYNC_RESPONSE_OR_NULL_C_
# define KAA_UNION_NOTIFICATION_SYNC_RESPONSE_OR_NULL_C_
static void kaa_union_notification_sync_response_or_null_destroy(void *data)
{
    if (data) {
        kaa_union_t *kaa_union = (kaa_union_t*)data;

        switch (kaa_union->type) {
        case KAA_UNION_NOTIFICATION_SYNC_RESPONSE_OR_NULL_BRANCH_0:
        {
            if (kaa_union->data) {
                kaa_notification_sync_response_t* record = (kaa_notification_sync_response_t*)kaa_union->data;
                record->destroy(record);
            }
            break;
        }
        default:
            break;
        }

        kaa_data_destroy(kaa_union);
    }
}
static kaa_union_t* kaa_union_notification_sync_response_or_null_create()
{
    kaa_union_t* kaa_union = KAA_CALLOC(1, sizeof(kaa_union_t));

    if (kaa_union) {
        kaa_union->destroy = kaa_union_notification_sync_response_or_null_destroy;
    }

    return kaa_union; 
}

kaa_union_t* kaa_union_notification_sync_response_or_null_deserialize(avro_reader_t reader)
{
    kaa_union_t *kaa_union = kaa_union_notification_sync_response_or_null_create();

    if (kaa_union) {
        int64_t branch;
        avro_binary_encoding.read_long(reader, &branch);
        kaa_union->type = branch;

        switch (kaa_union->type) {
        case KAA_UNION_NOTIFICATION_SYNC_RESPONSE_OR_NULL_BRANCH_0:
        {
            kaa_union->data = kaa_notification_sync_response_deserialize(reader);
            break;
        }
        default:
            break;
        }
    }

    return kaa_union;
}
# endif // KAA_UNION_NOTIFICATION_SYNC_RESPONSE_OR_NULL_C_


# ifndef KAA_UNION_USER_SYNC_RESPONSE_OR_NULL_C_
# define KAA_UNION_USER_SYNC_RESPONSE_OR_NULL_C_
static void kaa_union_user_sync_response_or_null_destroy(void *data)
{
    if (data) {
        kaa_union_t *kaa_union = (kaa_union_t*)data;

        switch (kaa_union->type) {
        case KAA_UNION_USER_SYNC_RESPONSE_OR_NULL_BRANCH_0:
        {
            if (kaa_union->data) {
                kaa_user_sync_response_t* record = (kaa_user_sync_response_t*)kaa_union->data;
                record->destroy(record);
            }
            break;
        }
        default:
            break;
        }

        kaa_data_destroy(kaa_union);
    }
}
static kaa_union_t* kaa_union_user_sync_response_or_null_create()
{
    kaa_union_t* kaa_union = KAA_CALLOC(1, sizeof(kaa_union_t));

    if (kaa_union) {
        kaa_union->destroy = kaa_union_user_sync_response_or_null_destroy;
    }

    return kaa_union; 
}

kaa_union_t* kaa_union_user_sync_response_or_null_deserialize(avro_reader_t reader)
{
    kaa_union_t *kaa_union = kaa_union_user_sync_response_or_null_create();

    if (kaa_union) {
        int64_t branch;
        avro_binary_encoding.read_long(reader, &branch);
        kaa_union->type = branch;

        switch (kaa_union->type) {
        case KAA_UNION_USER_SYNC_RESPONSE_OR_NULL_BRANCH_0:
        {
            kaa_union->data = kaa_user_sync_response_deserialize(reader);
            break;
        }
        default:
            break;
        }
    }

    return kaa_union;
}
# endif // KAA_UNION_USER_SYNC_RESPONSE_OR_NULL_C_


# ifndef KAA_UNION_EVENT_SYNC_RESPONSE_OR_NULL_C_
# define KAA_UNION_EVENT_SYNC_RESPONSE_OR_NULL_C_
static void kaa_union_event_sync_response_or_null_destroy(void *data)
{
    if (data) {
        kaa_union_t *kaa_union = (kaa_union_t*)data;

        switch (kaa_union->type) {
        case KAA_UNION_EVENT_SYNC_RESPONSE_OR_NULL_BRANCH_0:
        {
            if (kaa_union->data) {
                kaa_event_sync_response_t* record = (kaa_event_sync_response_t*)kaa_union->data;
                record->destroy(record);
            }
            break;
        }
        default:
            break;
        }

        kaa_data_destroy(kaa_union);
    }
}
static kaa_union_t* kaa_union_event_sync_response_or_null_create()
{
    kaa_union_t* kaa_union = KAA_CALLOC(1, sizeof(kaa_union_t));

    if (kaa_union) {
        kaa_union->destroy = kaa_union_event_sync_response_or_null_destroy;
    }

    return kaa_union; 
}

kaa_union_t* kaa_union_event_sync_response_or_null_deserialize(avro_reader_t reader)
{
    kaa_union_t *kaa_union = kaa_union_event_sync_response_or_null_create();

    if (kaa_union) {
        int64_t branch;
        avro_binary_encoding.read_long(reader, &branch);
        kaa_union->type = branch;

        switch (kaa_union->type) {
        case KAA_UNION_EVENT_SYNC_RESPONSE_OR_NULL_BRANCH_0:
        {
            kaa_union->data = kaa_event_sync_response_deserialize(reader);
            break;
        }
        default:
            break;
        }
    }

    return kaa_union;
}
# endif // KAA_UNION_EVENT_SYNC_RESPONSE_OR_NULL_C_


# ifndef KAA_UNION_REDIRECT_SYNC_RESPONSE_OR_NULL_C_
# define KAA_UNION_REDIRECT_SYNC_RESPONSE_OR_NULL_C_
static void kaa_union_redirect_sync_response_or_null_destroy(void *data)
{
    if (data) {
        kaa_union_t *kaa_union = (kaa_union_t*)data;

        switch (kaa_union->type) {
        case KAA_UNION_REDIRECT_SYNC_RESPONSE_OR_NULL_BRANCH_0:
        {
            if (kaa_union->data) {
                kaa_redirect_sync_response_t* record = (kaa_redirect_sync_response_t*)kaa_union->data;
                record->destroy(record);
            }
            break;
        }
        default:
            break;
        }

        kaa_data_destroy(kaa_union);
    }
}
static kaa_union_t* kaa_union_redirect_sync_response_or_null_create()
{
    kaa_union_t* kaa_union = KAA_CALLOC(1, sizeof(kaa_union_t));

    if (kaa_union) {
        kaa_union->destroy = kaa_union_redirect_sync_response_or_null_destroy;
    }

    return kaa_union; 
}

kaa_union_t* kaa_union_redirect_sync_response_or_null_deserialize(avro_reader_t reader)
{
    kaa_union_t *kaa_union = kaa_union_redirect_sync_response_or_null_create();

    if (kaa_union) {
        int64_t branch;
        avro_binary_encoding.read_long(reader, &branch);
        kaa_union->type = branch;

        switch (kaa_union->type) {
        case KAA_UNION_REDIRECT_SYNC_RESPONSE_OR_NULL_BRANCH_0:
        {
            kaa_union->data = kaa_redirect_sync_response_deserialize(reader);
            break;
        }
        default:
            break;
        }
    }

    return kaa_union;
}
# endif // KAA_UNION_REDIRECT_SYNC_RESPONSE_OR_NULL_C_


# ifndef KAA_UNION_LOG_SYNC_RESPONSE_OR_NULL_C_
# define KAA_UNION_LOG_SYNC_RESPONSE_OR_NULL_C_
static void kaa_union_log_sync_response_or_null_destroy(void *data)
{
    if (data) {
        kaa_union_t *kaa_union = (kaa_union_t*)data;

        switch (kaa_union->type) {
        case KAA_UNION_LOG_SYNC_RESPONSE_OR_NULL_BRANCH_0:
        {
            if (kaa_union->data) {
                kaa_log_sync_response_t* record = (kaa_log_sync_response_t*)kaa_union->data;
                record->destroy(record);
            }
            break;
        }
        default:
            break;
        }

        kaa_data_destroy(kaa_union);
    }
}
static kaa_union_t* kaa_union_log_sync_response_or_null_create()
{
    kaa_union_t* kaa_union = KAA_CALLOC(1, sizeof(kaa_union_t));

    if (kaa_union) {
        kaa_union->destroy = kaa_union_log_sync_response_or_null_destroy;
    }

    return kaa_union; 
}

kaa_union_t* kaa_union_log_sync_response_or_null_deserialize(avro_reader_t reader)
{
    kaa_union_t *kaa_union = kaa_union_log_sync_response_or_null_create();

    if (kaa_union) {
        int64_t branch;
        avro_binary_encoding.read_long(reader, &branch);
        kaa_union->type = branch;

        switch (kaa_union->type) {
        case KAA_UNION_LOG_SYNC_RESPONSE_OR_NULL_BRANCH_0:
        {
            kaa_union->data = kaa_log_sync_response_deserialize(reader);
            break;
        }
        default:
            break;
        }
    }

    return kaa_union;
}
# endif // KAA_UNION_LOG_SYNC_RESPONSE_OR_NULL_C_


static void kaa_sync_response_destroy(void* data)
{
    if (data) {
        kaa_sync_response_t* record = (kaa_sync_response_t*)data;

        if (record->request_id && record->request_id->destroy) {
            record->request_id->destroy(record->request_id);
        }
        if (record->profile_sync_response && record->profile_sync_response->destroy) {
            record->profile_sync_response->destroy(record->profile_sync_response);
        }
        if (record->configuration_sync_response && record->configuration_sync_response->destroy) {
            record->configuration_sync_response->destroy(record->configuration_sync_response);
        }
        if (record->notification_sync_response && record->notification_sync_response->destroy) {
            record->notification_sync_response->destroy(record->notification_sync_response);
        }
        if (record->user_sync_response && record->user_sync_response->destroy) {
            record->user_sync_response->destroy(record->user_sync_response);
        }
        if (record->event_sync_response && record->event_sync_response->destroy) {
            record->event_sync_response->destroy(record->event_sync_response);
        }
        if (record->redirect_sync_response && record->redirect_sync_response->destroy) {
            record->redirect_sync_response->destroy(record->redirect_sync_response);
        }
        if (record->log_sync_response && record->log_sync_response->destroy) {
            record->log_sync_response->destroy(record->log_sync_response);
        }
        kaa_data_destroy(record);
    }
}


kaa_sync_response_t* kaa_sync_response_deserialize(avro_reader_t reader)
{
    kaa_sync_response_t* record = 
            (kaa_sync_response_t*)KAA_MALLOC(sizeof(kaa_sync_response_t));

    if (record) {
        record->destroy = kaa_sync_response_destroy;

        record->request_id = kaa_union_int_or_null_deserialize(reader);
        int64_t status_value;
        avro_binary_encoding.read_long(reader, &status_value);
        record->status = status_value;
        record->profile_sync_response = kaa_union_profile_sync_response_or_null_deserialize(reader);
        record->configuration_sync_response = kaa_union_configuration_sync_response_or_null_deserialize(reader);
        record->notification_sync_response = kaa_union_notification_sync_response_or_null_deserialize(reader);
        record->user_sync_response = kaa_union_user_sync_response_or_null_deserialize(reader);
        record->event_sync_response = kaa_union_event_sync_response_or_null_deserialize(reader);
        record->redirect_sync_response = kaa_union_redirect_sync_response_or_null_deserialize(reader);
        record->log_sync_response = kaa_union_log_sync_response_or_null_deserialize(reader);
    }

    return record;
}


static void kaa_topic_subscription_info_destroy(void* data)
{
    if (data) {
        kaa_topic_subscription_info_t* record = (kaa_topic_subscription_info_t*)data;

        if (record->topic_info && record->topic_info->destroy) {
            record->topic_info->destroy(record->topic_info);
        }
        kaa_data_destroy(record);
    }
}


kaa_topic_subscription_info_t* kaa_topic_subscription_info_deserialize(avro_reader_t reader)
{
    kaa_topic_subscription_info_t* record = 
            (kaa_topic_subscription_info_t*)KAA_MALLOC(sizeof(kaa_topic_subscription_info_t));

    if (record) {
        record->destroy = kaa_topic_subscription_info_destroy;

        record->topic_info = kaa_topic_deserialize(reader);
        avro_binary_encoding.read_int(reader, &record->seq_number);
    }

    return record;
}

