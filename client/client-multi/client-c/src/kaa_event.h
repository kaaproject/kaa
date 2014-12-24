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

#ifndef KAA_EVENT_H_
#define KAA_EVENT_H_

#ifdef __cplusplus
extern "C" {
#endif

#ifndef KAA_DISABLE_FEATURE_EVENTS

#include <stddef.h>
#include "kaa_error.h"

typedef void (*kaa_event_callback_t)(const char *event_fqn, const char *event_data, size_t event_data_size, const char *event_source);
typedef size_t kaa_event_block_id;

typedef struct kaa_event_manager_t kaa_event_manager_t;

/**
 * @brief Sends raw event
 *
 * It is not recommended to use this function directly. Instead you should use
 * functions contained in EventClassFamily auto-generated headers (placed at src/event/)
 *
 * @param[in]       self                Valid pointer to the event manager instance.
 * @param[in]       fqn                 Fully-qualified name of the event (null-terminated string).
 * @param[in]       event_data          Serialized event object.
 * @param[in]       event_data_size     Size of data in event_data parameter.
 * @param[in]       target              The target endpoint of the event. If @code NULL @endcode event will be broadcasted.
 *
 * @return Error code.
 */
kaa_error_t kaa_event_manager_send_event(kaa_event_manager_t *self, const char *fqn, const char *event_data, size_t event_data_size, const char *target);
#ifdef kaa_broadcast_event
#undef kaa_broadcast_event
#endif
#define kaa_broadcast_event(context, fqn, fqn_length, event_data, event_data_size) \
    kaa_event_manager_send_event((context), (fqn), (fqn_length), (event_data), (event_data_size), NULL, 0)


/**
 * @brief Register listener to an event.
 *
 * It is not recommended to use this function directly. Instead you should use
 * functions contained in EventClassFamily auto-generated headers (placed at src/event/)
 *
 * @param[in]       self                Valid pointer to the event manager instance.
 * @param[in]       fqn                 Fully-qualified name of the event (null-terminated string).
 *                                      If @code NULL @endcode, this callback will be invoked for
 *                                      all events which do not have registered specific callback.
 * @param[in]       callback            Event callback function.
 *
 * @return  Error code.
 */
kaa_error_t kaa_event_manager_add_on_event_callback(kaa_event_manager_t *self, const char *fqn, kaa_event_callback_t callback);

/**
 * @brief Start a new event block.
 *
 * Returns a new id which must be used to add an event to the block.
 *
 * @param[in]       self                Valid pointer to the event manager instance.
 * @param[in,out]   trx_id              Pointer to the @link kaa_event_block_id @endlink instance which will be fulfilled with a corresponding ID.
 *
 * @return Error code.
 */
kaa_error_t kaa_event_create_transaction(kaa_event_manager_t *self, kaa_event_block_id *trx_id);

/**
 * @brief Send all the events from the event block at once.
 *
 * The event block is identified by the given trx_id.
 *
 * @param[in]       self                Valid pointer to the event manager instance.
 * @param[in]       trx_id              The ID of the event block to be sent.
 *
 * @return Error code.
 */
kaa_error_t kaa_event_finish_transaction(kaa_event_manager_t *self, kaa_event_block_id trx_id);

/**
 * @brief Removes the event block without sending events.
 *
 * @param[in]       self                Valid pointer to the event manager instance.
 * @param[in]       trx_id              The ID of the event block to be sent.
 *
 * @return Error code.
 */
kaa_error_t kaa_event_remove_transaction(kaa_event_manager_t *self, kaa_event_block_id trx_id);

/**
 * @brief Adds a raw event to the transaction.
 *
 * It is not recommended to use this function directly. Instead you should use
 * functions contained in EventClassFamily auto-generated headers (@code kaa_event_manager_add_*_event_to_block(...) @endcode)
 *
 * @param[in]       self                Valid pointer to the event manager instance.
 * @param[in]       trx_id              The ID of the event block to be sent.
 * @param[in]       fqn                 Fully-qualified name of the event (null-terminated string).
 * @param[in]       event_data          Serialized event object.
 * @param[in]       event_data_size     Size of data in event_data parameter.
 * @param[in]       target              The target endpoint of the event. If @code NULL @endcode event will be broadcasted.
 * @param[in]       target_size         Size of data in target parameter.
 *
 * @return Error code.
 */
kaa_error_t kaa_event_manager_add_event_to_transaction(kaa_event_manager_t *self, kaa_event_block_id trx_id, const char *fqn, const char *event_data, size_t event_data_size, const char *target);

/**
 * @brief Find class family name of the event by its fully-qualified name.
 *
 * @param[in]       fqn                 Fully-qualified name of the event (null-terminated string).
 *
 * @return Null-terminated string if corresponding event class family was found, @code NULL @endcode otherwise.
 */

const char *kaa_find_class_family_name(const char *fqn);

#endif

#ifdef __cplusplus
} // extern "C"
#endif
#endif /* KAA_EVENT_H_ */
