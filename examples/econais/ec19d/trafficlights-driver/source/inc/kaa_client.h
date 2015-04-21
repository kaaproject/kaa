/*
 * Copyright 2015 CyberVision, Inc.
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
 * @brief Kaa client implementation for Econais boards. Initialize Kaa framework and include main loop around select() for IO network
 * operations, Kaa client loop run in separate thread.
 *
 * Created on: Feb 16, 2015
 *    Author: Andriy Panasenko <apanasenko@cybervisiontech.com>
*/

#ifndef KAA_CLIENT_H_
#define KAA_CLIENT_H_

typedef struct {
        unsigned long max_update_time;
} kaa_client_props_t;

struct kaa_client_t;
typedef struct kaa_client_t  kaa_client_t;


/**
 * @brief Creates and initializes Econais kaa_client
 *
 * Create Kaa client, initialize Kaa framework.
 * Create running thread with select() IO processing, and stop on semaphore
 * waiting kaa_client_start().
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
 * After a successful call @c kaa_client pointer becomes invalid.
 *
 * @param[in]       kaa_context     Pointer to an Kaa client.
 *
 * @return Error code.
 */
void kaa_client_destroy(kaa_client_t *kaa_client);

/**
 * @brief Start Kaa client.
 *
 * Start Kaa client IO loop.
 *
 * @param[in]       kaa_context     Pointer to an Kaa client.
 *
 * @return Error code.
 */
kaa_error_t kaa_client_start(kaa_client_t *kaa_client);

/**
 * @brief Stop Kaa client.
 *
 * Stop Kaa client IO loop.
 *
 * @param[in]       kaa_context     Pointer to an Kaa client.
 *
 * @return Error code.
 */
kaa_error_t kaa_client_stop(kaa_client_t *kaa_client);

/**
 * @brief Return pointer to Kaa context
 *
 * @param[in]       kaa_context     Pointer to an Kaa client.
 *
 * @return pointer to Kaa context.
 */
kaa_context_t* kaa_client_get_context(kaa_client_t *kaa_client);

/**
 * @brief Log record using Kaa logging.
 *
 * This is example implementation of logging, just char*. And depends on
 * configured schema on Kaa.
 *
 * This is EXAMPLE declaration, specific should be written based on auto generated
 * sources from Kaa UI.
 *
 * @param[in]       kaa_context     Pointer to an Kaa client.
 * @param[in]       record          Pointer to a log record.
 *
 * @return Error code..
 */
kaa_error_t kaa_client_log_record(kaa_client_t *kaa_client, const kaa_user_log_record_t *record);

/**
 * @brief Update configuration received from Kaa framework
 *
 * This is example implementation of configuration.
 * Configuration callback got changes on application.c level and
 * push it in Kaa client level.
 *
  * This is EXAMPLE declaration, specific should be written based on auto generated
 * sources from Kaa UI.
 *
 * @param[in]       kaa_context     Pointer to an Kaa client.
 * @param[in]       configuration   Pointer to root configuration
 *
 * @return Error code..
 */
//kaa_error_t kaa_client_configuration_update(kaa_client_t *kaa_client, const kaa_root_configuration_t *configuration);

#endif /* KAA_CLIENT_H_ */
