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

#include "../../../kaa/src/event/kaa_device_event_class_family.h"
# include "avro_src/avro/io.h"

# include "kaa_common.h"
# include "kaa_event.h"
# include "kaa_error.h"
# include "utilities/kaa_mem.h"

extern kaa_error_t kaa_event_manager_add_event_to_transaction(kaa_event_manager_t *self, kaa_event_block_id trx_id, const char *fqn, const char *event_data, size_t event_data_size, kaa_endpoint_id_p target);
extern kaa_error_t kaa_event_manager_add_on_event_callback(kaa_event_manager_t *self, const char *fqn, kaa_event_callback_t callback);
extern kaa_error_t kaa_event_manager_send_event(kaa_event_manager_t *self, const char *fqn, const char *event_data, size_t event_data_size, kaa_endpoint_id_p target);
# ifdef kaa_broadcast_event
# undef kaa_broadcast_event
# endif
# define kaa_broadcast_event(context, fqn, fqn_length, event_data, event_data_size) \
    kaa_event_manager_send_event((context), (fqn), (fqn_length), (event_data), (event_data_size), NULL, 0)


typedef struct kaa_device_event_class_family_ {
    on_kaa_device_event_class_family_device_info_request device_info_request_listener;
    on_kaa_device_event_class_family_device_info_response device_info_response_listener;
} kaa_device_event_class_family;

static kaa_device_event_class_family listeners;

static void kaa_event_manager_device_info_request_listener(const char * event_fqn, const char *data, size_t size, kaa_endpoint_id_p event_source)
{
    (void)event_fqn;
    avro_reader_t reader = avro_reader_memory(data, size);
    kaa_device_event_class_family_device_info_request_t * event = kaa_device_event_class_family_device_info_request_deserialize(reader);
    avro_reader_free(reader);
    listeners.device_info_request_listener(event, event_source);
}

kaa_error_t kaa_event_manager_set_kaa_device_event_class_family_device_info_request_listener(kaa_event_manager_t *self, on_kaa_device_event_class_family_device_info_request listener)
{
    KAA_RETURN_IF_NIL(self, KAA_ERR_BADPARAM);
    listeners.device_info_request_listener = listener;
    return kaa_event_manager_add_on_event_callback(self, "org.kaaproject.kaa.demo.smarthouse.device.DeviceInfoRequest", kaa_event_manager_device_info_request_listener);
}

static void kaa_event_manager_device_info_response_listener(const char * event_fqn, const char *data, size_t size, kaa_endpoint_id_p event_source)
{
    (void)event_fqn;
    avro_reader_t reader = avro_reader_memory(data, size);
    kaa_device_event_class_family_device_info_response_t * event = kaa_device_event_class_family_device_info_response_deserialize(reader);
    avro_reader_free(reader);
    listeners.device_info_response_listener(event, event_source);
}

kaa_error_t kaa_event_manager_set_kaa_device_event_class_family_device_info_response_listener(kaa_event_manager_t *self, on_kaa_device_event_class_family_device_info_response listener)
{
    KAA_RETURN_IF_NIL(self, KAA_ERR_BADPARAM);
    listeners.device_info_response_listener = listener;
    return kaa_event_manager_add_on_event_callback(self, "org.kaaproject.kaa.demo.smarthouse.device.DeviceInfoResponse", kaa_event_manager_device_info_response_listener);
}


kaa_error_t kaa_event_manager_send_kaa_device_event_class_family_device_info_request(kaa_event_manager_t *self, kaa_device_event_class_family_device_info_request_t *event, kaa_endpoint_id_p target)
{
    KAA_RETURN_IF_NIL2(self, event, KAA_ERR_BADPARAM);
    return kaa_event_manager_send_event(self, "org.kaaproject.kaa.demo.smarthouse.device.DeviceInfoRequest", NULL, 0, target);
}

kaa_error_t kaa_event_manager_send_kaa_device_event_class_family_device_info_response(kaa_event_manager_t *self, kaa_device_event_class_family_device_info_response_t *event, kaa_endpoint_id_p target)
{
    KAA_RETURN_IF_NIL2(self, event, KAA_ERR_BADPARAM);
    size_t event_size = event->get_size(event);
    char *buffer = (char *)KAA_MALLOC((event_size) * sizeof(char));
    KAA_RETURN_IF_NIL(buffer, KAA_ERR_NOMEM);
    avro_writer_t writer = avro_writer_memory(buffer, event_size);
    KAA_RETURN_IF_NIL(writer, KAA_ERR_NOMEM);
    event->serialize(writer, event);
    kaa_error_t result = kaa_event_manager_send_event(self, "org.kaaproject.kaa.demo.smarthouse.device.DeviceInfoResponse", writer->buf, writer->written, target);
    avro_writer_free(writer);
    return result;
}


kaa_error_t kaa_event_manager_add_kaa_device_event_class_family_device_info_request_event_to_block(kaa_event_manager_t *self, kaa_device_event_class_family_device_info_request_t *event, kaa_endpoint_id_p target, kaa_event_block_id trx_id)
{
    KAA_RETURN_IF_NIL2(self, event, KAA_ERR_BADPARAM);
    return kaa_event_manager_add_event_to_transaction(self, trx_id, "org.kaaproject.kaa.demo.smarthouse.device.DeviceInfoRequest", NULL, 0, target);
}

kaa_error_t kaa_event_manager_add_kaa_device_event_class_family_device_info_response_event_to_block(kaa_event_manager_t *self, kaa_device_event_class_family_device_info_response_t *event, kaa_endpoint_id_p target, kaa_event_block_id trx_id)
{
    KAA_RETURN_IF_NIL2(self, event, KAA_ERR_BADPARAM);
    size_t event_size = event->get_size(event);
    char *buffer = (char *)KAA_MALLOC((event_size) * sizeof(char));
    KAA_RETURN_IF_NIL(buffer, KAA_ERR_NOMEM);
    avro_writer_t writer = avro_writer_memory(buffer, event_size);
    KAA_RETURN_IF_NIL(writer, KAA_ERR_NOMEM);
    event->serialize(writer, event);
    kaa_error_t result = kaa_event_manager_add_event_to_transaction(self, trx_id, "org.kaaproject.kaa.demo.smarthouse.device.DeviceInfoResponse", writer->buf, writer->written, target);
    avro_writer_free(writer);
    return result;
}

