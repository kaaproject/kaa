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
 * @file ext_event_listeners_callback.h
 * @brief External interface for receiving event listeners responses used by Kaa Event subsystem.
 * Should be implemented to receive the list of available event listeners.
 * See @link kaa_event_manager_find_event_listeners @endlink for further information.
 */

#ifndef EXT_EVENT_LISTENERS_CALLBACK_H_
#define EXT_EVENT_LISTENERS_CALLBACK_H_

#include "kaa_common.h"
#include "kaa_error.h"

#ifdef __cplusplus
extern "C" {
#endif


/**
 *  @brief Callback for successful event listeners responses.
 *
 *  @param[in]      context             Callback's context.
 *  @param[in]      listeners           List of available event listeners (endpoint ids).
 *                                      This list is a temporary object, don't use it outside of the callback.
 *  @param[in]      listeners_count     Size of the listeners list.
 *
 *  @return Error code.
 */
typedef kaa_error_t (*on_event_listeners_fn) (void *context, const kaa_endpoint_id listeners[], size_t listeners_count);


/**
 * @brief Callback for failed event listeners responses.
 *
 * @param[in]       context             Callback's context.
 *
 * @return Error code.
 */
typedef kaa_error_t (*on_event_listeners_failed_fn) (void *context);


/**
 * Interface for the event listeners response receiver.
 */
typedef struct
{
    void *context;                                               /**< Context to pass to all functions below. */
    on_event_listeners_fn on_event_listeners;                    /**< Called on successful listeners response. */
    on_event_listeners_failed_fn on_event_listeners_failed;      /**< Called on failures. */
} kaa_event_listeners_callback_t;


#ifdef __cplusplus
}      /* extern "C" */
#endif

#endif /* EXT_EVENT_LISTENERS_CALLBACK_H_ */
