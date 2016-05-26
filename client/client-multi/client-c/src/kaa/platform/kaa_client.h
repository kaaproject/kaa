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

/*
 * @file kaa_client.h
 * @brief Kaa client header for different boards. Initialize Kaa framework and include main loop around IO network
 * operations.
 *
 * Created on: Apr 15, 2015
 *    Author: Andriy Panasenko <apanasenko@cybervisiontech.com>
 */

#ifndef KAA_PLATFORM_KAA_CLIENT_H_
#define KAA_PLATFORM_KAA_CLIENT_H_

#ifdef __cplusplus
extern "C" {
#endif

#include <platform/time.h>

#include "kaa_context.h"
#include <platform/kaa_client_properties.h>

struct kaa_client_t;
typedef struct kaa_client_t kaa_client_t;

typedef void (*external_process_fn)(void *context);

/**
 * @brief Creates and initializes kaa_client
 *
 * Create Kaa client, initialize Kaa framework.
 *
 * @param[in,out]   kaa_client   Pointer to return the address of initialized Kaa client.
 * @param[in]       props        Kaa client properties
 *
 * @return Error code.
 */
kaa_error_t kaa_client_create(kaa_client_t **kaa_client, kaa_client_props_t *props);

/**
 * @brief De-initializes and destroys Kaa client
 *
 * After a successful call @p kaa_client pointer becomes invalid.
 *
 * @param[in]       kaa_client     Pointer to an Kaa client.
 *
 * @return Error code.
 */
void kaa_client_destroy(kaa_client_t *kaa_client);

/**
 * @brief Start Kaa client.
 *
 * Start Kaa client IO loop.
 *
 * @param[in] kaa_client                  Pointer to an Kaa client.
 * @param[in] external_process            Callback function to process something outside a main loop
 * @param[in] external_process_context    Callback function context
 * @param[in] max_delay                   Callback delay
 *
 * @return Error code.
 */
kaa_error_t kaa_client_start(kaa_client_t *kaa_client
                           , external_process_fn external_process
                           , void *external_process_context
                           , kaa_time_t max_delay);

/**
 * @brief Stop Kaa client.
 *
 * Stop Kaa client IO loop.
 *
 * @param[in]       kaa_client     Pointer to an Kaa client.
 *
 * @return Error code.
 */
kaa_error_t kaa_client_stop(kaa_client_t *kaa_client);

/**
 * @brief Return pointer to Kaa context
 *
 * @param[in]       kaa_client     Pointer to an Kaa client.
 *
 * @return pointer to Kaa context.
 */
kaa_context_t* kaa_client_get_context(kaa_client_t *kaa_client);

#ifdef __cplusplus
} /* extern "C" */
#endif
#endif /* KAA_PLATFORM_KAA_CLIENT_H_ */
