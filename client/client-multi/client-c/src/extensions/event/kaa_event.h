/*
 * Copyright 2014-2016 CyberVision, Inc.
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

/**
 * @file kaa_event.h
 * @brief Kaa event subsystem API
 *
 * Supplies API for Kaa event subsystem
 */

#ifndef KAA_EVENT_H_
#define KAA_EVENT_H_

#ifdef __cplusplus
extern "C" {
#endif

#include <stddef.h>
#include "kaa_error.h"
#include "platform/ext_event_listeners_callback.h"

typedef void (*kaa_event_callback_t)(const char *event_fqn, const char *event_data, size_t event_data_size, kaa_endpoint_id_p event_source);
typedef size_t kaa_event_block_id;

#ifndef KAA_EVENT_MANAGER_T
# define KAA_EVENT_MANAGER_T
    typedef struct kaa_event_manager_t      kaa_event_manager_t;
#endif


/**
 * @brief Initiates a request to the server to search for available event listeners by given FQNs.
 *
 *
 * @param[in]       self                Valid pointer to the event manager instance.
 * @param[in]       fqns                List of FQN strings.
 * @param[in]       fqns_count          Number of FQNs in the list.
 * @param[in]       callback            Pointer to callback structure.
 *
 * @return Error code.
 */
kaa_error_t kaa_event_manager_find_event_listeners(kaa_event_manager_t *self, const char *fqns[], size_t fqns_count, const kaa_event_listeners_callback_t *callback);


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
 * The event block is identified by the given @p trx_id.
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
 * @brief Find class family name of the event by its fully-qualified name.
 *
 * @param[in]       fqn                 Fully-qualified name of the event (null-terminated string).
 *
 * @return Null-terminated string if corresponding event class family was found, @c NULL otherwise.
 */

const char *kaa_find_class_family_name(const char *fqn);

#ifdef __cplusplus
} // extern "C"
#endif
#endif /* KAA_EVENT_H_ */
