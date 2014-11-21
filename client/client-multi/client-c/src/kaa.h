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

#ifndef KAA_H_
#define KAA_H_

#ifdef __cplusplus
extern "C" {
#define CLOSE_EXTERN }
#else
#define CLOSE_EXTERN
#endif

#include "kaa_common.h"
#include "kaa_external.h"
#include "kaa_error.h"
#include "kaa_profile.h"
#include "kaa_logging.h"

#include "gen/kaa_endpoint_gen.h"
#include <stddef.h>

/**
 * Kaa client library initialization
 */

/**
 * Initializes Kaa library.
 */
kaa_error_t kaa_init();

/**
 * Kaa library clean-up
 */
kaa_error_t kaa_deinit();

/*
 * Profile management
 */

/**
 * Sets user profile.<br>
 * After profile is set a request to Operations server will be sent.<br>
 * <br>
 * Provide a valid pointer to user-defined profile structure. kaa_profile_t is
 * an alias of a given profile structure name.<br>
 * <br>
 * Use this to set profile before kaa_init() is called to provide default
 * profile value in order to perform successful registration in Operations server.
 */
kaa_error_t kaa_set_profile(kaa_profile_t *profile_body);

/**
 * User management
 */

/**
 * Set callback function to receive notification when current endpoint is being
 * attached or detached to (from) user.<br>
 * <br>
 * callback function example:<br>
 * <pre>
 * void on_attach_status_changed(bool is_attached)
 * {
 *     printf("Attached status is %d\n", is_attached);
 * }
 * ...
 * kaa_set_user_attached_callback(&on_attach_status_changed);
 * </pre>
 */
kaa_error_t kaa_set_user_attached_callback(user_response_handler_t callback);

/**
 * Set endpoint access token.<br>
 * <br>
 * Provide unique string token to Kaa library.<br>
 * Endpoint access token SHOULD be provided before initialization of Kaa library.<br>
 * <br>
 * \param token  null-terminated string.
 */
kaa_error_t kaa_set_endpoint_access_token(const char *token);

/**
 * Attach current endpoint to user.<br>
 * <br>
 * After Kaa is initialized and running use this to attach current endpoint to
 * user instance. Only attached endpoints are allowed to send and received events.<br>
 * <br>
 * \param user_external_id  null-terminated string representing user id
 * \param user_access_token null-terminated string representing user external id
 *
 */
kaa_error_t kaa_attach_to_user(const char *user_external_id, const char * user_access_token);

#ifndef KAA_DISABLE_FEATURE_EVENTS
/**
 * Event management
 */

/**
 * Sends raw event<br>
 * <br>
 * It is not recommended to use this function directly. Instead you should use
 * functions contained in EventClassFamily auto-generated headers (placed at src/event/)
 */
kaa_error_t kaa_send_event(const char * fqn, size_t fqn_length, const char *event_data, size_t event_data_size, const char *event_target, size_t event_target_size);
#ifdef kaa_broadcast_event
#undef kaa_broadcast_event
#endif
#define kaa_broadcast_event(fqn, fqn_length, event_data, event_data_size) kaa_send_event(fqn, fqn_length, event_data,event_data_size, NULL, 0)

/**
 * Adds a raw event to the transaction<br>
 * <br>
 * It is not recommended to use this function directly. Instead you should use
 * functions contained in EventClassFamily auto-generated headers (kaa_add_*_event_to_block(...))
 */
kaa_error_t kaa_event_add_to_transaction(kaa_trx_id trx_id, const char * fqn, size_t fqn_length, const char *event_data, size_t event_data_size, const char *event_target, size_t event_target_size);

/**
 * Start a new event block<br>
 * <br>
 * Returns a new id which must be used to add an event to the block.
 * \return new events block id.
 */
kaa_error_t kaa_start_event_block(kaa_trx_id* trx_id);

/**
 * Send all the events from the event block at once.<br>
 * <br>
 * The event block is identified by the given trx_id.
 * \param trx_id    The ID of the event block to be sent.
 */
kaa_error_t kaa_send_event_block(kaa_trx_id trx_id);

/**
 * Removes the event block without sending events.<br>
 * <br>
 * \param trx_id    The ID of the event block to be removed.
 */
kaa_error_t kaa_remove_event_block(kaa_trx_id trx_id);

/**
 * Register listener to an event.<br>
 * <br>
 * It is not recommended to use this function directly. Instead you should use
 * functions contained in EventClassFamily auto-generated headers (placed at src/event/)
 */
kaa_error_t kaa_register_event_listener(const char *fqn, size_t fqn_length, event_callback_t listener);
#ifdef kaa_set_global_event_callback
#undef kaa_set_global_event_callback
#endif
#define kaa_set_global_event_callback(callback) kaa_register_event_listener(NULL, 0, callback)
#endif

/**
 * Channel management
 */

/**
 * Set user transport channel.<br>
 * <br>
 * Kaa library will call \c handler when it is needed to send a request to
 * Operations server.<br>
 * \param handler               sync handler function \see kaa_sync_t
 * \param services_count        size of array of supported services by this handler
 * \param supported_services    array of supported services names
 */
kaa_error_t    kaa_set_sync_handler(kaa_sync_t handler, size_t services_count, const kaa_service_t supported_services[]);

/**
 * Create a Sync Request.<br>
 * <br>
 * Use this to create a valid sync request.<br>
 * Kaa library will allocate memory for *request itself.
 *
 * \return size of buffer which is needed to serialize the request
 * Example:<br>
 * <pre>
 * kaa_service_t services[1] = { KAA_SERVICE_EVENT };
 * kaa_sync_request_t *request = NULL;
 * size_t buffer_size = 0;
 * kaa_error_t error_code = kaa_compile_request(&request, &buffer_size, 1, services);
 * </pre>
 */
kaa_error_t    kaa_compile_request(kaa_sync_request_t **request, size_t *result_size, size_t service_count, const kaa_service_t services[]);

/**
 * Serialize Sync Request.<br>
 * <br>
 * Use this to serialize a valid sync request created using @link kaa_compile_request(...) @endlink.<br>
 * Serialized request is place to a given buffer. Buffer size must be of size
 * NOT LESS THAN the value returned by @link kaa_compile_request(...) @endlink <br>
 * <br>
 * Example:<br>
 * <pre>
 * kaa_service_t services[1] = { KAA_SERVICE_EVENT };
 * kaa_sync_request_t *request = NULL;
 * size_t buffer_size = 0;
 * kaa_error_t error_code = kaa_compile_request(&request, &buffer_size, 1, services);
 *
 * char *buffer = malloc(buffer_size * sizeof(char));
 * kaa_serialize_request(request, buffer, buffer_size);
 *
 * </pre>
 */
kaa_error_t    kaa_serialize_request(kaa_sync_request_t *request, char *buffer, size_t request_size);

/**
 * Process data received from Operations server.
 */
kaa_error_t    kaa_response_received(const char *buffer, size_t buffer_size);

#ifndef KAA_DISABLE_FEATURE_LOGGING

/**
 * Log management
 */

/**
 * Provide log storage to Kaa.<br>
 * <br>
 *
 * \param i_storage     Structure containing pointers to functions which are used
 * to manage log storage.
 * \param i_status      Structure containing pointers to functions describing
 * state of the storage (occupied size, records count etc.)
 * \param upload_properties     Properties which are used to control log storage
 * size and log upload neediness.
 * \param is_upload_needed  Pointer to function which will be used to decide
 * which operation (NO_OPERATION, UPLOAD or CLEANUP) should be performed on log storage.
 */
kaa_error_t   kaa_init_log_storage(
                    kaa_log_storage_t * i_storage
                  , kaa_storage_status_t * i_status
                  , kaa_log_upload_properties_t * upload_properties
                  , log_upload_decision_fn is_upload_needed
                  );

/**
 * Add log record to log storage.<br>
 * <br>
 * Use this to add the log entry to the predefined log storage.<br>
 * Log record will be serialized and pushed to a log storage interface via
 * <pre>
 * void            (* add_log_record)  (kaa_log_entry_t * record);
 * </pre>
 * See also \see kaa_log_storage_t
 */
kaa_error_t    kaa_add_log(kaa_user_log_record_t *entry);
#endif

CLOSE_EXTERN
#endif /* KAA_H_ */
